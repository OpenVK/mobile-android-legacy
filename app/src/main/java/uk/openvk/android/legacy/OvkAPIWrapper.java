package uk.openvk.android.legacy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Thread.sleep;

public class OvkAPIWrapper {

    public Context ctx;
    public String server;
    public String api_method;
    public String token;
    public String send_request;
    public boolean isConnected;
    public String state;
    public final static String CONNECTION_STATE = new String();
    public final static String API_METHOD = new String();
    public final static String JSON_RESPONSE = new String();
    public TimerTask updUI;
    public Boolean inputStream_isClosed;

    public TimerTask HTTPtoHTTPS;
    public TimerTask rawHTTPtoHTTPS;
    public Thread socketThread;
    public Thread sslSocketThread;
    public Thread socketRawThread;
    public Thread sslRawSocketThread;

    public HttpURLConnection httpConnection;
    public HttpsURLConnection httpsConnection;
    public HttpURLConnection httpRawConnection;
    public HttpsURLConnection httpsRawConnection;

    public StringBuilder response_sb;
    public String jsonResponseString;
    public JSONObject jsonResponse;
    public String connectionErrorString;
    public boolean allowHTTPS;
    public Handler handler;
    public String raw_addr;
    public String raw_server;
    public InputStreamReader raw_in;

    public OvkAPIWrapper(Context context, String instance, String access_token, JSONObject json, boolean allowSecureConnection) {
        ctx = context;
        server = instance;
        inputStream_isClosed = new Boolean(true);
        response_sb = new StringBuilder();
        allowHTTPS = allowSecureConnection;
        token = access_token;
        HTTPtoHTTPS = new switchToHTTPS();
        rawHTTPtoHTTPS = new switchRawToHTTPS();
        handler = new Handler();
        connectionErrorString = "";
    }

    public void sendMethod(String method, String args) {
        api_method = method;
        if(token == null) {
            send_request = "/token?" + args;
        } else {
            send_request = "/method/" + method + "?access_token=" + token + "&" + args;
        }
        new Thread(new socketThread()).start();
    }

    public void downloadRaw(String server, String address) {
        raw_addr = address;
        raw_server = server;
        new Thread(new socketRawThread()).start();
    }

