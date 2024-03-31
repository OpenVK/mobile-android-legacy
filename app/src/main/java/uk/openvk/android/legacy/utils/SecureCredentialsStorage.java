package uk.openvk.android.legacy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.HashMap;

import uk.openvk.android.legacy.OvkApplication;

public class SecureCredentialsStorage {
    public static HashMap<String, Object> generateClientInfo(Context ctx, HashMap<String, Object> client_info) {
        SharedPreferences global_prefs =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences instance_prefs =
                ((OvkApplication) ctx.getApplicationContext()).getAccountPreferences();
        client_info.put("server", instance_prefs.getString("server", ""));
        client_info.put("accessToken", instance_prefs.getString("access_token", ""));
        client_info.put("useHTTPS", global_prefs.getBoolean("useHTTPS", false));
        client_info.put("legacyHttpClient", global_prefs.getBoolean("legacyHttpClient", false));
        client_info.put("useProxy", global_prefs.getBoolean("useProxy", false));
        client_info.put("proxyType", global_prefs.getString("proxy_type", ""));
        client_info.put("proxyAddress", global_prefs.getString("proxy_address", ""));
        client_info.put("forcedCaching", global_prefs.getBoolean("forcedCaching", false));
        return client_info;
    }
}
