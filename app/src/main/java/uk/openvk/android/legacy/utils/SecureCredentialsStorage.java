package uk.openvk.android.legacy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.HashMap;

import uk.openvk.android.legacy.OvkApplication;

public class SecureCredentialsStorage {
    public static HashMap<String, Object> generateClientInfo(Context ctx) {
        HashMap<String, Object> clientInfo = new HashMap<>();

        SharedPreferences global_prefs =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences instance_prefs =
                ((OvkApplication) ctx.getApplicationContext()).getAccountPreferences();

        if(instance_prefs != null) {
            clientInfo.put("server", instance_prefs.getString("server", ""));
            clientInfo.put("accessToken", instance_prefs.getString("access_token", ""));
        }

        clientInfo.put("useHTTPS", global_prefs.getBoolean("useHTTPS", false));
        clientInfo.put("legacyHttpClient", global_prefs.getBoolean("legacyHttpClient", false));
        clientInfo.put("useProxy", global_prefs.getBoolean("useProxy", false));
        clientInfo.put("proxyType", global_prefs.getString("proxy_type", ""));
        clientInfo.put("proxyAddress", global_prefs.getString("proxy_address", ""));
        clientInfo.put("forcedCaching", global_prefs.getBoolean("forcedCaching", false));

        return clientInfo;
    }
}
