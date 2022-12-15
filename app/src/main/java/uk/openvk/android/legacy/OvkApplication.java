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
    public boolean isTablet;

    @Override
    public void onCreate() {
        super.onCreate();
        Global global = new Global(this);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);

        SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
        SharedPreferences.Editor instance_prefs_editor = instance_prefs.edit();

        version = BuildConfig.VERSION_NAME;

        if(!instance_prefs.contains("server")) {
            instance_prefs_editor.putString("server", "");
        }

        if(!global_prefs.contains("owner_id")) {
            global_prefs_editor.putInt("owner_id", 0);
        }
        long heap_size = global.getHeapSize();

        if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("VM heap size: %s MB", (double) heap_size / (double) 1024 / (double) 1024));

        if(!global_prefs.contains("photos_quality")) {
            if(heap_size <= 67108864L) {
                global_prefs_editor.putString("photos_quality", "medium");
            } else {
                global_prefs_editor.putString("photos_quality", "high");
            }
        }

        if(!instance_prefs.contains("account_password")) {
            instance_prefs_editor.putString("account_password", "");
        }

        if(!global_prefs.contains("useHTTPS")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                global_prefs_editor.putBoolean("useHTTPS", true);
            } else {
                global_prefs_editor.putBoolean("useHTTPS", false);
            }
        }

        if(!global_prefs.contains("enableNotification")) {
            global_prefs_editor.putBoolean("enableNotification", true);
        }

        if(!global_prefs.contains("notifyRingtone")) {
            global_prefs_editor.putString("notifyRingtone", "content://settings/system/notification_sound");
        }

        if(!global_prefs.contains("debugDangerZone")) {
            global_prefs_editor.putBoolean("debugDangerZone", false);
        }

        if(!global_prefs.contains("hideOvkWarnForBeginners")) {
            global_prefs_editor.putBoolean("hideOvkWarnForBeginners", false);
        }

        global_prefs_editor.commit();
        instance_prefs_editor.commit();

        isTablet = global.isTablet();
    }

}
