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
import org.pixmob.httpclient.HttpRequestBuilder;
import org.pixmob.httpclient.HttpResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import uk.openvk.android.client.OpenVKAPI;

import uk.openvk.android.client.entities.Error;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.interfaces.OvkAPIListeners;

@SuppressWarnings("deprecation")
public class OvkAPIWrapper {

    private final HashMap<String, Object> client_info;
    public String server;
    private boolean use_https;
    private boolean legacy_mode;
    public boolean proxy_connection;
    public String proxy_type;
    private String status;
    public Error error;
    private Context ctx;
    private String access_token;

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;
    private boolean logging_enabled = true; // default for beta releases
    private String client_name = "openvk_legacy_android";
    public Handler handler;
    OvkAPIListeners apiListeners;
    private String relayAddress;


    public OvkAPIWrapper(Context ctx, HashMap<String, Object> client_info, Handler handler) {
        this.client_info = client_info;
        logging_enabled = uk.openvk.android.client.BuildConfig.VERSION_NAME.endsWith("debug");
        setAPIListeners();
        this.handler = handler;
        if(handler == null) {
            searchHandler();
        }
        this.ctx = ctx;
        this.use_https = use_https;
        this.legacy_mode = legacy_mode;
        this.proxy_type = "";
        error = new Error();
        try {
            if (legacy_mode || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                Log.v(OpenVKAPI.TAG, "Starting OvkAPIWrapper in Legacy mode...");
                httpClientLegacy = new HttpClient(ctx);
                httpClientLegacy.setConnectTimeout(30000);
                httpClientLegacy.setReadTimeout(30000);
                httpClientLegacy.setUserAgent(generateUserAgent());
                this.legacy_mode = true;
            } else {
                Log.v(OpenVKAPI.TAG, "Starting OvkAPIWrapper...");
                if (use_https) {
                    httpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).build();
                } else {
                    httpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).followSslRedirects(false).build();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setAPIListeners() {
        apiListeners = new OvkAPIListeners();
        apiListeners.failListener = new OvkAPIListeners.OnAPIFailListener() {
            @Override
            public void onAPIFailed(Context ctx, int msg_code, Bundle data) {
                Log.e(OpenVKAPI.TAG,
                        String.format("This is dummy API listener. " +
                                        "\r\nStatus: Failed" +
                                        "\r\nMessage code: %s" +
                                        "\r\nContext class: %s",
                                msg_code, ctx.getClass().getSimpleName()));
            }
        };
        apiListeners.successListener = new OvkAPIListeners.OnAPISuccessListener() {
            @Override
            public void onAPISuccess(Context ctx, int msg_code, Bundle data) {
                Log.d(OpenVKAPI.TAG,
                        String.format("This is dummy API listener. " +
                                        "\r\nStatus: Success" +
                                        "\r\nMessage code: %s" +
                                        "\r\nContext class: %s",
                                msg_code, ctx.getClass().getSimpleName()));
            }
        };
    }

    private void searchHandler() {

    }

    public String getStatus() {
        return status;
    }

    public Error getError() {
        return error;
    }

    public void setAccessToken(String token) {
        this.access_token = token;
    }

    public void setProxyConnection(boolean useProxy, String type, String address) {
        try {
            if(useProxy) {
                proxy_type = type;
                String[] address_array = address.split(":");
                if (address_array.length == 2) {
                    if (legacy_mode) {
                        if(type.startsWith("http")) {
                            httpClientLegacy.setProxy(address_array[0], Integer.valueOf(address_array[1]));
                        }
                    } else {
                        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false);
                        if(type.startsWith("http")) {
                            httpClientBuilder = httpClientBuilder.proxy(
                                    new Proxy(Proxy.Type.HTTP,
                                            new InetSocketAddress(address_array[0],
                                                    Integer.valueOf(address_array[1])
                                            )
                                    )
                            );
                            if (type.equals("https")) {
                                // Set custom TrustManager for HTTPS proxies
                                final TrustManager[] trustAllCerts = new TrustManager[]{
                                        new X509TrustManager() {
                                            @SuppressLint("TrustAllX509TrustManager")
                                            @Override
                                            public void checkClientTrusted(
                                                    java.security.cert.X509Certificate[] chain, String authType
                                            ) {
                                            }

                                            @Override
                                            public void checkServerTrusted(
                                                    java.security.cert.X509Certificate[] chain, String authType
                                            ) {
                                            }

                                            @Override
                                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                                return new java.security.cert.X509Certificate[]{};
                                            }
                                        }
                                };
                                final SSLContext sslContext = SSLContext.getInstance("SSL");
                                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                                httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                            }
                            httpClient = httpClientBuilder.build();
                        }
                    }
                } else {
                    relayAddress = String.format("http://%s", address);
                }
                this.proxy_connection = true;
            } else {
                proxy_type = "";
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

    public void setServer(String server) {
        this.server = server;
    }

    public void requireHTTPS(boolean value) {
        this.use_https = value;
    }

    public void log(boolean value) {
        this.logging_enabled = value;
    }

    public void authorize(String username, String password) {
        error.description = "";
        String url;
        if(use_https) {
            url = String.format("https://%s/token?username=%s&password=%s&grant_type=password" +
                    "&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username),
                    URLEncoder.encode(password), client_name);
            if(logging_enabled) Log.d(OpenVKAPI.TAG, String.format("Connecting to %s... (Secured)", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password" +
                    "&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username),
                    URLEncoder.encode(password), client_name);
            if(logging_enabled) Log.d(OpenVKAPI.TAG, String.format("Connecting to %s...", server));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() throws OutOfMemoryError {
                try {
                    if(legacy_mode) {
                        request_legacy = proxy_connection && proxy_type.equals("selfeco-relay") ?
                                httpClientLegacy.post(relayAddress) : httpClientLegacy.get(fUrl);

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_type.equals("selfeco-relay")) {
                            request_legacy.content(
                                    String.format("%s", fUrl).getBytes(),
                                    null
                            );
                        }
                    } else {
                        Request.Builder builder = new Request.Builder()
                                .url(proxy_connection && proxy_type.equals("selfeco-relay") ? relayAddress : fUrl)
                                .addHeader("User-Agent", generateUserAgent());

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            builder.post(
                                    RequestBody.create(
                                            MediaType.parse("text/plain"), fUrl
                                    )
                            );
                        }
                        request = builder.build();
                    }
                    try {
                        if (legacy_mode) {
                            HttpResponse response = null;
                            response = request_legacy.execute();
                            assert response != null;
                            response_body = response.readString();
                            response_code = response.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.d(OpenVKAPI.TAG, String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                                Log.e(OpenVKAPI.TAG, String.format("Authorization error (%d)", response_code));
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                                Log.e(OpenVKAPI.TAG, String.format("Authorization error (%d)", 401));
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                                Log.e(OpenVKAPI.TAG, String.format("Authorization error (%d)", response_code));
                            } else if (response_code == 200) {
                                if(!(response_body.startsWith("{") && response_body.endsWith("}"))) {
                                    if(response_body.length() > 16) {
                                        throw new java.text.ParseException(String.format("Response data " +
                                                "must be in JSON format only. Start of response: [%s...]",
                                                response_body.replace("\r", "").replace("\n", "").substring(0, 16)), 0);
                                    } else {
                                        throw new java.text.ParseException(String.format("Response data " +
                                                        "must be in JSON format only. Start of response: [%s]",
                                                response_body.replace("\r", "").replace("\n", "")), 0);
                                    }
                                }
                                Log.d(OpenVKAPI.TAG, String.format("Connected (%d)", response_code));
                                sendMessage(HandlerMessages.AUTHORIZED, response_body);
                            } else if (response_code == 502) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, response_body);
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, response_body);
                            } else {
                                sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                            }
                        } else if (response_code == 301 && !use_https) {
                            sendMessage(HandlerMessages.INTERNAL_ERROR, response_body);
                        } else if (response_code == 302 && !use_https) {
                            sendMessage(HandlerMessages.INTERNAL_ERROR, response_body);
                        }
                    } catch (ProtocolException | UnknownHostException | ConnectException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OpenVKAPI.TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OpenVKAPI.TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e(OpenVKAPI.TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, "");
                    } catch (HttpClientException | IOException ex) {
                        if (ex.getMessage().startsWith("Authorization required")) {
                            response_code = 401;
                            sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                        } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                            String code_str = ex.getMessage().substring
                                    (ex.getMessage().length() - 3);
                            response_code = Integer.parseInt(code_str);
                            if(response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void authorize(String username, String password, String code) {
        error.description = "";
        String url;
        if(use_https) {
            url = String.format("https://%s/token?username=%s&password=%s&grant_type=password&code=%s" +
                    "&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username),
                    URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.d(OpenVKAPI.TAG,
                    String.format("Connecting to %s (Secured)...", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&code=%s" +
                    "&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username),
                    URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.d(OpenVKAPI.TAG,
                    String.format("Connecting to %s...", server));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = proxy_connection && proxy_type.equals("selfeco-relay") ?
                                httpClientLegacy.post(relayAddress) : httpClientLegacy.get(fUrl);

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            request_legacy.content(
                                    String.format("%s", fUrl).getBytes(),
                                    null
                            );
                        }
                    } else {
                        Request.Builder builder = new Request.Builder()
                                .url(proxy_connection && proxy_type.equals("selfeco-relay") ? relayAddress : fUrl)
                                .addHeader("User-Agent", generateUserAgent());

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_type.equals("selfeco-relay")) {
                            builder.post(
                                    RequestBody.create(
                                            MediaType.parse("text/plain"), fUrl
                                    )
                            );
                        }
                        request = builder.build();
                    }
                    try {
                        if (legacy_mode) {
                            HttpResponse response = null;
                            response = request_legacy.execute();
                            assert response != null;
                            response_body = response.readString();
                            response_code = response.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.d(OpenVKAPI.TAG, String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                            } else if (response_code == 200) {
                                if(!(response_body.startsWith("{") && response_body.endsWith("}"))) {
                                    if(response_body.length() > 16) {
                                        throw new java.text.ParseException(String.format("Response data " +
                                                        "must be in JSON format only. Start of response: [%s...]",
                                                response_body.substring(0, 16)), 0);
                                    } else {
                                        throw new java.text.ParseException(String.format("Response data " +
                                                        "must be in JSON format only. Start of response: [%s]",
                                                response_body), 0);
                                    }
                                }
                                sendMessage(HandlerMessages.AUTHORIZED, response_body);
                            } else if (response_code == 301 && !use_https) {
                                sendMessage(HandlerMessages.INTERNAL_ERROR, response_body);
                            } else if (response_code == 302 && !use_https) {
                                sendMessage(HandlerMessages.INTERNAL_ERROR, response_body);
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, response_body);
                            } else {
                                sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                            }
                        }
                    } catch (ProtocolException | ConnectException |
                            javax.net.ssl.SSLProtocolException | UnknownHostException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OpenVKAPI.TAG,
                                        String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OpenVKAPI.TAG,
                                        String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e(OpenVKAPI.TAG,
                                    String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (javax.net.ssl.SSLException | OutOfMemoryError e) {
                        if (logging_enabled)
                            Log.e(OpenVKAPI.TAG,
                                    String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, "");
                    } catch (IOException | HttpClientException ex) {
                        if (ex.getMessage().startsWith("Authorization required")) {
                            response_code = 401;
                            sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                        } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                            String code_str = ex.getMessage().substring
                                    (ex.getMessage().length() - 3);
                            response_code = Integer.parseInt(code_str);
                            if(response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                            }
                        }
                    } catch (Exception e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args, final String where) {
        error.description = "";
        String url;
        if(server.length() == 0) {
            sendMessage(HandlerMessages.INTERNAL_ERROR, "Instance may not be without address!");
            return;
        }
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.d(OpenVKAPI.TAG, String.format("Connecting to %s (Secured)..." +
                    "\r\nMethod: %s\r\n" +
                    "Arguments: %s\r\n" +
                    "Where: %s",
                    server, method, args, where));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.d(OpenVKAPI.TAG,
                    String.format("Connecting to %s..." +
                            "\r\nMethod: %s\r\n" +
                            "Arguments: %s\r\n" +
                            "Where: %s",
                            server, method, args, where));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            Object request_obj;
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = proxy_connection && proxy_type.equals("selfeco-relay") ?
                                httpClientLegacy.post(relayAddress) : httpClientLegacy.get(fUrl);

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            request_legacy.content(
                                    String.format("%s", fUrl).getBytes(),
                                    null
                            );
                        }
                        request_obj = request_legacy;
                    } else {
                        Request.Builder builder = new Request.Builder()
                                .url(proxy_connection && proxy_type.equals("selfeco-relay") ? relayAddress : fUrl)
                                .addHeader("User-Agent", generateUserAgent());

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_type.equals("selfeco-relay")) {
                            builder.post(
                                    RequestBody.create(
                                        MediaType.parse("text/plain"), fUrl
                                    )
                            );
                        }
                        request = builder.build();
                        request_obj = request;
                    }
                    sendAPIRequest(request_obj, fUrl, method, args, where);
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method, final String args) {
        if(server.length() == 0) {
            sendMessage(HandlerMessages.INTERNAL_ERROR, "Instance may not be without address!");
            return;
        }
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.d(OpenVKAPI.TAG, String.format("Connecting to %s (Secured)..." +
                    "\r\nMethod: %s\r\nArguments: %s", server, method, args));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.d(OpenVKAPI.TAG, String.format("Connecting to %s..." +
                    "\r\nMethod: %s\r\nArguments: %s", server, method, args));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            Object request_obj;
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = proxy_connection && proxy_type.equals("selfeco-relay") ?
                                httpClientLegacy.post(relayAddress) : httpClientLegacy.get(fUrl);

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            request_legacy.content(
                                    String.format("%s", fUrl).getBytes(),
                                    null
                            );
                        }
                        request_obj = request_legacy;
                    } else {
                        Request.Builder builder = new Request.Builder()
                                .url(proxy_connection && proxy_type.equals("selfeco-relay") ? relayAddress : fUrl)
                                .addHeader("User-Agent", generateUserAgent());

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            builder.post(
                                    RequestBody.create(
                                            MediaType.parse("text/plain"), fUrl
                                    )
                            );
                        }
                        request = builder.build();
                        request_obj = request;
                    }
                    sendAPIRequest(request_obj, fUrl, method, args, null);
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, method, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method) {
        if(server.length() == 0) {
            sendMessage(HandlerMessages.INTERNAL_ERROR, "Instance may not be without address!");
            return;
        }
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.d(OpenVKAPI.TAG,
                    String.format("Connecting to %s (Secured)..." +
                            "\r\nMethod: %s\r\n" +
                            "Arguments: [without arguments]",
                            server, method));
        } else {
            url = String.format("http://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.d(OpenVKAPI.TAG,
                    String.format("Connecting to %s..." +
                            "\r\nMethod: %s\r\n" +
                            "Arguments: [without arguments]",
                            server, method));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            private Object request_obj;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = proxy_connection && proxy_type.equals("selfeco-relay") ?
                                httpClientLegacy.post(relayAddress) : httpClientLegacy.get(fUrl);

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            request_legacy.content(
                                    String.format("%s", fUrl).getBytes(),
                                    null
                            );
                        }
                        request_obj = request_legacy;
                    } else {
                        Request.Builder builder = new Request.Builder()
                                .url(proxy_connection && proxy_type.equals("selfeco-relay") ? relayAddress : fUrl)
                                .addHeader("User-Agent", generateUserAgent());

                        // Use SelfEco Relay as alternative proxy connection
                        // default: http://minvk.ru/apirelay.php (POST)

                        if(proxy_connection && proxy_type.equals("selfeco-relay")) {
                            builder.post(
                                    RequestBody.create(
                                            MediaType.parse("text/plain"), fUrl
                                    )
                            );
                        }
                        request = builder.build();
                        request_obj = request;
                    }
                    sendAPIRequest(request_obj, fUrl, method, null, null);
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, method, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    private void sendAPIRequest(Object request, String fUrl,
                                String method, String args,
                                String where) {
        int response_code = 0;
        String response_body = "";
        try {
            if(request instanceof HttpRequestBuilder) {
                HttpResponse response = null;
                response = ((HttpRequestBuilder) request).execute();
                assert response != null;
                response_body = response.readString();
                response_code = response.getStatusCode();
            } else {
                Response response = httpClient.newCall((Request) request).execute();
                response_body = response.body().string();
                response_code = response.code();
            }
            if (response_body.length() > 0) {
                if(response_code == 200) {
                    if(logging_enabled) Log.d(OpenVKAPI.TAG,
                            String.format("Getting response from %s (%s, %s):\r\n[%s]",
                                    server, method, response_code, response_body));
                    sendMessage(HandlerMessages.PARSE_JSON, method, args, where, response_body);
                } else if(response_code == 400) {
                    error = new Error();
                    error.parse(response_body);
                    if(logging_enabled) Log.e(OpenVKAPI.TAG,
                            String.format("Getting response from %s (%s, %s): [%s / Error code: %d]",
                                    server, method, response_code, error.description, error.code));
                    if(error.code == 3) {
                        sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, where, error.description);
                    } else if(error.code == 5) {
                        sendMessage(HandlerMessages.INVALID_TOKEN, method, args, where, error.description);
                    } else if (error.code == 15) {
                        sendMessage(HandlerMessages.ACCESS_DENIED, method, args, where, error.description);
                    } else if (error.code == 18) {
                        sendMessage(HandlerMessages.BANNED_ACCOUNT, method, args, where, error.description);
                    } else if (error.code == 10 || error.code == 100) {
                        sendMessage(HandlerMessages.INVALID_USAGE, method, args, where, error.description);
                    } else if(error.code == 945) {
                        sendMessage(HandlerMessages.CHAT_DISABLED, method, args, where, error.description);
                    }
                } else if (response_code == 301 && !use_https) {
                    sendMessage(HandlerMessages.INTERNAL_ERROR, method, response_body);
                } else if (response_code == 302 && !use_https) {
                    sendMessage(HandlerMessages.INTERNAL_ERROR, method, args, where, response_body);
                } else if (response_code == 503) {
                    sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, method, args, where, response_body);
                } else if (response_code >= 500 && response_code <= 526) {
                    Log.e(OpenVKAPI.TAG,
                            String.format("Getting response from %s (%s, %s)", server,
                                    method, response_code));
                    sendMessage(HandlerMessages.INTERNAL_ERROR, method, args, where, "");
                }
            }
        } catch (ConnectException | ProtocolException e) {
            if (logging_enabled) {
                if (e.getMessage() != null) {
                    Log.e(OpenVKAPI.TAG,
                            String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                } else {
                    Log.e(OpenVKAPI.TAG,
                            String.format("Connection error: %s", e.getClass().getSimpleName()));
                    error.description = e.getClass().getSimpleName();
                }
            }
            error.description = e.getMessage();
            sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, where, error.description);
        } catch (SocketException e) {
            if(e.getMessage().contains("ETIMEDOUT")) {
                if(logging_enabled) Log.e(OpenVKAPI.TAG,
                        String.format("Connection error: %s", e.getMessage()));
                error.description = e.getMessage();
                sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, where, error.description);
            }
        } catch (SocketTimeoutException e) {
            if (logging_enabled) {
                if (e.getMessage() != null) {
                    Log.e(OpenVKAPI.TAG,
                            String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                } else {
                    Log.e(OpenVKAPI.TAG,
                            String.format("Connection error: %s", e.getClass().getSimpleName()));
                    error.description = e.getClass().getSimpleName();
                }
            }
            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, where, error.description);
        } catch (UnknownHostException e) {
            if(logging_enabled) Log.e(OpenVKAPI.TAG,
                    String.format("Connection error: %s", e.getMessage()));
            error.description = e.getMessage();
            sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, where, error.description);
        } catch(javax.net.ssl.SSLException e) {
            if(logging_enabled) Log.e(OpenVKAPI.TAG,
                    String.format("Connection error: %s", e.getMessage()));
            error.description = e.getMessage();
            sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, method, args, where, error.description);
        } catch (IOException | HttpClientException ex) {
            if (ex.getMessage().startsWith("Authorization required")) {
                response_code = 401;
            } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                String code_str = ex.getMessage().substring
                        (ex.getMessage().length() - 3);
                response_code = Integer.parseInt(code_str);
                if (response_code == 400) {
                    error = new Error();
                    error.parse(response_body);
                    if(logging_enabled) Log.e(OpenVKAPI.TAG,
                            String.format("Getting response from %s (%s, %s): [%s / Error code: %d]",
                                    server, method, response_code, error.description, error.code));
                    if (error.code == 3) {
                        sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, where, error.description);
                    } else if (error.code == 5) {
                        sendMessage(HandlerMessages.INVALID_TOKEN, method, args, where,  error.description);
                    } else if (error.code == 15) {
                        sendMessage(HandlerMessages.ACCESS_DENIED, method, args, where, error.description);
                    } else if (error.code == 10 || error.code == 100) {
                        sendMessage(HandlerMessages.INVALID_USAGE, method, args, where, error.description);
                    }
                }
            }
        } catch (Exception e) {
            if(e.getMessage() != null) {
                if (e.getMessage().equals("Scheme 'https' not registered.")) {
                    Log.e(OpenVKAPI.TAG, String.format("WTF? %s", fUrl));
                }
                sendMessage(HandlerMessages.UNKNOWN_ERROR, method, args, where);
                e.printStackTrace();
                error.description = e.getMessage();
            }
        }
    }

    private void sendMessage(final int message, String response) {
        try {
            Message msg = new Message();
            msg.what = message;
            final Bundle bundle = new Bundle();
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(final int message, String method, String response) {
        sendMessage(message, method, null, null, response);
    }

    private void sendMessage(final int message, String method, String args, String response) {
       sendMessage(message, method, args, null, response);
    }

    private void sendMessage(final int message, String method, String args, String where, String response) {
        try {
            Message msg = new Message();
            msg.what = message;
            final Bundle bundle = new Bundle();
            if(response != null)
                bundle.putString("response", response);
            if(method != null)
                bundle.putString("method", method);
            if(args != null)
                bundle.putString("args", args);
            if(where != null)
                bundle.putString("where", where);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkHTTPS() {
        OkHttpClient httpClient = null;
        HttpClient httpClientLegacy = null;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            Log.v(OpenVKAPI.TAG, "Starting OvkAPIWrapper in Legacy mode...");
            httpClientLegacy = new HttpClient(ctx);
            httpClientLegacy.setConnectTimeout(30);
            httpClientLegacy.setReadTimeout(30);
        } else {
            if (use_https)
                httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS).followRedirects(false)
                        .followSslRedirects(true).build();
            else
                httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS).followRedirects(false)
                        .followSslRedirects(false).build();
        }
        String url = "";
        url = String.format("http://%s", server);
        if(logging_enabled) Log.e(OpenVKAPI.TAG, String.format("Checking %s...", server));
        final String fUrl = url;
        final HttpClient finalHttpClientLegacy = new HttpClient(ctx);
        final OkHttpClient finalHttpClient = httpClient;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                if(legacy_mode) {
                    request_legacy = finalHttpClientLegacy.get(fUrl);
                } else {
                    request = new Request.Builder()
                            .url(fUrl)
                            .build();
                }
                try {
                    if(legacy_mode) {
                        HttpResponse response = request_legacy.execute();
                        assert response != null;
                        response.read(response_body);
                        response_code = response.getStatusCode();
                    } else {
                        Response response = finalHttpClient.newCall(request).execute();
                        response_body = response.body().string();
                        response_code = response.code();
                    }
                    if(response_code == 200) {
                        sendMessage(HandlerMessages.OVK_CHECK_HTTP, response_body);
                    } else if(response_code == 301) {
                        sendMessage(HandlerMessages.OVK_CHECK_HTTPS, response_body);
                    }
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e(OpenVKAPI.TAG, String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e(OpenVKAPI.TAG, String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (IOException | HttpClientException ex) {
                    if(ex.getMessage().startsWith("Expected status code 2xx")) {
                        String code_str = ex.getMessage().substring
                                (ex.getMessage().length() - 3);
                        response_code = Integer.parseInt(code_str);
                        if(response_code == 301) {
                            sendMessage(HandlerMessages.OVK_CHECK_HTTPS, "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void setAPIListeners(OvkAPIListeners apiListeners) {
        this.apiListeners = apiListeners;
    }

    public HashMap<String, Object> getClientInfo() {
        return client_info;
    }
}
