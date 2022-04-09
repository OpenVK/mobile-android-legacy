package uk.openvk.android.legacy;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.TimerTask;

public class MainSettingsActivity extends PreferenceActivity {
    public boolean isQuiting;
    public static Handler handler;
    public String state;
    public String send_request;
    public String server;
    public boolean isSecured;
    public JSONObject json_response;
    public static final int UPDATE_UI = 0;
    public static final int GET_CONNECTION_TYPE = 1;
    public AlertDialog about_instance_dlg;
    public OvkAPIWrapper openVK_API;
    public View about_instance_view;
    public UpdateUITask updateUITask;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        updateUITask = new UpdateUITask();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case UPDATE_UI:
                        state = msg.getData().getString("State");
                        send_request = msg.getData().getString("API_method");
                        if(state != "no_connection" && state != "timeout") {
                            try {
                                json_response = new JSONObject(msg.getData().getString("JSON_response"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                        break;
                    case GET_CONNECTION_TYPE:
                        state = msg.getData().getString("State");
                        server = msg.getData().getString("Server");
                        isSecured = msg.getData().getBoolean("IsSecured");
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                }
            }
        };
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        if(sharedPreferences.getString("auth_token", "").length() > 0) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_2);
        }
        setContentView(R.layout.custom_preferences_layout);
        final CheckBoxPreference https_connection = (CheckBoxPreference) findPreference("useHTTPS");
        https_connection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                return false;
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(R.string.menu_settings);
            final ImageButton back_btn = findViewById(R.id.backButton);
            final ImageButton ovk_btn = findViewById(R.id.ovkButton);
            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            ovk_btn.setOnClickListener(new View.OnClickListener() {
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

        Preference about_preference = findPreference("about");
        about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog about_dlg;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
                View about_view = getLayoutInflater().inflate(R.layout.about_application_layout, null, false);
                TextView about_text = about_view.findViewById(R.id.about_text);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    about_text.setText(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number) + "</font>"));
                } else {
                    about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number)));
                }
                isQuiting = true;
                about_text.setMovementMethod(LinkMovementMethod.getInstance());
                builder.setView(about_view);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isQuiting = false;
                    }
                });
                about_dlg = builder.create();
                about_dlg.show();
                return false;
            }
        });
        Preference logout_preference = findPreference("logOut");
        if(logout_preference != null) {
            logout_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog logout_dlg;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
                    builder.setMessage(R.string.log_out_warning);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                            editor.putString("auth_token", "");
                            editor.putString("server", "");
                            editor.commit();
                            Intent authActivity = new Intent(getApplicationContext(), this.getClass());
                            int pendingIntentId = 1;
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), pendingIntentId, authActivity, PendingIntent.FLAG_MUTABLE);
                            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                            System.exit(0);
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    logout_dlg = builder.create();
                    logout_dlg.show();
                    return false;
                }
            });
        }

        Preference debug_menu = findPreference("debug_menu");
        debug_menu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                startActivity(intent);
                return false;
            }
        });

        final Preference about_instance = findPreference("about_instance");
        if(about_instance != null) {
            about_instance.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(MainSettingsActivity.this);
                    about_instance_view = getLayoutInflater().inflate(R.layout.about_instance_layout, null, false);
                    TextView server_name = about_instance_view.findViewById(R.id.server_addr_label2);
                    ((TextView) about_instance_view.findViewById(R.id.connection_type_label2)).setText(getResources().getString(R.string.loading));
                    ((TextView) about_instance_view.findViewById(R.id.instance_version_label2)).setText(getResources().getString(R.string.loading));
                    ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.GONE);
                    server_name.setText(getSharedPreferences("instance", 0).getString("server", ""));
                    builder.setTitle(getResources().getString(R.string.about_instance));
                    isQuiting = true;
                    builder.setView(about_instance_view);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isQuiting = false;
                        }
                    });
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        setDialogStyle(about_instance_view);
                    }
                    about_instance_dlg = builder.create();
                    about_instance_dlg.show();
                    openVK_API = new OvkAPIWrapper(MainSettingsActivity.this, getSharedPreferences("instance", 0).getString("server", ""), null, json_response, true);
                    openVK_API.isSecured();
                    openVK_API.sendMethod("Ovk.version", null);
                    return false;
                }
            });
        }
    }

    private void setDialogStyle(View view) {
        try {
            ((TextView) view.findViewById(R.id.server_addr_label)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.server_addr_label2)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.connection_type_label)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.connection_type_label2)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.instance_version_label)).setTextColor(Color.WHITE);
            ((TextView) view.findViewById(R.id.instance_version_label2)).setTextColor(Color.WHITE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isFinishing() == false) {
                        if (state == "checking_connection") {
                            TextView connection_type_tv = about_instance_view.findViewById(R.id.connection_type_label2);
                            try {
                                if (isSecured == true) {
                                    connection_type_tv.setText(getResources().getString(R.string.secured_connection));
                                } else {
                                    connection_type_tv.setText(getResources().getString(R.string.default_connection));
                                }
                                ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.VISIBLE);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (state == "getting_response") {
                            TextView openvk_version_tv = about_instance_view.findViewById(R.id.instance_version_label2);
                            try {
                                openvk_version_tv.setText("OpenVK " + json_response.getString("response"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (state == "no_connection") {
                            TextView connection_type_tv = about_instance_view.findViewById(R.id.connection_type_label2);
                            try {
                                connection_type_tv.setText(getResources().getString(R.string.connection_error));
                                ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.GONE);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else if (state == "timeout") {
                            TextView connection_type_tv = about_instance_view.findViewById(R.id.connection_type_label2);
                            try {
                                connection_type_tv.setText(getResources().getString(R.string.connection_error));
                                ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.GONE);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
}
