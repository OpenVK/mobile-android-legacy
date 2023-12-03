package uk.openvk.android.legacy.ui.core.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentPreferenceActivity;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/*  Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class DebugMenuActivity extends TranslucentPreferenceActivity {

    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_custom_preferences);
        addPreferencesFromResource(R.xml.preferences_debug);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setTitle(R.string.debug_menu);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.AbstractAction(0) {
                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
        }
        setListeners();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setListeners() {
        PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general_category");

        Preference logToFile = (Preference) findPreference("logToFile");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            logToFile.setEnabled(false);
            logToFile.setSummary(getResources().getString(R.string.debug_incompatibillity_error));
        }
        logToFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                writeLogToFile();
                return false;
            }
        });
        Preference bugReport = (Preference) findPreference("bugReport");
        bugReport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                writeLogToFile();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getResources().getString(R.string.app_issues_link)));
                startActivity(intent);
                return false;
            }
        });

        Preference causeAppToCrash = findPreference("causeAppToCrash");
        causeAppToCrash.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                throw new UnsupportedOperationException("App crash test");
            }
        });

        Preference terminate_process = findPreference("terminate");
        terminate_process.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return false;
            }
        });

        Preference legacy_http_client = findPreference("legacyHttpClient");

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            legacy_http_client.setEnabled(false);
            legacy_http_client.setSummary(
                    getResources().getString(R.string.use_legacy_http_client_okhttp_err));
        }

        PreferenceCategory dangerZoneCategory = (PreferenceCategory) findPreference("danger_zone");
        Preference showAccessToken = findPreference("copyAccessToken");
        if(global_prefs.getBoolean("debugDangerZone", false)) {
            showAccessToken.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showConfirmDialog("access_token");
                    return false;
                }
            });
        } else {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("debug_menu");
            preferenceScreen.removePreference(dangerZoneCategory);
        }

        if(BuildConfig.BUILD_TYPE.equals("release")) {
            generalCategory.removePreference(causeAppToCrash);
            generalCategory.removePreference(legacy_http_client);
        }
    }

    private void showConfirmDialog(final String target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DebugMenuActivity.this);
        final View confirm_view = getLayoutInflater().inflate(R.layout.dialog_confirm_with_passw, null, false);
        final EditText password_edit = ((EditText) confirm_view.findViewById(R.id.password_edit));
        builder.setView(confirm_view);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        final OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.confirm), "", confirm_view);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button ok_btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if(ok_btn != null) {
                    ok_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if(instance_prefs.getString("account_password_hash", "")
                                        .equals(Global.GetSHA256Hash(password_edit.getText().toString()))) {
                                    if (target.equals("access_token")) {
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                                            android.text.ClipboardManager clipboard =
                                                    (android.text.ClipboardManager)
                                                            getSystemService(Context.CLIPBOARD_SERVICE);
                                            clipboard.setText(instance_prefs.getString("access_token", ""));
                                        } else {
                                            android.content.ClipboardManager clipboard =
                                                    (android.content.ClipboardManager)
                                                    getSystemService(Context.CLIPBOARD_SERVICE);
                                            android.content.ClipData clip = android.content.ClipData
                                                    .newPlainText("OpenVK API Access Token", instance_prefs.getString("access_token", ""));
                                            clipboard.setPrimaryClip(clip);
                                        }
                                    }
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), R.string.copy_token_toast,
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    password_edit.setError(getResources().getString(R.string.invalid_password));
                                    ok_btn.setEnabled(false);
                                }
                            } catch (NoSuchAlgorithmException e) {
                                password_edit.setError(getResources().getString(R.string.error));
                                ok_btn.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
        password_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (password_edit.getText().length() > 0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                } catch (NullPointerException ignored) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
        if (password_edit.getText().length() > 0) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public boolean writeLogToFile() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\r\n");
            }
            try {
                File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "OpenVK");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/OpenVK", "App Logs");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                Date dt = new Date(System.currentTimeMillis());
                File file = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(dt) + ".log");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                    } else {
                        Global.allowPermissionDialog(this, false);
                        return false;
                    }
                } else {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                }
                if(BuildConfig.DEBUG) Log.d("OpenVK Legacy", "Log file created!");
                FileWriter writer = null;
                writer = new FileWriter(file);
                OvkApplication ovk = ((OvkApplication) getApplicationContext());
                String isTablet;
                String server;
                String usingHTTPS;
                if(ovk.isTablet) {
                    isTablet = "Yes";
                } else {
                    isTablet = "No";
                }
                if(instance_prefs.contains("server")) {
                    server = instance_prefs.getString("server", "");
                } else {
                    server = "N/A";
                }
                if(global_prefs.getBoolean("useHTTPS", false)) {
                    usingHTTPS = "Yes";
                } else {
                    usingHTTPS = "No";
                }
                writer.append(String.format(
                        "OpenVK Legacy %s (%s)\r\n" +
                                "==============================================" +
                                "\r\nDEVICE" +
                                "\r\nDevice: %s %s (codename: %s)" +
                                "\r\nAndroid: %s (API %s)\r\n" +
                                "==============================================" +
                                "\r\nAPP SETTINGS" +
                                "\r\nInstance: %s" +
                                "\r\nHTTPS: %s" +
                                "\r\nTablet UI?: %s\r\n" +
                                "==============================================\r\n",
                        ovk.version, BuildConfig.GITHUB_COMMIT, Build.BRAND, Build.MODEL, Build.DEVICE,
                        Build.VERSION.RELEASE, Build.VERSION.SDK_INT, server, usingHTTPS, isTablet));
                if(log.length() == 0) {
                    log.append("ERROR: Logcat not supported or not allowed. Please enable USB debugging.");
                }
                writer.append(log);
                writer.flush();
                writer.close();
                Toast.makeText(getApplicationContext(), getResources().getString(
                        R.string.saved_logs_successfully,
                        "OpenVK/App Logs"), Toast.LENGTH_LONG).show();
                return true;
            } catch (Exception e) {
                Log.e("OpenVK Legacy", "Could not save log to file: " + e.getMessage());
                e.printStackTrace();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Global.allowPermissionDialog(this, false);
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.debug_on_pre_jellybean, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    R.string.debug_on_pre_jellybean, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
