package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class OvkApplication extends Application {

    public String version;
    public int build_number = 103;
    public boolean isTablet;

    @Override
    public void onCreate() {
        super.onCreate();
        Global global = new Global(this);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);

        SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
        SharedPreferences.Editor instance_prefs_editor = instance_prefs.edit();

        if(!instance_prefs.contains("server")) {
            instance_prefs_editor.putString("server", "");
        }

        if(!global_prefs.contains("owner_id")) {
            global_prefs_editor.putInt("owner_id", 0);
        }

        if(!global_prefs.contains("useHTTPS")) {
            global_prefs_editor.putBoolean("useHTTPS", true);
        }

        global_prefs_editor.commit();
        instance_prefs_editor.commit();

        isTablet = global.isTablet();
    }
}
