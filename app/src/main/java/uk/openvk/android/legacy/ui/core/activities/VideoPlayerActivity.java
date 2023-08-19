package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.utils.media.OvkMediaPlayer;

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
    private OvkMediaPlayer mp;
    private boolean ready;
    private Handler handler;
    private Runnable hideCtrl;
    private int invalid_pos;
    private SurfaceView vsv;
    private int duration;
    private boolean isErr;
    private boolean fitVideo;
    private Bitmap thumbnail;
    private long owner_id;
    private SystemBarTintManager tintManager;
    private boolean overduration;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.video));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        loadVideo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(data.getString("title"));
                }
                owner_id = data.getLong("owner_id");
                setThumbnail();
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

                /* Because ijkplayer does not support TLS connection.
                /
                /  W/IJKMEDIA: https protocol not found, recompile FFmpeg with openssl,
                /  gnutls or securetransport enabled.
                /  E/IJKMEDIA: https://[CDN address]: Protocol not found
                */

                createMediaPlayer(url);
                (findViewById(R.id.video_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
                (findViewById(R.id.video_resize)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(fitVideo) {
                            ((ImageButton) v).setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_video_expand));
                        } else {
                            ((ImageButton) v).setImageDrawable(
                                    getResources().getDrawable(R.drawable.ic_video_shrink));
                        }
                        resizeVideo();
                    }
                });
            }
        } else {
            finish();
        }
    }

    private void setThumbnail() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        thumbnail = BitmapFactory.decodeFile(
                getCacheDir() + "/photos_cache/video_thumbnails/thumbnail_"
                        + video.id + "o" + owner_id, options);
        if(thumbnail != null)
        ((ImageView) findViewById(R.id.video_thumbnail)).setImageBitmap(thumbnail);
    }

    private void createMediaPlayer(String url) {
        OvkMediaPlayer mp = new OvkMediaPlayer(this);
        try {
            mp.setOnPreparedListener(new OvkMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(OvkMediaPlayer mp) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    ready = true;
                    findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                    findViewById(R.id.video_thumbnail).setVisibility(View.GONE);
                    SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    rescaleVideo(vsv, vsh);
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                    handler.postDelayed(hideCtrl, 5000);
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isError()) {
                                updateControlPanel();
                            }
                            new Handler(Looper.myLooper()).postDelayed(this, 200);
                        }
                    });
                }
            });
            mp.setOnErrorListener(new OvkMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(OvkMediaPlayer mp, int what) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isErr = true;
                    mp.release();
                    handler.removeCallbacks(hideCtrl);
                    Log.e(OvkApplication.APP_TAG, String.format("Cannot load video. Code: %s", what));
                    OvkAlertDialog err_dlg;
                    err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                    findViewById(R.id.video_thumbnail).setVisibility(View.GONE);
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
                    int error_reason_id = R.string.video_err_decode;
                    err_dlg.build(builder, getResources().getString(R.string.error),
                            getResources().getString(error_reason_id), null);
                    err_dlg.show();
                    return false;
                }
            });
            mp.setOnCompletionListener(new OvkMediaPlayer.OnCompletionListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onCompleted(OvkMediaPlayer mp) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if (mp.getDuration() > 0) {
                        ((TextView) findViewById(R.id.video_time1)).setText(String.format("%d:%02d",
                                duration / 60, duration % 60));
                        ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(duration);
                    }
                    showPlayControls();
                }
            });

            try {
                mp.setDataSource(url);
                mp.prepareAsync();
            } catch (IllegalArgumentException | IllegalStateException |
                    SecurityException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
    }

    private void seekProgress(int progress) {
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            imp.seekTo(progress * 1000);
        } else {
            mp.seekTo(progress * 1000);
        }*/
    }

    private void resizeVideo() {
        /*SurfaceView mSurfaceView = findViewById(R.id.video_surface_view);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                && !Build.CPU_ABI.equals("x86_64")) {
            if(!fitVideo) {
                int videoWidth = imp.getVideoWidth();
                int videoHeight = imp.getVideoHeight();
                int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

                android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();

                lp.width = screenWidth;

                lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);
                mSurfaceView.setLayoutParams(lp);
                fitVideo = true;
            } else {
                fitVideo = false;
                rescaleVideo(mSurfaceView, mSurfaceView.getHolder());
            }
        } else {
            if(!fitVideo) {
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

                android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();

                lp.width = screenWidth;

                lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);
                mSurfaceView.setLayoutParams(lp);
                fitVideo = true;
            } else {
                fitVideo = false;
            }
        }*/
    }

    private void rescaleVideo(SurfaceView vsv, SurfaceHolder vsh) {
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
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
        }*/
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void showPlayControls() {
        try {
            if (handler == null) {
                handler = new Handler(Looper.myLooper());
            }
            if (hideCtrl != null) {
                handler.removeCallbacks(hideCtrl);
            } else {
                hideCtrl = new Runnable() {
                    @Override
                    public void run() {
                        hidePlayControls();
                    }
                };
            }
            if (isPlaying()) {
                handler.postDelayed(hideCtrl, 5000);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE;
                getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
            // Resize top view
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight =
                            TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
            } else {
                actionBarHeight = 50;
            }
            ValueAnimator animator = ValueAnimator.ofInt(1, getStatusBarHeight() + actionBarHeight);
            final View statusbar_tint = findViewById(R.id.statusbartint_view);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    if (statusbar_tint != null) {
                        ViewGroup.LayoutParams layoutParams = statusbar_tint.getLayoutParams();
                        layoutParams.height = value;
                        statusbar_tint.setLayoutParams(layoutParams);
                    }
                    ViewGroup.LayoutParams layoutParams = findViewById(R.id.video_bottombar).getLayoutParams();
                    layoutParams.height = value;
                    findViewById(R.id.video_bottombar).setLayoutParams(layoutParams);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (statusbar_tint != null) {
                        statusbar_tint.setVisibility(View.VISIBLE);
                    }
                    findViewById(R.id.video_bottombar).setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(200);
            animator.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                handler.postDelayed(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        getActionBar().show();
                    }
                }, 100);
            }
        } catch (Exception ignored) {

        }
    }

    public void hidePlayControls() {
        findViewById(R.id.video_bottombar).setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                    getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
            }
        }, 50);
        // Resize top view
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        final ValueAnimator animator = ValueAnimator.ofInt(getStatusBarHeight() + actionBarHeight, 1);
        final View statusbar_tint = findViewById(R.id.statusbartint_view);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                if(statusbar_tint != null) {
                    ViewGroup.LayoutParams layoutParams = statusbar_tint.getLayoutParams();
                    layoutParams.height = value;
                    statusbar_tint.setLayoutParams(layoutParams);
                }
                ViewGroup.LayoutParams layoutParams = findViewById(R.id.video_bottombar).getLayoutParams();
                layoutParams.height = value;
                findViewById(R.id.video_bottombar).setLayoutParams(layoutParams);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(statusbar_tint != null) {
                    statusbar_tint.setVisibility(View.GONE);
                }
                findViewById(R.id.video_bottombar).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(250);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.start();
            }
        }, 45);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().hide();
        }
    }

    private void playVideo() {
        /*try {
            if(!isPlaying()) {
                handler.postDelayed(hideCtrl, 5000);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
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
        }*/
    }

    private boolean isPlaying() {
        /*try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                    && !Build.CPU_ABI.equals("x86_64")) {
                return imp.isPlaying();
            } else {
                return mp.isPlaying();
            }
        } catch (Exception ex) {
            return false;
        }*/
        return false;
    }

    @SuppressLint("DefaultLocale")
    private void updateControlPanel() {
        /*if(ready) {
            int pos = 0;
            int duration = 0;
            try {
                if(invalid_pos != 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
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
                        } else if (pos > this.duration) {
                            this.duration = 0;
                            if(!overduration) {
                                this.overduration = true;
                                throw new Error(String.format("Incorrect information about the " +
                                                "duration of the video\r\nPosition != Duration: %s > %s",
                                        imp.getCurrentPosition(), imp.getDuration()));
                            }
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
                        } else if (pos > this.duration) {
                            this.duration = 0;
                            throw new Error(String.format("Incorrect information about the " +
                                            "duration of the video\r\nPosition != Duration: %s > %s",
                                    imp.getCurrentPosition(), imp.getDuration()));
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
        }*/
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
        /*ready = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
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
        handler.removeCallbacks(hideCtrl);*/
        super.onDestroy();
    }

    public boolean isError() {
        return isErr;
    }
}
