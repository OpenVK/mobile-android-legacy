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

package uk.openvk.android.legacy.utils.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;

public class OvkMediaPlayer extends MediaPlayer {
    private final Context ctx;
    String MPLAY_TAG = "OVK-MPLAY";

    public static final int FFMPEG_ERROR_EOF = -541478725;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int MESSAGE_PREPARE = 10000;
    public static final int MESSAGE_COMPLETE = 10001;
    public static final int MESSAGE_ERROR = -10000;
    public static final int MESSAGE_AUDIO_DECODING = 100;
    boolean prepared_audio_buffer;
    private int audio_buffer_read_pos = 0;
    private int audio_buffer_write_pos = 0;
    private int audio_delay = 0;
    private int last_audio_delay_upd;
    private long frames_count;
    private String dataSourceUrl;
    private ArrayList<OvkMediaTrack> tracks;
    private float current_frame_rate;
    private TimerTask getFpsTimerTask = new TimerTask() {
        @Override
        public void run() {
            if(tracks != null && tracks.size() > 0) {
                if(tracks.get(0) instanceof OvkVideoTrack) {
                    OvkVideoTrack video_track = (OvkVideoTrack) tracks.get(0);
                    current_frame_rate = video_track.frame_rate;
                }
            }
        }
    };
    private SurfaceHolder holder;
    private int minAudioBufferSize;
    private int minVideoBufferSize;
    private byte[] audio_buffer;
    private byte[] video_buffer;
    private OnPreparedListener onPreparedListener;
    private OnErrorListener onErrorListener;
    private OnCompletionListener onCompletionListener;
    private Handler handler;
    private AudioTrack audio_track;
    private final Object frameLocker = new Object();
    private native void naInit();
    private native String naShowLogo();
    private native Object naGenerateTrackInfo(int type);
    private native int naOpenFile(String filename);
    private native int naPlay();
    private native void naSetDebugMode(boolean value);

    public static interface OnPreparedListener {
        public void onPrepared(OvkMediaPlayer mp);
    }

    public static interface OnErrorListener {
        public boolean onError(OvkMediaPlayer mp, int what);
    }

    public static interface OnCompletionListener {
        public void onCompleted(OvkMediaPlayer mp);
    }

    @SuppressLint({"UnsafeDynamicallyLoadedCode", "SdCardPath"})
    private static void loadLibrary(Context ctx, String name) {
        if(BuildConfig.DEBUG
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            System.loadLibrary(String.format("%s", name));
        } else {
            // unsafe but changeable
            System.load(String.format("/data/data/%s/lib/lib%s.so", ctx.getPackageName(), name));
        }
    }

