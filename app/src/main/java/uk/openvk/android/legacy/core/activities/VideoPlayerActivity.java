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

package uk.openvk.android.legacy.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.utils.media.OvkMediaPlayer;

@SuppressWarnings("deprecation")
public class VideoPlayerActivity extends Activity {
    private Video video;
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
                try {
                    video.files = data.getParcelable("files");
                    assert video.files != null;
                } catch (Exception | AssertionError err) {
                    Log.e(OvkApplication.APP_TAG, "Video files not found");
                    finish();
                }
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

//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                intent.setDataAndType(Uri.parse(url), "video/*");
//                startActivity(intent);
//                finish();
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
                        //resizeVideo();
                    }
                });
            }
        } else {
            finish();
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
                    //rescaleVideo(vsv, vsh);
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                    handler.postDelayed(hideCtrl, 5000);
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
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
                    //showPlayControls();
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

    private void setThumbnail() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        thumbnail = BitmapFactory.decodeFile(
                getCacheDir() + "/photos_cache/video_thumbnails/thumbnail_"
                        + video.id + "o" + owner_id, options);
        if(thumbnail != null)
        ((ImageView) findViewById(R.id.video_thumbnail)).setImageBitmap(thumbnail);
    }

    public boolean isError() {
        return isErr;
    }
}
