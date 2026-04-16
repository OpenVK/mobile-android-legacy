package uk.openvk.android.legacy.utils.media;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;

import java.io.IOException;
import java.net.URLEncoder;

public class ProxifiedMediaPlayer extends MediaPlayer {
    private final Context ctx;
    private final String proxyType;
    private final String proxyAddress;
    SharedPreferences globalPrefs;

    public ProxifiedMediaPlayer(Context ctx) {
        this.ctx = ctx;
        globalPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        proxyType = globalPrefs.getString("proxy_type", "");
        proxyAddress = globalPrefs.getString("proxy_address", "");
    }

    @Override
    public void setDataSource(String path) throws
            IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        if(globalPrefs.getBoolean("useProxy", false) &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if(proxyType.equals("selfeco-relay")) {
                super.setDataSource(String.format("http://%s/?goto=%s", proxyAddress,
                        URLEncoder.encode(path)
                ));
            } else
                super.setDataSource(path);
        } else
            super.setDataSource(path);
    }
}
