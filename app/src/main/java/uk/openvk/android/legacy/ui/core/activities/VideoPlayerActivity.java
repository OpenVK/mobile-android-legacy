package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import wseemann.media.FFmpegMediaPlayer;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;

/**
 * File created by Dmitry on 14.02.2023.
 */

@SuppressWarnings("deprecation")
public class VideoPlayerActivity extends Activity {
    private VideoAttachment video;
    private String url;
    private MediaController mediaCtrl;
    private VideoView video_view;
    private MediaPlayer mp;
    private FFmpegMediaPlayer fmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        loadVideo();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().hide();
        }
        loadVideo();
    }

    private void loadVideo() {
        Bundle data = getIntent().getExtras();
        if(data != null) {
            if(data.containsKey("attachment")) {
                video = data.getParcelable("attachment");
                assert video != null;
            } if(data.containsKey("files")) {
                video.files = data.getParcelable("files");
                assert video.files != null;
                if(video.files.ogv_480 != null && video.files.ogv_480.length() > 0) {
                    url = video.files.ogv_480;
                } if(video.files.mp4_144 != null && video.files.mp4_144.length() > 0) {
                    url = video.files.mp4_144;
                } if(video.files.mp4_240 != null && video.files.mp4_240.length() > 0) {
                    url = video.files.mp4_240;
                } if(video.files.mp4_360 != null && video.files.mp4_360.length() > 0) {
                    url = video.files.mp4_360;
                } if(video.files.mp4_480 != null && video.files.mp4_480.length() > 0) {
                    url = video.files.mp4_480;
                } if(video.files.mp4_720 != null && video.files.mp4_720.length() > 0) {
                    url = video.files.mp4_720;
                } if(video.files.mp4_1080 != null && video.files.mp4_1080.length() > 0) {
                    url = video.files.mp4_1080;
                }

                if(url == null) {
                    url = "";
                }
                createMediaPlayer();
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateControlPanel();
                        new Handler(Looper.myLooper()).postDelayed(this, 200);
                    }
                });
                findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                ((ImageButton) findViewById(R.id.video_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
            }
        } else {
            finish();
        }
    }

    private void createMediaPlayer() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            fmp = new FFmpegMediaPlayer();
            fmp.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    vsh.setFixedSize(mp.getVideoWidth(), mp.getVideoHeight());
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                }
            });
            fmp.setOnErrorListener(new FFmpegMediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(FFmpegMediaPlayer mp, int what, int extra) {
                    fmp.release();
                    return false;
                }
            });

            try {
                fmp.setDataSource(url);
                fmp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
            mp = new MediaPlayer();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    vsh.setFixedSize(mp.getVideoWidth(), mp.getVideoHeight());
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.release();
                    return false;
                }
            });

            try {
                mp.setDataSource(url);
                mp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void playVideo() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (isPlaying()) {
                fmp.pause();
            } else {
                fmp.start();
            }
        } else {
            if (isPlaying()) {
                mp.pause();
            } else {
                mp.start();
            }
        }
    }

    private boolean isPlaying() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return fmp.isPlaying();
        } else {
            return mp.isPlaying();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateControlPanel() {
        int pos = video_view.getCurrentPosition() / 1000;
        int duration = video_view.getDuration() / 1000;
        if(video_view.isPlaying()) {
            ((TextView) findViewById(R.id.video_time1)).setText(String.format("%d:%02d", pos / 60, pos % 60));
            ((TextView) findViewById(R.id.video_time2)).setText(String.format("%d:%02d", duration / 60, duration % 60));
            ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(pos);
            ((SeekBar) findViewById(R.id.video_seekbar)).setMax(duration);
            ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_video_pause));
        } else {
            ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_video_play));
        }
    }
}
