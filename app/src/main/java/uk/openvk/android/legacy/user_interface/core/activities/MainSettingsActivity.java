package uk.openvk.android.legacy.user_interface.core.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Ovk;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.InstanceLink;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.OvkAlertDialog;
import uk.openvk.android.legacy.user_interface.view.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;

public class MainSettingsActivity extends PreferenceActivity {
    private boolean isQuiting;
    private OvkApplication app;
    private Global global = new Global();
    public OvkAPIWrapper ovk_api;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkAlertDialog about_instance_dlg;
    public Handler handler;
    private View about_instance_view;
    private Ovk ovk;
    private int danger_zone_multiple_tap;
    private String account_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(instance_prefs.getString("account_password_hash", "").length() > 0) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_2);
        }
        setContentView(R.layout.custom_preferences_layout);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                account_name = "";
            } else {
                account_name = extras.getString("account_name");
            }
        } else {
            account_name = (String) savedInstanceState.getSerializable("account_name");
        }
        app = ((OvkApplication) getApplicationContext());
        setListeners();
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk = new Ovk();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setTitle(getResources().getString(R.string.menu_settings));
            } catch (Exception ex) {
                Log.e("OpenVK", "Cannot display home button.");
            }
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibility(true);
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
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
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
                if(ovk.version.startsWith("OpenVK")) {
                    openvk_version_tv.setText(ovk.version);
                } else {
                    openvk_version_tv.setText(String.format("OpenVK %s", ovk.version));
                }
                ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.VISIBLE);
            } else if(message == HandlerMessages.OVK_ABOUTINSTANCE) {
                ovk.parseAboutInstance(data.getString("response"));
                TextView users_counter = (TextView) about_instance_view.findViewById(R.id.instance_users_count);
                users_counter.setText(getResources().getString(R.string.instance_users_count, ovk.instance_stats.users_count));
                TextView online_users_counter = (TextView) about_instance_view.findViewById(R.id.instance_online_users_count);
                online_users_counter.setText(getResources().getString(R.string.instance_online_users_count, ovk.instance_stats.online_users_count));
                TextView active_users_counter = (TextView) about_instance_view.findViewById(R.id.instance_active_users_count);
                active_users_counter.setText(getResources().getString(R.string.instance_active_users_count, ovk.instance_stats.active_users_count));
                TextView groups_counter = (TextView) about_instance_view.findViewById(R.id.instance_groups_count);
                groups_counter.setText(getResources().getString(R.string.instance_groups_count, ovk.instance_stats.groups_count));
                TextView wall_posts_counter = (TextView) about_instance_view.findViewById(R.id.instance_wall_posts_count);
                wall_posts_counter.setText(getResources().getString(R.string.instance_wall_posts_count, ovk.instance_stats.wall_posts_count));
                TextView admins_counter = (TextView) about_instance_view.findViewById(R.id.instance_admins_count);
                admins_counter.setText(getResources().getString(R.string.instance_admins_count, ovk.instance_admins.size()));
                ((LinearLayout) about_instance_view.findViewById(R.id.instance_statistics_ll)).setVisibility(View.VISIBLE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label2)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label3)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label4)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label5)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label6)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label7)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label8)).setVisibility(View.GONE);
                ((TextView) about_instance_view.findViewById(R.id.instance_links_label9)).setVisibility(View.GONE);
                for(int i = 0; i < ovk.instance_links.size(); i++) {
                    InstanceLink link = ovk.instance_links.get(i);
                    TextView textView = null;
                    if(i == 0) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label2));
                    } else if(i == 1) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label3));
                    } else if(i == 2) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label4));
                    } else if(i == 3) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label5));
                    } else if(i == 4) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label6));
                    } else if(i == 5) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label7));
                    } else if(i == 6) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label8));
                    } else if(i == 7) {
                        textView = ((TextView) about_instance_view.findViewById(R.id.instance_links_label9));
                    }
                    if(textView != null) {
                        textView.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>", link.url, link.name)));
                        textView.setVisibility(View.VISIBLE);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
                ((LinearLayout) about_instance_view.findViewById(R.id.instance_links_ll)).setVisibility(View.VISIBLE);
            } else {
                TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
                connection_type.setText(getResources().getString(R.string.connection_error));
            }
        } catch (Exception ex) {
            TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
            connection_type.setText(getResources().getString(R.string.connection_error));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setListeners() {
        PreferenceScreen screen = (PreferenceScreen) findPreference("main_settings");
        PreferenceGroup others = (PreferenceGroup) findPreference("cat_others");
        Preference about_preference = findPreference("about");
        about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openAboutActivity();
                return false;
            }
        });
        Preference logout_preference = findPreference("logOut");
        if(logout_preference != null) {
            logout_preference.setSummary(account_name);
            logout_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openLogoutConfirmationDialog();
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
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            others.removePreference(debug_menu);
        } else {
            danger_zone_multiple_tap = 0;
            global_prefs.getBoolean("debugDangerZone", false);
            debug_menu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (instance_prefs.getString("access_token", "").length() > 0) {
                        MainSettingsActivity.this.danger_zone_multiple_tap += 1;
                        if (MainSettingsActivity.this.danger_zone_multiple_tap == 1) {
                            Timer timer = new Timer();
                            timer.schedule(new HideDangerZone(), 8000, 8000);
                        }
                        if (MainSettingsActivity.this.danger_zone_multiple_tap < 5) {
                            Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                            startActivity(intent);
                        } else if (MainSettingsActivity.this.danger_zone_multiple_tap == 5) {
                            Toast.makeText(MainSettingsActivity.this, "злой армянин кушает", Toast.LENGTH_LONG).show();
                        } else if (MainSettingsActivity.this.danger_zone_multiple_tap == 10) {
                            global_prefs.edit().putBoolean("debugDangerZone", true).commit();
                            Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                            startActivity(intent);
                            MainSettingsActivity.this.danger_zone_multiple_tap = 0;
                        }
                    } else {
                        Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                        startActivity(intent);
                    }
                    return false;
                }
            });
        }

        Preference interface_language = findPreference("interfaceLanguage");
        interface_language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(MainSettingsActivity.this, R.string.sett_app_restart_required, Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = global_prefs.edit();
                editor.putString("interfaceLanguage", (String) newValue);
                editor.commit();
                return false;
            }
        });

        Preference network_settings = findPreference("network_settings");
        network_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getApplicationContext(), NetworkSettingsActivity.class);
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

    private void openAboutActivity() {
        Intent intent = new Intent(getApplicationContext(), AboutApplicationActivity.class);
        startActivity(intent);
    }

    private void openLogoutConfirmationDialog() {
        OvkAlertDialog logout_dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                editor.putString("access_token", "");
                editor.putString("server", "");
                editor.putString("account_password_hash", "");
                editor.commit();
                Intent activity = new Intent(getApplicationContext(), MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(activity);
                System.exit(0);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        logout_dlg = new OvkAlertDialog(this);
        logout_dlg.build(builder, "", getResources().getString(R.string.log_out_warning), null, "");
        logout_dlg.show();
    }

    private void openAboutInstanceDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainSettingsActivity.this);
        about_instance_view = getLayoutInflater().inflate(R.layout.about_instance_layout, null, false);
        TextView server_name = (TextView) about_instance_view.findViewById(R.id.server_addr_label2);
        ((TextView) about_instance_view.findViewById(R.id.connection_type_label2)).setText(getResources().getString(R.string.loading));
        ((TextView) about_instance_view.findViewById(R.id.instance_version_label2)).setText(getResources().getString(R.string.loading));
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.GONE);
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_statistics_ll)).setVisibility(View.GONE);
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_links_ll)).setVisibility(View.GONE);
        server_name.setText(instance_prefs.getString("server", ""));
        ((TextView) about_instance_view.findViewById(R.id.rules_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebAddress(String.format("http://%s/about", instance_prefs.getString("server", "")));
            }
        });
        ((TextView) about_instance_view.findViewById(R.id.privacy_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebAddress(String.format("http://%s/privacy", instance_prefs.getString("server", "")));
            }
        });
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
            setDialogStyle(about_instance_view, "about_instance");
        }
        about_instance_dlg = new OvkAlertDialog(this);
        about_instance_dlg.build(builder, getResources().getString(R.string.about_instance), "", about_instance_view);
        about_instance_dlg.show();
        ovk_api.checkHTTPS();
        ovk.getVersion(ovk_api);
        ovk.aboutInstance(ovk_api);
    }

    private void setDialogStyle(View view, String dialog_name) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        startActivity(i);
    }

    private class HideDangerZone extends TimerTask {
        @Override
        public void run() {
            MainSettingsActivity.this.danger_zone_multiple_tap = 0;
            MainSettingsActivity.this.global_prefs.edit().putBoolean("debugDangerZone", false).commit();
        }
    }
}
