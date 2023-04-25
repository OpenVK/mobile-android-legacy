package uk.openvk.android.legacy.longpoll_api.wrappers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class LongPollWrapper {

    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private String status;
    private uk.openvk.android.legacy.api.models.Error error;
    private Context ctx;
    private Handler handler;
    private String access_token;
    private boolean isActivated;
    private boolean logging_enabled = true;

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;


    public LongPollWrapper(Context ctx, boolean use_https) {
        this.ctx = ctx;

        this.use_https = use_https;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpProtocolParams.setUseExpectContinue((HttpParams) basicHttpParams, false);
            HttpProtocolParams.setUserAgent((HttpParams) basicHttpParams, generateUserAgent(ctx));
            HttpConnectionParams.setSocketBufferSize((HttpParams) basicHttpParams, 8192);
            HttpConnectionParams.setConnectionTimeout((HttpParams) basicHttpParams, 30000);
            HttpConnectionParams.setSoTimeout((HttpParams) basicHttpParams, 30000);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            if (use_https) {
                schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            } else {
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            }
            httpClientLegacy = (HttpClient) new DefaultHttpClient((ClientConnectionManager) new ThreadSafeClientConnManager((HttpParams) basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
            legacy_mode = true;
        } else {
            httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
            legacy_mode = false;
        }
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        String user_agent = "";
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
        } finally {
            user_agent = String.format("OpenVK Legacy/%s (Android %s; SDK %s; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
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
        Log.v(OvkApplication.LP_TAG, String.format("Activating LongPoll via %s...", lp_server));
        final String fUrl = url;
        isActivated = true;
        Thread thread = null;
        Runnable longPollRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                if (legacy_mode) {
                    request_legacy = new HttpGet(fUrl);
                    request_legacy.getParams().setParameter("timeout", 30000);
                } else {
                    request = new Request.Builder()
                                .url(fUrl)
                                .build();
                }
                try {
                    if(isActivated) {
                        Log.v(OvkApplication.LP_TAG, String.format("LongPoll activated."));
                    }
                    while(isActivated) {
                        if (legacy_mode) {
                            HttpResponse response = httpClientLegacy.execute(request_legacy);
                            StatusLine statusLine = response.getStatusLine();
                            response_body = EntityUtils.toString(response.getEntity());
                            response_code = statusLine.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_code == 200) {
                            if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                            sendLongPollMessageToActivity(response_body);
                        } else {
                            if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Getting response from %s (%s)", server, response_code));
                        }
                        Thread.sleep(2000);
                    }
                } catch(ConnectException | SocketTimeoutException | UnknownHostException ex) {
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Connection error: %s", ex.getMessage()));
                    try {
                        if(logging_enabled) Log.v(OvkApplication.LP_TAG, "Retrying in 60 seconds...");
                        Thread.sleep(60000);
                        run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch(SSLProtocolException ex) {
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Connection error: %s", ex.getMessage()));
                    isActivated = false;
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, "LongPoll service stopped.");
                } catch(SSLHandshakeException ex) {
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Connection error: %s", ex.getMessage()));
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, "LongPoll service stopped.");
                    isActivated = false;
                } catch(SSLException ex) {
                    if(logging_enabled) Log.v(OvkApplication.LP_TAG, String.format("Connection error: %s", ex.getMessage()));
                    Log.v(OvkApplication.LP_TAG, "LongPoll service stopped.");
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
                        HttpHost proxy = new HttpHost(address_array[0], Integer.valueOf(address_array[1]));
                        httpClientLegacy.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    } else {
                        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false).proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address_array[0],
                                        Integer.valueOf(address_array[1])))).build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendLongPollMessageToActivity(final String response) {
        handler = new Handler();
        Log.d("OK", "OK! LongPolling 1...");
        Runnable sendLongPoll = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("uk.openvk.android.legacy.LONGPOLL_RECEIVE");
                intent.putExtra("response", response);
                ctx.sendBroadcast(intent);
                Log.d("OK", "OK! LongPolling 2...");
            }
        };
        handler.post(sendLongPoll);
    }

    public void updateCounters(final OvkAPIWrapper ovk) {
        Thread thread = null;
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ovk.sendAPIMethod("Account.getCounters");
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

    public void keepUptime(final OvkAPIWrapper ovk) {
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ovk.sendAPIMethod("Account.setOnline");
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
