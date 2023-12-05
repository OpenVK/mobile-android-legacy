package uk.openvk.android.legacy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.utils.NotificationManager;

public class AudioPlayerService extends Service implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

    private final ArrayList<Audio> audios;
    private final NotificationManager notifMan;
    private Context ctx;
    private int currentTrackPos;

    public AudioPlayerService(Context ctx, ArrayList<Audio> audios, NotificationManager notifMan) {
        this.ctx = ctx;
        this.audios = audios;
        this.notifMan = notifMan;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        try {
            if (data != null) {
                String action = data.getString("action");
                if(action != null && action.equals("create_player_notif")) {
                    notifMan.buildAudioPlayerNotification(ctx, audios, data, true, false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    public void setCurrentTrackPosition(int position) {
        currentTrackPos = position;
    }
}
