package uk.openvk.android.legacy.utils.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;

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
    private native OvkMediaTrack getTrackInfo(String filename, int type);

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

    public ArrayList<OvkMediaTrack> getMediaInfo(String filename) {
        ArrayList<OvkMediaTrack> tracks = new ArrayList<>();
        OvkVideoTrack video_track = (OvkVideoTrack) getTrackInfo(filename, OvkMediaTrack.TYPE_VIDEO);
        OvkAudioTrack audio_track = (OvkAudioTrack) getTrackInfo(filename, OvkMediaTrack.TYPE_AUDIO);
        tracks.add(video_track);
        tracks.add(audio_track);
        if(audio_track != null) {
            Log.d(MPLAY_TAG,
                    String.format("A: %s, %s Hz, %s bps, %s",
                            audio_track.codec_name, audio_track.sample_rate, audio_track.bitrate, audio_track.channels)
            );
        } else {
            Log.e(MPLAY_TAG, "Audio track not found!");
        }
        return tracks;
    }
}
