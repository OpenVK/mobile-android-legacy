package uk.openvk.android.legacy.utils;

import android.content.Context;
import android.widget.Toast;

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

public class MediaPlayer {
    public static LibraryLoader libLoader = new LibraryLoader() {
        @Override
        public void loadSharedLibrary(String library_name) throws UnsatisfiedLinkError,
                SecurityException, Exception {
            System.loadLibrary(library_name);
        }
    };

    private static native String testString();

    public MediaPlayer(Context ctx) {
        if(libLoader != null) {
            initMediaPlayer(libLoader);
            Toast.makeText(ctx, testString(), Toast.LENGTH_LONG).show();
        }
    }

    private void initMediaPlayer(LibraryLoader loader) {
        try {
            loader.loadSharedLibrary("ffmpeg-v4.0.4");
            loader.loadSharedLibrary("ovkmplayer");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
