package uk.openvk.android.legacy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.AuthenticationActivity;
import uk.openvk.android.legacy.activities.ConversationActivity;
import uk.openvk.android.legacy.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.activities.MainSettingsActivity;
import uk.openvk.android.legacy.activities.NewPostActivity;
import uk.openvk.android.legacy.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.activities.SearchActivity;

import static java.lang.Thread.sleep;

public class OvkAPIWrapper {

    public Context ctx;
    public String server;
    public String api_method;
    public String token;
    public String send_request;
    public boolean isConnected;
    public String state;
    public String arguments;
    public Boolean inputStream_isClosed;

    public TimerTask HTTPtoHTTPS;
    public Thread socketThread;
    public Thread sslSocketThread;
    public boolean isSecured;

    public StringBuilder response_sb;
    public String jsonResponseString;
    public JSONObject jsonResponse;
    public String connectionErrorString;
    public boolean allowHTTPS;
    public Handler handler;
    public String raw_addr;
    public String raw_server;
    public String file_name;
    public String raw_url;
    public LruCache memCache;

    public OvkAPIWrapper(Context context, String instance, String access_token, JSONObject json, boolean allowSecureConnection) {
        ctx = context;
        server = instance;
        inputStream_isClosed = true;
        response_sb = new StringBuilder();
        allowHTTPS = allowSecureConnection;
        HTTPtoHTTPS = new switchToHTTPS();
        token = access_token;
        handler = new Handler();
        connectionErrorString = "";
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        final int cacheSize = maxMemory / 8;

        memCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount();
                } else {
                    File file = new File(key);
                    if(file.exists()) {
                        return (int) file.length();
                    } else {
                        return 0;
                    }
                }
            }
        };
    }

    public void sendMethod(String method, String args) {
        api_method = method;
        if(token == null) {
            if(method.startsWith("Ovk.")) {
                send_request = "/method/" + method;
            } else {
                send_request = "/token?" + args + "&2fa_supported=1";
            }
        } else {
            send_request = "/method/" + method + "?access_token=" + token + "&" + args;
        }
        arguments = args;
        new Thread(new socketThread()).start();
    }

    public void downloadRaw(String server, String address, String filename, int id, String from) {
        raw_addr = address;
        raw_server = server;
        file_name = filename;
        raw_url = server + "/" + address;
        HttpRawDownloader httpRawDownloader = new HttpRawDownloader();
        httpRawDownloader.setParameters(id, from);
        httpRawDownloader.execute(raw_url, file_name);
    }

    public void isSecured() {
        send_request = "/";
        new Thread(new CheckingConnection()).start();
    }

    class socketThread implements Runnable {
        @Override
        public void run() {
            try {
                if(api_method.equals("Account.getProfileInfo")) {
                    if(arguments != null && arguments.length() > 0) {
                        Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + api_method + "\r\nArguments: [CONFIDENTIAL]" );
                    } else {
                        Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + api_method + "\r\nArguments: (without arguments)");
                    }
                } else if(api_method.length() > 0) {
                    if(arguments != null && arguments.length() > 0) {
                        Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + api_method + "\r\nArguments: " + arguments);
                    } else {
                        Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + api_method + "\r\nArguments: (without arguments)");
                    }
                } else {
                    Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nGetting token...");
                }
                String url_addr;
                url_addr = "http://" + server + send_request;
                URL url = new URL(url_addr);
                HttpURLConnection httpConnection;
                httpConnection = (HttpURLConnection) url.openConnection();
                isConnected = true;
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Host", server);
                httpConnection.setRequestProperty("Accept","application/json");
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setConnectTimeout(60000);
                httpConnection.setReadTimeout(60000);
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(true);
                httpConnection.connect();
                isConnected = true;
                BufferedReader in;
                int status = -1;
                inputStream_isClosed = false;
                status = httpConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                String response;
                Log.d("OpenVK Legacy","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        sleep(20);
                        if (response.length() > 0) {
                            if(response.contains("access_token")) {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response.substring(0, response.indexOf("access_token") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                            } else if(response.contains("password")) {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response.substring(0, response.indexOf("password") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                            } else {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response + "]");
                            }
                            response_sb.append(response).append("\n");
                        }
                    }
                    jsonResponseString = response_sb.toString();
                    jsonResponse = new JSONObject(response_sb.toString());
                    response_sb = new StringBuilder();
                    httpConnection.getInputStream().close();
                    inputStream_isClosed = true;
                    isConnected = false;
                    state = "getting_response";
                    sendMessageToParent();
                    Log.e("OpenVK Legacy", "InputStream closed");
                } else if(status == 301) {
                    if(allowHTTPS == true) {
                        Log.d("OpenVK Legacy", "Creating SSL connection...");
                        state = "creating_ssl_connection";
                        HTTPtoHTTPS.run();
                    } else {
                        connectionErrorString = "HTTPS required";
                        state = "no_connection";
                        sendMessageToParent();
                    }
                } else {
                    if (httpConnection.getErrorStream() != null) {
                        in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        jsonResponseString = response_sb.toString();
                        if(response_sb.toString().contains("\"access_token\"")) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString().substring(0, response_sb.toString().indexOf("\"access_token\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                        } else if(response_sb.toString().contains("\"password\"")) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString().substring(0, response_sb.toString().indexOf("\"password\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                        } else {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        }
                        jsonResponse = new JSONObject(response_sb.toString());
                        response_sb = new StringBuilder();
                        httpConnection.getErrorStream().close();
                        isConnected = false;
                        inputStream_isClosed = true;
                        state = "getting_response";
                        sendMessageToParent();
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                sendMessageToParent();
                if(ex.getMessage() != null)
                Log.e("OpenVK Legacy", ex.getMessage());
                else Log.e("OpenVK Legacy", connectionErrorString);
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                if(ex.getMessage() != null)
                    Log.e("OpenVK Legacy", ex.getMessage());
                else Log.e("OpenVK Legacy", connectionErrorString);
            } catch(SocketException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                if(ex.getMessage() != null)
                    Log.e("OpenVK Legacy", ex.getMessage());
                else Log.e("OpenVK Legacy", connectionErrorString);
            } catch(NullPointerException ex) {
                ex.printStackTrace();
            } catch(ProtocolException ex) {
                ex.printStackTrace();
            } catch(JSONException ex) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    connectionErrorString = ((AppActivity) ctx).getResources().getString(R.string.unable_to_parse_error);
                } else if(ctx.getClass().getSimpleName().equals("AuthenticationActivity")) {
                    connectionErrorString = ((AuthenticationActivity) ctx).getResources().getString(R.string.unable_to_parse_error);
                } else if(ctx.getClass().getSimpleName().equals("AppIntentActivity")) {
                    connectionErrorString = ((AuthenticationActivity) ctx).getResources().getString(R.string.unable_to_parse_error);
                }
                state = "no_connection";
                sendMessageToParent();
                if(ex.getMessage() != null)
                    Log.e("OpenVK Legacy", ex.getMessage());
                else Log.e("OpenVK Legacy", connectionErrorString);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class sslSocketThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + server + "... (Secured)");
                String url_addr;
                url_addr = "https://" + server + send_request;
                URL url = new URL(url_addr);
                HttpsURLConnection httpsConnection;
                httpsConnection = (HttpsURLConnection) url.openConnection();
                httpsConnection.setRequestMethod("GET");
                httpsConnection.setRequestProperty("Host", server);
                httpsConnection.setRequestProperty("Accept","application/json");
                httpsConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpsConnection.setConnectTimeout(60000);
                httpsConnection.setReadTimeout(60000);
                httpsConnection.setDoInput(true);
                httpsConnection.setDoOutput(true);
                httpsConnection.connect();
                isConnected = true;
                BufferedReader in;
                int status = -1;
                inputStream_isClosed = false;
                status = httpsConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                String response;
                Log.d("OpenVK Legacy","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        sleep(20);
                        if (response.length() > 0) {
                            if(response.contains("\"access_token\"")) {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response.substring(0, response.indexOf("\"access_token\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                            } else if(response.contains("\"password\"")) {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response.substring(0, response.indexOf("\"password\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                            } else {
                                Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response + "]");
                            }
                            response_sb.append(response).append("\n");
                        }
                    }
                    jsonResponseString = response_sb.toString();
                    jsonResponse = new JSONObject(response_sb.toString());
                    response_sb = new StringBuilder();
                    httpsConnection.getInputStream().close();
                    inputStream_isClosed = true;
                    isConnected = false;
                    state = "getting_response";
                    sendMessageToParent();
                    Log.e("OpenVK Legacy", "InputStream closed");
                } else if(status == 301) {

                } else {
                    if (httpsConnection.getErrorStream() != null) {
                        in = new BufferedReader(new InputStreamReader(httpsConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        jsonResponseString = response_sb.toString();
                        if(response_sb.toString().contains("\"access_token\"")) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString().substring(0, response_sb.toString().indexOf("\"access_token\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                        } else if(response_sb.toString().contains("\"password\"")) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString().substring(0, response_sb.toString().indexOf("\"password\"") + "\"access_token\"".length()) + "... | CONFIDENTIAL]");
                        } else {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        }
                        jsonResponse = new JSONObject(response_sb.toString());
                        response_sb = new StringBuilder();
                        httpsConnection.getErrorStream().close();
                        inputStream_isClosed = true;
                        isConnected = false;
                        state = "getting_response";
                        sendMessageToParent();
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                sendMessageToParent();
            } catch(UnknownHostException uhEx) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
            } catch(JSONException jEx) {
                connectionErrorString = "JSONException";
                state = "no_connection";
                sendMessageToParent();
            } catch(NullPointerException ex) {
                ex.printStackTrace();
                state = "no_connection";
                sendMessageToParent();
            } catch(SSLException ex) {
                ex.printStackTrace();
                connectionErrorString = "SSLException";
                state = "no_connection";
                sendMessageToParent();
            } catch (SocketException ex) {
                ex.printStackTrace();
                isConnected = true;
            } catch(IOException ex) {
                ex.printStackTrace();
                isConnected = true;
            } catch(Exception ex) {
                ex.printStackTrace();
                isConnected = true;
            }
        }
    }

    private void sendMessageToParent() {
        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            Message msg = handler.obtainMessage(AppActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            msg.setData(bundle);
            AppActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
            Message msg = handler.obtainMessage(FriendsIntentActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            msg.setData(bundle);
            FriendsIntentActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
            Message msg = handler.obtainMessage(ProfileIntentActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            msg.setData(bundle);
            ProfileIntentActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("AuthenticationActivity")) {
            Message msg = handler.obtainMessage(AuthenticationActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            bundle.putString("Error_message", connectionErrorString);
            msg.setData(bundle);
            AuthenticationActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("NewPostActivity")) {
            Message msg = handler.obtainMessage(NewPostActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            bundle.putString("Error_message", connectionErrorString);
            msg.setData(bundle);
            NewPostActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
            Message msg = handler.obtainMessage(MainSettingsActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            bundle.putString("Error_message", connectionErrorString);
            msg.setData(bundle);
            MainSettingsActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("SearchActivity")) {
            Message msg = handler.obtainMessage(SearchActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            bundle.putString("Error_message", connectionErrorString);
            msg.setData(bundle);
            SearchActivity.handler.sendMessage(msg);
        } else if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
            Message msg = handler.obtainMessage(SearchActivity.UPDATE_UI);
            Bundle bundle = new Bundle();
            bundle.putString("State", state);
            bundle.putString("API_method", "/method/" + api_method);
            bundle.putString("JSON_response", "" + jsonResponseString);
            bundle.putString("Error_message", connectionErrorString);
            msg.setData(bundle);
            ConversationActivity.handler.sendMessage(msg);
        }
    }

    private class HttpRawDownloader extends AsyncTask<String, String, String> {
        public int responseCode = 0;
        public String url_addr;
        public String downloaded_file_path;
        public long total = 0;
        public int elements_id = 0;
        public String from_control;
        public String contentType;
        public Bitmap bitmap;

        @Override
        protected String doInBackground(String... _url) {
            int count;
            Log.d("OpenVK Legacy", "Downloading " + _url[0] + "...");
            try {
                    url_addr = _url[0];
                    URL url = new URL("http://" + _url[0]);
                    HttpURLConnection httpRawConnection = (HttpURLConnection) url.openConnection();
                    httpRawConnection.setRequestMethod("GET");
                    httpRawConnection.setRequestProperty("Host", _url[0].split("/")[0]);
                    httpRawConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                    httpRawConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*");
                    httpRawConnection.setRequestProperty("Authorization", "Basic");
                    httpRawConnection.setRequestProperty("Connection", "keep-alive");
                    httpRawConnection.setConnectTimeout(60000);
                    httpRawConnection.setReadTimeout(60000);
                    httpRawConnection.connect();
                    responseCode = httpRawConnection.getResponseCode();
                    Log.d("OpenVK Legacy", "Response code: " + responseCode);
                    if (responseCode == 200) {
                        contentType = httpRawConnection.getContentType();
                        int contentLength = httpRawConnection.getContentLength();
                        InputStream input = new BufferedInputStream(url.openStream());
                        OutputStream output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]);
                        try {
                            File directory = new File(ctx.getCacheDir().getAbsolutePath(), "cache");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }
                            if (contentType.equals("image/jpeg")) {
                                downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".jpeg";
                                output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".jpeg");
                            } else if (contentType.equals("image/png")) {
                                downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png";
                                output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png");
                            } else if (contentType.equals("image/gif")) {
                                downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png";
                                output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png");
                            } else {
                                downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1];
                                output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        byte data[] = new byte[1024];

                        if(contentType.equals("image/jpeg") && !new File(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".jpeg").exists()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else if(contentType.equals("image/png") && !new File(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png").exists()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else if(!new File(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]).exists()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else {
                            if(contentType.equals("image/jpeg") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".jpeg").length()) {
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }
                                output.flush();
                            } else if(contentType.equals("image/png") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".png").length()) {
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }
                                output.flush();
                            } else if(contentType.equals("image/gif") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/cache/" + _url[1] + ".gif").length()) {
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }
                                output.flush();
                            } else if(contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]).length()) {
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    output.write(data, 0, count);
                                }
                                output.flush();
                            } else {
                                Log.w("OpenVK Legacy", "Downloaded \"" + _url[1] + "\" file already exists in cache.");
                            }
                        }
                        output.close();
                        input.close();
                    } else if(responseCode == 301) {
                        Log.d("OpenVK Legacy", "Creating SSL connection...");
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseCode == 301 && allowHTTPS == true) {
                HttpsRawDownloader httpsRawDownloader = new HttpsRawDownloader();
                httpsRawDownloader.setParameters(elements_id, from_control);
                httpsRawDownloader.execute(url_addr, file_name);
            } else if(responseCode == 200) {
                Log.d("OpenVK Legacy", "Downloaded " + total + " bytes!");
                if(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif")) {
                    state = "getting_picture";
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    final Runtime runtime = Runtime.getRuntime();
                    final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                    final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                    final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    try {
                        if(maxHeapSizeInMB <= 256) {
                            bitmap = (Bitmap) BitmapFactory.decodeFile(downloaded_file_path, options2);
                        } else {
                            bitmap = (Bitmap) BitmapFactory.decodeFile(downloaded_file_path, options);
                        }
                        if (bitmap != null) {
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            float scaleWidth = 0;
                            float scaleHeight = 0;
                            if (width > 1280 && height > 1280) {
                                if (height > width) {
                                    scaleWidth = ((float) 960) / width;
                                    scaleHeight = ((float) 1280) / height;
                                }

                                if (width > height) {
                                    scaleWidth = ((float) 1280) / width;
                                    scaleHeight = ((float) 960) / height;
                                }

                                Matrix matrix = new Matrix();
                                matrix.postScale(scaleWidth, scaleHeight);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
                            }
                            if (memCache.get(downloaded_file_path) == null) {
                                memCache.put(downloaded_file_path, bitmap);
                            }
                        }
                        sendRawMessageToParent(downloaded_file_path, elements_id, from_control, (Bitmap) memCache.get(downloaded_file_path));
                        total = 0;
                    } catch (OutOfMemoryError outOfMemoryError) {
                        outOfMemoryError.printStackTrace();
                        state = "raw_error";
                    } catch (Exception ex) {

                    }
                }
            }
        }

        public void setParameters(int id, String from) {
            elements_id = id;
            from_control = from;
        }
    }

    private class HttpsRawDownloader extends AsyncTask<String, String, String> {
        public int responseCode = 0;
        public String url_addr;
        public String downloaded_file_path;
        public long total = 0;
        public int elements_id = 0;
        public String from_control;
        public String contentType;
        public Bitmap bitmap;

        @Override
        protected String doInBackground(String... _url) {
            int count;
            Log.d("OpenVK Legacy", "Downloading " + _url[0] + "... (Secured)");
            try {
                url_addr = _url[0];
                URL url = new URL("https://" + _url[0]);
                HttpsURLConnection httpsRawConnection = (HttpsURLConnection) url.openConnection();
                httpsRawConnection.setRequestMethod("GET");
                httpsRawConnection.setRequestProperty("Host", _url[0].split("/")[0]);
                httpsRawConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                httpsRawConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*");
                httpsRawConnection.setRequestProperty("Authorization", "Basic");
                httpsRawConnection.setRequestProperty("Connection", "keep-alive");
                httpsRawConnection.setConnectTimeout(60000);
                httpsRawConnection.setReadTimeout(60000);
                httpsRawConnection.connect();
                responseCode = httpsRawConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Response code: " + responseCode);
                if (responseCode == 200) {
                    contentType = httpsRawConnection.getContentType();
                    int contentLength = httpsRawConnection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]);
                    try {
                        File directory = new File(ctx.getCacheDir().getAbsolutePath(), "photos");
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        if (contentType.equals("image/jpeg")) {
                            downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".jpeg";
                            output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".jpeg");
                        } else if (contentType.equals("image/png")) {
                            downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".png";
                            output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".png");
                        } else if (contentType.equals("image/gif")) {
                            downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".gif";
                            output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".gif");
                        }else {
                            downloaded_file_path = ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1];
                            output = new FileOutputStream(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    byte data[] = new byte[1024];

                    if(contentType.equals("image/jpeg") && !new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".jpeg").exists()) {
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                    } else if(contentType.equals("image/png") && !new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".png").exists()) {
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                    } else if(contentType.equals("image/gif") && !new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".gif").exists()) {
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                    } else if(!new File(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]).exists()) {
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                        }
                        output.flush();
                    } else {
                        if(contentType.equals("image/jpeg") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".jpeg").length()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else if(contentType.equals("image/png") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".png").length()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else if(contentType.equals("image/gif") && contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/photos/" + _url[1] + ".gif").length()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else if(contentLength != new File(ctx.getCacheDir().getAbsolutePath() + "/" + _url[1]).length()) {
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                        } else {
                            Log.w("OpenVK Legacy", "Downloaded \"" + _url[1] + "\" file already exists in cache.");
                        }
                    }
                    output.close();
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(responseCode == 200) {
                    Log.d("OpenVK Legacy", "Downloaded " + total + " bytes!");
                    if(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/gif")) {
                        state = "getting_picture";
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        final Runtime runtime = Runtime.getRuntime();
                        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                        final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
                        BitmapFactory.Options options2 = new BitmapFactory.Options();
                        final int REQUIRED_SIZE = 240;

                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        try {
                            if(maxHeapSizeInMB <= 256) {
                                bitmap = (Bitmap) BitmapFactory.decodeFile(downloaded_file_path, options2);
                            } else {
                                bitmap = (Bitmap) BitmapFactory.decodeFile(downloaded_file_path, options);
                            }
                            if (bitmap != null) {
                                int width = bitmap.getWidth();
                                int height = bitmap.getHeight();
                                float scaleWidth = 0;
                                float scaleHeight = 0;
                                if (width > 1280 && height > 1280) {
                                    if (height > width) {
                                        scaleWidth = ((float) 960) / width;
                                        scaleHeight = ((float) 1280) / height;
                                    }

                                    if (width > height) {
                                        scaleWidth = ((float) 1280) / width;
                                        scaleHeight = ((float) 960) / height;
                                    }

                                    Matrix matrix = new Matrix();
                                    matrix.postScale(scaleWidth, scaleHeight);
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
                                }
                                if (memCache.get(downloaded_file_path) == null) {
                                    memCache.put(downloaded_file_path, bitmap);
                                }
                            }
                            sendRawMessageToParent(downloaded_file_path, elements_id, from_control, (Bitmap) memCache.get(downloaded_file_path));
                            total = 0;
                        } catch (OutOfMemoryError outOfMemoryError) {
                            outOfMemoryError.printStackTrace();
                            state = "raw_error";
                        } catch (Exception ex) {

                        }
                    }
            }
        }

        public void setParameters(int id, String from) {
            elements_id = id;
            from_control = from;
        }
    }

    private void sendRawMessageToParent(String downloaded_file_path, int id, String from, Bitmap bitmap) {
        if(state.equals("getting_picture")) {
            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                Message msg = handler.obtainMessage(AppActivity.GET_PICTURE);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", raw_server);
                bundle.putParcelable("Picture", (Parcelable) bitmap);
                bundle.putInt("ID", id);
                bundle.putString("From", from);
                msg.setData(bundle);
                AppActivity.handler.sendMessage(msg);
            } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                Message msg = handler.obtainMessage(ProfileIntentActivity.GET_PICTURE);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", raw_server);
                bundle.putParcelable("Picture", (Parcelable) bitmap);
                bundle.putInt("ID", id);
                bundle.putString("From", from);
                msg.setData(bundle);
                ProfileIntentActivity.handler.sendMessage(msg);
            }
        }
    }

    class CheckingConnection implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + server + "...");
                String url_addr;
                url_addr = "http://" + server + "/";
                URL url = new URL(url_addr);
                HttpURLConnection httpRawConnection = (HttpURLConnection) url.openConnection();
                isConnected = true;
                httpRawConnection.setRequestMethod("GET");
                httpRawConnection.setRequestProperty("Host", server);
                httpRawConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                httpRawConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*");
                httpRawConnection.setRequestProperty("Authorization", "Basic");
                httpRawConnection.setRequestProperty("Connection", "keep-alive");
                httpRawConnection.setConnectTimeout(60000);
                httpRawConnection.setReadTimeout(60000);
                httpRawConnection.setDoInput(true);
                httpRawConnection.setDoOutput(true);
                httpRawConnection.connect();
                isConnected = true;
                int status = -1;
                inputStream_isClosed = false;
                status = httpRawConnection.getResponseCode();
                if(status == 301) {
                    isSecured = true;
                } else {
                    isSecured = false;
                }
                state = "checking_connection";
                sendTestMessageToParent();
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                sendTestMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendTestMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(SocketException ex) {
                connectionErrorString = "SocketException";
                state = "no_connection";
                sendTestMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(NullPointerException ex) {
                ex.printStackTrace();
            } catch(ProtocolException ex) {
                ex.printStackTrace();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private void sendTestMessageToParent() {
        if(state.equals("checking_connection")) {
            if (ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
                Message msg = handler.obtainMessage(MainSettingsActivity.GET_CONNECTION_TYPE);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", server);
                bundle.putBoolean("IsSecured", isSecured);
                msg.setData(bundle);
                MainSettingsActivity.handler.sendMessage(msg);
            }
        } else if(state.equals("no_connection")) {
            if (ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
                Message msg = handler.obtainMessage(MainSettingsActivity.GET_CONNECTION_TYPE);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", server);
                bundle.putBoolean("IsSecured", isSecured);
                msg.setData(bundle);
                MainSettingsActivity.handler.sendMessage(msg);
            }
        } else if(state.equals("timeout")) {
            if (ctx.getClass().getSimpleName().equals("MainSettingsActivity")) {
                Message msg = handler.obtainMessage(MainSettingsActivity.GET_CONNECTION_TYPE);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", server);
                bundle.putBoolean("IsSecured", isSecured);
                msg.setData(bundle);
                MainSettingsActivity.handler.sendMessage(msg);
            }
        }
    }

    class switchToHTTPS extends TimerTask {
        @Override
        public void run() {
            if(state.equals("creating_ssl_connection")) {
                socketThread = new Thread(new socketThread());
                sslSocketThread = new Thread(new sslSocketThread());
                sslSocketThread.start();
            }
        }
    }

    public boolean getConnectionState() {
        return isConnected;
    }
}
