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

package uk.openvk.android.client.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.pixmob.httpclient.BuildConfig;
import org.pixmob.httpclient.HttpClient;
import org.pixmob.httpclient.HttpClientException;
import org.pixmob.httpclient.HttpProgressHandler;
import org.pixmob.httpclient.HttpRequestBuilder;
import org.pixmob.httpclient.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.TrackingRequestBody;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.interfaces.OvkAPIListeners;

@SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody", "ConstantConditions"})
public class UploadManager {

    private final HashMap<String, Object> client_info;
    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private Context ctx;
    public ArrayList<Photo> photoAttachments;
    private boolean logging_enabled = true; // default for beta releases

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;
    private boolean forceCaching;
    private String instance;
    private Handler handler;
    private OvkAPIListeners apiListeners;

    public UploadManager(Context ctx, HashMap<String, Object> client_info, Handler handler) {
        this.client_info = client_info;
        this.handler = handler;
        apiListeners = new OvkAPIListeners();
        this.ctx = ctx;
        this.use_https = use_https;
        this.legacy_mode = legacy_mode;
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        try {
            if (legacy_mode || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                Log.v(OpenVKAPI.ULM_TAG, "Starting UploadManager in Legacy Mode...");
                httpClientLegacy = new HttpClient(ctx);
                httpClientLegacy.setConnectTimeout(30000);
                httpClientLegacy.setReadTimeout(30000);
                httpClientLegacy.setUserAgent(generateUserAgent());
                this.legacy_mode = true;
            } else {
                Log.v(OpenVKAPI.ULM_TAG, "Starting UploadManager...");
                SSLContext sslContext = null;
                try {
                    sslContext = SSLContext.getInstance("SSL");
                    TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                @SuppressLint("TrustAllX509TrustManager")
                                @Override
                                public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                               String authType) {
                                }

                                @SuppressLint("TrustAllX509TrustManager")
                                @Override
                                public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                               String authType) {
                                }

                                @Override
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new java.security.cert.X509Certificate[]{};
                                }
                            }
                    };
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    javax.net.ssl.SSLSocketFactory ssf = (javax.net.ssl.SSLSocketFactory)
                            sslContext.getSocketFactory();
                    httpClient = new OkHttpClient.Builder()
                            .sslSocketFactory(sslContext.getSocketFactory())
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false)
                            .build();
                } catch (Exception e) {
                    httpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false)
                            .build();
                }
                legacy_mode = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setForceCaching(boolean value) {
        forceCaching = value;
    }

    public void setProxyConnection(boolean useProxy, String address) {
        try {
            if(useProxy) {
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    if (legacy_mode) {
                        httpClientLegacy.setProxy(address_array[0], Integer.valueOf(address_array[1]));
                    } else {
                        httpClient = new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(15, TimeUnit.SECONDS
                                ).readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false)
                                .proxy(new Proxy(Proxy.Type.HTTP,
                                        new InetSocketAddress(address_array[0],
                                        Integer.valueOf(address_array[1]))))
                                .build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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


    public void uploadFile(final String address, final File file, final String where) {
        if (file == null) {
            Log.e(OpenVKAPI.ULM_TAG, "File is empty. Upload canceled.");
            return;
        }
        Log.v(OpenVKAPI.ULM_TAG, String.format("Uploading file to %s...", address));
        final File file_f = file;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    String mime = "application/octet-stream";
                    if (file_f.getName().endsWith(".jpeg") || file_f.getName().endsWith(".jpg")) {
                        mime = "image/jpeg";
                    } else if (file_f.getName().endsWith(".png")) {
                        mime = "image/png";
                    } else if (file_f.getName().endsWith(".gif")) {
                        mime = "image/gif";
                    }
                    String short_address = address;
                    if(address.length() > 50) {
                        short_address = address.substring(0, 49);
                    }
                    if (legacy_mode) {
                        HttpRequestBuilder request_legacy = httpClientLegacy.post(address);
                        request_legacy.header("Content-Type", "multipart/form-data; boundary=*****");
                        request_legacy.contentDisposition(
                                "form-data; name=\"photo\"; filename=\"" + file.getName() + "\"");
                        request_legacy.content(new FileInputStream(file_f), mime);
                        request_legacy.withProgressHandler(new HttpProgressHandler() {
                            @Override
                            public void onProgress(long position, long max) throws Exception {
                                super.onProgress(position, max);
                                if ((max >= 1048576L && position % 4096 == 0)) {
                                    updateLoadProgress(file_f.getName(), address, position, max);
                                } else if(max >= 8192L && position % 64 == 0) {
                                    updateLoadProgress(file_f.getName(), address, position, max);
                                } else if(max < 8192L) {
                                    updateLoadProgress(file_f.getName(), address, position, max);
                                } else if(position == max) {
                                    updateLoadProgress(file_f.getName(), address, position, max);
                                }
                            }
                        });
                        final String finalShort_address = short_address;
                        HttpResponse response = null;
                        response = request_legacy.execute();
                        assert response != null;
                        response_body = response.readString();
                        response_code = response.getStatusCode();
                    } else {
                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        builder.addPart(
                                Headers.of(
                                        "Content-Disposition",
                                        "form-data; name=\"photo\"; filename=\"" + file.getName() + "\""),
                                        new TrackingRequestBody(file_f, mime,
                                                new TrackingRequestBody.LoadTrackListener() {
                                                    @Override
                                                    public void onLoad(long position, long max) {
                                                        if ((max >= 1048576L && position % 4096 == 0)) {
                                                            updateLoadProgress(file_f.getName(), address, position, max);
                                                        } else if(max >= 8192L && position % 64 == 0) {
                                                            updateLoadProgress(file_f.getName(), address, position, max);
                                                        } else if(max < 8192L) {
                                                            updateLoadProgress(file_f.getName(), address, position, max);
                                                        } else if(position == max) {
                                                            updateLoadProgress(file_f.getName(), address, position, max);
                                                        }
                                                    }
                                                }
                                        )
                        );

                        RequestBody requestBody = builder.build();
                        Request request = new Request.Builder()
                                .addHeader("Content-Type", "multipart/form-data")
                                .url(address)
                                .post(requestBody)
                                .addHeader("User-Agent", generateUserAgent())
                                .build();
                        if (logging_enabled) Log.d(OpenVKAPI.ULM_TAG,
                                String.format("Uploading to %s... (%d kB)\r\nHeaders: %s",
                                        short_address, file_f.length() / 1024, request.headers()
                                                .toMultimap()));
                        Response response = httpClient.newCall(request).execute();
                        response_body = response.body().string();
                        response_code = response.code();
                    }
                    if (response_code == 202) {
                        Log.v(OpenVKAPI.ULM_TAG, "Uploaded!");
                        if(logging_enabled) Log.d(OpenVKAPI.ULM_TAG,
                                String.format("Getting response from %s (%s): [%s]",
                                        short_address, response_code, response_body));
                        sendMessage(HandlerMessages.UPLOADED_SUCCESSFULLY, file_f.getName(), response_body);
                    } else {
                        if(logging_enabled) Log.e(OpenVKAPI.ULM_TAG,
                                String.format("Getting response from %s (%s): [%s]",
                                        short_address, response_code, response_body));
                        sendMessage(HandlerMessages.UPLOAD_ERROR, file_f.getName(), "");
                    }
                } catch (IOException | HttpClientException ex) {
                    ex.printStackTrace();
                    if (ex.getMessage().startsWith("Authorization required")) {
                        response_code = 401;
                    } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                        String code_str = ex.getMessage().substring
                                (ex.getMessage().length() - 3);
                        response_code = Integer.parseInt(code_str);
                        if(logging_enabled) Log.e(OpenVKAPI.ULM_TAG,
                                String.format("Getting response from %s (%s)",
                                        address, response_code));
                    }
                } catch (Exception e) {
                    sendMessage(HandlerMessages.UPLOAD_ERROR, file_f.getName(), "");
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    private void sendMessage(final int message, String filename, String response) {
        Message msg = new Message();
        msg.what = message;
        final Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
        bundle.putString("response", response);
        bundle.putString("address", apiListeners.from);
        msg.setData(bundle);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(message < 0) {
                    apiListeners.failListener.onAPIFailed(ctx, message, bundle);
                } else {
                    apiListeners.successListener.onAPISuccess(ctx, message, bundle);
                }
            }
        });
    }


    public void updateLoadProgress(String filename, String url, final long position, final long length) {
        Message msg = new Message();
        msg.what = HandlerMessages.UPLOAD_PROGRESS;
        final Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
        bundle.putString("url", url);
        bundle.putLong("position", position);
        bundle.putLong("length", length);
        bundle.putString("address", apiListeners.from);
        msg.setData(bundle);
        handler.post(new Runnable() {
            @Override
            public void run() {
                apiListeners.processListener.onAPIProcess(ctx, bundle, position, length);
            }
        });
    }

    public void setAPIListeners(OvkAPIListeners apiListeners) {
        this.apiListeners = apiListeners;
    }
}
