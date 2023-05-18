package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

@SuppressWarnings("deprecation")
public class VideoPlayerActivity extends Activity {
    private VideoAttachment video;
    private String url;
    private MediaController mediaCtrl;
    private VideoView video_view;
    private MediaPlayer mp;
    private IMediaPlayer imp;
    private boolean ready;
    private Handler handler;
    private Runnable hideCtrl;
    private int invalid_pos;
    private SurfaceView vsv;
    private int duration;
    private boolean isErr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        loadVideo();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().hide();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void loadVideo() {
        Bundle data = getIntent().getExtras();
        showPlayControls();
        findViewById(R.id.video_surface_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayControls();
            }
        });
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

                createMediaPlayer(uri);
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

    private void createMediaPlayer(Uri uri) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                !Build.CPU_ABI.equals("x86")
                && !Build.CPU_ABI.equals("x86_64")) {
            try {
                imp = new IjkMediaPlayer();
                imp.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer mp) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        ready = true;
                        findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                        vsv = findViewById(R.id.video_surface_view);
                        SurfaceHolder vsh = vsv.getHolder();
                        rescaleVideo(vsv, vsh);
                        vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        mp.setDisplay(vsh);
                        mp.start();
                        new Handler(Looper.myLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(!isError()) {
                                    updateControlPanel();
                                }
                                new Handler(Looper.myLooper()).postDelayed(this, 200);
                            }
                        });
                    }
                });
                imp.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer mp, int what, int extra) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        isErr = true;
                        mp.release();
                        OvkAlertDialog err_dlg;
                        err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                        builder.setCancelable(false);
                        builder.setMessage(R.string.error);
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        builder.setPositiveButton(R.string.retry_short, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent tp_player = new Intent(Intent.ACTION_VIEW);
                                tp_player.setDataAndType(Uri.parse(VideoPlayerActivity.this.url), "video/*");
                                startActivity(tp_player);
                                finish();
                            }
                        });
                        err_dlg.build(builder, getResources().getString(R.string.error), getResources().getString(R.string.video_err_decode),
                                null);
                        err_dlg.show();
                        return false;
                    }
                });
                imp.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(IMediaPlayer iMediaPlayer) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        if(imp.getDuration() > 0) {
                            ((TextView) findViewById(R.id.video_time1)).setText(String.format("%d:%02d",
                                    duration / 60, duration % 60));
                            ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(duration);
                        }
                    }
                });

                try {
                    imp.setDataSource(this, uri);
                    imp.prepareAsync();
                } catch (IllegalArgumentException | IOException | IllegalStateException |
                        SecurityException e) {
                    e.printStackTrace();
                }
            } catch (Exception ex) {
                OvkAlertDialog err_dlg;
                err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                builder.setCancelable(false);
                builder.setMessage(R.string.error);
                builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setPositiveButton(R.string.retry_short, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent tp_player = new Intent(Intent.ACTION_VIEW);
                        tp_player.setDataAndType(Uri.parse(VideoPlayerActivity.this.url), "video/*");
                        startActivity(tp_player);
                        finish();
                    }
                });
                err_dlg.build(builder, getResources().getString(R.string.error), getResources().getString(R.string.video_err_decode),
                        null);
                err_dlg.show();
            }
        } else {
            mp = new MediaPlayer();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    ready = true;
                    findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                    SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    rescaleVideo(vsv, vsh);
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isError()) {
                                updateControlPanel();
                            }
                            new Handler(Looper.myLooper()).postDelayed(this, 200);
                        }
                    });
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isErr = true;
                    mp.release();
                    OvkAlertDialog err_dlg;
                    err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                    builder.setMessage(R.string.error);
                    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.setPositiveButton(R.string.retry_short, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent tp_player = new Intent(Intent.ACTION_VIEW);
                            tp_player.setDataAndType(Uri.parse(VideoPlayerActivity.this.url), "video/*");
                            startActivity(tp_player);
                            finish();
                        }
                    });
                    err_dlg.build(builder, getResources().getString(R.string.error), getResources().getString(R.string.video_err_decode), null);
                    err_dlg.show();
                    return false;
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if(mp.getDuration() > 0) {
                        ((TextView) findViewById(R.id.video_time2)).setText(String.format("%d:%02d",
                                duration / 60, duration % 60));
                        ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(duration);
                    }
                }
            });

            try {
                mp.setDataSource(this, uri);
                mp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        }
        ((SeekBar) findViewById(R.id.video_seekbar)).setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((SeekBar) findViewById(R.id.video_seekbar)).requestFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekProgress(seekBar.getProgress());
            }
        });
    }

    private void seekProgress(int progress) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            imp.seekTo(progress * 1000);
        } else {
            mp.seekTo(progress * 1000);
        }
    }

    private void rescaleVideo(SurfaceView vsv, SurfaceHolder vsh) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                !Build.CPU_ABI.equals("x86")
                && !Build.CPU_ABI.equals("x86_64")) {
            vsh.setFixedSize(imp.getVideoWidth(), imp.getVideoHeight());
            // Get the width of the frame
            int videoWidth = imp.getVideoWidth();
            int videoHeight = imp.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            // Get the width of the screen
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            android.view.ViewGroup.LayoutParams lp = vsv.getLayoutParams();
            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) ((float) screenWidth / videoProportion);
            } else {
                lp.width = (int) (videoProportion * (float) screenHeight);
                lp.height = screenHeight;
            }
            vsv.setLayoutParams(lp);
        } else {
            vsh.setFixedSize(mp.getVideoWidth(), mp.getVideoHeight());
            // Get the width of the frame
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            // Get the width of the screen
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            android.view.ViewGroup.LayoutParams lp = vsv.getLayoutParams();
            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) ((float) screenWidth / videoProportion);
            } else {
                lp.width = (int) (videoProportion * (float) screenHeight);
                lp.height = screenHeight;
            }
            vsv.setLayoutParams(lp);
        }
    }

    public void showPlayControls() {
        if(handler == null) {
            handler = new Handler(Looper.myLooper());
        }
        findViewById(R.id.video_bottombar).setVisibility(View.VISIBLE);
        if(hideCtrl != null) {
            handler.removeCallbacks(hideCtrl);
        } else {
            hideCtrl = new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.video_bottombar).setVisibility(View.GONE);
                }
            };
        }
        handler.postDelayed(hideCtrl, 5000);
    }

    private void playVideo() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                    !Build.CPU_ABI.equals("x86")
                    && !Build.CPU_ABI.equals("x86_64")) {
                if (isPlaying()) {
                    imp.pause();
                } else {
                    imp.start();
                }
            } else {
                if (isPlaying()) {
                    mp.pause();
                } else {
                    mp.start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isPlaying() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                    !Build.CPU_ABI.equals("x86")
                    && !Build.CPU_ABI.equals("x86_64")) {
                return imp.isPlaying();
            } else {
                return mp.isPlaying();
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateControlPanel() {
        if(ready) {
            int pos = 0;
            int duration = 0;
            try {
                if(invalid_pos != 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                            !Build.CPU_ABI.equals("x86")
                            && !Build.CPU_ABI.equals("x86_64")) {
                        pos = (int) (imp.getCurrentPosition() / 1000);
                        // calculating video duration workaround
                        if(this.duration == 0) {
                            this.duration = (int) (imp.getDuration() / 1000);
                        }
                        if (pos < 0 && this.duration < 0) {
                            pos = 0;
                            this.duration = 0;
                            invalid_pos = 1;
                            throw new Error(String.format("Incorrect information about the position and/or " +
                                    "duration of the video\r\nPosition: %s < 0\r\nDuration: %s < 0",
                                    imp.getCurrentPosition(), imp.getDuration()));
                        }
                    } else {
                        pos = mp.getCurrentPosition() / 1000;
                        this.duration = mp.getDuration() / 1000;
                        if (pos < 0 && this.duration < 0) {
                            pos = 0;
                            this.duration = 0;
                            invalid_pos = 1;
                            throw new Error(String.format("Incorrect information about the position and/or " +
                                            "duration of the video\r\nPosition: %s < 0\r\nDuration: %s < 0",
                                    mp.getCurrentPosition(), mp.getDuration()));
                        }
                    }
                }
            } catch (Error | Exception err) {
                err.printStackTrace();
            }
            if (isPlaying()) {
                ((TextView) findViewById(R.id.video_time1)).setText(String.format("%d:%02d",
                        pos / 60, pos % 60));
                ((TextView) findViewById(R.id.video_time2)).setText(String.format("%d:%02d",
                        this.duration / 60, this.duration % 60));
                if(!((SeekBar) findViewById(R.id.video_seekbar)).isFocused()) {
                    ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(pos);
                    ((SeekBar) findViewById(R.id.video_seekbar)).setMax(this.duration);
                }
                ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_video_pause));
            } else {
                ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_video_play));
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
        SurfaceHolder vsh = vsv.getHolder();
        rescaleVideo(vsv, vsh);
    }

    @Override
    protected void onDestroy() {
        ready = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                    !Build.CPU_ABI.equals("x86")
                    && !Build.CPU_ABI.equals("x86_64")) {
                if (isPlaying()) {
                    imp.stop();
                }
                imp.release();
            } else {
                if (isPlaying()) {
                    mp.stop();
                }
                mp.release();
            }
        } catch (Exception ignored){

        }
        handler.removeCallbacks(hideCtrl);
        super.onDestroy();
    }

    public boolean isError() {
        return isErr;
    }
}
