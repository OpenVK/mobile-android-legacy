package uk.openvk.android.legacy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.services.AudioPlayerService;

public class AudioPlayerReceiver extends BroadcastReceiver {
    private final Context ctx;

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
                    }
                    break;
                case AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS:
                    status = data.getInt("status");
                    track_pos = data.getInt("track_position");
                    if(audiosFragment != null) {
                        audiosFragment.updateCurrentTrackPosition(track_pos, status);
                    }
                    break;
            }

        }
    }
}
