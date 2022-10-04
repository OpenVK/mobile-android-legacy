package uk.openvk.android.legacy.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Ovk;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;

public class MainSettingsActivity extends PreferenceActivity {
    private boolean isQuiting;
    private OvkApplication app;
    private Global global = new Global();
    public OvkAPIWrapper ovk_api;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private AlertDialog about_instance_dlg;
    public Handler handler;
    private View about_instance_view;
    private Ovk ovk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(instance_prefs.getString("access_token", "").length() > 0) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_2);
        }
        setContentView(R.layout.custom_preferences_layout);
        app = ((OvkApplication) getApplicationContext());
        setListeners();
        ovk_api = new OvkAPIWrapper(this, true);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk = new Ovk();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception ex) {
                Log.e("OpenVK", "Cannot display home button.");
            }
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibillity(true);
            actionBarImitation.setTitle(getResources().getString(R.string.menu_settings));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.OVK_CHECK_HTTP) {
                TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
                connection_type.setText(getResources().getString(R.string.default_connection));
            } else if (message == HandlerMessages.OVK_CHECK_HTTPS) {
                TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
                connection_type.setText(getResources().getString(R.string.secured_connection));
            } else if (message == HandlerMessages.OVK_VERSION) {
                ovk.parseVersion(data.getString("response"));
                TextView openvk_version_tv = (TextView) about_instance_view.findViewById(R.id.instance_version_label2);
                openvk_version_tv.setText(String.format("OpenVK %s", ovk.version));
                ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        Preference about_preference = findPreference("about");
        about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog about_dlg;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
                View about_view = getLayoutInflater().inflate(R.layout.about_application_layout, null, false);
                TextView about_text = (TextView) about_view.findViewById(R.id.about_text);
                if(getSharedPreferences("instance", 0).getString("server", "").equals("openvk.uk") || getSharedPreferences("instance", 0).getString("server", "").equals("openvk.co")
                        || getSharedPreferences("instance", 0).getString("server", "").equals("openvk.su")) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        about_text.setText(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, app.build_number) + "</font>"));
                    } else {
                        about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, app.build_number)));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text_lollipop, BuildConfig.VERSION_NAME, app.build_number)));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text_froyo, BuildConfig.VERSION_NAME, app.build_number)));
                    } else {
                        about_text.setText(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.about_text_froyo, BuildConfig.VERSION_NAME, app.build_number) + "</font>"));
                    }
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
                            editor.putString("access_token", "");
                            editor.putString("server", "");
                            editor.commit();
                            Intent authActivity = new Intent(getApplicationContext(), this.getClass());
                            int pendingIntentId = 1;
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), pendingIntentId, authActivity, PendingIntent.FLAG_NO_CREATE);
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

        Preference advanced_settings = findPreference("advanced");
        if(advanced_settings != null) {
            advanced_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getApplicationContext(), AdvancedSettingsActivity.class);
                    startActivity(intent);
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
                       openAboutInstanceDialog();
                       return false;
                   }
               });
        }
    }

    private void openAboutInstanceDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainSettingsActivity.this);
        about_instance_view = getLayoutInflater().inflate(R.layout.about_instance_layout, null, false);
        TextView server_name = (TextView) about_instance_view.findViewById(R.id.server_addr_label2);
        ((TextView) about_instance_view.findViewById(R.id.connection_type_label2)).setText(getResources().getString(R.string.loading));
        ((TextView) about_instance_view.findViewById(R.id.instance_version_label2)).setText(getResources().getString(R.string.loading));
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.GONE);
        server_name.setText(instance_prefs.getString("server", ""));
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
        ovk_api.checkHTTPS();
        ovk.getVersion(ovk_api);
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

}
