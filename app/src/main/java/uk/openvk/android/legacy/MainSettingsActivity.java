package uk.openvk.android.legacy;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        if(sharedPreferences.getString("auth_token", "").length() > 0) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_2);
        }
        final CheckBoxPreference https_connection = (CheckBoxPreference) findPreference("useHTTPS");
        https_connection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                return false;
            }
        });
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
                about_text.setMovementMethod(LinkMovementMethod.getInstance());
                builder.setView(about_view);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    AlertDialog about_dlg;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsActivity.this);
                    builder.setMessage(R.string.log_out_warning);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                            editor.putString("auth_token", "");
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
                    about_dlg = builder.create();
                    about_dlg.show();
                    return false;
                }
            });
            Preference debug_menu = findPreference("debug_menu");
            debug_menu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }
    }
}
