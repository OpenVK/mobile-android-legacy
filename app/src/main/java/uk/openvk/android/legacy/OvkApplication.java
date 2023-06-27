package uk.openvk.android.legacy;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;

import com.seppius.i18n.plurals.PluralResources;

import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import uk.openvk.android.legacy.api.wrappers.NotificationManager;
import uk.openvk.android.legacy.longpoll_api.LongPollService;

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

public class OvkApplication extends Application {

    public String version;
    public boolean isTablet;
    public LongPollService longPollService;
    public NotificationManager notifMan;
    public static String APP_TAG = "OpenVK";
    public static String API_TAG = "OVK-API";
    public static String DL_TAG = "OVK-DLM";
    public static String LP_TAG = "OVK-LP";
    public PluralResources pluralResources;
    public Configuration config;
    private Global global;
    public int swdp;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            try {
                pluralResources = new PluralResources(getResources());
            } catch (SecurityException | NoSuchMethodException e1) {
                e1.printStackTrace();
            }
        }
        global = new Global(this);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);

        version = BuildConfig.VERSION_NAME;
        config = getResources().getConfiguration();

        createSettings(global_prefs, instance_prefs);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            swdp = getResources().getConfiguration().smallestScreenWidthDp;
        } else {
            if(isTablet) {
                swdp = 600;
            } else {
                swdp = 420;
            }
        }
        isTablet = global.isTablet();
    }

    private void createSettings(SharedPreferences global_prefs, SharedPreferences instance_prefs) {
        SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
        SharedPreferences.Editor instance_prefs_editor = instance_prefs.edit();

        if(!instance_prefs.contains("server")) {
            instance_prefs_editor.putString("server", "");
        }

        if(!global_prefs.contains("owner_id")) {
            global_prefs_editor.putInt("owner_id", 0);
        }
        long heap_size = global.getHeapSize();

        // Create preference parameters

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
            global_prefs_editor.putString("notifyRingtone",
                    "content://settings/system/notification_sound");
        }

        if(!global_prefs.contains("debugDangerZone")) {
            global_prefs_editor.putBoolean("debugDangerZone", false);
        }

        if(!global_prefs.contains("hideOvkWarnForBeginners")) {
            global_prefs_editor.putBoolean("hideOvkWarnForBeginners", false);
        }

        if(!global_prefs.contains("startupSplash")) {
            global_prefs_editor.putBoolean("startupSplash", true);
        }

        if(!global_prefs.contains("forcedCaching")) {
            global_prefs_editor.putBoolean("forcedCaching", true);
        }

        if(global_prefs.contains("account_password") && global_prefs.getString("account_password", "").length() > 0) {
            try {
                global_prefs_editor.putString("encrypted_account_password",
                        Global.GetSHA256Hash(global_prefs.getString("account_password", "")));
            } catch (NoSuchAlgorithmException e) {
                global_prefs_editor.putString("encrypted_account_password", "");
            }
            global_prefs_editor.putString("account_password", "");
        }

        global_prefs_editor.commit();
        instance_prefs_editor.commit();
    }


    public static Locale getLocale(Context ctx) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String language = global_prefs.getString("interfaceLanguage", "System");
        String language_code;
        switch (language) {
            case "English":
                language_code = "en";
                break;
            case "Русский":
                language_code = "ru";
                break;
            case "Украïнська":
                language_code = "uk";
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    language_code = ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
                } else {
                    language_code = ctx.getResources().getConfiguration().locale.getLanguage();
                }
                break;
        }


        return new Locale(language_code);
    }

}
