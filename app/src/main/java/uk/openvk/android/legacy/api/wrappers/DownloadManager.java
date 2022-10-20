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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.AuthActivity;
import uk.openvk.android.legacy.user_interface.activities.GroupIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.WallPostActivity;
import uk.openvk.android.legacy.user_interface.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Photo;

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
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                javax.net.ssl.SSLSocketFactory ssf = (javax.net.ssl.SSLSocketFactory) sslContext.getSocketFactory();
                httpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory()).connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
            } catch (Exception e) {
                httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
            }
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
            user_agent = String.format("OpenVK Legacy/%s (Android %s; SDK %d; %s; %s %s; %s)", version_name,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.CPU_ABI, Build.MANUFACTURER, Build.MODEL, System.getProperty("user.language"));
        }
        return user_agent;
    }


    public void downloadPhotosToCache(final ArrayList<Photo> photos, final String where) {
        Log.v("DownloadManager", String.format("Downloading %d photos...", photos.size()));
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;
            private String url = "";
            private String filename = "";

            @Override
            public void run() {
                try {
                    File directory = new File(ctx.getCacheDir().getAbsolutePath(), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                for (int i = 0; i < photos.size(); i++) {
                    filesize = 0;
                    Photo photo = photos.get(i);
                    if(photo.url == null) {
                        photo.url = "";
                    }
                    if(filename.equals(photo.filename)) {
                        Log.e("DownloadManager", "Duplicated filename. Skipping...");
                    } else if (photo.url.length() == 0) {
                        filename = photo.filename;
                        Log.e("DownloadManager", "Invalid address. Skipping...");
                        try {
                            File downloadedFile = new File(String.format("%s/%s", ctx.getCacheDir(), where), filename);
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
                        filename = photo.filename;
                        String short_address = "";
                        if(photos.get(i).url.length() > 40) {
                            short_address = photos.get(i).url.substring(0, 39);
                        } else {
                            short_address = photos.get(i).url;
                        }
                        Log.v("DownloadManager", String.format("Downloading %s (%d/%d)...", short_address, i + 1, photos.size()));
                        url = photos.get(i).url;
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
                                content_length = response.getEntity().getContentLength();
                                File downloadedFile = new File(String.format("%s/%s", ctx.getCacheDir(), where), filename);
                                if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                    FileOutputStream fos = new FileOutputStream(downloadedFile);
                                    int inByte;
                                    while ((inByte = response_in.read()) != -1) {
                                        fos.write(inByte);
                                        filesize++;
                                    }
                                    fos.close();
                                } else {
                                    Log.w("DownloadManager", "Filesizes match, skipping...");
                                }
                                response_in.close();
                                response_code = statusLine.getStatusCode();
                            } else {
                                Response response = httpClient.newCall(request).execute();
                                response_code = response.code();
                                content_length = response.body().contentLength();
                                File downloadedFile = new File(String.format("%s/%s", ctx.getCacheDir(), where), filename);
                                if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                    FileOutputStream fos = new FileOutputStream(downloadedFile);
                                    int inByte;
                                    while ((inByte = response.body().byteStream().read()) != -1) {
                                        fos.write(inByte);
                                        filesize++;
                                    }
                                    fos.close();
                                } else {
                                    Log.w("DownloadManager", "Filesizes match, skipping...");
                                }
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
                if (where.equals("account_avatar")) {
                    sendMessage(HandlerMessages.ACCOUNT_AVATAR, where);
                } else if (where.equals("profile_avatars")) {
                    sendMessage(HandlerMessages.PROFILE_AVATARS, where);
                } else if (where.equals("newsfeed_avatars")) {
                    sendMessage(HandlerMessages.NEWSFEED_AVATARS, where);
                } else if (where.equals("group_avatars")) {
                    sendMessage(HandlerMessages.GROUP_AVATARS, where);
                } else if (where.equals("newsfeed_photo_attachments")) {
                    sendMessage(HandlerMessages.NEWSFEED_ATTACHMENTS, where);
                } else if (where.equals("wall_photo_attachments")) {
                    sendMessage(HandlerMessages.WALL_ATTACHMENTS, where);
                } else if (where.equals("wall_avatars")) {
                    sendMessage(HandlerMessages.WALL_AVATARS, where);
                } else if (where.equals("friend_avatars")) {
                    sendMessage(HandlerMessages.FRIEND_AVATARS, where);
                } else if (where.equals("comment_avatars")) {
                    sendMessage(HandlerMessages.COMMENT_AVATARS, where);
                }
            }
        };

        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void downloadOnePhotoToCache(final String url, final String filename, final String where) {
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
            StatusLine statusLine = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;

            @Override
            public void run() {
                try {
                    File directory = new File(ctx.getCacheDir().getAbsolutePath(), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                filesize = 0;
                if (url.length() == 0) {
                    Log.e("DownloadManager", "Invalid address. Skipping...");
                    try {
                        File downloadedFile = new File(String.format("%s/%s", ctx.getCacheDir(), where), filename);
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
                    if(url.length() > 40) {
                        short_address = url.substring(0, 39);
                    } else {
                        short_address = url;
                    }
                    Log.v("DownloadManager", String.format("Downloading %s...", short_address));
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
                            content_length = response.getEntity().getContentLength();
                            File downloadedFile = new File(ctx.getCacheDir(), String.format("%s/%s", ctx.getCacheDir(), where));
                            if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                int inByte;
                                while ((inByte = response_in.read()) != -1) {
                                    fos.write(inByte);
                                    filesize++;
                                }
                                fos.close();
                            } else {
                                Log.w("DownloadManager", "Filesizes match, skipping...");
                            }
                            response_in.close();
                            response_code = statusLine.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_code = response.code();
                            File downloadedFile = new File(String.format("%s/%s", ctx.getCacheDir(), where), filename);
                            if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                FileOutputStream fos = new FileOutputStream(downloadedFile);
                                int inByte;
                                while ((inByte = response.body().byteStream().read()) != -1) {
                                    fos.write(inByte);
                                    filesize++;
                                }
                                fos.close();
                            } else {
                                Log.w("DownloadManager", "Filesizes match, skipping...");
                            }
                            response.body().byteStream().close();
                        }
                        Log.v("DownloadManager", String.format("Downloaded from %s (%s): %d kB", short_address, response_code, (int) (filesize / 1024)));
                    } catch (IOException e) {
                        Log.e("DownloadManager", String.format("Download error: %s", e.getMessage()));
                    } catch (Exception e) {
                        Log.e("DownloadManager", String.format("Download error: %s", e.getMessage()));
                    }
                }
                if (where.equals("account_avatar")) {
                    sendMessage(HandlerMessages.ACCOUNT_AVATAR, where);
                } else if (where.equals("profile_avatars")) {
                    sendMessage(HandlerMessages.PROFILE_AVATARS, where);
                } else if (where.equals("newsfeed_avatars")) {
                    sendMessage(HandlerMessages.NEWSFEED_AVATARS, where);
                } else if (where.equals("newsfeed_photo_attachments")) {
                    sendMessage(HandlerMessages.NEWSFEED_ATTACHMENTS, where);
                } else if (where.equals("group_avatars")) {
                    sendMessage(HandlerMessages.GROUP_AVATARS, where);
                } else if (where.equals("wall_photo_attachments")) {
                    sendMessage(HandlerMessages.WALL_ATTACHMENTS, where);
                } else if (where.equals("wall_avatars")) {
                    sendMessage(HandlerMessages.WALL_AVATARS, where);
                } else if (where.equals("friend_avatars")) {
                    sendMessage(HandlerMessages.FRIEND_AVATARS, where);
                } else if (where.equals("comment_avatars")) {
                    sendMessage(HandlerMessages.COMMENT_AVATARS, where);
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
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
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
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("CommentsIntentActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
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

    public long getCacheSize() {
        final long[] size = {0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long foldersize = 0;
                File[] filelist = new File(ctx.getCacheDir().getAbsolutePath()).listFiles();
                for (int i = 0; i < filelist.length; i++) {
                    if (filelist[i].isDirectory()) {
                        File[] filelist2 = new File(filelist[i].getAbsolutePath()).listFiles();
                        for(int file_index = 0; file_index < filelist2.length; file_index++) {
                            foldersize += filelist2.length;
                        }
                    } else {
                        foldersize += filelist[i].length();
                    }
                }
                size[0] = foldersize;
            }
        };
        new Thread(runnable).run();
        return size[0];
    }
}
