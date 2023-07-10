package uk.openvk.android.legacy.ui.core.fragments.app;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Ovk;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.InstanceLink;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.AboutApplicationActivity;
import uk.openvk.android.legacy.ui.core.activities.AdvancedSettingsActivity;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.DebugMenuActivity;
import uk.openvk.android.legacy.ui.core.activities.MainActivity;
import uk.openvk.android.legacy.ui.core.activities.NetworkSettingsActivity;
import uk.openvk.android.legacy.ui.list.items.InstanceAccount;

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

public class MainSettingsFragment extends PreferenceFragmentCompatDividers {
    private boolean isQuiting;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkApplication app;
    private OvkAPIWrapper ovk_api;
    private int danger_zone_multiple_tap;
    private View about_instance_view;
    private OvkAlertDialog about_instance_dlg;
    public  int selectedPosition;
    private ArrayList<InstanceAccount> accountArray;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreatePreferencesFix(Bundle bundle, String s) {
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        instance_prefs = ((OvkApplication) getContext().getApplicationContext()).getAccountPreferences();
        Bundle data = getActivity().getIntent().getExtras();
        if(data != null && data.containsKey("start_from")
                && data.getString("start_from").equals("AuthActivity")) {
            addPreferencesFromResource(R.xml.preferences_2);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
        setListeners();
        accountArray = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
        view.setBackgroundColor(getResources().getColor(R.color.white));
    }

    private void setListeners() {
        PreferenceScreen screen = (PreferenceScreen) findPreference("main_settings");
        PreferenceGroup others = (PreferenceGroup) findPreference("cat_others");

        Preference language = findPreference("interfaceLanguage");
        String[] langauge_array = getResources().getStringArray(R.array.interface_languages);
        language.setSummary(global_prefs.getString("interfaceLanguage", ""));
        language.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showUiLanguageSelectionDialog();
                return false;
            }
        });

        Preference ui_theme = findPreference("uiTheme");
        String[] theme_array = getResources().getStringArray(R.array.ui_themes);
        String value = global_prefs.getString("uiTheme", "Blue");
        int valuePos = 0;
        switch (value) {
            default:
                break;
            case "Gray":
                valuePos = 1;
                break;
            case "Black":
                valuePos = 2;
                break;
        }
        ui_theme.setSummary(theme_array[valuePos]);
        ui_theme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showUiThemeSelectionDialog();
                return false;
            }
        });

        Preference notif_ringtone = findPreference("notifyRingtone");
        if (notif_ringtone != null) {
            Uri notif_uri = Uri.parse("content://settings/system/notification_sound");
            if(global_prefs.getString("notifyRingtone", "content://settings/system/notification_sound")
                    .equals("content://settings/system/notification_sound")) {
                notif_ringtone.setSummary("OpenVK");
            } else {
                notif_uri = Uri.parse(global_prefs.getString("notifyRingtone",
                        "content://settings/system/notification_sound"));
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), notif_uri);
                notif_ringtone.setSummary(ringtone.getTitle(getContext()));
            }
            final Uri notif_uri_f = notif_uri;
            notif_ringtone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                            getResources().getString(R.string.sett_ringtone));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notif_uri_f);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    getActivity().startActivityForResult(intent, 5);
                    return false;
                }
            });
        }

        Preference about_preference = findPreference("about");
        about_preference.setSummary(
                String.format("OpenVK Legacy %s (%s)", BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT)
        );
        about_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openAboutActivity();
                return false;
            }
        });

        Preference change_account = findPreference("changeAccount");
        if(change_account != null) {
            change_account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openChangeAccountDialog();
                    return false;
                }
            });
        }

        Preference logout_preference = findPreference("logOut");
        if (logout_preference != null) {
            if(getActivity() instanceof AppActivity) {
                AppActivity appActivity = ((AppActivity) getActivity());
                if(appActivity.ovk_api.account.first_name != null &&
                        appActivity.ovk_api.account.last_name != null) {
                    logout_preference.setSummary(
                            String.format("%s %s", appActivity.ovk_api.account.first_name,
                                    appActivity.ovk_api.account.last_name));
                }
            }
            logout_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openLogoutConfirmationDialog();
                    return false;
                }
            });
        }

        Preference network_settings = findPreference("network_settings");
        if (network_settings != null) {
            network_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext().getApplicationContext(), NetworkSettingsActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }

        Preference advanced_settings = findPreference("advanced");
        if (advanced_settings != null) {
            advanced_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext().getApplicationContext(), AdvancedSettingsActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }

        final Preference about_instance = findPreference("about_instance");
        if(about_instance != null) {
            about_instance.setSummary(
                    ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance()
            );
            about_instance.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openAboutInstanceDialog();
                    return false;
                }
            });
        }

        Preference debug_menu = findPreference("debug_menu");
        if (BuildConfig.BUILD_TYPE.equals("release")) {
            others.removePreference(debug_menu);
        } else {
            danger_zone_multiple_tap = 0;
            global_prefs.getBoolean("debugDangerZone", false);
            debug_menu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (instance_prefs.getString("access_token", "").length() > 0) {
                        danger_zone_multiple_tap += 1;
                        if (danger_zone_multiple_tap == 1) {
                            Timer timer = new Timer();
                            //timer.schedule(new MainSettingsActivity.HideDangerZone(), 8000, 8000);
                        }
                        if (danger_zone_multiple_tap < 5) {
                            Intent intent = new Intent(getContext(), DebugMenuActivity.class);
                            startActivity(intent);
                        } else if (danger_zone_multiple_tap == 5) {
                            Toast.makeText(getContext(), "злой армянин кушает", Toast.LENGTH_LONG).show();
                        } else if (danger_zone_multiple_tap == 10) {
                            global_prefs.edit().putBoolean("debugDangerZone", true).commit();
                            Intent intent = new Intent(getContext(), DebugMenuActivity.class);
                            startActivity(intent);
                            danger_zone_multiple_tap = 0;
                        }
                    } else {
                        Intent intent = new Intent(getContext(), DebugMenuActivity.class);
                        startActivity(intent);
                    }
                    return false;
                }
            });
        }
    }

    private void showUiLanguageSelectionDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("interfaceLanguage", "System");
        String[] array = getResources().getStringArray(R.array.interface_languages);
        selectedPosition = 0;
        switch (value) {
            default:
                break;
            case "English":
                valuePos = 1;
                break;
            case "Русский":
                valuePos = 2;
                break;
            case "Украïнська":
                valuePos = 3;
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(R.array.interface_languages, valuePos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPosition = which;
            }
        });
        OvkAlertDialog dialog = new OvkAlertDialog(getContext());
        dialog.build(builder, getResources().getString(R.string.interface_language), "", null, "listDlg");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = global_prefs.edit();
                        editor.putString("interfaceLanguage",
                                getResources().getStringArray(R.array.interface_languages)[selectedPosition]);
                        editor.commit();
                        Toast.makeText(getContext(), R.string.sett_app_restart_required,
                                Toast.LENGTH_LONG).show();
                    }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        });
        dialog.show();
    }



    private void showUiThemeSelectionDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("uiTheme", "Blue");
        String[] array = getResources().getStringArray(R.array.ui_themes);
        selectedPosition = 0;
        switch (value) {
            default:
                break;
            case "Gray":
                valuePos = 1;
                break;
            case "Black":
                valuePos = 2;
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(R.array.ui_themes, valuePos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPosition = which;
            }
        });
        OvkAlertDialog dialog = new OvkAlertDialog(getContext());
        dialog.build(builder, getResources().getString(R.string.interface_theme), "", null, "listDlg");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = global_prefs.edit();
                        if(selectedPosition == 0) {
                            editor.putString("uiTheme", "Blue");
                        } else if(selectedPosition == 1) {
                            editor.putString("uiTheme", "Gray");
                        } else {
                            editor.putString("uiTheme", "Black");
                        }
                        editor.commit();
                        Toast.makeText(getContext(), R.string.sett_app_restart_required,
                                Toast.LENGTH_LONG).show();
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void openLogoutConfirmationDialog() {
        OvkAlertDialog logout_dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor =
                        ((OvkApplication) getContext().getApplicationContext())
                                .getAccountPreferences().edit();
                editor.putString("access_token", "");
                editor.putString("server", "");
                editor.putString("account_name", "");
                editor.putLong("uid", 0);
                editor.putString("account_password_hash", "");
                editor.commit();
                if(getActivity() instanceof AppActivity) {
                    AccountManager am = AccountManager.get(getContext());
                    am.removeAccount(((AppActivity) getActivity()).androidAccount, null, null);
                }
                DownloadManager dlm = new DownloadManager(getActivity(), false,
                        global_prefs.getBoolean("legacyHttpClient", false));
                dlm.clearCache(getContext().getCacheDir());
                Intent activity = new Intent(getContext().getApplicationContext(), MainActivity.class);
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
        logout_dlg = new OvkAlertDialog(getContext());
        logout_dlg.build(builder, "", getResources().getString(R.string.log_out_warning), null, "");
        logout_dlg.show();
    }

    private void openAboutActivity() {
        Intent intent = new Intent(getContext().getApplicationContext(), AboutApplicationActivity.class);
        startActivity(intent);
    }

    private void openAboutInstanceDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getContext());
        about_instance_view = getLayoutInflater(null).inflate(R.layout.layout_about_instance, null, false);
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
                openWebAddress(String.format("http://%s/terms", instance_prefs.getString("server", "")));
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
        about_instance_dlg = new OvkAlertDialog(getContext());
        about_instance_dlg.build(builder, getResources().getString(R.string.about_instance), "", about_instance_view);
        about_instance_dlg.show();
        if(getActivity() instanceof AppActivity) {
            ((AppActivity) getActivity()).ovk_api.wrapper.checkHTTPS();
        }
    }

    private void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        startActivity(i);
    }

    public void setAboutInstanceData(Ovk ovk) {
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
    }


    public void setInstanceVersion(Ovk ovk) {
        TextView openvk_version_tv = (TextView) about_instance_view.findViewById(R.id.instance_version_label2);
        if(ovk.version.startsWith("OpenVK")) {
            openvk_version_tv.setText(ovk.version);
        } else {
            openvk_version_tv.setText(String.format("OpenVK %s", ovk.version));
        }
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll)).setVisibility(View.VISIBLE);
    }

    public void setConnectionType(int message, boolean isProxy) {
        if(message == HandlerMessages.OVK_CHECK_HTTP) {
            TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
            if(isProxy) {
                connection_type.setText(getResources().getString(R.string.proxy_connection));
            } else {
                connection_type.setText(getResources().getString(R.string.default_connection));
            }
        } else if(message == HandlerMessages.OVK_CHECK_HTTPS){
            TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
            if(isProxy) {
                connection_type.setText(getResources().getString(R.string.proxy_connection));
            } else {
                connection_type.setText(getResources().getString(R.string.secured_connection));
            }
        } else {
            TextView connection_type = (TextView) about_instance_view.findViewById(R.id.connection_type_label2);
            connection_type.setText(getResources().getString(R.string.connection_error));
        }
    }

    public void setAccount(Account account) {
        if(account.first_name != null && account.last_name != null) {
            Preference logout_preference = findPreference("logOut");
            try {
                logout_preference.setSummary(
                        String.format("%s %s", account.first_name, account.last_name));
            } catch (Exception ignore) {
            }
        }
    }

    public void setNotificationSound(String uri) {
        SharedPreferences.Editor editor = global_prefs.edit();
        editor.putString("notifyRingtone", uri);
        editor.commit();
    }

    public void openChangeAccountDialog() {
        int valuePos = 0;
        selectedPosition = 0;
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String package_name = getContext().getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = getContext().getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            getContext().getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "[Unknown account]");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if(server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        accountArray.add(account);
                    }
                }
            }
            account_names = new String[accountArray.size()];
            for(int i = 0; i < accountArray.size(); i++) {
                account_names[i] = accountArray.get(i).name;
                if (accountArray.get(i).instance.equals(current_instance)) {
                    valuePos = i;
                }
            }
            Log.d(OvkApplication.APP_TAG, String.format("Files: %s", account_names.length));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        builder.setSingleChoiceItems(account_names, valuePos,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedPosition = which;
                }
            }
        );
        OvkAlertDialog dialog = new OvkAlertDialog(getContext());
        dialog.build(builder, getResources().getString(R.string.sett_account), "", null, "listDlg");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = global_prefs.edit();
                        editor.putString("current_instance", accountArray.get(selectedPosition).instance);
                        editor.putLong("current_uid", accountArray.get(selectedPosition).id);
                        editor.commit();
                        Toast.makeText(getContext(), R.string.sett_app_restart_required,
                                Toast.LENGTH_LONG).show();
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }
}