    class socketThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + api_method);
                String url_addr = new String();
                url_addr = "http://" + server + send_request;
                URL url = new URL(url_addr);
                httpConnection = (HttpURLConnection) url.openConnection();
                isConnected = true;
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Host", server);
                httpConnection.setRequestProperty("Accept","application/json");
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setConnectTimeout(240000);
                httpConnection.setReadTimeout(240000);
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(true);
                httpConnection.connect();
                isConnected = true;
                BufferedReader in;
                int status = -1;
                inputStream_isClosed = false;
                status = httpConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                String response = new String();
                Log.d("OpenVK","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        sleep(20);
                        if (response.length() > 0) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
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
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
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
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(SocketException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
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
                Log.e("OpenVK Legacy", ex.getMessage());
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
                String url_addr = new String();
                url_addr = "https://" + server + send_request;
                URL url = new URL(url_addr);
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
                String response = new String();
                Log.d("OpenVK","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        sleep(20);
                        if (response.length() > 0) {
                            response_sb.append(response).append("\n");
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
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
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
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
        }
    }

    class socketRawThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + raw_server + "...\r\nRAW address: " + raw_addr);
                String url_addr = new String();
                url_addr = "http://" + raw_server + "/" + raw_addr;
                URL url = new URL(url_addr);
                HttpURLConnection httpRawConnection = (HttpURLConnection) url.openConnection();
                isConnected = true;
                httpRawConnection.setRequestMethod("GET");
                httpRawConnection.setRequestProperty("Host", raw_server);
                httpRawConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                httpRawConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*");
                httpRawConnection.setRequestProperty("Authorization", "Basic");
                httpRawConnection.setRequestProperty("Connection", "keep-alive");
                httpRawConnection.setConnectTimeout(240000);
                httpRawConnection.setReadTimeout(240000);
                httpRawConnection.setDoInput(true);
                httpRawConnection.setDoOutput(true);
                httpRawConnection.connect();
                isConnected = true;
                int status = -1;
                inputStream_isClosed = false;
                status = httpRawConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                int bytes = 0;
                int bytes_count = 0;
                Log.d("OpenVK","Response code: " + status);
                Log.d("OpenVK Legacy", "Response content: " + httpRawConnection.getHeaderFields().toString());
                if(status == 200) {
                    raw_in = new InputStreamReader(httpRawConnection.getInputStream(), "utf-8");
                    while ((bytes = raw_in.read()) != -1) {
                        bytes_count++;
                        if(bytes_count == 1) {
                            Log.d("OpenVK Legacy", "Downloading...");
                        }
                    }
                    Log.d("OpenVK Legacy", "Downloaded " + bytes_count + " bytes!");
                    response_sb = new StringBuilder();
                    httpRawConnection.getInputStream().close();
                    inputStream_isClosed = true;
                    isConnected = false;
                    if(httpRawConnection.getContentType().equals("image/jpeg") || httpRawConnection.getContentType().equals("image/png")) {
                        state = "getting_picture";
                    } else {
                        state = "getting_raw";
                    }
                    sendMessageToParent();
                    Log.e("OpenVK Legacy", "InputStream closed");
                } else if(status == 301) {
                    if(allowHTTPS == true) {
                        Log.d("OpenVK Legacy", "Creating SSL connection...");
                        state = "creating_ssl_connection";
                        rawHTTPtoHTTPS.run();
                    } else {
                        connectionErrorString = "HTTPS required";
                        state = "no_connection";
                        sendMessageToParent();
                    }
                } else {
                    if (httpRawConnection.getErrorStream() != null) {
                        String response;
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpRawConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        jsonResponseString = response_sb.toString();
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        response_sb = new StringBuilder();
                        httpRawConnection.getErrorStream().close();
                        isConnected = false;
                        inputStream_isClosed = true;
                        state = "getting_response";
                        sendRawMessageToParent(httpRawConnection.getErrorStream());
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(SocketException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
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

    class sslRawSocketThread implements Runnable {
        @Override
        public void run() {
            try {
                sleep(1000);
                Log.d("OpenVK Legacy", "Connecting to " + raw_server + "... (Secured)\r\nRAW address: " + raw_addr);
                String url_addr = new String();
                url_addr = "https://" + raw_server + "/" + raw_addr;
                URL url = new URL(url_addr);
                HttpURLConnection httpsRawConnection = (HttpsURLConnection) url.openConnection();
                isConnected = true;
                httpsRawConnection.setRequestMethod("GET");
                httpsRawConnection.setRequestProperty("Host", raw_server);
                httpsRawConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                httpsRawConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*");
                httpsRawConnection.setRequestProperty("Authorization", "Basic");
                httpsRawConnection.setRequestProperty("Connection", "keep-alive");
                httpsRawConnection.setConnectTimeout(240000);
                httpsRawConnection.setReadTimeout(240000);
                httpsRawConnection.setDoInput(true);
                httpsRawConnection.setDoOutput(true);
                httpsRawConnection.connect();
                isConnected = true;
                int status = -1;
                inputStream_isClosed = false;
                status = httpsRawConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                int bytes = 0;
                int bytes_count = 0;
                Log.d("OpenVK","Response code: " + status);
                Log.d("OpenVK Legacy", "Response content: " + httpsRawConnection.getHeaderFields().toString());
                if(status == 200) {
                    raw_in = new InputStreamReader(httpsRawConnection.getInputStream(), "utf-8");
                    while ((bytes = raw_in.read()) != -1) {
                        sleep(20);
                        bytes_count++;
                        if(bytes_count == 1) {
                            Log.d("OpenVK Legacy", "Downloading...");
                        }
                    }
                    Log.d("OpenVK Legacy", "Downloaded! " + bytes_count + " bytes");
                    response_sb = new StringBuilder();
                    inputStream_isClosed = true;
                    isConnected = false;
                    if(httpsRawConnection.getContentType().equals("image/jpeg") || httpRawConnection.getContentType().equals("image/png")) {
                        state = "getting_picture";
                    } else {
                        state = "getting_raw";
                    }
                    sendRawMessageToParent(httpsRawConnection.getInputStream());
                    httpsRawConnection.getInputStream().close();
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
                    if (httpsRawConnection.getErrorStream() != null) {
                        String response;
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpsRawConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        jsonResponseString = response_sb.toString();
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        response_sb = new StringBuilder();
                        httpsRawConnection.getErrorStream().close();
                        isConnected = false;
                        inputStream_isClosed = true;
                        state = "getting_response";
                        sendRawMessageToParent(httpRawConnection.getErrorStream());
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(SocketException ex) {
                connectionErrorString = "UnknownHostException";
                state = "no_connection";
                sendMessageToParent();
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

    private void sendRawMessageToParent(InputStream in) {
        if(state.equals("getting_picture")) {
            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                Message msg = handler.obtainMessage(AppActivity.GET_PICTURE);
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                Bundle bundle = new Bundle();
                bundle.putString("State", state);
                bundle.putString("Server", raw_server);
                bundle.putString("RAW_address", raw_addr);
                bundle.putParcelable("Picture", (Parcelable) bitmap);
                msg.setData(bundle);
                AppActivity.handler.sendMessage(msg);
            }
        }
    }

    class switchToHTTPS extends TimerTask {
        @Override
        public void run() {
            if(state == "creating_ssl_connection") {
                socketThread = new Thread(new socketThread());
                sslSocketThread = new Thread(new sslSocketThread());
                sslSocketThread.start();
            }
        }
    }

    class switchRawToHTTPS extends TimerTask {
        @Override
        public void run() {
            if(state == "creating_ssl_connection") {
                socketRawThread = new Thread(new socketRawThread());
                sslRawSocketThread = new Thread(new sslRawSocketThread());
                sslRawSocketThread.start();
            }
        }
    }
}
