package uk.openvk.android.legacy.utils.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class OvkMediaPlayer extends MediaPlayer {
    private final Context ctx;
    String MPLAY_TAG = "OVK-MPLAY";

    public static final int FFMPEG_ERROR_EOF = -541478725;
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int MESSAGE_PREPARE = 10000;
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
    private byte[] audio_buffer;
    private OnPreparedListener onPreparedListener;
    private OnErrorListener onErrorListener;
    private OnCompletionListener onCompletionListener;
    private Handler handler;
    private AudioTrack audio_track;

    private native void initFFmpeg();
    private native String showLogo();
    private native Object getTrackInfo(String filename, int type);
    private native Object getTrackInfo2(int type);
    private native int getPlaybackState();
    private native void setPlaybackState(int playbackState);
    private native int openMediaFile(String filename);
    private native int renderFrames(IntBuffer buffer, long frame_number);
    private native void renderAudio(byte[] buffer, int length);
    public native int getLastErrorCode();
    private native void setDebugMode(boolean value);

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
        if(BuildConfig.BUILD_TYPE.equals("release")
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
        Log.v(MPLAY_TAG, showLogo());
        setDebugMode(true);
        initFFmpeg();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == MESSAGE_ERROR) {
                        if (msg.getData() != null && msg.getData().getInt("error_code") < 0)
                            onErrorListener.onError(OvkMediaPlayer.this, msg.getData().getInt("error_code"));
                    } else if (msg.what == MESSAGE_PREPARE) {
                        onPreparedListener.onPrepared(OvkMediaPlayer.this);
                    }
                } catch (Exception ignored) {

                }
                super.handleMessage(msg);
            }
        };
    }


    public ArrayList<OvkMediaTrack> getMediaInfo(String filename) {
        ArrayList<OvkMediaTrack> tracks = new ArrayList<>();
        OvkVideoTrack video_track;
        OvkAudioTrack audio_track;
        if(filename == null) {
            video_track = (OvkVideoTrack) getTrackInfo2(OvkMediaTrack.TYPE_VIDEO);
            if (getLastErrorCode() == FFMPEG_ERROR_EOF) {
                return null;
            }
            audio_track = (OvkAudioTrack) getTrackInfo2(OvkMediaTrack.TYPE_AUDIO);
            if(video_track == null && audio_track == null) {
                return null;
            }
        } else {
            video_track = (OvkVideoTrack) getTrackInfo(filename, OvkMediaTrack.TYPE_VIDEO);
            if (getLastErrorCode() == FFMPEG_ERROR_EOF) {
                return null;
            }
            audio_track = (OvkAudioTrack) getTrackInfo(filename, OvkMediaTrack.TYPE_AUDIO);
            if(video_track == null && audio_track == null) {
                return null;
            }
        }
        tracks.add(video_track);
        tracks.add(audio_track);
        if(audio_track != null) {
            Log.d(MPLAY_TAG,
                    String.format("A: %s, %s Hz, %s bps, %s",
                            audio_track.codec_name, audio_track.sample_rate,
                            audio_track.bitrate, audio_track.channels)
            );
        } if(video_track != null){
            Log.d(MPLAY_TAG,
                    String.format("V: %s, %sx%s, %s bps, %s fps",
                            video_track.codec_name, video_track.frame_size[0],
                            video_track.frame_size[1], video_track.bitrate, video_track.frame_rate)
            );
        }
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
        if(openMediaFile(dataSourceUrl) < 0) {
            Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
            onErrorListener.onError(this, getLastErrorCode());
        } else if(getMediaInfo() == null) {
            Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
            onErrorListener.onError(this, getLastErrorCode());
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
                if(openMediaFile(dataSourceUrl) < 0) {
                    Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
                    Message msg = new Message();
                    msg.what = MESSAGE_ERROR;
                    msg.getData().putInt("error_code", getLastErrorCode());
                    handler.sendMessage(msg);
                    setPlaybackState(STATE_STOPPED);
                } else if(getMediaInfo() == null) {
                    Log.e(MPLAY_TAG, String.format("Can't open file: %s", dataSourceUrl));
                    Message msg = new Message();
                    msg.what = MESSAGE_ERROR;
                    msg.getData().putInt("error_code", getLastErrorCode());
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
        if(getPlaybackState() == STATE_STOPPED || getPlaybackState() == STATE_PAUSED) {
            if(tracks != null) {
                Log.d(MPLAY_TAG, "Playing...");
                setPlaybackState(STATE_PLAYING);
                OvkAudioTrack track = null;
                for(int tracks_index = 0; tracks_index < tracks.size(); tracks_index++) {
                    if(tracks.get(tracks_index) instanceof OvkAudioTrack) {
                        track = (OvkAudioTrack) tracks.get(tracks_index);
                    }
                }
                int ch_config = 0;
                int bufferSize = 0;
                if(track != null) {
                    ch_config = track.channels == 2 ?
                            AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
                    bufferSize = AudioTrack.getMinBufferSize((int) track.sample_rate, ch_config,
                            AudioFormat.ENCODING_PCM_16BIT);
                }
                final int finalBufferSize = bufferSize;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(tracks.size() == 1) {
                            if (tracks.get(0) instanceof OvkVideoTrack) {
                                startRenderingFrames();
                            } else if (tracks.get(0) instanceof OvkAudioTrack) {
                                //Log.d(MPLAY_TAG, "Decoding audio...");
                                renderAudio(audio_buffer, finalBufferSize);
                            }
                        } else if(tracks.size() == 2) {
                            if (tracks.get(0) instanceof OvkVideoTrack) {
                                startRenderingFrames();
                            }
                            if (tracks.get(1) instanceof OvkAudioTrack) {
                                renderAudio(audio_buffer, finalBufferSize);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void decodeAudio(final byte[] buffer, final int length) {
        int state = getPlaybackState();
        if(state == STATE_PLAYING) {
            this.audio_buffer = buffer;
            OvkAudioTrack track = null;
            Log.d(MPLAY_TAG, "Checking audio buffer...");
            if (audio_buffer == null) {
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
                int bufferSize = AudioTrack.getMinBufferSize((int) track.sample_rate, ch_config,
                        AudioFormat.ENCODING_PCM_16BIT);

                audio_track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) track.sample_rate,
                        ch_config,
                        AudioFormat.ENCODING_PCM_16BIT, length * 2, AudioTrack.MODE_STREAM);

                audio_track.play();
                prepared_audio_buffer = true;
            }
            Log.d(MPLAY_TAG, "Playing sound... [" + audio_track + "]");
            audio_track.write(buffer, 0, buffer.length);
        } else if(state == STATE_STOPPED) {
            if(audio_track != null) {
                audio_track.stop();
            }
            prepared_audio_buffer = false;
        } else {
            if(audio_track != null) {
                audio_track.pause();
            }
        }
    }

    private void completePlayback() {
        setPlaybackState(STATE_STOPPED);
        if(audio_track != null) {
            audio_track.stop();
        }
        if(onCompletionListener != null) {
            onCompletionListener.onCompleted(this);
        }
    }

    private void startRenderingFrames() {
        new Thread(new Runnable() {
            @SuppressWarnings("Since15")
            @Override
            public void run() {
                Canvas c;
                OvkVideoTrack videoTrack = (OvkVideoTrack) tracks.get(0);
                int frame_width = videoTrack.frame_size[0];
                int frame_height = videoTrack.frame_size[1];
                if(frame_width > 0 && frame_height > 0) {
                    // RGB_565  == 65K colours (16 bit)
                    // RGB_8888 == 16.7M colours (24 bit w/ alpha ch.)
                    int bpp = Build.VERSION.SDK_INT > 9 ? 16 : 24;
                    IntBuffer int_buf = ByteBuffer.allocateDirect(
                            frame_width * frame_height * bpp
                    ).asIntBuffer();
                    Bitmap.Config bmp_config =
                            bpp == 24 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                    Bitmap bmp = Bitmap.createBitmap(frame_width, frame_height, bmp_config);
                    float scaleFactor = ctx.getResources().getDisplayMetrics().heightPixels
                            / frame_height;
                    Paint bitmap_paint = new Paint();
                    bitmap_paint.setFilterBitmap(true);
                    Rect srcRect =
                            new Rect(0, 0, frame_width, frame_height);
                    new Timer().scheduleAtFixedRate(getFpsTimerTask, 1000L, 1000L);
                    Paint fpaint = new Paint();
                    fpaint.setTextSize(15);
                    fpaint.setColor(Color.parseColor("#222222"));
                    try {
                        while (getPlaybackState() == STATE_PLAYING) {
                            if (holder != null && holder.lockCanvas() != null) {
                                int_buf.rewind();
                                renderFrames(int_buf, frames_count++);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean isPlaying() {
        return (getPlaybackState() == STATE_PLAYING);
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
