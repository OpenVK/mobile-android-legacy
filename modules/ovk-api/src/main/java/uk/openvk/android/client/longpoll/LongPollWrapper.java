/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.longpoll;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.pixmob.httpclient.BuildConfig;
import org.pixmob.httpclient.HttpClient;
import org.pixmob.httpclient.HttpRequestBuilder;
import org.pixmob.httpclient.HttpResponse;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

public class LongPollWrapper {

    private final HashMap<String, Object> client_info;
    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private String status;
    private uk.openvk.android.client.entities.Error error;
    private Context ctx;
    private Handler handler;
    private String access_token;
    private boolean isActivated;
    private boolean logging_enabled = true;

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;
    private boolean looper_prepared;


    public LongPollWrapper(Context ctx, HashMap<String, Object> client_info) {
        this.client_info = client_info;
        this.ctx = ctx;
        this.use_https = use_https;
        if(legacy_mode || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            Log.v(OpenVKAPI.LP_TAG, "Starting LongPollWrapper in Legacy Mode...");
            httpClientLegacy = new HttpClient(ctx);
            httpClientLegacy.setConnectTimeout(30);
            httpClientLegacy.setReadTimeout(30);
            httpClientLegacy.setUserAgent(generateUserAgent());
            this.legacy_mode = true;
        } else {
            httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS).build();
            this.legacy_mode = false;
        }
    }

    private String generateUserAgent() {
        String client_name = "";
        String version = "";
        String user_agent;
        try {
            client_name = (String) client_info.get("name");
            version = (String) client_info.get("version");
        } catch (Exception e) {
            client_name = "OpenVK API";
            version = BuildConfig.VERSION_NAME;
        } finally {
            user_agent = String.format("%s/%s (Android %s; SDK %s; %s; %s %s; %s)",
                    client_name, version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                    Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language")
            );
        }
        return user_agent;
    }

    public void log(boolean value) {
        this.logging_enabled = value;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void requireHTTPS(boolean value) {
        this.use_https = value;
    }

    public void longPoll(String lp_server, String key, int ts) {
        this.server = lp_server;
        String url = "";
        url = String.format("%s?act=a_check&key=%s&ts=%s&wait=15", lp_server, key, ts);
        Log.v(OpenVKAPI.LP_TAG, String.format("Activating LongPoll via %s...", lp_server));
        final String fUrl = url;
        isActivated = true;
        Thread thread = null;
        Runnable longPollRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                if (legacy_mode) {
                    request_legacy = httpClientLegacy.get(fUrl);
                } else {
                    request = new Request.Builder()
                                .url(fUrl)
                                .build();
                }
                try {
                    if(isActivated) {
                        Log.v(OpenVKAPI.LP_TAG, "LongPoll activated.");
                    }
                    while(isActivated) {
                        if (legacy_mode) {
                            HttpResponse response = request_legacy.execute();
                            assert response != null;
                            response.read(response_body);
                            response_code = response.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_code == 200) {
                            if(logging_enabled &&
                                    ((response_body.startsWith("[") && response_body.endsWith("]"))
                                    || (response_body.startsWith("{") && response_body.endsWith("}")))) {
                                Log.v(OpenVKAPI.LP_TAG,
                                        String.format("Getting response from %s (%s): [%s]", server,
                                                response_code, response_body));
                                sendLongPollMessageToActivity(response_body);
                                Thread.sleep(5000);
                            } else {
                                Log.v(OpenVKAPI.LP_TAG,
                                        String.format("Getting response from %s (%s): Invalid JSON data", server,
                                                response_code));
                                sendLongPollMessageToActivity(response_body);
                                Thread.sleep(60000);
                            }
                        } else if(response_code >= 400 && response_code <= 528) {
                            if(logging_enabled) Log.e(OpenVKAPI.LP_TAG,
                                    String.format("Getting response from %s (%s)", server,
                                            response_code));
                            if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, "Retrying in 60 seconds...");
                            Thread.sleep(60000);
                        } else {
                            if(logging_enabled) Log.e(OpenVKAPI.LP_TAG,
                                    String.format("Getting response from %s (%s)", server,
                                            response_code));
                            Thread.sleep(5000);
                        }

                    }
                } catch(ConnectException | SocketTimeoutException | UnknownHostException ex) {
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, String.format("Connection error: %s", ex.getMessage()));
                    try {
                        if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, "Retrying in 60 seconds...");
                        Thread.sleep(60000);
                        run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch(SSLProtocolException ex) {
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, String.format("Connection error: %s",
                            ex.getMessage()));
                    isActivated = false;
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, "LongPoll service stopped.");
                } catch(SSLHandshakeException ex) {
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, String.format("Connection error: %s",
                            ex.getMessage()));
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, "LongPoll service stopped.");
                    isActivated = false;
                } catch(SSLException ex) {
                    if(logging_enabled) Log.v(OpenVKAPI.LP_TAG, String.format("Connection error: %s",
                            ex.getMessage()));
                    Log.v(OpenVKAPI.LP_TAG, "LongPoll service stopped.");
                    isActivated = false;
                } catch (Exception ex) {
                    isActivated = false;
                    ex.printStackTrace();
                }
            }
        };
        thread = new Thread(longPollRunnable);
        thread.start();
    }

    public void setProxyConnection(boolean useProxy, String address) {
        try {
            if(useProxy) {
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    if (legacy_mode) {
                        httpClientLegacy.setProxy(address_array[0], Integer.valueOf(address_array[1]));
                    } else {
                        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).
                                writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false).proxy(new Proxy(Proxy.Type.HTTP, new
                                        InetSocketAddress(address_array[0],
                                        Integer.valueOf(address_array[1])))).build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendLongPollMessageToActivity(final String response) {
        if(!looper_prepared) {
            Looper.prepare();
            looper_prepared = true;
            handler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(android.os.Message msg) {
                    super.handleMessage(msg);
                    if(msg.what == HandlerMessages.LONGPOLL) {
                        Intent intent = new Intent();
                        intent.setAction("uk.openvk.android.legacy.LONGPOLL_RECEIVE");
                        intent.putExtra("response", response);
                        ctx.sendBroadcast(intent);
                    }
                }
            };
        }
        android.os.Message msg = new android.os.Message();
        msg.what = HandlerMessages.LONGPOLL;
        Bundle data = new Bundle();
        data.putString("response", response);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    public void updateCounters(final OvkAPIWrapper wrapper) {
        Thread thread = null;
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                wrapper.sendAPIMethod("Account.getCounters");
                try {
                    if(error != null && error.description.length() > 0) {
                        handler.postDelayed(this, 5000);
                    } else {
                        handler.postDelayed(this, 60000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    public void keepUptime(final OvkAPIWrapper wrapper) {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                wrapper.sendAPIMethod("Account.setOnline");
                try {
                    if(error != null && error.description.length() > 0) {
                        handler.postDelayed(this, 60000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 2000);
    }
}
