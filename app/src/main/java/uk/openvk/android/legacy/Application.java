package uk.openvk.android.legacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

public class Application extends android.app.Application {
    public String version;
    public int build_number = 18;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!sharedPreferences.contains("server")) {
            SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
            sharedPrefsEditor.putString("server", "");
            sharedPrefsEditor.commit();
        }
        if(!sharedPreferences.contains("owner_id")) {
            SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
            sharedPrefsEditor.putInt("owner_id", 0);
            sharedPrefsEditor.commit();
        }

        if(!global_prefs.contains("useHTTPS")) {
            SharedPreferences.Editor sharedPrefsEditor = global_prefs.edit();
            sharedPrefsEditor.putBoolean("useHTTPS", true);
            sharedPrefsEditor.commit();
        }
        if(!global_prefs.contains("currentLayout")) {
            SharedPreferences.Editor sharedPrefsEditor = global_prefs.edit();
            sharedPrefsEditor.putString("currentLayout", "NewsLinearLayout");
            sharedPrefsEditor.commit();
        }
    }

}
