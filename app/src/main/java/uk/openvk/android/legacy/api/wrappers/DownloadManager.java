package uk.openvk.android.legacy.api.wrappers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.AuthActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;

/**
 * Created by Dmitry on 27.09.2022.
 */
@SuppressWarnings("ALL")
public class DownloadManager {

    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private String status;
    private uk.openvk.android.legacy.api.models.Error error;
    private Context ctx;
    private Handler handler;
    private String access_token;

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;

    public DownloadManager(Context ctx, boolean use_https) {
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
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            } else {
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            }
            httpClientLegacy = (HttpClient) new DefaultHttpClient((ClientConnectionManager) new ThreadSafeClientConnManager((HttpParams) basicHttpParams, schemeRegistry), (HttpParams) basicHttpParams);
            legacy_mode = true;
        } else {
            httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
            legacy_mode = false;
        }
    }

    private String generateUserAgent(Context ctx) {
        String version_name = "";
        int version_code = 0;
        String user_agent;
        try {
            PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            version_name = packageInfo.versionName;
            version_code = packageInfo.versionCode;
        } catch (Exception e) {
            OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
            version_name = app.version;
            version_code = app.build_number;
        } finally {
            user_agent = String.format("OpenVK Legacy/%s.%d (Android %s; SDK %d; %s; %s %s; %s)", version_name, version_code,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }


    public void downloadPhotosToCache(final ArrayList<String> photos, final String prefix) {
        Log.v("DownloadManager", String.format("Downloading %d photos...", photos.size()));
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            int filesize = 0;
            private InputStream response_in;
            private String url = "";
            private String filename;

            @Override
            public void run() {
                for (int i = 0; i < photos.size(); i++) {
                    filesize = 0;
                    filename = String.format("%s_%d", prefix, i);
                    if (photos.get(i).length() == 0) {
                        Log.e("DownloadManager", "Invalid address. Skipping...");
                        try {
                            File downloadedFile = new File(ctx.getCacheDir(), filename);
                            if(downloadedFile.exists()) {
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                byte[] bytes = new byte[1];
                                bytes[0] = 0;
                                fos.write(bytes);
                                fos.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String short_address = "";
                        if(photos.get(i).length() > 40) {
                            short_address = photos.get(i).substring(0, 39);
                        } else {
                            short_address = photos.get(i);
                        }
                        Log.v("DownloadManager", String.format("Downloading %s (%d/%d)...", short_address, i + 1, photos.size()));
                        url = photos.get(i);
                        if (legacy_mode) {
                            request_legacy = new HttpGet(url);
                            request_legacy.getParams().setParameter("timeout", 30000);
                        } else {
                            request = new Request.Builder()
                                    .url(url)
                                    .build();
                        }
                        try {
                            if (legacy_mode) {
                                HttpResponse response = httpClientLegacy.execute(request_legacy);
                                StatusLine statusLine = response.getStatusLine();
                                response_in = response.getEntity().getContent();
                                File downloadedFile = new File(ctx.getCacheDir(), filename);
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                int inByte;
                                while ((inByte = response_in.read()) != -1) {
                                    fos.write(inByte);
                                    filesize++;
                                }
                                response_in.close();
                                fos.close();
                                response_code = statusLine.getStatusCode();
                            } else {
                                Response response = httpClient.newCall(request).execute();
                                response_code = response.code();
                                File downloadedFile = new File(ctx.getCacheDir(), filename);
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                int inByte;
                                while ((inByte = response.body().byteStream().read()) != -1) {
                                    fos.write(inByte);
                                    filesize++;
                                }
                                fos.close();
                                response.body().byteStream().close();
                            }
                            Log.v("DownloadManager", String.format("Downloaded from %s (%s): %d kB (%d/%d)", short_address, response_code, (int) (filesize / 1024), i + 1, photos.size()));
                        } catch (IOException e) {
                            Log.e("DownloadManager", String.format("Download error: %s (%d/%d)", e.getMessage(), i + 1, photos.size()));
                        } catch (Exception e) {
                            Log.e("DownloadManager", String.format("Download error: %s (%d/%d)", e.getMessage(), i + 1, photos.size()));
                        }
                    }
                }
                if (response_code == 200) {
                    if (prefix.equals("profile_avatar")) {
                        sendMessage(HandlerMessages.PROFILE_AVATAR, prefix);
                    } else if (prefix.equals("newsfeed_item_avatar")) {
                        sendMessage(HandlerMessages.NEWSFEED_ITEM_AVATAR, prefix);
                    } else if (prefix.equals("newsfeed_item_photo")) {
                        sendMessage(HandlerMessages.NEWSFEED_ATTACHMENT, prefix);
                    } else if (prefix.equals("wall_item_photo")) {
                        sendMessage(HandlerMessages.WALL_ATTACHMENT, prefix);
                    }
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
        }
    }

    private void sendMessage(int message, String response, int id) {
        Message msg = new Message();
        msg.what = message;
        Bundle bundle = new Bundle();
        bundle.putString("response", response);
        bundle.putInt("id", id);
        msg.setData(bundle);
        if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
            ((AuthActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) ctx).handler.sendMessage(msg);
        }
    }

    public void clearCache() {
        try {
            File cache_dir = new File(ctx.getCacheDir().getAbsolutePath());
            if (cache_dir.isDirectory()) {
                String[] children = cache_dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(cache_dir, children[i]).delete();
                }
                Toast.makeText(ctx, R.string.img_cache_cleared, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {

        }
    }
}
