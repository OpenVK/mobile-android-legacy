package uk.openvk.android.legacy.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.AuthActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.core.activities.ConversationActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.MainSettingsActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Error;

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

@SuppressWarnings("deprecation")
public class OvkAPIWrapper {

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

    public OvkAPIWrapper(Context ctx, boolean use_https) {
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        this.ctx = ctx;
        this.use_https = use_https;
        error = new Error();
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                BasicHttpParams basicHttpParams = new BasicHttpParams();
                HttpProtocolParams.setUseExpectContinue((HttpParams) basicHttpParams, false);
                HttpProtocolParams.setUserAgent((HttpParams) basicHttpParams, generateUserAgent(ctx));
                HttpConnectionParams.setSocketBufferSize((HttpParams) basicHttpParams, 8192);
                HttpConnectionParams.setConnectionTimeout((HttpParams) basicHttpParams, 30000);
                HttpConnectionParams.setSoTimeout((HttpParams) basicHttpParams, 30000);
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                httpClientLegacy = (HttpClient) new DefaultHttpClient((ClientConnectionManager)
                        new ThreadSafeClientConnManager((HttpParams) basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
                legacy_mode = true;
            } else {
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
                legacy_mode = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                        httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                                .retryOnConnectionFailure(false).proxy(new Proxy(Proxy.Type.HTTP,
                                        new InetSocketAddress(address_array[0],
                                        Integer.valueOf(address_array[1])))).build();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        String user_agent;
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
        } finally {
            user_agent = String.format("OpenVK Legacy/%s (Android %s; SDK %s; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER,
                    Build.MODEL, System.getProperty("user.language"));
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
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s... (Secured)", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password" +
                    "&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username),
                    URLEncoder.encode(password), client_name);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s...", server));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() throws OutOfMemoryError {
                try {
                    if (legacy_mode) {
                        request_legacy = new HttpGet(fUrl);
                        request_legacy.getParams().setParameter("timeout", 30000);
                    } else {
                        request = new Request.Builder()
                                .url(fUrl)
                                .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    }
                    try {
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
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.d(OvkApplication.API_TAG, String.format("Connected (%d)", response_code));
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
                                                response_body.replace("\r", "").replace("\n", "").substring(0, 16)), 0);
                                    } else {
                                        throw new java.text.ParseException(String.format("Response data " +
                                                        "must be in JSON format only. Start of response: [%s]",
                                                response_body.replace("\r", "").replace("\n", "")), 0);
                                    }
                                }
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
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, "");
                    } catch (IOException ignored) {

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
            url = String.format("https://%s/token?username=%s&password=%s&grant_type=password&code=%s&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username), URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s (Secured)...", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&code=%s&client_name=%s&2fa_supported=1", server, URLEncoder.encode(username), URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s...", server));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if (legacy_mode) {
                        request_legacy = new HttpGet(fUrl);
                        request_legacy.getParams().setParameter("timeout", 30000);
                    } else {
                        request = new Request.Builder()
                                .url(fUrl)
                                .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    }
                    try {
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
                        if (response_body.length() > 0) {
                            if (logging_enabled)
                                Log.e(OvkApplication.API_TAG, String.format("Connected (%d)", response_code));
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
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, response_body);
                            } else {
                                sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                            }
                        }
                        ;
                    } catch (ProtocolException | ConnectException | javax.net.ssl.SSLProtocolException | UnknownHostException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (javax.net.ssl.SSLException | OutOfMemoryError e) {
                        if (logging_enabled)
                            Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, "");
                    } catch (IOException ignored) {

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
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecti to %s (Secured)...\r\nMethod: %s\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = new HttpGet(fUrl);
                        request_legacy.getParams().setParameter("timeout", 30000);
                    } else {
                        request = new Request.Builder()
                                .url(fUrl)
                                .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    }
                    try {
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
                        if (response_body.length() > 0) {
                            if (response_code == 200) {
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                        break;
                                    case "Friends.get":
                                        switch (where) {
                                            case "friends_list":
                                                sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                                break;
                                            case "profile_counter":
                                                sendMessage(HandlerMessages.FRIENDS_GET_ALT, method, args, response_body);
                                                break;
                                            case "more_friends":
                                                sendMessage(HandlerMessages.FRIENDS_GET_MORE, method, args, response_body);
                                                break;
                                        }
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, args, response_body);
                                        break;
                                    case "Groups.get":
                                        if (where.equals("more_groups")) {
                                            sendMessage(HandlerMessages.GROUPS_GET_MORE, method, args, response_body);
                                        } else {
                                            sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                        }
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.GROUPS_JOIN, method, response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.GROUPS_LEAVE, method, response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                        break;
                                    case "Messages.getConverstaions":
                                        sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, args, response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                        break;
                                    case "Users.get":
                                        switch (where) {
                                            case "profile":
                                                sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                                break;
                                            case "account_user":
                                                sendMessage(HandlerMessages.USERS_GET_ALT, method, args, response_body);
                                                break;
                                            case "peers":
                                                sendMessage(HandlerMessages.USERS_GET_ALT2, method, args, response_body);
                                                break;
                                        }
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, args, response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                        break;
                                    case "Newsfeed.get":
                                        if (where.equals("more_news")) {
                                            sendMessage(HandlerMessages.NEWSFEED_GET_MORE, method, args, response_body);
                                        } else {
                                            sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                        }
                                        break;
                                    case "Newsfeed.getGlobal":
                                        if (where.equals("more_news")) {
                                            sendMessage(HandlerMessages.NEWSFEED_GET_MORE_GLOBAL, method, args, response_body);
                                        } else {
                                            sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, args, response_body);
                                        }
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                        break;
                                }
                            } else if (response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                                if (error.code == 3) {
                                    sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, error.description);
                                } else if (error.code == 5) {
                                    sendMessage(HandlerMessages.INVALID_TOKEN, method, args, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                                } else if (error.code == 100) {
                                    sendMessage(HandlerMessages.INVALID_USAGE, method, args, error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, method, args, response_body);
                            } else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        }
                    } catch (ConnectException | ProtocolException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (IOException ignored) {

                    } catch (OutOfMemoryError | Exception e) {
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

    public void sendAPIMethod(final String method, final String args) {
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.d(OvkApplication.API_TAG, String.format("Connecti to %s (Secured)...\r\nMethod: %s\r\nArguments: %s", server, method, args));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.d(OvkApplication.API_TAG, String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s", server, method, args));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = new HttpGet(fUrl);
                        request_legacy.getParams().setParameter("timeout", 30000);
                    } else {
                        request = new Request.Builder()
                                .url(fUrl)
                                .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    }
                    try {
                        if(legacy_mode) {
                            HttpResponse response = httpClientLegacy.execute(request_legacy);
                            StatusLine statusLine = response.getStatusLine();
                            response_body = EntityUtils.toString(response.getEntity());
                            response_code = statusLine.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_body.length() > 0) {
                            if(response_code == 200) {
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                        break;
                                    case "Friends.get":
                                        sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                        break;
                                    case "Groups.get":
                                        sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.GROUPS_SEARCH, method, args, response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.GROUPS_JOIN, method, args, response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.GROUPS_LEAVE, method, args, response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, args, response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                        break;
                                    case "Messages.getConversations":
                                        sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, args, response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                        break;
                                    case "Users.get":
                                        sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, args, response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                        break;
                                    case "Newsfeed.get":
                                        sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                        break;
                                    case "Newsfeed.getGlobal":
                                        sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, args, response_body);
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                        break;
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                                if(error.code == 3) {
                                    sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, error.description);
                                } else if(error.code == 5) {
                                    sendMessage(HandlerMessages.INVALID_TOKEN, method, args, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                                } else if(error.code == 100) {
                                    sendMessage(HandlerMessages.INVALID_USAGE, method, args, error.description);
                                } else if(error.code == 945) {
                                    sendMessage(HandlerMessages.CHAT_DISABLED, method, args, error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, method, args, response_body);
                            }  else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        };
                    } catch (ConnectException | ProtocolException | UnknownHostException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, method, args, error.description);
                    } catch (IOException ignored) {

                    } catch (OutOfMemoryError | Exception e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, method, args, "");
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    sendMessage(HandlerMessages.UNKNOWN_ERROR, method, args, "");
                    ex.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void sendAPIMethod(final String method) {
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecti to %s (Secured)...\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
        } else {
            url = String.format("http://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                try {
                    if(legacy_mode) {
                        request_legacy = new HttpGet(fUrl);
                        request_legacy.getParams().setParameter("timeout", 30000);
                    } else {
                        request = new Request.Builder()
                                .url(fUrl)
                                .addHeader("User-Agent", generateUserAgent(ctx)).build();
                    }
                    try {
                        if(legacy_mode) {
                            HttpResponse response = httpClientLegacy.execute(request_legacy);
                            StatusLine statusLine = response.getStatusLine();
                            response_body = EntityUtils.toString(response.getEntity());
                            response_code = statusLine.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_body = response.body().string();
                            response_code = response.code();
                        }
                        if (response_body.length() > 0) {
                            if(response_code == 200) {
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                switch (method) {
                                    case "Account.getProfileInfo":
                                        sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, response_body);
                                        break;
                                    case "Account.setOnline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, response_body);
                                        break;
                                    case "Account.setOffline":
                                        sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, response_body);
                                        break;
                                    case "Account.getCounters":
                                        sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, response_body);
                                        break;
                                    case "Friends.get":
                                        sendMessage(HandlerMessages.FRIENDS_GET, method, response_body);
                                        break;
                                    case "Friends.add":
                                        sendMessage(HandlerMessages.FRIENDS_ADD, method, response_body);
                                        break;
                                    case "Friends.delete":
                                        sendMessage(HandlerMessages.FRIENDS_DELETE, method, response_body);
                                        break;
                                    case "Friends.areFriends":
                                        sendMessage(HandlerMessages.FRIENDS_CHECK, method, response_body);
                                        break;
                                    case "Friends.getRequests":
                                        sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, response_body);
                                        break;
                                    case "Groups.get":
                                        sendMessage(HandlerMessages.GROUPS_GET, method, response_body);
                                        break;
                                    case "Groups.getById":
                                        sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, response_body);
                                        break;
                                    case "Groups.search":
                                        sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                        break;
                                    case "Groups.join":
                                        sendMessage(HandlerMessages.GROUPS_JOIN, method, response_body);
                                        break;
                                    case "Groups.leave":
                                        sendMessage(HandlerMessages.GROUPS_LEAVE, method, response_body);
                                        break;
                                    case "Likes.add":
                                        sendMessage(HandlerMessages.LIKES_ADD, method, response_body);
                                        break;
                                    case "Likes.delete":
                                        sendMessage(HandlerMessages.LIKES_DELETE, method, response_body);
                                        break;
                                    case "Likes.isLiked":
                                        sendMessage(HandlerMessages.LIKES_CHECK, method, response_body);
                                        break;
                                    case "Messages.getById":
                                        sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, response_body);
                                        break;
                                    case "Messages.send":
                                        sendMessage(HandlerMessages.MESSAGES_SEND, method, response_body);
                                        break;
                                    case "Messages.delete":
                                        sendMessage(HandlerMessages.MESSAGES_DELETE, method, response_body);
                                        break;
                                    case "Messages.restore":
                                        sendMessage(HandlerMessages.MESSAGES_RESTORE, method, response_body);
                                        break;
                                    case "Messages.getConverstaions":
                                        sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, response_body);
                                        break;
                                    case "Messages.getConverstaionsByID":
                                        sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, response_body);
                                        break;
                                    case "Messages.getHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, response_body);
                                        break;
                                    case "Messages.getLongPollHistory":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, response_body);
                                        break;
                                    case "Messages.getLongPollServer":
                                        sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, response_body);
                                        break;
                                    case "Ovk.version":
                                        sendMessage(HandlerMessages.OVK_VERSION, method, response_body);
                                        break;
                                    case "Ovk.test":
                                        sendMessage(HandlerMessages.OVK_TEST, method, response_body);
                                        break;
                                    case "Ovk.chickenWings":
                                        sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, response_body);
                                        break;
                                    case "Ovk.aboutInstance":
                                        sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, response_body);
                                        break;
                                    case "Users.getFollowers":
                                        sendMessage(HandlerMessages.USERS_FOLLOWERS, method, response_body);
                                        break;
                                    case "Users.search":
                                        sendMessage(HandlerMessages.USERS_SEARCH, method, response_body);
                                        break;
                                    case "Users.get":
                                        sendMessage(HandlerMessages.USERS_GET, method, response_body);
                                        break;
                                    case "Wall.get":
                                        sendMessage(HandlerMessages.WALL_GET, method, response_body);
                                        break;
                                    case "Wall.getById":
                                        sendMessage(HandlerMessages.WALL_GET_BY_ID, method, response_body);
                                        break;
                                    case "Wall.post":
                                        sendMessage(HandlerMessages.WALL_POST, method, response_body);
                                        break;
                                    case "Wall.repost":
                                        sendMessage(HandlerMessages.WALL_REPOST, method, response_body);
                                        break;
                                    case "Wall.createComment":
                                        sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, response_body);
                                        break;
                                    case "Wall.getComment":
                                        sendMessage(HandlerMessages.WALL_COMMENT, method, response_body);
                                        break;
                                    case "Wall.getComments":
                                        sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, response_body);
                                        break;
                                    case "Newsfeed.get":
                                        sendMessage(HandlerMessages.NEWSFEED_GET, method, response_body);
                                        break;
                                    case "Newsfeed.getGlobal":
                                        sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, response_body);
                                        break;
                                    case "Polls.addVote":
                                        sendMessage(HandlerMessages.POLL_ADD_VOTE, method, response_body);
                                        break;
                                    case "Polls.deleteVote":
                                        sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, response_body);
                                        break;
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.d(OvkApplication.API_TAG, String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                                if(error.code == 3) {
                                    sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, error.description);
                                } else if(error.code == 5) {
                                    sendMessage(HandlerMessages.INVALID_TOKEN, method, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.ACCESS_DENIED, method, error.description);
                                } else if(error.code == 100) {
                                    sendMessage(HandlerMessages.INVALID_USAGE, method, error.description);
                                } else if(error.code == 945) {
                                    sendMessage(HandlerMessages.CHAT_DISABLED, method, error.description);
                                }
                            } else if (response_code == 503) {
                                sendMessage(HandlerMessages.INSTANCE_UNAVAILABLE, method, response_body);
                            }  else if (response_code >= 500 && response_code <= 526) {
                                Log.e(OvkApplication.API_TAG, String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        };
                    } catch (ConnectException | ProtocolException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled) {
                            if (e.getMessage() != null) {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                                error.description = e.getMessage();
                            } else {
                                Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getClass().getSimpleName()));
                                error.description = e.getClass().getSimpleName();
                            }
                        }
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (IOException ignored) {

                    } catch (Exception e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                        error.description = e.getMessage();
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

    private void sendMessage(int message, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            ((MainSettingsActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    private void sendMessage(int message, String method, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putString("method", method);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            ((MainSettingsActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    private void sendMessage(int message, String method, String args, String response) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putString("method", method);
        bundle.putString("args", args);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            ((MainSettingsActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            ((ConversationActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            ((NewPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        }
    }

    public void checkHTTPS() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpProtocolParams.setUseExpectContinue(basicHttpParams, false);
            HttpProtocolParams.setUserAgent(basicHttpParams, generateUserAgent(ctx));
            HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);
            HttpConnectionParams.setConnectionTimeout(basicHttpParams, 30000);
            HttpConnectionParams.setSoTimeout(basicHttpParams, 30000);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            if (use_https) {
                schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            } else {
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            }
            httpClientLegacy = new DefaultHttpClient(new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
        } else {
            if (use_https)
                httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(true).build();
            else
                httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build();
        }
        String url = "";
        url = String.format("http://%s", server);
        if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Checking %s...", server));
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            private String response_body = "";

            @Override
            public void run() {
                if(legacy_mode) {
                    request_legacy = new HttpGet(fUrl);
                    request_legacy.getParams().setParameter("timeout", 30000);
                } else {
                    request = new Request.Builder()
                            .url(fUrl)
                            .build();
                }
                try {
                    if(legacy_mode) {
                        HttpResponse response = httpClientLegacy.execute(request_legacy);
                        StatusLine statusLine = response.getStatusLine();
                        response_body = EntityUtils.toString(response.getEntity());
                        response_code = statusLine.getStatusCode();
                    } else {
                        Response response = httpClient.newCall(request).execute();
                        response_body = response.body().string();
                        response_code = response.code();
                    }
                    if(response_code == 200) {
                        sendMessage(HandlerMessages.OVK_CHECK_HTTP, response_body);
                    } else if(response_code == 301) {
                        sendMessage(HandlerMessages.OVK_CHECK_HTTPS, response_body);
                    }
                } catch (SocketTimeoutException e) {
                    if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e(OvkApplication.API_TAG, String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(httpRunnable);
        thread.start();
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

}
