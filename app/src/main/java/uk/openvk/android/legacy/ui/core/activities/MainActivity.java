package uk.openvk.android.legacy.ui.core.activities;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.ui.list.items.InstanceAccount;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
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

public class MainActivity extends TranslucentActivity {

    private Global global = new Global();
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private View warn_view;
    private Handler handler;
    private OvkAlertDialog warn_dialog;
    private ArrayList<InstanceAccount> accountArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        getAndroidAccounts();
        if(global_prefs.getBoolean("startupSplash", true)) {
            if (Global.isXmas()) {
                setTranslucentStatusBar(1, Color.parseColor("#7331C2"));
                setContentView(R.layout.activity_splash_xmas);
            } else {
                setContentView(R.layout.activity_splash);
                if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                    findViewById(R.id.auth)
                            .setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_auth_gray));
                    ((ImageView) findViewById(R.id.auth_logo))
                            .setImageDrawable(getResources().getDrawable(R.drawable.login_logo_gray));
                } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                    findViewById(R.id.auth)
                            .setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_auth_black));
                    ((ImageView) findViewById(R.id.auth_logo))
                            .setImageDrawable(getResources().getDrawable(R.drawable.login_logo_black));
                }
            }
            handler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        createOvkWarnDialogForBeginners();
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(new AutoRun(), 1000);
        } else {
            handler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        createOvkWarnDialogForBeginners();
                    }
                }
            };
            setTheme(R.style.BaseStyle);
            Timer timer = new Timer();
            timer.schedule(new AutoRun(), 0);
        }
    }

    class AutoRun extends TimerTask {
        public void run() {
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext());
            SharedPreferences instance_prefs = getApplicationContext().
                    getSharedPreferences("instance", 0);
            if ((accountArray.size() == 0) &&
                    !global_prefs.getBoolean("hideOvkWarnForBeginners", false)) {
                Message msg = new Message();
                msg.what = 0;
                Looper.prepare();
                try {
                    handler.sendMessage(msg);
                } catch (Exception ex) {
                    closeSplashScreen();
                }
            } else {
                closeSplashScreen();
            }
        }
    }

    private void createOvkWarnDialogForBeginners() {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(new ContextThemeWrapper(
                MainActivity.this, R.style.BaseStyle));
        warn_view = getLayoutInflater().inflate(R.layout.dialog_warn_message, null, false);
        dialog_builder.setView(warn_view);
        dialog_builder.setNeutralButton(R.string.ok, null);
        warn_dialog = new OvkAlertDialog(this);
        warn_dialog.build(dialog_builder, "", getResources().getString(R.string.ovk_warning_title), warn_view);
        warn_dialog.show();
        warn_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ((TextView) warn_view.findViewById(R.id.warn_message_text)).setText(
                Html.fromHtml(getResources().getString(R.string.ovk_warning)));
        ((TextView) warn_view.findViewById(R.id.warn_message_text)).setMovementMethod(
                LinkMovementMethod.getInstance());
        ((CheckBox) warn_view.findViewById(R.id.do_not_show_messages)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
                global_prefs_editor.putBoolean("hideOvkWarnForBeginners", b);
                global_prefs_editor.commit();
            }
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setDialogStyle(warn_dialog, warn_view, "ovk_warn");
        }
        warn_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                closeSplashScreen();
            }
        });
    }

    private void closeSplashScreen() {
        if ((accountArray.size() == 0)) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, AppActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getAndroidAccounts() {
        accountArray = new ArrayList<>();
        AccountManager accountManager = AccountManager.get(this);
        android.accounts.Account[] accounts = accountManager.getAccounts();
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        String package_name = getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            getSharedPreferences(
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
            Log.d(OvkApplication.APP_TAG, String.format("Files: %s", account_names.length));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setDialogStyle(AlertDialog dialog, View view, String dialog_name) {
        if(dialog_name.equals("ovk_warn")) {
            //((TextView) view.findViewById(R.id.warn_message_text)).setTextColor(Color.WHITE);
            ((CheckBox) view.findViewById(R.id.do_not_show_messages)).setTextColor(Color.BLACK);
        }

    }
}
