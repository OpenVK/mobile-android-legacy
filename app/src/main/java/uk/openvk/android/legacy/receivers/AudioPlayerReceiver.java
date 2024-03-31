/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.AudioPlayerActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.services.AudioPlayerService;

public class AudioPlayerReceiver extends BroadcastReceiver {
    private Context ctx;

    public AudioPlayerReceiver() {

    }

    public AudioPlayerReceiver(Context ctx) {
        this.ctx = ctx;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        AudiosFragment audiosFragment = null;
        if(ctx instanceof AppActivity) {
            audiosFragment = ((AppActivity)(ctx)).audiosFragment;
        }
        if(intent.getExtras() != null && intent.getAction() != null) {
            Bundle data = intent.getExtras();
            String action = intent.getAction();
            switch (action) {
                case AudioPlayerService.ACTION_PLAYER_CONTROL:
                    int status = data.getInt("status");
                    int track_pos = data.getInt("track_position");
                    if(audiosFragment != null) {
                        audiosFragment.receivePlayerStatus(action, status, track_pos, data);
                    } else if(ctx instanceof AudioPlayerActivity) {
                        ((AudioPlayerActivity) ctx).receivePlayerStatus(action, status, track_pos, data);
                    }
                    break;
                case AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS:
                    status = data.getInt("status");
                    track_pos = data.getInt("track_position");
                    track_pos = data.getInt("track_position");
                    if(audiosFragment != null) {
                        audiosFragment.updateCurrentTrackPosition(track_pos, status);
                    } else if(ctx instanceof AudioPlayerActivity) {
                        ((AudioPlayerActivity) ctx).updateCurrentTrackPosition(track_pos, status);
                    }
                    break;
            }

        }
    }
}
