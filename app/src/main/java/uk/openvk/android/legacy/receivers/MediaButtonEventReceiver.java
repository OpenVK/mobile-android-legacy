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
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.services.AudioPlayerService;

import static android.content.Context.BIND_AUTO_CREATE;

public class MediaButtonEventReceiver extends BroadcastReceiver {
    private Context ctx;
    private Intent serviceIntent;

    public MediaButtonEventReceiver() {
        super();
    }

    public MediaButtonEventReceiver(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }

        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    setAudioPlayerState(
                            context.getApplicationContext(),
                            0,
                            AudioPlayerService.STATUS_PLAYING
                    );
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    setAudioPlayerState(
                            context.getApplicationContext(),
                            0,
                            AudioPlayerService.STATUS_PAUSED
                    );
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    setAudioPlayerState(
                            context.getApplicationContext(),
                            0,
                            AudioPlayerService.STATUS_GOTO_PREVIOUS
                    );
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    setAudioPlayerState(
                            context.getApplicationContext(),
                            0,
                            AudioPlayerService.STATUS_GOTO_NEXT
                    );
                    break;
            }
        }


        abortBroadcast();
    }

    public void setAudioPlayerState(Context appCtx, int position, int status) {
        String action = "";
        switch (status) {
            case AudioPlayerService.STATUS_STARTING:
                action = "PLAYER_START";
                break;
            case AudioPlayerService.STATUS_PLAYING:
                action = "PLAYER_PLAY";
                break;
            case AudioPlayerService.STATUS_PAUSED:
                action = "PLAYER_PAUSE";
                break;
            default:
                action = "PLAYER_STOP";
                break;
        }
        serviceIntent = new Intent(appCtx, AudioPlayerService.class);
        serviceIntent.putExtra("action", action);
        if(status == AudioPlayerService.STATUS_STARTING) {
            serviceIntent.putExtra("position", position);
        }
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        appCtx.startService(serviceIntent);
    }
}
