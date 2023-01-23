package uk.openvk.android.legacy.user_interface.activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;


public class MainActivity extends Activity {

    private Global global = new Global();
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private View warn_view;
    private Handler handler;
    private AlertDialog warn_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int month = new Date().getMonth();
        int day = new Date().getDate();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(global_prefs.getBoolean("startupSplash", true)) {
            if ((month == 11 && day >= 1) || (month == 0 && day <= 15)) {
                setContentView(R.layout.xmas_splash_activity);
            } else {
                setContentView(R.layout.splash_activity);
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
            setTheme(R.style.BaseStyle);
            Timer timer = new Timer();
            timer.schedule(new AutoRun(), 0);
        }
    }

    class AutoRun extends TimerTask {
        public void run() {
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
            if ((instance_prefs.getString("server", "").length() == 0 || instance_prefs.getString("access_token", "").length() == 0 ||
                    instance_prefs.getString("account_password_hash", "").length() == 0) && !global_prefs.getBoolean("hideOvkWarnForBeginners", false)) {
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            } else {
                closeSplashScreen();
            }
        }
    }

    private void createOvkWarnDialogForBeginners() {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(MainActivity.this);
        dialog_builder.setTitle(R.string.ovk_warning_title);
        warn_view = getLayoutInflater().inflate(R.layout.warn_message_layout, null, false);
        dialog_builder.setView(warn_view);
        dialog_builder.setNeutralButton(R.string.ok, null);
        warn_dialog = dialog_builder.create();
        warn_dialog.show();
        ((TextView) warn_view.findViewById(R.id.warn_message_text)).setText(Html.fromHtml(getResources().getString(R.string.ovk_warning)));
        ((TextView) warn_view.findViewById(R.id.warn_message_text)).setMovementMethod(LinkMovementMethod.getInstance());
        ((CheckBox) warn_view.findViewById(R.id.do_not_show_messages)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
                global_prefs_editor.putBoolean("hideOvkWarnForBeginners", b);
                global_prefs_editor.commit();
            }
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setDialogStyle(warn_view, "ovk_warn");
        }
        warn_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                closeSplashScreen();
            }
        });
    }

    private void closeSplashScreen() {
        if (instance_prefs.getString("server", "").length() == 0 || instance_prefs.getString("access_token", "").length() == 0 ||
                instance_prefs.getString("account_password", "").length() == 0) {
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

    private void setDialogStyle(View view, String dialog_name) {
        if(dialog_name.equals("ovk_warn")) {
            ((TextView) view.findViewById(R.id.warn_message_text)).setTextColor(Color.WHITE);
            ((CheckBox) view.findViewById(R.id.do_not_show_messages)).setTextColor(Color.WHITE);
        }
    }
}
