package uk.openvk.android.legacy.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
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

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.OvkApplication;

/**
 * Created by Dmitry on 29.09.2022.
 */
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
            if (use_https == true) {
                schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
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
        int version_code = 0;
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
            version_code = packageInfo.versionCode;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
            version_code = app.build_number;
        } finally {
            String user_agent = String.format("OpenVK Legacy/%s.%d (Android %s; SDK %d; %s; %s %s; %s)", version_name, version_code,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return null;
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
        url = String.format("%s?act=a_check&key=%s&ts=%d&wait=15", lp_server, key, ts);
        Log.v("OpenVK LPW", String.format("Connecting to %s via LongPoll...", lp_server));
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
                        Log.v("OpenVK LPW", String.format("Getting response from %s (%s): [%s]", server, response_code, response_body));
                        if (response_body.length() > 0) {
                            if (response_code == 200) {

                            }
                        }
                        Thread.sleep(2000);
                    }
                } catch(SocketTimeoutException ex) {
                    Log.e("OpenVK LPW", "Connection error: " + ex.getMessage());
                    try {
                        Thread.sleep(60000);
                        run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    isActivated = false;
                    ex.printStackTrace();
                }
            }
        };
        thread = new Thread(longPollRunnable);
        thread.start();
    }

    public void updateCounters(final OvkAPIWrapper ovk) {
        Thread thread = null;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ovk.sendAPIMethod("Account.getCounters");
                try {
                    Thread.sleep(15000);
                    run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }
}
