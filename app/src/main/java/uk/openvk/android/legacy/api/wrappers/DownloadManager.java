package uk.openvk.android.legacy.api.wrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.pixmob.httpclient.HttpClient;
import org.pixmob.httpclient.HttpClientException;
import org.pixmob.httpclient.HttpRequestBuilder;
import org.pixmob.httpclient.HttpResponse;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.AuthActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;

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
public class DownloadManager {

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

    public DownloadManager(Context ctx, boolean use_https, boolean legacy_mode) {
        this.ctx = ctx;
        this.use_https = use_https;
        this.legacy_mode = legacy_mode;
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            logging_enabled = false;
        }
        try {
            if (legacy_mode || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                Log.v(OvkApplication.DL_TAG, "Starting DownloadManager in Legacy Mode...");
                httpClientLegacy = new HttpClient(ctx);
                httpClientLegacy.setConnectTimeout(30000);
                httpClientLegacy.setReadTimeout(30000);
                httpClientLegacy.setUserAgent(generateUserAgent(ctx));
                this.legacy_mode = true;
            } else {
                Log.v(OvkApplication.DL_TAG, "Starting DownloadManager...");
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
                        httpClientLegacy.setProxy(address_array[0], Integer.valueOf(address_array[1]));
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


    public void downloadPhotosToCache(final ArrayList<PhotoAttachment> photoAttachments, final String where) {
        if(photoAttachments == null) {
            Log.e(OvkApplication.DL_TAG, "Attachments array is empty. Download canceled.");
            return;
        }
        Log.v("DownloadManager", String.format("Downloading %d photos...", photoAttachments.size()));
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;
            private String url = "";
            private String filename = "";

            @Override
            public void run() {
                try {
                    File directory = new File(String.format("%s/%s/photos_cache",
                            ctx.getCacheDir().getAbsolutePath(), instance), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                for (int i = 0; i < photoAttachments.size(); i++) {
                    filesize = 0;
                    filename = photoAttachments.get(i).filename;
                    File downloadedFile = new File(String.format("%s/%s/photos_cache/%s",
                            ctx.getCacheDir().getAbsolutePath(), instance, where), filename);
                    PhotoAttachment photoAttachment = photoAttachments.get(i);
                    if(photoAttachment.url == null) {
                        photoAttachment.url = "";
                    }
                    Date lastModDate;
                    if(downloadedFile.exists()) {
                        lastModDate = new Date(downloadedFile.lastModified());
                    } else {
                        lastModDate = new Date(0);
                    }
                    long time_diff = System.currentTimeMillis() - lastModDate.getTime();
                    TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                    // photo autocaching
                    if(forceCaching && downloadedFile.exists() && downloadedFile.length() >= 5120 &&
                            timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) >= 360000L &&
                            timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) < 259200000L) {
                        if(logging_enabled) Log.e(OvkApplication.DL_TAG, "Duplicated filename. Skipping..." +
                                "\r\nTimeDiff: " + timeUnit.convert(time_diff,TimeUnit.MILLISECONDS)
                                + " ms | Filesize: " + downloadedFile.length() + " bytes");
                    } else if (photoAttachment.url.length() == 0) {
                        filename = photoAttachment.filename;
                        if(logging_enabled) Log.e(OvkApplication.DL_TAG, "Invalid address. Skipping...");
                        try {
                            if(downloadedFile.exists() || !downloadedFile.isDirectory()) {
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
                        try {
                            filename = photoAttachment.filename;
                            String short_address = "";
                            if(photoAttachments.get(i).url.length() > 40) {
                                short_address = photoAttachments.get(i).url.substring(0, 39);
                            } else {
                                short_address = photoAttachments.get(i).url;
                            }
                            //Log.v("DownloadManager", String.format("Downloading %s (%d/%d)...",
                            // short_address, i + 1, photoAttachments.size()));
                            url = photoAttachments.get(i).url;
                            if(!url.startsWith("http://") && !url.startsWith("https://")) {
                                Log.e(OvkApplication.DL_TAG, "Invalid URL. Download canceled.");
                                return;
                            }

                            if (legacy_mode) {
                                request_legacy = httpClientLegacy.get(url);
                            } else {
                                request = new Request.Builder()
                                        .url(url)
                                        .addHeader("User-Agent", generateUserAgent(ctx))
                                        .build();
                            }

                            if (legacy_mode) {
                                HttpResponse response = request_legacy.execute();
                                assert response != null;
                                response_in = response.getPayload();
                                content_length = response.getContentLength();
                                if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                    FileOutputStream fos = new FileOutputStream(downloadedFile);
                                    int inByte;
                                    while ((inByte = response_in.read()) != -1) {
                                        fos.write(inByte);
                                        filesize++;
                                    }
                                    fos.close();
                                } else {
                                    if(logging_enabled) Log.w("DownloadManager", "Filesizes match, skipping...");
                                }
                                response_in.close();
                                response_code = response.getStatusCode();
                            } else {
                                Response response = httpClient.newCall(request).execute();
                                response_code = response.code();
                                content_length = response.body().contentLength();
                                downloadedFile = new File(String.format("%s/%s/photos_cache/%s",
                                        ctx.getCacheDir().getAbsolutePath(), instance, where), filename);
                                if(!downloadedFile.exists() || content_length != downloadedFile.length()) {
                                    FileOutputStream fos = new FileOutputStream(downloadedFile);
                                    int inByte;
                                    while ((inByte = response.body().byteStream().read()) != -1) {
                                        fos.write(inByte);
                                        filesize++;
                                    }
                                    fos.close();
                                } else {
                                    if(logging_enabled) Log.w("DownloadManager", "Filesizes match, skipping...");
                                }
                                response.body().byteStream().close();
                            }
                            if(logging_enabled) Log.d(OvkApplication.DL_TAG,
                                    String.format("Downloaded from %s (%s): %d kB (%d/%d)",
                                            short_address, response_code, (int) (filesize / 1024), i + 1,
                                            photoAttachments.size()));
                        } catch (IOException | HttpClientException ex) {
                            if(logging_enabled) Log.e(OvkApplication.DL_TAG,
                                    String.format("Download error: %s (%d/%d)", ex.getMessage(), i + 1,
                                            photoAttachments.size()));
                            if (ex.getMessage().startsWith("Authorization required")) {
                                response_code = 401;
                            } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                                String code_str = ex.getMessage().substring
                                        (ex.getMessage().length() - 3);
                                response_code = Integer.parseInt(code_str);
                            }
                        } catch (Exception e) {
                            photoAttachment.error = e.getClass().getSimpleName();
                            if(logging_enabled) Log.e(OvkApplication.DL_TAG,
                                    String.format("Download error: %s (%d/%d)", e.getMessage(), i + 1,
                                            photoAttachments.size()));
                        }
                    }
                    if(i % 10 == 0 || i == photoAttachments.size() - 1) {
                        switch (where) {
                            case "account_avatar":
                                sendMessage(HandlerMessages.ACCOUNT_AVATAR, where);
                                break;
                            case "profile_avatars":
                                sendMessage(HandlerMessages.PROFILE_AVATARS, where);
                                break;
                            case "newsfeed_avatars":
                                sendMessage(HandlerMessages.NEWSFEED_AVATARS, where);
                                break;
                            case "group_avatars":
                                sendMessage(HandlerMessages.GROUP_AVATARS, where);
                                break;
                            case "newsfeed_photo_attachments":
                                sendMessage(HandlerMessages.NEWSFEED_ATTACHMENTS, where);
                                break;
                            case "wall_photo_attachments":
                                sendMessage(HandlerMessages.WALL_ATTACHMENTS, where);
                                break;
                            case "wall_avatars":
                                sendMessage(HandlerMessages.WALL_AVATARS, where);
                                break;
                            case "friend_avatars":
                                sendMessage(HandlerMessages.FRIEND_AVATARS, where);
                                break;
                            case "comment_avatars":
                                sendMessage(HandlerMessages.COMMENT_AVATARS, where);
                                break;
                            case "comment_photos":
                                sendMessage(HandlerMessages.COMMENT_PHOTOS, where);
                                break;
                            case "conversations_avatars":
                                sendMessage(HandlerMessages.CONVERSATIONS_AVATARS, where);
                                break;
                        }
                    }
                }
                Log.v("DownloadManager", "Downloaded!");
            }
        };

        Thread thread = new Thread(httpRunnable);
        thread.start();
    }

    public void downloadOnePhotoToCache(final String url, final String filename, final String where) {
        if(url == null) {
            Log.e(OvkApplication.DL_TAG, "URL is empty. Download canceled.");
            return;
        }
        if(!url.startsWith("http://") && !url.startsWith("https://")) {
            Log.e(OvkApplication.DL_TAG, "Invalid URL. Download canceled.");
            return;
        }
        Runnable httpRunnable = new Runnable() {
            private Request request = null;
            private HttpRequestBuilder request_legacy = null;
            int response_code = 0;
            long filesize = 0;
            long content_length = 0;
            private InputStream response_in;

            @Override
            public void run() {
                Log.v("DownloadManager", String.format("Downloading %s...", url));
                try {
                    File directory = new File(String.format("%s/%s/photos_cache",
                            ctx.getCacheDir().getAbsolutePath(), instance), where);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                filesize = 0;
                File downloadedFile = new File(String.format("%s/%s/photos_cache/%s",
                        ctx.getCacheDir().getAbsolutePath(), instance, where), filename);
                Date lastModDate;
                if(downloadedFile.exists()) {
                    lastModDate = new Date(downloadedFile.lastModified());
                } else {
                    lastModDate = new Date(0);
                }
                long time_diff = System.currentTimeMillis() - lastModDate.getTime();
                TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                if(forceCaching && downloadedFile.exists() && downloadedFile.length() >= 5120 &&
                        timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) >= 360000L &&
                        timeUnit.convert(time_diff,TimeUnit.MILLISECONDS) < 259200000L) {
                    if(logging_enabled) Log.e(OvkApplication.DL_TAG, "Duplicated filename. Skipping..." +
                            "\r\nTimeDiff: " + timeUnit.convert(time_diff,TimeUnit.MILLISECONDS)
                            + " ms | Filesize: " + downloadedFile.length() + " bytes");
                } else if (url.length() == 0) {
                    if(logging_enabled) Log.e(OvkApplication.DL_TAG, "Invalid address. Skipping...");
                    try {
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

                    if(logging_enabled) Log.v("DownloadManager", String.format("Downloading %s...", short_address));
                    if (legacy_mode) {
                        request_legacy = httpClientLegacy.get(url);
                    } else {
                        request = new Request.Builder()
                                .url(url)
                                .addHeader("User-Agent", generateUserAgent(ctx))
                                .build();
                    }
                    try {
                        if (legacy_mode) {
                            HttpResponse response = request_legacy.execute();
                            assert response != null;
                            response_in = response.getPayload();
                            content_length = response.getContentLength();
                            FileOutputStream fos = new FileOutputStream(downloadedFile);
                            int inByte;
                            while ((inByte = response_in.read()) != -1) {
                                fos.write(inByte);
                                filesize++;
                            }
                            fos.close();
                            response_in.close();
                            response_code = response.getStatusCode();
                        } else {
                            Response response = httpClient.newCall(request).execute();
                            response_code = response.code();
                            FileOutputStream fos = new FileOutputStream(downloadedFile);
                            int inByte;
                            while ((inByte = response.body().byteStream().read()) != -1) {
                                fos.write(inByte);
                                filesize++;
                            }
                            fos.close();
                            response.body().byteStream().close();
                            if (response != null){
                                response.close();
                            }
                        }
                        if(response_code == 200) {
                            if (logging_enabled) Log.v("DownloadManager",
                                    String.format("Downloaded from %s (%s): %d kB", short_address,
                                            response_code, (int) (filesize / 1024)));
                        } else {
                            if(logging_enabled) Log.e(OvkApplication.DL_TAG,
                                    String.format("Download error: %s", response_code));
                        }
                    } catch (IOException | HttpClientException ex) {
                        if (ex.getMessage().startsWith("Authorization required")) {
                            response_code = 401;
                        } else if(ex.getMessage().startsWith("Expected status code 2xx")) {
                            String code_str = ex.getMessage().substring
                                    (ex.getMessage().length() - 3);
                            response_code = Integer.parseInt(code_str);
                        }
                    } catch (Exception e) {
                        if(logging_enabled) Log.e(OvkApplication.DL_TAG,
                                String.format("Download error: %s", e.getMessage()));
                    }
                }
                Log.v("DownloadManager", "Downloaded!");
                switch (where) {
                    case "account_avatar":
                        sendMessage(HandlerMessages.ACCOUNT_AVATAR, where);
                        break;
                    case "profile_avatars":
                        sendMessage(HandlerMessages.PROFILE_AVATARS, where);
                        break;
                    case "newsfeed_avatars":
                        sendMessage(HandlerMessages.NEWSFEED_AVATARS, where);
                        break;
                    case "newsfeed_photo_attachments":
                        sendMessage(HandlerMessages.NEWSFEED_ATTACHMENTS, where);
                        break;
                    case "group_avatars":
                        sendMessage(HandlerMessages.GROUP_AVATARS, where);
                        break;
                    case "wall_photo_attachments":
                        sendMessage(HandlerMessages.WALL_ATTACHMENTS, where);
                        break;
                    case "wall_avatars":
                        sendMessage(HandlerMessages.WALL_AVATARS, where);
                        break;
                    case "friend_avatars":
                        sendMessage(HandlerMessages.FRIEND_AVATARS, where);
                        break;
                    case "comment_avatars":
                        sendMessage(HandlerMessages.COMMENT_AVATARS, where);
                        break;
                    case "comment_photos":
                        sendMessage(HandlerMessages.COMMENT_PHOTOS, where);
                        break;
                    case "video_thumbnails":
                        sendMessage(HandlerMessages.VIDEO_THUMBNAILS, where);
                        break;
                    case "original_photos":
                        sendMessage(HandlerMessages.ORIGINAL_PHOTO, where);
                        break;
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
                            foldersize += aFilelist2.length();
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