    public OvkMediaPlayer(Context ctx) {
        this.ctx = ctx;
        loadLibrary(ctx, "ffmpeg");
        loadLibrary(ctx, "ovkmplayer");
        Log.v(MPLAY_TAG, naShowLogo());
        naSetDebugMode(true);
        naInit();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == MESSAGE_ERROR) {
                        if (msg.getData() != null && msg.getData().getInt("error_code") < 0)
                            onErrorListener.onError(OvkMediaPlayer.this, msg.getData().getInt("error_code"));
                    } else if (msg.what == MESSAGE_PREPARE) {
                        onPreparedListener.onPrepared(OvkMediaPlayer.this);
                    } else if(msg.what == MESSAGE_COMPLETE) {
                        //setPlaybackState(STATE_STOPPED);
                        if(audio_track != null) {
                            audio_track.stop();
                        }
                        if(onCompletionListener != null) {
                            onCompletionListener.onCompleted(OvkMediaPlayer.this);
                        }
                    }
                } catch (Exception ignored) {

                }
                super.handleMessage(msg);
            }
        };
    }


    @SuppressWarnings("MalformedFormatString")
    public ArrayList<OvkMediaTrack> getMediaInfo(String filename) {
        ArrayList<OvkMediaTrack> tracks = new ArrayList<>();
        OvkVideoTrack video_track;
        OvkAudioTrack audio_track;
        if(filename == null) {
            video_track = (OvkVideoTrack) naGenerateTrackInfo(OvkMediaTrack.TYPE_VIDEO);
            audio_track = (OvkAudioTrack) naGenerateTrackInfo(OvkMediaTrack.TYPE_AUDIO);
            if(video_track == null && audio_track == null) {
                return null;
            }
        } else {
            naOpenFile(filename);
            video_track = (OvkVideoTrack) naGenerateTrackInfo(OvkMediaTrack.TYPE_VIDEO);
            audio_track = (OvkAudioTrack) naGenerateTrackInfo(OvkMediaTrack.TYPE_AUDIO);
            if(video_track == null && audio_track == null) {
                return null;
            }
        }


        if(audio_track != null) {
            Log.d(MPLAY_TAG,
                    String.format("A: %s, %s Hz, %s bps, %s",
                            audio_track.codec_name, audio_track.sample_rate,
                            audio_track.bitrate, audio_track.channels)
            );
            tracks.add(audio_track);
        }
        if(video_track != null) {
            Log.d(MPLAY_TAG,
                    String.format("V: %s, %.2f MHz, %sx%s, %s bps, %s fps",
                            video_track.codec_name, ((double)video_track.sample_rate / 1000 / 1000),
                            video_track.frame_size[0],
                            video_track.frame_size[1], video_track.bitrate, video_track.frame_rate)
            );
            tracks.add(video_track);
        }
        this.tracks = tracks;
        return tracks;
    }

    public ArrayList<OvkMediaTrack> getMediaInfo() {
        if(tracks == null) {
            tracks = getMediaInfo(null);
        }
        return tracks;
    }

    public void setDataSource(String url) {
        this.dataSourceUrl = url;
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        this.holder = sh;
        //super.setDisplay(sh);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        int result = 0;
        if((result = naOpenFile(dataSourceUrl)) < 0) {
            Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
            onErrorListener.onError(this, result);
        } else if(getMediaInfo() == null) {
            Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
            onErrorListener.onError(this, -1);
        } else {
            getMediaInfo();
            onPreparedListener.onPrepared(this);
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result;
                if((result = naOpenFile(dataSourceUrl)) < 0) {
                    Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
                    Message msg = new Message();
                    msg.what = MESSAGE_ERROR;
                    msg.getData().putInt("error_code", result);
                    handler.sendMessage(msg);
                    //setPlaybackState(STATE_STOPPED);
                } else if(getMediaInfo() == null) {
                    Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
                    Message msg = new Message();
                    msg.what = MESSAGE_ERROR;
                    msg.getData().putInt("error_code", -1);
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = MESSAGE_PREPARE;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    @Override
    public void start() throws IllegalStateException {
        if(tracks != null) {
            Log.d(MPLAY_TAG, "Playing...");
            //setPlaybackState(STATE_PLAYING);
            OvkAudioTrack audio_track = null;
            OvkVideoTrack video_track = null;
            for(int tracks_index = 0; tracks_index < tracks.size(); tracks_index++) {
                if(tracks.get(tracks_index) instanceof OvkAudioTrack) {
                    audio_track = (OvkAudioTrack) tracks.get(tracks_index);
                } else if(tracks.get(tracks_index) instanceof OvkVideoTrack) {
                    video_track = (OvkVideoTrack) tracks.get(tracks_index);
                }
            }
            int ch_config = 0;
            int bpp = Integer.parseInt(Build.VERSION.SDK) > 9 ? 16 : 24;
            if(audio_track != null) {
                ch_config = audio_track.channels == 2 ?
                        AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
                minAudioBufferSize = AudioTrack.getMinBufferSize((int) audio_track.sample_rate, ch_config,
                        AudioFormat.ENCODING_PCM_16BIT);
            }
            if(video_track != null) {
                minVideoBufferSize = video_track.frame_size[0] * video_track.frame_size[1] * bpp;
            }
            final int finalAudioBufferSize = minAudioBufferSize;
            final int finalVideoBufferSize = minVideoBufferSize;
            final OvkVideoTrack finalVideo_track = video_track;
            final OvkAudioTrack finalAudio_track = audio_track;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(finalAudio_track != null) {
                        Log.d(MPLAY_TAG, "Decoding media file...");
                        try {
                            naPlay();
                        } catch (OutOfMemoryError oom) {
                            stop();
                        }
                    } else {
                        Log.e(MPLAY_TAG, "Audio track not found. Skipping...");
                    }
                }
            }).start();
        }
    }

    @SuppressWarnings("deprecation")
    private void renderAudio(final byte[] buffer, final int length) {
        OvkAudioTrack track = null;
        Log.d(MPLAY_TAG, "Checking audio buffer...");
        if (buffer == null) {
            Log.e(MPLAY_TAG, "Audio buffer is empty");
            return;
        }
        if (!prepared_audio_buffer) {
            Log.d(MPLAY_TAG, "Checking audio track...");
            for (int tracks_index = 0; tracks_index < tracks.size(); tracks_index++) {
                if (tracks.get(tracks_index) instanceof OvkAudioTrack) {
                    track = (OvkAudioTrack) tracks.get(tracks_index);
                }
            }
            if (track == null) {
                Log.e(MPLAY_TAG, "Audio track not found");
                return;
            }
            int ch_config = track.channels == 2 ?
                    AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;

            audio_track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) track.sample_rate,
                    ch_config,
                    AudioFormat.ENCODING_PCM_16BIT, length * 2, AudioTrack.MODE_STREAM);

            audio_track.play();
            prepared_audio_buffer = true;
        }
        Log.d(MPLAY_TAG, "Playing sound... [" + audio_track + "]");
        try {
            audio_track.write(buffer, 0, buffer.length);
        } catch (Exception ignored) {
        }
    }

    private void completePlayback() {
        handler.sendEmptyMessage(MESSAGE_COMPLETE);
    }

    private void renderVideoFrames(final byte[] buffer, final int length) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Canvas c;
                OvkVideoTrack track = null;
                for (int tracks_index = 0; tracks_index < tracks.size(); tracks_index++) {
                    if (tracks.get(tracks_index) instanceof OvkVideoTrack) {
                        track = (OvkVideoTrack) tracks.get(tracks_index);
                    }
                }
                if (track != null) {
                    int frame_width = track.frame_size[0];
                    int frame_height = track.frame_size[1];
                    if (frame_width > 0 && frame_height > 0) {
                        try {
                            // RGB_565  == 65K colours (16 bit)
                            // RGB_8888 == 16.7M colours (24 bit w/ alpha ch.)
                            int bpp = Build.VERSION.SDK_INT > 9 ? 16 : 24;
                            Bitmap.Config bmp_config =
                                    bpp == 24 ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
                            Paint paint = new Paint();
                            if(buffer != null && holder != null) {
                                holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
                                if((c = holder.lockCanvas()) == null) {
                                    Log.d(MPLAY_TAG, "Lock canvas failed");
                                    return;
                                }
                                ByteBuffer bbuf =
                                        ByteBuffer.allocateDirect(minVideoBufferSize);
                                bbuf.rewind();
                                for(int i = 0; i < buffer.length; i++) {
                                    bbuf.put(i, buffer[i]);
                                }
                                bbuf.rewind();
                                Bitmap bmp = Bitmap.createBitmap(frame_width, frame_height, bmp_config);
                                bmp.copyPixelsFromBuffer(bbuf);
                                float aspect_ratio = (float) frame_width / (float) frame_height;
                                int scaled_width = (int)(aspect_ratio * (c.getHeight()));
                                c.drawBitmap(bmp,
                                        null,
                                        new RectF(
                                                ((c.getWidth() - scaled_width) / 2), 0,
                                                ((c.getWidth() - scaled_width) / 2) + scaled_width,
                                                c.getHeight()),
                                        null);
                                holder.unlockCanvasAndPost(c);
                                bmp.recycle();
                                bbuf.clear();
                            } else {
                                Log.d(MPLAY_TAG, "Video frame buffer is null");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } catch (OutOfMemoryError oom) {
                            oom.printStackTrace();
                            stop();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        //return (getPlaybackState() == STATE_PLAYING);
        return false;
    }

    public void setOnPreparedListener(OvkMediaPlayer.OnPreparedListener listener) {
        this.onPreparedListener = listener;
    }

    public void setOnErrorListener(OvkMediaPlayer.OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    public void setOnCompletionListener(OvkMediaPlayer.OnCompletionListener listener) {
        this.onCompletionListener = listener;
    }
}
