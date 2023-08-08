package uk.openvk.android.legacy.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import uk.openvk.android.legacy.BuildConfig;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class OvkMediaPlayer {
    private final Context ctx;
    String MPLAY_TAG = "OVK-MPLAY";

    private native String showLogo();

    @SuppressLint({"UnsafeDynamicallyLoadedCode", "SdCardPath"})
    private static void loadLibrary(Context ctx, String name) {
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            System.loadLibrary(String.format("%s", name));
        } else {
            // unsafe but changeable
            System.load(String.format("/data/data/%s/lib/lib%s.so", ctx.getPackageName(), name));
        }
    }

    public OvkMediaPlayer(Context ctx) {
        this.ctx = ctx;
        loadLibrary(ctx, "ffmpeg");
        loadLibrary(ctx, "ovkmplayer");
        Log.v(MPLAY_TAG, showLogo());
    }
}
