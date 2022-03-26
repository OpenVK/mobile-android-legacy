package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Thread.sleep;

public class NewPostActivity extends Activity {
    public HttpURLConnection httpConnection;
    public HttpsURLConnection httpsConnection;
    public HttpURLConnection httpRawConnection;
    public HttpsURLConnection httpsRawConnection;
    public String server;
    public String server_2;
    public String raw_address;
    public String state;
    public String auth_token;
    private UpdateUITask updateUITask;
    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public JSONObject json_response;
    public JSONObject json_response_user;
    public JSONObject json_response_group;
    public JSONArray newsfeed;
    public JSONArray attachments;
    public String connectionErrorString;
    public boolean connection_status;
    public Thread socketThread;
    public Thread sslSocketThread;
    public String send_request;
    public Boolean inputStream_isClosed;
    public SharedPreferences global_sharedPreferences;
    public int owner_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_status);
        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        inputStream_isClosed = new Boolean(false);
        server = getApplicationContext().getSharedPreferences("instance", 0).getString("server", "");
        auth_token = getApplicationContext().getSharedPreferences("instance", 0).getString("auth_token", "");
        owner_id = getApplicationContext().getSharedPreferences("instance", 0).getInt("user_id", 0);
        updateUITask = new UpdateUITask();
        if(owner_id == 0) {
            finish();
        }
        response_sb = new StringBuilder();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            final TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.new_status));
            final ImageButton back_btn = findViewById(R.id.title_back_btn);
            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            titlebar_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newpost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if(item.getItemId() == R.id.sendpost) {
            if(connection_status == false) {
                socketThread = new Thread(new socketThread());
                sslSocketThread = new Thread(new sslSocketThread());
                try {
                    EditText statusEditText = findViewById(R.id.status_text_edit2);
                    send_request = ("/method/Wall.post?access_token=" + URLEncoder.encode(auth_token, "UTF-8") + "&owner_id=" + owner_id + "&message=" + statusEditText.getText());
                    socketThread.start();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    class socketThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + server + "...\r\nMethod: " + send_request.substring(7).split("\\?")[0]);
                String url_addr = new String();
                url_addr = "http://" + server + send_request;
                URL url = new URL(url_addr);
                httpConnection = (HttpURLConnection) url.openConnection();
                connection_status = true;
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Host", server);
                httpConnection.setRequestProperty("Accept","application/json");
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setConnectTimeout(240000);
                httpConnection.setReadTimeout(240000);
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(true);
                httpConnection.connect();
                connection_status = true;
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
                    json_response = new JSONObject(response_sb.toString());
                    response_sb = new StringBuilder();
                    httpConnection.getInputStream().close();
                    inputStream_isClosed = true;
                    connection_status = false;
                    state = "getting_response";
                    updateUITask.run();
                    Log.e("OpenVK Legacy", "InputStream closed");
                } else if(status == 301) {
                    if(global_sharedPreferences.getBoolean("useHTTPS", true) == true) {
                        Log.d("OpenVK", "Creating SSL connection...");
                        state = "creating_ssl_connection";
                    } else {
                        connectionErrorString = "HTTPS required";
                        state = "no_connection";
                    }
                    updateUITask.run();
                } else {
                    if (httpConnection.getErrorStream() != null) {
                        in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        json_response = new JSONObject(response_sb.toString());
                        response_sb = new StringBuilder();
                        httpConnection.getErrorStream().close();
                        connection_status = false;
                        inputStream_isClosed = true;
                        state = "something_went_wrong";
                        updateUITask.run();
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "something_went_wrong";
                updateUITask.run();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(UnknownHostException ex) {
                connectionErrorString = "UnknownHostException";
                state = "something_went_wrong";
                updateUITask.run();
                Log.e("OpenVK Legacy", ex.getMessage());
            } catch(SocketException ex) {
                connectionErrorString = "UnknownHostException";
                state = "something_went_wrong";
                updateUITask.run();
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
                connection_status = true;
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
                    json_response = new JSONObject(response_sb.toString());
                    response_sb = new StringBuilder();
                    httpsConnection.getInputStream().close();
                    inputStream_isClosed = true;
                    connection_status = false;
                    state = "getting_response";
                    updateUITask.run();
                    Log.e("OpenVK Legacy", "InputStream closed");
                } else if(status == 301) {

                } else {
                    if (httpsConnection.getErrorStream() != null) {
                        in = new BufferedReader(new InputStreamReader(httpsConnection.getErrorStream()));
                        while ((response = in.readLine()) != null) {
                            response_sb.append(response).append("\n");
                        }
                        Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                        json_response = new JSONObject(response_sb.toString());
                        response_sb = new StringBuilder();
                        httpsConnection.getErrorStream().close();
                        inputStream_isClosed = true;
                        connection_status = false;
                        state = "something_went_wrong";
                        updateUITask.run();
                    }
                }
            } catch(SocketTimeoutException ex) {
                connectionErrorString = "SocketTimeoutException";
                state = "something_went_wrong";
                updateUITask.run();
            } catch(UnknownHostException uhEx) {
                connectionErrorString = "UnknownHostException";
                state = "something_went_wrong";
                updateUITask.run();
            } catch(JSONException jEx) {
                connectionErrorString = "JSONException";
                state = "something_went_wrong";
                updateUITask.run();
            } catch(NullPointerException ex) {
                ex.printStackTrace();
                connection_status = true;
            } catch(Exception ex) {
                ex.printStackTrace();
                connection_status = true;
            }
        }
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state == "getting_response") {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                        finish();
                    } else if(state == "something_went_wrong") {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
