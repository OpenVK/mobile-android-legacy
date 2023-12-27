package uk.openvk.android.legacy.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.core.listeners.AudioPlayerListener;
import uk.openvk.android.legacy.databases.AudioCacheDB;
import uk.openvk.android.legacy.receivers.AudioPlayerReceiver;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.utils.NotificationManager;

import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_PLAYER_CONTROL;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_PLAYLIST;

public class AudioPlayerActivity extends NetworkActivity implements
        AudioPlayerService.AudioPlayerListener {
    private boolean isBoundAP;
    private AudioPlayerService audioPlayerService;
    private AudioPlayerReceiver audioPlayerReceiver;
    private MediaPlayer mediaPlayer;
    private Timer timer = new Timer();
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(audioPlayerService != null) {
                audioPlayerService.notifyPlayerStatus();
                audioPlayerService.notifySeekbarStatus();
            }
        }
    };
    private ServiceConnection audioPlayerConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            audioPlayerService.removeListener(AudioPlayerActivity.this);
            isBoundAP = false;
            audioPlayerService = null;
            mediaPlayer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            isBoundAP = true;
            AudioPlayerService.AudioPlayerBinder mLocalBinder =
                    (AudioPlayerService.AudioPlayerBinder) service;
            ((AudioPlayerService.AudioPlayerBinder) service).setAudioPlayerListener(AudioPlayerActivity.this);
            audioPlayerService = mLocalBinder.getService();
            mediaPlayer = audioPlayerService.getMediaPlayer();
            audioPlayerService.addListener(AudioPlayerActivity.this);
            audioPlayerService.notifyPlayerStatus();
        }
    };
    private ArrayList<Audio> audio_tracks;
    private int currentTrackPos;
    private int playerStatus;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        TextView title_tv = findViewById(R.id.aplayer_title);
        TextView artist_tv = findViewById(R.id.aplayer_artist);
        title_tv.setText("Unknown title");
        artist_tv.setText("Unknown artist");
        Intent serviceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
        serviceIntent.putExtra("action", "PLAYER_CONNECT");
        startService(serviceIntent);
        bindService(serviceIntent, audioPlayerConnection, BIND_AUTO_CREATE);
        findViewById(R.id.aplayer_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAudioPlayerState(currentTrackPos, AudioPlayerService.STATUS_GOTO_PREVIOUS);
            }
        });
        findViewById(R.id.aplayer_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAudioPlayerState(currentTrackPos, AudioPlayerService.STATUS_GOTO_NEXT);
            }
        });
        findViewById(R.id.aplayer_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playerStatus == AudioPlayerService.STATUS_PAUSED)
                    setAudioPlayerState(currentTrackPos, AudioPlayerService.STATUS_PLAYING);
                else
                    setAudioPlayerState(currentTrackPos, AudioPlayerService.STATUS_PAUSED);
            }
        });
        audio_tracks = AudioCacheDB.getCachedAudiosList(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.now_playing));
            getActionBar().setSubtitle(
                    getResources().getString(R.string.player_num, currentTrackPos + 1, audio_tracks.size())
            );
            getActionBar().setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.bg_actionbar_black_transparent)
            );
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBar(0, android.R.color.black);
        }
    }

    public void receivePlayerStatus(String action, int status, int track_pos, Bundle data) {
        ImageView play_button = findViewById(R.id.aplayer_play);
        playerStatus = status;
        if(action.equals(AudioPlayerService.ACTION_PLAYER_CONTROL)) {
            switch (status) {
                case AudioPlayerService.STATUS_PLAYING:
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(audioPlayerService != null && audioPlayerService.getMediaPlayer() != null) {
                                handler.sendEmptyMessage(0);
                            } else {
                                cancel();
                            }
                        }
                    }, 0, 200);
                    play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_panel_pause));
                    break;
                case AudioPlayerService.STATUS_PAUSED:
                    play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_panel_play));
                    try {
                        timer.cancel();
                    } catch (Exception ignored) {

                    }
                    break;
                default:
                    try {
                        timer.cancel();
                    } catch (Exception ignored) {

                    }
                    break;
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setSubtitle(
                    getResources().getString(R.string.player_num, currentTrackPos + 1, audio_tracks.size())
            );
        }
    }

    public void updateCurrentTrackPosition(int track_pos, int status) {
        ImageView play_button = findViewById(R.id.aplayer_play);
        Audio currentTrack = audio_tracks.get(track_pos);
        if(currentTrackPos != track_pos) {
            TextView title_tv = findViewById(R.id.aplayer_title);
            TextView artist_tv = findViewById(R.id.aplayer_artist);
            TextView lyrics_tv = findViewById(R.id.audio_player_lyrics);
            title_tv.setText(currentTrack.title);
            artist_tv.setText(currentTrack.artist);
            lyrics_tv.setText(currentTrack.lyrics);
            title_tv.setSelected(true);
            artist_tv.setSelected(true);
            this.currentTrackPos = track_pos;
            this.playerStatus = status;
        }
        switch (status) {
            case AudioPlayerService.STATUS_PLAYING:
                play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_panel_pause));
                break;
            case AudioPlayerService.STATUS_PAUSED:
                play_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_panel_play));
                break;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setSubtitle(
                    getResources().getString(R.string.player_num, currentTrackPos + 1, audio_tracks.size())
            );
        }
    }

    public void setAudioPlayerState(int position, int status) {
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
            case AudioPlayerService.STATUS_GOTO_PREVIOUS:
                action = "PLAYER_PREVIOUS";
                break;
            case AudioPlayerService.STATUS_GOTO_NEXT:
                action = "PLAYER_NEXT";
                break;
            default:
                action = "PLAYER_STOP";
                break;
        }
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        serviceIntent.putExtra("action", action);
        if(status == AudioPlayerService.STATUS_STARTING) {
            serviceIntent.putExtra("position", position);
        }
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        unbindService(audioPlayerConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onChangeAudioPlayerStatus(String action, int status, int track_pos, Bundle data) {
        receivePlayerStatus(action, status, track_pos, data);
    }

    @Override
    public void onReceiveCurrentTrackPosition(int track_pos, int status) {
        updateCurrentTrackPosition(track_pos, status);
    }

    @Override
    public void onUpdateSeekbarPosition(int position, int duration, double buffer_length) {
        updateSeekbarPosition(position, duration, buffer_length);
    }

    @Override
    public void onAudioPlayerError(int what, int extra, int currentTrackPos) {
        Audio track = audio_tracks.get(currentTrackPos);
        Toast.makeText(
                this,
                getResources().getString(R.string.audio_play_error),
                Toast.LENGTH_LONG).show();
    }

    @SuppressLint("DefaultLocale")
    private void updateSeekbarPosition(int position, int duration, double buffer_length) {
        SeekBar seekBar = findViewById(R.id.aplayer_progress);
        seekBar.setMax(duration);
        seekBar.setSecondaryProgress((int) buffer_length);
        seekBar.setProgress(position);
        TextView time_tv = findViewById(R.id.aplayer_time);
        time_tv.setText(String.format("%d:%02d", position / 60 / 1000, (position / 1000) % 60));
        TextView duration_tv = findViewById(R.id.aplayer_duration);
        duration_tv.setText(String.format("%d:%02d", duration / 60 / 1000, (duration / 1000) % 60));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
