package uk.openvk.android.legacy.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.AuthActivity;
import uk.openvk.android.legacy.user_interface.activities.GroupIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.WallPostActivity;
import uk.openvk.android.legacy.user_interface.activities.ConversationActivity;
import uk.openvk.android.legacy.user_interface.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.MainSettingsActivity;
import uk.openvk.android.legacy.user_interface.activities.NewPostActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Error;

@SuppressWarnings("ALL")
public class OvkAPIWrapper {

    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    public boolean proxy_connection;
    public String proxy_type;
    private String status;
    public Error error;
    private Context ctx;
    private Handler handler;
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
                if (use_https) {
                    schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                } else {
                    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                }
                httpClientLegacy = (HttpClient) new DefaultHttpClient((ClientConnectionManager) new ThreadSafeClientConnManager((HttpParams) basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
                legacy_mode = true;
            } else {
                if (use_https) {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(false).build();
                } else {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
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
            user_agent = String.format("OpenVK Legacy/%s (Android %s; SDK %d; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
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
        String url = "";
        if(use_https) {
            url = String.format("https://%s/token?username=%s&password=%s&grant_type=password&client_name=%s&2fa_supported=1", server, username, URLEncoder.encode(password), client_name);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s... (Secured)", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&client_name=%s&2fa_supported=1", server, username, URLEncoder.encode(password), client_name);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...", server));
        }
        final String fUrl = url;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            boolean isHttps = false;
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
                                Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                            } else if (response_code == 200) {
                                sendMessage(HandlerMessages.AUTHORIZED, response_body);
                            } else {
                                sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                            }
                        }
                        ;
                    } catch (ProtocolException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (ConnectException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (UnknownHostException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLProtocolException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLHandshakeException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
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
        String url = "";
        if(use_https) {
            url = String.format("https://%s/token?username=%s&password=%s&grant_type=password&code=%s&client_name=%s&2fa_supported=1", server, username, URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s (Secured)...", server));
        } else {
            url = String.format("http://%s/token?username=%s&password=%s&grant_type=password&code=%s&client_name=%s&2fa_supported=1", server, username, URLEncoder.encode(password), code, client_name);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...", server));
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
                                Log.v("OpenVK API", String.format("Connected (%d)", response_code));
                            if (response_code == 400) {
                                sendMessage(HandlerMessages.INVALID_USERNAME_OR_PASSWORD, response_body);
                            } else if (response_code == 401) {
                                sendMessage(HandlerMessages.TWOFACTOR_CODE_REQUIRED, response_body);
                            } else if (response_code == 404) {
                                sendMessage(HandlerMessages.NOT_OPENVK_INSTANCE, response_body);
                            } else if (response_code == 200) {
                                sendMessage(HandlerMessages.AUTHORIZED, response_body);
                            } else {
                                sendMessage(HandlerMessages.UNKNOWN_ERROR, response_body);
                            }
                        }
                        ;
                    } catch (ProtocolException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (ConnectException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketTimeoutException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                    } catch (UnknownHostException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLProtocolException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLHandshakeException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (javax.net.ssl.SSLException e) {
                        if (logging_enabled)
                            Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (OutOfMemoryError e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    } catch (IOException e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
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
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s (Secured)...\r\nMethod: %s\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s\r\nWhere: %s", server, method, args, where));
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
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                if (method.equals("Account.getProfileInfo")) {
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                } else if (method.equals("Account.setOnline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                } else if (method.equals("Account.setOffline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                } else if (method.equals("Account.getCounters")) {
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                } else if (method.equals("Friends.get")) {
                                    if (where.equals("friends_list")) {
                                        sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                    } else if (where.equals("profile_counter")) {
                                        sendMessage(HandlerMessages.FRIENDS_GET_ALT, method, args, response_body);
                                    } else if(where.equals("more_friends")) {
                                        sendMessage(HandlerMessages.FRIENDS_GET_MORE, method, args, response_body);
                                    }
                                } else if (method.equals("Friends.get")) {
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                } else if (method.equals("Friends.add")) {
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                } else if (method.equals("Friends.delete")) {
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                } else if (method.equals("Friends.areFriends")) {
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                } else if (method.equals("Friends.getRequests")) {
                                    sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, args, response_body);
                                } else if (method.equals("Groups.get")) {
                                    if(where.equals("more_groups")) {
                                        sendMessage(HandlerMessages.GROUPS_GET_MORE, method, args, response_body);
                                    } else {
                                        sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                    }
                                } else if (method.equals("Groups.getById")) {
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Groups.search")) {
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                } else if (method.equals("Groups.join")) {
                                    sendMessage(HandlerMessages.GROUPS_JOIN, method, response_body);
                                } else if (method.equals("Groups.leave")) {
                                    sendMessage(HandlerMessages.GROUPS_LEAVE, method, response_body);
                                } else if (method.equals("Likes.add")) {
                                    sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                } else if (method.equals("Likes.delete")) {
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                } else if (method.equals("Likes.isLiked")) {
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                } else if (method.equals("Messages.getById")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Messages.send")) {
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                } else if (method.equals("Messages.delete")) {
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                } else if (method.equals("Messages.restore")) {
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                } else if (method.equals("Messages.getConverstaions")) {
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                } else if (method.equals("Messages.getConverstaionsByID")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                } else if (method.equals("Messages.getHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                } else if (method.equals("Messages.getLongPollHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                } else if (method.equals("Messages.getLongPollServer")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                } else if (method.equals("Ovk.version")) {
                                    sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                } else if (method.equals("Ovk.test")) {
                                    sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                } else if (method.equals("Ovk.chickenWings")) {
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                } else if (method.equals("Ovk.aboutInstance")) {
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, args, response_body);
                                } else if (method.equals("Users.getFollowers")) {
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                } else if (method.equals("Users.search")) {
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                } else if (method.equals("Users.get")) {
                                    if (where.equals("profile")) {
                                        sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                    } else if (where.equals("account_user")) {
                                        sendMessage(HandlerMessages.USERS_GET_ALT, method, args, response_body);
                                    } else if (where.equals("peers")) {
                                        sendMessage(HandlerMessages.USERS_GET_ALT2, method, args, response_body);
                                    }
                                } else if (method.equals("Wall.get")) {
                                    sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                } else if (method.equals("Wall.getById")) {
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Wall.post")) {
                                    sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                } else if (method.equals("Wall.repost")) {
                                    sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.getComment")) {
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.getComments")) {
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                } else if (method.equals("Newsfeed.get")) {
                                    if(where.equals("more_news")) {
                                        sendMessage(HandlerMessages.NEWSFEED_GET_MORE, method, args, response_body);
                                    } else {
                                        sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                    }
                                } else if (method.equals("Newsfeed.getGlobal")) {
                                    if(where.equals("more_news")) {
                                        sendMessage(HandlerMessages.NEWSFEED_GET_MORE_GLOBAL, method, args, response_body);
                                    } else {
                                        sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, args, response_body);
                                    }
                                } else if (method.equals("Polls.addVote")) {
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                } else if (method.equals("Polls.deleteVote")) {
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                }
                            } else if (response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                                if (error.code == 3) {
                                    sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, args, error.description);
                                } else if (error.code == 5) {
                                    sendMessage(HandlerMessages.INVALID_TOKEN, method, args, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                                } else if (error.code == 100) {
                                    sendMessage(HandlerMessages.INVALID_USAGE, method, args, error.description);
                                }
                            } else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        };
                    } catch (ConnectException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (ProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, args, error.description);
                    } catch(javax.net.ssl.SSLProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLHandshakeException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (OutOfMemoryError e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    } catch (IOException e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
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

    public void sendAPIMethod(final String method, final String args) {
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.v("OpenVK API", String.format("Connecting to %s (Secured)...\r\nMethod: %s\r\nArguments: %s", server, method, args));
        } else {
            url = String.format("http://%s/method/%s?%s&access_token=%s", server, method, args, access_token);
            Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: %s", server, method, args));
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
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                if (method.equals("Account.getProfileInfo")) {
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, args, response_body);
                                } else if (method.equals("Account.setOnline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, args, response_body);
                                } else if (method.equals("Account.setOffline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, args, response_body);
                                } else if (method.equals("Account.getCounters")) {
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, args, response_body);
                                } else if (method.equals("Friends.get")) {
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                } else if (method.equals("Friends.get")) {
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, args, response_body);
                                } else if (method.equals("Friends.add")) {
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, args, response_body);
                                } else if (method.equals("Friends.delete")) {
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, args, response_body);
                                } else if (method.equals("Friends.areFriends")) {
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, args, response_body);
                                }  else if (method.equals("Friends.getRequests")) {
                                    sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, response_body);
                                } else if (method.equals("Groups.get")) {
                                    sendMessage(HandlerMessages.GROUPS_GET, method, args, response_body);
                                } else if (method.equals("Groups.getById")) {
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Groups.search")) {
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                } else if (method.equals("Groups.join")) {
                                    sendMessage(HandlerMessages.GROUPS_JOIN, method, response_body);
                                } else if (method.equals("Groups.leave")) {
                                    sendMessage(HandlerMessages.GROUPS_LEAVE, method, response_body);
                                } else if (method.equals("Friends.getRequests")) {
                                    sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, args, response_body);
                                } else if (method.equals("Likes.add")) {
                                    sendMessage(HandlerMessages.LIKES_ADD, method, args, response_body);
                                } else if (method.equals("Likes.delete")) {
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, args, response_body);
                                } else if (method.equals("Likes.isLiked")) {
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, args, response_body);
                                } else if (method.equals("Messages.getById")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Messages.send")) {
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, args, response_body);
                                } else if (method.equals("Messages.delete")) {
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, args, response_body);
                                } else if (method.equals("Messages.restore")) {
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, args, response_body);
                                } else if (method.equals("Messages.getConversations")) {
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, args, response_body);
                                } else if (method.equals("Messages.getConverstaionsByID")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, args, response_body);
                                } else if (method.equals("Messages.getHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, args, response_body);
                                } else if (method.equals("Messages.getLongPollHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, args, response_body);
                                } else if (method.equals("Messages.getLongPollServer")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, args, response_body);
                                } else if (method.equals("Ovk.version")) {
                                    sendMessage(HandlerMessages.OVK_VERSION, method, args, response_body);
                                } else if (method.equals("Ovk.test")) {
                                    sendMessage(HandlerMessages.OVK_TEST, method, args, response_body);
                                } else if (method.equals("Ovk.chickenWings")) {
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, args, response_body);
                                } else if (method.equals("Ovk.aboutInstance")) {
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE,  method, args, response_body);
                                } else if (method.equals("Users.get")) {
                                    sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                } else if (method.equals("Users.getFollowers")) {
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, args, response_body);
                                } else if (method.equals("Users.search")) {
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, args, response_body);
                                } else if (method.equals("Users.get")) {
                                    sendMessage(HandlerMessages.USERS_GET, method, args, response_body);
                                } else if (method.equals("Wall.get")) {
                                    sendMessage(HandlerMessages.WALL_GET, method, args, response_body);
                                } else if (method.equals("Wall.getById")) {
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, args, response_body);
                                } else if (method.equals("Wall.post")) {
                                    sendMessage(HandlerMessages.WALL_POST, method, args, response_body);
                                } else if (method.equals("Wall.repost")) {
                                    sendMessage(HandlerMessages.WALL_REPOST, method, args, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.getComment")) {
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, args, response_body);
                                } else if (method.equals("Wall.getComments")) {
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, args, response_body);
                                } else if (method.equals("Newsfeed.get")) {
                                    sendMessage(HandlerMessages.NEWSFEED_GET, method, args, response_body);
                                } else if (method.equals("Newsfeed.getGlobal")) {
                                    sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, args, response_body);
                                } else if (method.equals("Polls.addVote")) {
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, args, response_body);
                                } else if (method.equals("Polls.deleteVote")) {
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, args, response_body);
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
                                if(error.code == 3) {
                                    sendMessage(HandlerMessages.METHOD_NOT_FOUND, method, error.description);
                                } else if(error.code == 5) {
                                    sendMessage(HandlerMessages.INVALID_TOKEN, method, error.description);
                                } else if (error.code == 15) {
                                    sendMessage(HandlerMessages.ACCESS_DENIED, method, args, error.description);
                                } else if(error.code == 100) {
                                    sendMessage(HandlerMessages.INVALID_USAGE, method, error.description);
                                } else if(error.code == 945) {
                                    sendMessage(HandlerMessages.CHAT_DISABLED, method, error.description);
                                }
                            } else if (response_code >= 500 && response_code <= 526) {
                                if(logging_enabled) Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        };
                    } catch (ConnectException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (ProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, args, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, error.description);
                    } catch(javax.net.ssl.SSLProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLHandshakeException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (OutOfMemoryError e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    } catch (IOException e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    } catch (Exception e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                    }
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
        error.description = "";
        String url = "";
        if(use_https) {
            url = String.format("https://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s (Secured)...\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
        } else {
            url = String.format("http://%s/method/%s?access_token=%s", server, method, access_token);
            if(logging_enabled) Log.v("OpenVK API", String.format("Connecting to %s...\r\nMethod: %s\r\nArguments: [without arguments]", server, method));
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
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                                if (method.equals("Account.getProfileInfo")) {
                                    sendMessage(HandlerMessages.ACCOUNT_PROFILE_INFO, method, response_body);
                                } else if (method.equals("Account.setOnline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_ONLINE, method, response_body);
                                } else if (method.equals("Account.setOffline")) {
                                    sendMessage(HandlerMessages.ACCOUNT_SET_TO_OFFLINE, method, response_body);
                                } else if (method.equals("Account.getCounters")) {
                                    sendMessage(HandlerMessages.ACCOUNT_COUNTERS, method, response_body);
                                } else if (method.equals("Friends.get")) {
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, response_body);
                                } else if (method.equals("Friends.get")) {
                                    sendMessage(HandlerMessages.FRIENDS_GET, method, response_body);
                                } else if (method.equals("Friends.add")) {
                                    sendMessage(HandlerMessages.FRIENDS_ADD, method, response_body);
                                } else if (method.equals("Friends.delete")) {
                                    sendMessage(HandlerMessages.FRIENDS_DELETE, method, response_body);
                                } else if (method.equals("Friends.areFriends")) {
                                    sendMessage(HandlerMessages.FRIENDS_CHECK, method, response_body);
                                } else if (method.equals("Friends.getRequests")) {
                                    sendMessage(HandlerMessages.FRIENDS_REQUESTS, method, response_body);
                                } else if (method.equals("Groups.get")) {
                                    sendMessage(HandlerMessages.GROUPS_GET, method, response_body);
                                } else if (method.equals("Groups.getById")) {
                                    sendMessage(HandlerMessages.GROUPS_GET_BY_ID, method, response_body);
                                } else if (method.equals("Groups.search")) {
                                    sendMessage(HandlerMessages.GROUPS_SEARCH, method, response_body);
                                } else if (method.equals("Groups.join")) {
                                    sendMessage(HandlerMessages.GROUPS_JOIN, method, response_body);
                                } else if (method.equals("Groups.leave")) {
                                    sendMessage(HandlerMessages.GROUPS_LEAVE, method, response_body);
                                } else if (method.equals("Likes.add")) {
                                    sendMessage(HandlerMessages.LIKES_ADD, method, response_body);
                                } else if (method.equals("Likes.delete")) {
                                    sendMessage(HandlerMessages.LIKES_DELETE, method, response_body);
                                } else if (method.equals("Likes.isLiked")) {
                                    sendMessage(HandlerMessages.LIKES_CHECK, method, response_body);
                                } else if (method.equals("Messages.getById")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_BY_ID, method, response_body);
                                } else if (method.equals("Messages.send")) {
                                    sendMessage(HandlerMessages.MESSAGES_SEND, method, response_body);
                                } else if (method.equals("Messages.delete")) {
                                    sendMessage(HandlerMessages.MESSAGES_DELETE, method, response_body);
                                } else if (method.equals("Messages.restore")) {
                                    sendMessage(HandlerMessages.MESSAGES_RESTORE, method, response_body);
                                } else if (method.equals("Messages.getConverstaions")) {
                                    sendMessage(HandlerMessages.MESSAGES_CONVERSATIONS, method, response_body);
                                } else if (method.equals("Messages.getConverstaionsByID")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID, method, response_body);
                                } else if (method.equals("Messages.getHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_HISTORY, method, response_body);
                                } else if (method.equals("Messages.getLongPollHistory")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_HISTORY, method, response_body);
                                } else if (method.equals("Messages.getLongPollServer")) {
                                    sendMessage(HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER, method, response_body);
                                } else if (method.equals("Ovk.version")) {
                                    sendMessage(HandlerMessages.OVK_VERSION, method, response_body);
                                } else if (method.equals("Ovk.test")) {
                                    sendMessage(HandlerMessages.OVK_TEST, method, response_body);
                                } else if (method.equals("Ovk.chickenWings")) {
                                    sendMessage(HandlerMessages.OVK_CHICKEN_WINGS, method, response_body);
                                } else if (method.equals("Ovk.aboutInstance")) {
                                    sendMessage(HandlerMessages.OVK_ABOUTINSTANCE, method, response_body);
                                } else if (method.equals("Users.get")) {
                                    sendMessage(HandlerMessages.USERS_GET, method, response_body);
                                } else if (method.equals("Users.getFollowers")) {
                                    sendMessage(HandlerMessages.USERS_FOLLOWERS, method, response_body);
                                } else if (method.equals("Users.search")) {
                                    sendMessage(HandlerMessages.USERS_SEARCH, method, response_body);
                                } else if (method.equals("Users.get")) {
                                    sendMessage(HandlerMessages.USERS_GET, method, response_body);
                                } else if (method.equals("Wall.get")) {
                                    sendMessage(HandlerMessages.WALL_GET, method, response_body);
                                } else if (method.equals("Wall.getById")) {
                                    sendMessage(HandlerMessages.WALL_GET_BY_ID, method, response_body);
                                } else if (method.equals("Wall.post")) {
                                    sendMessage(HandlerMessages.WALL_POST, method, response_body);
                                } else if (method.equals("Wall.repost")) {
                                    sendMessage(HandlerMessages.WALL_REPOST, method, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_CREATE_COMMENT, method, response_body);
                                } else if (method.equals("Wall.createComment")) {
                                    sendMessage(HandlerMessages.WALL_DELETE_COMMENT, method, response_body);
                                } else if (method.equals("Wall.getComment")) {
                                    sendMessage(HandlerMessages.WALL_COMMENT, method, response_body);
                                } else if (method.equals("Wall.getComments")) {
                                    sendMessage(HandlerMessages.WALL_ALL_COMMENTS, method, response_body);
                                } else if (method.equals("Newsfeed.get")) {
                                    sendMessage(HandlerMessages.NEWSFEED_GET, method, response_body);
                                } else if (method.equals("Newsfeed.getGlobal")) {
                                    sendMessage(HandlerMessages.NEWSFEED_GET_GLOBAL, method, response_body);
                                } else if (method.equals("Polls.addVote")) {
                                    sendMessage(HandlerMessages.POLL_ADD_VOTE, method, response_body);
                                } else if (method.equals("Polls.deleteVote")) {
                                    sendMessage(HandlerMessages.POLL_DELETE_VOTE, method, response_body);
                                }
                            } else if(response_code == 400) {
                                error = new Error();
                                error.parse(response_body);
                                if(logging_enabled) Log.v("OpenVK API", String.format("Getting response from %s (%s): [%s / Error code: %d]", server, response_code, error.description, error.code));
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
                            } else if (response_code >= 500 && response_code <= 526) {
                                Log.e("OpenVK API", String.format("Getting response from %s (%s)", server, response_code));
                                sendMessage(HandlerMessages.INTERNAL_ERROR, method, "");
                            }
                        };
                    } catch (ConnectException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (ProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                    } catch (SocketException e) {
                        if(e.getMessage().contains("ETIMEDOUT")) {
                            if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                            error.description = e.getMessage();
                            sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                        }
                    } catch (SocketTimeoutException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.CONNECTION_TIMEOUT, method, error.description);
                    } catch (UnknownHostException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, method, error.description);
                    } catch(javax.net.ssl.SSLProtocolException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLHandshakeException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch(javax.net.ssl.SSLException e) {
                        if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                        error.description = e.getMessage();
                        sendMessage(HandlerMessages.BROKEN_SSL_CONNECTION, error.description);
                    } catch (IOException e) {
                        sendMessage(HandlerMessages.UNKNOWN_ERROR, "");
                        e.printStackTrace();
                        error.description = e.getMessage();
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
            HttpProtocolParams.setUseExpectContinue((HttpParams) basicHttpParams, false);
            HttpProtocolParams.setUserAgent((HttpParams) basicHttpParams, generateUserAgent(ctx));
            HttpConnectionParams.setSocketBufferSize((HttpParams) basicHttpParams, 8192);
            HttpConnectionParams.setConnectionTimeout((HttpParams) basicHttpParams, 30000);
            HttpConnectionParams.setSoTimeout((HttpParams) basicHttpParams, 30000);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            if (use_https) {
                schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            } else {
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            }
            httpClientLegacy = (HttpClient) new DefaultHttpClient((ClientConnectionManager) new ThreadSafeClientConnManager((HttpParams) basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
        } else {
            httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build();
        }
        String url = "";
        url = String.format("http://%s", server);
        if(logging_enabled) Log.v("OpenVK API", String.format("Checking %s...", server));
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
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.CONNECTION_TIMEOUT, error.description);
                } catch (UnknownHostException e) {
                    if(logging_enabled) Log.e("OpenVK API", String.format("Connection error: %s", e.getMessage()));
                    error.description = e.getMessage();
                    sendMessage(HandlerMessages.NO_INTERNET_CONNECTION, error.description);
                } catch (IOException e) {
                    e.printStackTrace();
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
