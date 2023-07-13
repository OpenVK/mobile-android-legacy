package uk.openvk.android.legacy.api.wrappers;

import android.annotation.SuppressLint;
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
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.TrackingRequestBody;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.AuthActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;

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

@SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody", "ConstantConditions"})
public class UploadManager {

    public String server;
    public boolean use_https;
    public boolean legacy_mode;
    private Context ctx;
    public ArrayList<PhotoAttachment> photoAttachments;
    private boolean logging_enabled = true; // default for beta releases

    private OkHttpClient httpClient = null;
    private HttpClient httpClientLegacy = null;
    private boolean forceCaching;
    private String instance;

    public UploadManager(Context ctx, boolean use_https, boolean legacy_mode) {
        this.ctx = ctx;
        this.use_https = use_https;
        this.legacy_mode = legacy_mode;
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        try {
            if (legacy_mode || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                Log.v(OvkApplication.DL_TAG, "Starting DownloadManager in Legacy Mode...");
                BasicHttpParams basicHttpParams = new BasicHttpParams();
                HttpProtocolParams.setUseExpectContinue(basicHttpParams, false);
                HttpProtocolParams.setUserAgent(basicHttpParams, generateUserAgent(ctx));
                HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);
                HttpConnectionParams.setConnectionTimeout(basicHttpParams, 30000);
                HttpConnectionParams.setSoTimeout(basicHttpParams, 30000);
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                if (use_https) {
                    schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                } else {
                    basicHttpParams.setParameter("http.protocol.handle-redirects",false);
                }
                httpClientLegacy = new DefaultHttpClient(new ThreadSafeClientConnManager(basicHttpParams,
                        schemeRegistry), basicHttpParams);
                this.legacy_mode = true;
            } else {
                Log.v(OvkApplication.UL_TAG, "Starting UploadManager...");
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
                    httpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory())
                            .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
                } catch (Exception e) {
                    httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout
                            (30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
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
        String user_agent = "";
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


    public void uploadFile(final String address, File file, final String where) {
        if (file == null) {
            Log.e(OvkApplication.UL_TAG, "File is empty. Upload canceled.");
            return;
        }
        Log.v(OvkApplication.UL_TAG, String.format("Uploading file to %s...", address));
        final File file_f = file;
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpGet request_legacy = null;
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
                    if (legacy_mode) {

                    } else {
                        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addPart(Headers.of("Content-Disposition",
                                        "form-data; name=\"image\"; filename=\"" + file_f.getName() + "\""),
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
                                        }))
                                .build();
                        Request request = new Request.Builder()
                                .url(address)
                                .post(requestBody)
                                .build();
                        if (logging_enabled) Log.d(OvkApplication.UL_TAG,
                                String.format("Uploading to %s... (%d kB)",
                                        address, file_f.length() / 1024));
                        Response response = httpClient.newCall(request).execute();
                        response_body = response.body().string();
                        response_code = response.code();
                    }
                    if (response_code == 200) {
                        Log.v(OvkApplication.UL_TAG, "Uploaded!");
                        if(logging_enabled) Log.d(OvkApplication.UL_TAG,
                                String.format("Getting response from %s (%s): [%s]",
                                        address, response_code, response_body));
                        sendMessage(HandlerMessages.UPLOADED_SUCCESSFULLY, response_body);
                    }
                } catch (Exception e) {
                    sendMessage(HandlerMessages.UPLOAD_ERROR, "");
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
        } else if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
            ((QuickSearchActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            ((ProfileIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            ((FriendsIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
            ((GroupIntentActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
            ((WallPostActivity) ctx).handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("PhotoViewerActivity")) {
            ((PhotoViewerActivity) ctx).handler.sendMessage(msg);
        }
    }


    public void updateLoadProgress(String filename, String url, long position, long length) {
        Message msg = new Message();
        msg.what = HandlerMessages.UPLOAD_PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
        bundle.putString("url", url);
        bundle.putLong("position", position);
        bundle.putLong("length", length);
        msg.setData(bundle);
        if(ctx instanceof NewPostActivity) {
            NewPostActivity newpost_a = ((NewPostActivity) ctx);
            newpost_a.handler.sendMessage(msg);
        }
    }

    public boolean clearCache(File dir) {
        if (dir == null) {
            dir = ctx.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (String aChildren : children) {
                    boolean success = clearCache(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else
                return dir != null && dir.isFile() && dir.delete();
        } else if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = clearCache(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    public long getCacheSize() {
        final long[] size = {0};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long foldersize = 0;
                File[] filelist = new File(ctx.getCacheDir().getAbsolutePath()).listFiles();
                for (File aFilelist : filelist) {
                    if (aFilelist.isDirectory()) {
                        File[] filelist2 = new File(aFilelist.getAbsolutePath()).listFiles();
                        for (File aFilelist2 : filelist2) {
                            foldersize += filelist2.length;
                        }
                    } else {
                        foldersize += aFilelist.length();
                    }
                }
                size[0] = foldersize;
            }
        };
        new Thread(runnable).run();
        return size[0];
    }
}
