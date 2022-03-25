package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLProtocolException;

import static java.lang.Thread.sleep;

public class AuthenticationActivity extends Activity {
    public String server;
    public String email;
    public String password;
    public InputStream input;
    public String state;
    public UpdateUITask updateUITask;
    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public JSONObject json_login;
    public String connectionErrorString;
    public HttpURLConnection httpConnection;
    public HttpsURLConnection httpsConnection;
    public Thread socketThread;
    public Thread sslSocketThread;
    public SharedPreferences sharedPreferences;
    public SharedPreferences global_sharedPreferences;
    public boolean two_factor_required;
    public int two_factor_code_integer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateUITask = new UpdateUITask();
        response_sb = new StringBuilder();
        json_login = new JSONObject();
        sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        socketThread = new Thread(new socketThread());
        sslSocketThread = new Thread(new sslSocketThread());

        if(!sharedPreferences.contains("auth_token") || !sharedPreferences.contains("server") || sharedPreferences.getString("auth_token", "").length() == 0 || sharedPreferences.getString("server", "").length() == 0) {
            setContentView(R.layout.auth);
            initKeyboardListener();
            Button auth_btn = findViewById(R.id.auth_btn);
            auth_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AutoCompleteTextView instance_edit = findViewById(R.id.instance_name);
                    server = instance_edit.getText().toString();
                    EditText email_edit = findViewById(R.id.auth_login);
                    email = email_edit.getText().toString();
                    EditText password_edit = findViewById(R.id.auth_pass);
                    password = password_edit.getText().toString();
                    Log.d("OpenVK Legacy", "Signing in...");
                    connectionDialog = new ProgressDialog(AuthenticationActivity.this);
                    connectionDialog.setMessage(getString(R.string.loading));
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    if(socketThread.getState() == Thread.State.RUNNABLE) {
                        socketThread.start();
                    } else {
                        socketThread = new Thread(new socketThread());
                        sslSocketThread = new Thread(new sslSocketThread());
                        socketThread.start();
                    }
                }
            });
            Button settings_btn = findViewById(R.id.settings_btn);
            settings_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, MainSettingsActivity.class);
                    startActivity(intent);
                }
            });
            final AutoCompleteTextView instance_edit = findViewById(R.id.instance_name);
            String[] instances = getResources().getStringArray(R.array.instances_list);
            List<String> instancesList = Arrays.asList(instances);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, instancesList);
            instance_edit.setAdapter(adapter);
        } else {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, AppActivity.class);
            intent.putExtra("auth_token", sharedPreferences.getString("auth_token", ""));
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if(connectionDialog != null) {
            connectionDialog.cancel();
        }
        super.onDestroy();
    }

    private void initKeyboardListener() {
        final int MIN_KEYBOARD_HEIGHT_PX = 150;
        final View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    ImageView auth_logo = findViewById(R.id.auth_logo);
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        auth_logo.setVisibility(View.GONE);
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        auth_logo.setVisibility(View.VISIBLE);
                    }
                }
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });
    }

    class socketThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d("OpenVK Legacy", "Connecting to " + server + "...");
                String url_addr = new String();
                if(!server.startsWith("http://")) {
                    url_addr = "http://" + server + "/token?username=" + email + "&password=" + password + "&grant_type=password";
                } else {
                    server = server.replace("http://", "");
                    url_addr = "http://" + server + "/token?username=" + URLEncoder.encode(email, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&grant_type=" + URLEncoder.encode("password", "UTF-8");
                }
                URL url = new URL(url_addr);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Host", server);
                httpConnection.setRequestProperty("Accept","application/json");
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setConnectTimeout(30000);
                httpConnection.setReadTimeout(30000);
                httpConnection.connect();
                BufferedReader in;
                int status = httpConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                String response = new String();
                Log.d("OpenVK","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        if (response.length() > 0) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                            response_sb.append(response).append("\n");
                        }
                    }
                    json_login = new JSONObject(response_sb.toString());
                    connectionDialog.cancel();
                    in.close();
                    response_sb = new StringBuilder();
                    state = "getting_response";
                    updateUITask.run();
                } else if(status == 301) {
                    if(global_sharedPreferences.getBoolean("useHTTPS", true) == true) {
                        Log.d("OpenVK", "Creating SSL connection...");
                        state = "creating_ssl_connection";
                    } else {
                        state = "no_connection";
                    }
                    updateUITask.run();
                } else {
                    in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
                    while ((response = in.readLine()) != null) {
                        response_sb.append(response).append("\n");
                    }
                    Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                    json_login = new JSONObject(response_sb.toString());
                    in.close();
                    response_sb = new StringBuilder();
                    state = "getting_response";
                    updateUITask.run();
                }
            } catch(JSONException ex) {
                ex.printStackTrace();
                connectionErrorString = getResources().getString(R.string.unable_to_parse_error);
                state = "no_connection";
                updateUITask.run();
            } catch(SocketTimeoutException ex) {
                ex.printStackTrace();
                connectionErrorString = "SocketTimeoutException";
                state = "timeout";
                updateUITask.run();
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
                if(!server.startsWith("http://") || !server.startsWith("https://")) {
                    url_addr = "https://" + server + "/token?username=" + email + "&password=" + password + "&grant_type=password";
                } else {
                    server = server.replace("https://", "").replace("http://", "");
                    url_addr = "https://" + server + "/token?username=" + URLEncoder.encode(email, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&grant_type=" + URLEncoder.encode("password", "UTF-8");
                }
                if(two_factor_required == true)
                {
                    url_addr = url_addr + "&code=" + two_factor_code_integer;
                }
                two_factor_required = false;
                URL url = new URL(url_addr);
                httpsConnection = (HttpsURLConnection) url.openConnection();
                httpsConnection.setRequestMethod("GET");
                httpsConnection.setRequestProperty("Host", server);
                httpsConnection.setRequestProperty("Accept","application/json");
                httpsConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpsConnection.setConnectTimeout(30000);
                httpsConnection.setReadTimeout(30000);
                httpsConnection.connect();
                BufferedReader in;
                int status = httpsConnection.getResponseCode();
                Log.d("OpenVK Legacy", "Connected!");
                String response = new String();
                Log.d("OpenVK","Response code: " + status);
                if(status == 200) {
                    in = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream(), "utf-8"));
                    while ((response = in.readLine()) != null) {
                        if (response.length() > 0) {
                            Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                            response_sb.append(response).append("\n");
                        }
                    }
                    json_login = new JSONObject(response_sb.toString());
                    connectionDialog.cancel();
                    in.close();
                    response_sb = new StringBuilder();
                    state = "getting_response";
                    updateUITask.run();
                } else if(status == 301) {

                } else {
                    in = new BufferedReader(new InputStreamReader(httpsConnection.getErrorStream()));
                    while ((response = in.readLine()) != null) {
                        response_sb.append(response).append("\n");
                    }
                    Log.d("OpenVK Legacy", "Getting response from " + server + ": [" + response_sb.toString() + "]");
                    json_login = new JSONObject(response_sb.toString());
                    in.close();
                    response_sb = new StringBuilder();
                    state = "getting_response";
                    updateUITask.run();
                }
            } catch(SSLProtocolException ex) {
                ex.printStackTrace();
                connectionErrorString = "SSLProtocolException";
                state = "no_connection";
                updateUITask.run();
            } catch(JSONException ex) {
                ex.printStackTrace();
                connectionErrorString = getResources().getString(R.string.unable_to_parse_error);
                state = "no_connection";
                updateUITask.run();
            } catch(Exception ex) {
                ex.printStackTrace();
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
                        httpConnection.disconnect();
                        try {
                            if(json_login.has("error_code")) {
                                if (json_login.getInt("error_code") == 28 && json_login.getString("error_msg").equals("Invalid username or password")) {
                                    connectionDialog.cancel();
                                    AlertDialog wrong_userdata_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                                    builder.setTitle(R.string.auth_error_title);
                                    builder.setMessage(R.string.auth_error);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    wrong_userdata_dlg = builder.create();
                                    if(!AuthenticationActivity.this.isFinishing()) wrong_userdata_dlg.show();
                                } if (json_login.getInt("error_code") == 28 && json_login.getString("error_msg").equals("Invalid 2FA code")) {
                                    connectionDialog.cancel();
                                    AlertDialog twofactor_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                                    View twofactor_view = getLayoutInflater().inflate(R.layout.twofactor_auth, null, false);
                                    builder.setTitle(R.string.auth);
                                    builder.setView(twofactor_view);
                                    final EditText two_factor_code = twofactor_view.findViewById(R.id.two_factor_code);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            twoFactorLogin(two_factor_code.getText().toString());
                                        }
                                    });
                                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            two_factor_required = false;
                                        }
                                    });
                                    twofactor_dlg = builder.create();
                                    if(!AuthenticationActivity.this.isFinishing()) twofactor_dlg.show();
                                } else if(json_login.getInt("error_code") == 3) {
                                    connectionDialog.cancel();
                                    AlertDialog wrong_userdata_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                                    builder.setTitle(R.string.auth_error_title);
                                    builder.setMessage(R.string.auth_error);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    wrong_userdata_dlg = builder.create();
                                    if(!AuthenticationActivity.this.isFinishing()) wrong_userdata_dlg.show();
                                }
                            } else if(json_login.has("access_token")) {
                                connectionDialog.cancel();
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
                                SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
                                sharedPrefsEditor.putString("auth_token", json_login.getString("access_token"));
                                sharedPrefsEditor.putString("server", server);
                                sharedPrefsEditor.commit();
                                Context context = getApplicationContext();
                                Intent intent = new Intent(context, AppActivity.class);
                                intent.putExtra("auth_token", sharedPreferences.getString("auth_token", ""));
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if(state == "connection_lost") {
                        AlertDialog error_dlg;
                        AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                        builder.setTitle(R.string.auth_error_title);
                        builder.setMessage(getString(R.string.err_text) + " (" + getString(R.string.error) + ": " + connectionErrorString + ")");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        error_dlg = builder.create();
                        error_dlg.show();
                        connectionDialog.cancel();
                    } else if(state == "timeout") {
                        AlertDialog error_dlg;
                        AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                        builder.setTitle(R.string.auth_error_title);
                        builder.setMessage(getString(R.string.err_text) + " (" + getString(R.string.error) + ": " + connectionErrorString + ")");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        error_dlg = builder.create();
                        if(!AuthenticationActivity.this.isFinishing()) error_dlg.show();
                        connectionDialog.cancel();
                    } else if(state == "no_connection") {
                        connectionDialog.cancel();
                        AlertDialog error_dlg;
                        AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
                        builder.setTitle(R.string.auth_error_title);
                        builder.setMessage(getString(R.string.err_text) + " (" + getString(R.string.error) + ": " + connectionErrorString + ")");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        error_dlg = builder.create();
                        if(!AuthenticationActivity.this.isFinishing()) error_dlg.show();
                    } else if(state == "creating_ssl_connection") {
                        sslSocketThread.start();
                    }
                }
            });
        }
    }

    private void twoFactorLogin(String two_factor_code) {
        two_factor_required = true;
        two_factor_code_integer = Integer.valueOf(two_factor_code);
        sslSocketThread = new Thread(new sslSocketThread());
        new Thread(new socketThread()).start();
    }
}
