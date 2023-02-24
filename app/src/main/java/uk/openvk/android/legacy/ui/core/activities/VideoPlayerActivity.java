package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
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

public class VideoPlayerActivity extends Activity {
    private VideoAttachment video;
    private String url;
    private MediaController mediaCtrl;
    private VideoView video_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        loadVideo();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            FFmpegMediaPlayer mp = new FFmpegMediaPlayer();
            mp.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    mp.start();
                }
            });
            mp.setOnErrorListener(new FFmpegMediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(FFmpegMediaPlayer mp, int what, int extra) {
                    mp.release();
                    return false;
                }
            });

            try {
                mp.setDataSource("<some path or URL>");
                mp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
        }
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
                Uri uri = Uri.parse(url);
                video_view = findViewById(R.id.video_view);
                video_view.setMediaController(null);
                video_view.setVideoURI(uri);
                video_view.requestFocus();
                MediaController controllers = new MediaController(this) {
                    @Override
                    public void hide() {
                        //super.hide();
                        try {
                            VideoPlayerActivity.this.findViewById(R.id.video_bottombar).setVisibility(GONE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void show() {
                        //super.show();
                        try {
                            VideoPlayerActivity.this.findViewById(R.id.video_bottombar).setVisibility(VISIBLE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public boolean isShowing() {
                        return VideoPlayerActivity.this.findViewById(R.id.video_bottombar).getVisibility() != GONE;
                    }
                };
                controllers.setAnchorView(video_view);
                video_view.setMediaController(controllers);
                video_view.start();
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

    private void playVideo() {
        if(video_view.isPlaying()) {
            video_view.pause();
        } else {
            video_view.start();
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
