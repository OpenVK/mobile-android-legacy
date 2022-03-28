package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    public OvkAPIWrapper openVK_API;
    public static final int UPDATE_UI = 0;
    public static Handler handler;

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
        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case UPDATE_UI:
                        state = msg.getData().getString("State");
                        send_request = msg.getData().getString("API_method");
                        try {
                            json_response = new JSONObject(msg.getData().getString("JSON_response"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        connectionErrorString = msg.getData().getString("Error_message");
                        updateUITask.run();
                }
            }
        };
        openVK_API = new OvkAPIWrapper(NewPostActivity.this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));
        response_sb = new StringBuilder();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            final TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.new_status));
            final ImageButton back_btn = findViewById(R.id.backButton);
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
            ImageButton send_btn = findViewById(R.id.send_post_btn);
            send_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText statusEditText = findViewById(R.id.status_text_edit);
                    if(statusEditText.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
                    } else if(connection_status == false) {
                        try {
                            connectionDialog = new ProgressDialog(NewPostActivity.this);
                            connectionDialog.setMessage(getString(R.string.loading));
                            connectionDialog.setCancelable(false);
                            connectionDialog.show();
                            send_request = ("/method/Wall.post?access_token=" + URLEncoder.encode(auth_token, "UTF-8") + "&owner_id=" + owner_id + "&message=" + URLEncoder.encode(statusEditText.getText().toString(), "utf-8"));
                            openVK_API.sendMethod("Wall.post", "owner_id=" + owner_id + "&message=" + URLEncoder.encode(statusEditText.getText().toString(), "utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
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
            EditText statusEditText = findViewById(R.id.status_text_edit2);
            if(statusEditText.getText().toString().length() == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
            } else if(connection_status == false) {
                try {
                    connectionDialog = new ProgressDialog(this);
                    connectionDialog.setMessage(getString(R.string.loading));
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    send_request = ("/method/Wall.post?access_token=" + URLEncoder.encode(auth_token, "UTF-8") + "&owner_id=" + owner_id + "&message=" + URLEncoder.encode(statusEditText.getText().toString(), "utf-8"));
                    openVK_API.sendMethod("Wall.post", "owner_id=" + owner_id + "&message=" + URLEncoder.encode(statusEditText.getText().toString(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state == "getting_response") {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                        connectionDialog.cancel();
                        finish();
                    } else if(state == "no_connection") {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                        connectionDialog.cancel();
                    } else if(state == "timeout") {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                        connectionDialog.cancel();
                    }
                }
            });
        }
    }
}
