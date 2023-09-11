/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This file is part of OpenVK Legacy.
 *
 * OpenVK Legacy is free software: you can redistribute it and/or modify it under the terms of
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

// Java/C++ standard implementations headers
#include <jni.h>
#include <stdio.h>

// Android implementations headers
#include <android/log.h>

// FFmpeg implementation headers (using LGPLv3.0 model)
extern "C" {
    #include <libavutil/avstring.h>
    #include <libavutil/pixdesc.h>
    #include <libavutil/imgutils.h>
    #include <libavutil/samplefmt.h>
    #include <libavformat/avformat.h>
    #include <libavformat/url.h>
    #include <libavformat/avio.h>
    #include <libswscale/swscale.h>
    #include <libavcodec/avcodec.h>
    #include <libavcodec/avfft.h>
    #include <libavdevice/avdevice.h>
}

/*for Android logs*/
#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

void ffmpeg_log_callback(void *ptr, int level, const char *fmt, va_list vargs)
{
    LOGD(10, fmt, vargs);
}

char version[7] = "0.0.1";
char *gFileName;	      //file name of the video
int gErrorCode;

AVFormatContext *gFormatCtx;
AVFormatContext *gTempFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

AVDictionary *avFormatOptions = NULL;
AVDictionary *avCodecOptions = NULL;

bool debug_mode;

jint g_playbackState;
jint FFMPEG_PLAYBACK_STOPPED = 0;
jint FFMPEG_PLAYBACK_PLAYING = 1;
jint FFMPEG_PLAYBACK_PAUSED = 2;

jlong gFrameCount;

AVCodec *gVideoCodec;
AVCodec *gAudioCodec;

jobject generateTrackInfo(JNIEnv* env,
                          AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);

void decodeVideoFromPacket(JNIEnv *env, jobject instance,
                           jclass mplayer_class, AVPacket avpkt, int total_frames, int length);

void decodeAudioFromPacket(
        JNIEnv *pEnv, jobject pJobject, jclass mplayer_class, AVPacket packet,
        short* buffer, int i, int length);

unsigned char* convertYuv2Rgb(AVPixelFormat pxf, AVFrame* frame, int length);

extern "C" {
    JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_initFFmpeg(JNIEnv *env, jobject instance) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Initializing FFmpeg...");
        }
        av_register_all();
        avcodec_register_all();
        avformat_network_init();
        gFormatCtx = avformat_alloc_context();
    }

    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }

    JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_setDebugMode(
            JNIEnv *env, jobject instance, jboolean value
    ) {
        if(value == JNI_TRUE) {
            debug_mode = true;
            av_log_set_level(AV_LOG_VERBOSE);
            av_log_set_callback(ffmpeg_log_callback);
            LOGD(10, "[DEBUG] Enabled Debug Mode");
        } else {
            LOGD(10, "[DEBUG] Disabled Debug Mode");
        }
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile(
            JNIEnv *env, jobject instance,
            jstring filename_) {
        if(filename_ == NULL) {
            LOGE(1, "[ERROR] Invalid filename");
            g_playbackState = 0;
            return -10;
        }
        const char *filename = env->GetStringUTFChars(filename_, 0);
        gFileName = (char *) filename;
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening file %s...", filename);
        }
        if ((gErrorCode = avformat_open_input(&gFormatCtx, filename, NULL, NULL)) != 0) {
            if(debug_mode) {
                char error_string[192];
                if(av_strerror(gErrorCode, error_string, 192) < 0) {
                    strerror_r(-gErrorCode, error_string, 192);
                }
                LOGE(1, "[ERROR] Can't open file: %d (%s)", gErrorCode, error_string);
            }
            g_playbackState = 0;
            return gErrorCode;    //open file failed
        }
        /*retrieve stream information*/
        if ((gErrorCode = avformat_find_stream_info(gFormatCtx, NULL)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            g_playbackState = 0;
            return gErrorCode;
        }
    }

    AVFormatContext* openTempFile(char* filename) {
        if(filename == NULL) {
            LOGE(1, "[ERROR] Invalid filename");
            return NULL;
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening temporary file %s...", filename);
        }
        if ((gErrorCode = avformat_open_input(&gTempFormatCtx, filename, NULL, 0)) != 0) {
            char error_string[192];
            if(gErrorCode == -2) {
                sprintf(error_string, "File not found");
            } else {
                if (av_strerror(gErrorCode, error_string, 192) < 0) {
                    strerror_r(-gErrorCode, error_string, 192);
                }
            }
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't open file: %d (%s)", gErrorCode, error_string);
            }
            return NULL;    //open file failed
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Searching A/V streams...");
        }
        /*retrieve stream information*/
        if ((gErrorCode = avformat_find_stream_info(gTempFormatCtx, NULL)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            return NULL;
        }
        return gTempFormatCtx;
    }

    JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_decodeMedia
            (JNIEnv *env, jobject instance, jint audio_length, jint video_length) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Decoding audio stream #%d and video stream #%d...",
                 gAudioStreamIndex, gVideoStreamIndex)
        }
        jclass mplayer_class = env->GetObjectClass(instance);
        jmethodID completePlayback = env->GetMethodID(mplayer_class, "completePlayback", "()V");
        AVPacket avpkt;
        short *audio_buf, input_buf[4096 + FF_INPUT_BUFFER_PADDING_SIZE];
        int received_frame = 0;
        int total_audio_frames = 0;
        int total_video_frames = 0;
        audio_buf = (short*)malloc(192000 * 4);
        if(debug_mode) {
            LOGD(10, "[DEBUG] AVPacket initializing...")
        }
        av_init_packet(&avpkt);
        if(!gAudioCodec) {
            LOGE(10, "[ERROR] Audio codec not found.");
        }
        if(!gVideoCodec) {
            LOGE(10, "[ERROR] Video codec not found.");
        }
        if(!gAudioCodec && !gVideoCodec) {
            return;
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Output buffer allocating...");
        }

        int read_frame_status = -1;
        while ((read_frame_status = av_read_frame(gFormatCtx, &avpkt)) >= 0) {
            if (avpkt.stream_index == gAudioStreamIndex) {
                decodeAudioFromPacket(
                        env, instance, mplayer_class,
                        avpkt, audio_buf, total_audio_frames++, audio_length);
            } else if(avpkt.stream_index == gVideoStreamIndex) {
                decodeVideoFromPacket(env, instance, mplayer_class, avpkt, total_video_frames++, video_length);
            }
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Decoding result:\r\nTotal audio frames: %d | Total video frames: %d",
                 total_audio_frames, total_video_frames);
        }
        env->CallVoidMethod(instance, completePlayback);
        env->DeleteLocalRef(mplayer_class);
    }

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo(
            JNIEnv *env, jobject instance,
            jstring filename_,
            jint type) {
        const char *filename = env->GetStringUTFChars(filename_, 0);
        if(gFileName == NULL) {
            gFileName = (char *) filename;
            if(Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile
                       (env, instance, filename_) < 0) {
                return NULL;
            }
        }
        if (type == 0) {
            AVCodec *lVideoCodec;
            /*some global variables initialization*/
            if(debug_mode) {
                LOGI(10, "[DEBUG] Getting video track info...");
            }

            /*find the video stream and its decoder*/
            gVideoStreamIndex = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                    &lVideoCodec,
                                                    0);
            if (gVideoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Cannot find a video stream");
                }
                g_playbackState = 0;
                return NULL;
            } else {
                if(debug_mode) {
                    LOGI(10, "[INFO] Video codec: %s", lVideoCodec->name);
                }
            }
            if (gVideoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Video stream found, but '%s' decoder is unavailable.",
                         lVideoCodec->name);
                }
                g_playbackState = 0;
                return NULL;
            }

            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d |  Video stream #%d detected. Opening...",
                     gFormatCtx->nb_streams, gVideoStreamIndex + 1);
            }

            /*open the codec*/
            gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
            if(debug_mode) {
                LOGI(10, "[INFO] Frame size: %dx%d", gVideoCodecCtx->height, gVideoCodecCtx->width);
            }
            #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open2(gVideoCodecCtx, lVideoCodec, NULL) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the video codec!");
                }
                g_playbackState = 0;
                return NULL;
            }
            return generateTrackInfo(
                    env, gFormatCtx->streams[gVideoStreamIndex],
                    lVideoCodec, gVideoCodecCtx, AVMEDIA_TYPE_VIDEO
            );
        } else {
            AVCodec *lAudioCodec;
            if(debug_mode) {
                LOGI(10, "[DEBUG] Getting audio track info...");
            }

            /*find the video stream and its decoder*/
            gAudioStreamIndex = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1,
                                                    &lAudioCodec,
                                                    0);

            if (gAudioStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Cannot find a audio stream");
                }
                return NULL;
            }

            if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Audio stream found, but '%s' decoder is unavailable.",
                         lAudioCodec->name);
                }
                return NULL;
            }

            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d |  Audio stream #%d detected. Opening...",
                     gFormatCtx->nb_streams, gAudioStreamIndex + 1);
            }

            /*open the codec*/
            gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIndex]->codec;
            #ifdef SELECTIVE_DECODING
                    gAudioCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open2(gAudioCodecCtx, lAudioCodec, NULL) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the audio codec!");
                }
                return NULL;
            }
            return generateTrackInfo(
                    env, gFormatCtx->streams[gAudioStreamIndex],
                    lAudioCodec, gAudioCodecCtx, AVMEDIA_TYPE_AUDIO
            );
        }

        env->ReleaseStringUTFChars(filename_, filename);
    };

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo2(
            JNIEnv *env, jobject instance,
            jint type) {

        const char *filename;
        try {
            if (type == 0) {
                /*some global variables initialization*/
                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting video track info...");
                }

                /*find the video stream and its decoder*/
                gVideoStreamIndex = -1;
                for(int i = 0; i < gFormatCtx->nb_streams; i++) {
                    if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                        gVideoStreamIndex = i;
                        gVideoCodecCtx = gFormatCtx->streams[i]->codec;
                        if (debug_mode) {
                            LOGD(10, "[DEBUG] Total streams: %d | Video stream #%d detected. Opening...",
                                 gFormatCtx->nb_streams, gVideoStreamIndex);
                        }
                    }
                }

                if (gVideoStreamIndex < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a video stream");
                    }
                    return NULL;
                }

                /*open the codec*/
                gVideoCodec = avcodec_find_decoder(
                        gFormatCtx->streams[gVideoStreamIndex]->codec->codec_id);
                LOGI(10, "[INFO] Codec initialized. Reading...");
                #ifdef SELECTIVE_DECODING
                gVideoCodecCtx->allow_selective_decoding = 1;
                #endif
                if (avcodec_open2(gVideoCodecCtx, gVideoCodec, NULL) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the video codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gFormatCtx->streams[gVideoStreamIndex],
                        gVideoCodec, gVideoCodecCtx, AVMEDIA_TYPE_VIDEO
                );
            } else {

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting audio track info...");
                }

                /*find the audio stream and its decoder*/
                gAudioStreamIndex = -1;
                for(int i = 0; i < gFormatCtx->nb_streams; i++) {
                    if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
                        gAudioStreamIndex = i;
                        gAudioCodecCtx = gFormatCtx->streams[i]->codec;
                        if (debug_mode) {
                            LOGD(10, "[DEBUG] Total streams: %d | Audio stream #%d detected. Opening...",
                                 gFormatCtx->nb_streams, gAudioStreamIndex);
                        }
                    }
                }

                if (gAudioStreamIndex < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a audio stream");
                    }
                    return NULL;
                }

                if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Audio stream found, but '%s' decoder is unavailable.",
                             gAudioCodec->name);
                    }
                    return NULL;
                }

                /*open the codec*/
                gAudioCodec = avcodec_find_decoder(
                        gFormatCtx->streams[gAudioStreamIndex]->codec->codec_id);
                LOGI(10, "[INFO] Codec initialized. Reading...");
    #ifdef SELECTIVE_DECODING
                gAudioCodecCtx->allow_selective_decoding = 1;
    #endif
                if (avcodec_open2(gAudioCodecCtx, gAudioCodec, NULL) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the audio codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gFormatCtx->streams[gAudioStreamIndex],
                        gAudioCodec, gAudioCodecCtx, AVMEDIA_TYPE_AUDIO
                );
            }
        } catch (const char* error_message) {
            return NULL;
        }
    };

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_setPlaybackState
            (JNIEnv *env, jobject instance, jint state) {
        g_playbackState = state;
        if(state == FFMPEG_PLAYBACK_PLAYING) {
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Playing\"...");
            }
        } else if(state == FFMPEG_PLAYBACK_PAUSED){
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Paused\"...");
            }
        } else if(state == FFMPEG_PLAYBACK_STOPPED) {
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Stopped\"...");
            }
        }
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getPlaybackState
                                                (JNIEnv *env, jobject instance) {
        return g_playbackState;
    }

    JNIEXPORT jlong JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getFrameCount(
            JNIEnv *env, jobject instance
    ) {
        return gFrameCount;
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorCode(
            JNIEnv *env, jobject instance
    ) {
        return gErrorCode;
    }

    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorString(
            JNIEnv *env, jobject instance
    ) {
        char error_str[192];
        av_strerror(gErrorCode, error_str, 192);
        return env->NewStringUTF(error_str);
    }
}

jobject generateTrackInfo(
        JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type
) {
    // JNI field types (bad stuff, don't you agree?)
    // [JNI] => [java]
    // "[?"  => ?[] == int[], bool[], long[] and etc.
    // "I"   => int
    // "J"   => long (aka. int64)
    // "Z"   => boolean
    // "D"   => double
    // "F"   => float

    jclass track_class;
    jobject track;
    try {
        if (type == AVMEDIA_TYPE_VIDEO) {
            // Load OvkVideoTrack class
            track_class = env->FindClass(
                    "uk/openvk/android/legacy/utils/media/OvkVideoTrack"
            );

            // Load OvkVideoTrack class methods
            jmethodID video_track_init = env->GetMethodID(
                    track_class, "<init>", "()V"
            );
            jfieldID codec_name_field = env->GetFieldID(
                    track_class, "codec_name", "Ljava/lang/String;"
            );
            jfieldID frame_size_field = env->GetFieldID(track_class, "frame_size", "[I");
            jfieldID bitrate_field = env->GetFieldID(
                    track_class, "bitrate", "J"
            );
            jfieldID frame_rate_field = env->GetFieldID(
                    track_class, "frame_rate", "F"
            );
            jfieldID sample_rate_field = env->GetFieldID(
                    track_class, "sample_rate", "J"
            );

            track = env->NewObject(track_class, video_track_init);

            // Load OvkVideoTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            jintArray array = (jintArray) env->GetObjectField(track, frame_size_field);
            jint *frame_size = env->GetIntArrayElements(array, 0);
            frame_size[0] = pCodecCtx->width;
            frame_size[1] = pCodecCtx->height;
            env->ReleaseIntArrayElements(array, frame_size, 0);
            env->SetLongField(track, bitrate_field, pCodecCtx->bit_rate);

            // Referenced from:
            // https://en.wikipedia.org/wiki/Rec.709
            // https://en.wikipedia.org/wiki/Sampling_(signal_processing)#Applications
            //
            // 1920x1080@60fps = 1920 (active) lpf * 1080 spl * 60 fps = 124.416 MHz
            long sample_rate = pCodecCtx->width * pCodecCtx->height * pStream->avg_frame_rate.num;
            env->SetLongField(track, sample_rate_field, sample_rate);

            env->SetFloatField(track, frame_rate_field, pStream->avg_frame_rate.num);
        } else {
            // Load OvkAudioTrack class
            track_class = env->FindClass(
                    "uk/openvk/android/legacy/utils/media/OvkAudioTrack"
            );
            // Load OvkVideoTrack class methods
            jmethodID audio_track_init = env->GetMethodID(
                    track_class, "<init>", "()V"
            );
            jfieldID codec_name_field = env->GetFieldID(
                    track_class, "codec_name", "Ljava/lang/String;"
            );
            jfieldID bitrate_field = env->GetFieldID(
                    track_class, "bitrate", "J"
            );
            jfieldID sample_rate_field = env->GetFieldID(
                    track_class, "sample_rate", "J"
            );
            jfieldID channels_field = env->GetFieldID(
                    track_class, "channels", "I"
            );

            track = env->NewObject(track_class, audio_track_init);

            // Load OvkAudioTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            env->SetLongField(track, sample_rate_field, pCodecCtx->sample_rate);
            env->SetLongField(track, bitrate_field, pCodecCtx->bit_rate);
            env->SetIntField(track, channels_field, pCodecCtx->channels);
        }
    } catch (...) {
        if(debug_mode) {
            LOGE(1, "[ERROR] Track not found");
        }
        return NULL;
    }
    return track;
};

void decodeAudioFromPacket(JNIEnv *env, jobject instance, jclass mplayer_class, AVPacket avpkt,
                           short* buffer, int total_frames, int length) {
    jbyteArray buffer2;
    jmethodID renderAudio = env->GetMethodID(mplayer_class, "renderAudio", "([BI)V");
    int size = avpkt.size;
    int received_frame = 0;
    int decoded_frame;
    int audio_linesize;
    AVFrame *pFrame = av_frame_alloc();
    int ret = avcodec_decode_audio4(gAudioCodecCtx,
                                    pFrame,
                                    &decoded_frame,
                                    &avpkt);
    if (decoded_frame) {
        int data_size = av_samples_get_buffer_size(NULL,
                                                   gAudioCodecCtx->channels,
                                                   pFrame->nb_samples,
                                                   gAudioCodecCtx->sample_fmt,
                                                   1);
        if (data_size < 0) {
            data_size = 192000 * 4;
        }
        buffer = (short *) pFrame->data[0];
        buffer2 = env->NewByteArray((jsize) data_size);
        env->SetByteArrayRegion(buffer2, 0, (jsize) data_size, (jbyte *) buffer);
        env->CallVoidMethod(instance, renderAudio, buffer2, length);
        env->DeleteLocalRef(buffer2);
    }
    total_frames++;
}

void decodeVideoFromPacket(JNIEnv *env, jobject instance,
                           jclass mplayer_class, AVPacket avpkt, int total_frames, int length) {
    AVFrame *pFrame = NULL, *pFrameRGB = NULL;
    pFrame = av_frame_alloc();
    pFrameRGB = av_frame_alloc();
    int frame_size = avpicture_get_size(AV_PIX_FMT_RGB32, gVideoCodecCtx->width, gVideoCodecCtx->height);
    unsigned char* buffer = (unsigned char*)av_malloc((size_t)frame_size * 3);
    if (!buffer) {
        av_free(pFrame);
        av_free(pFrameRGB);
        return;
    }
    jbyteArray buffer2;
    jmethodID renderVideoFrames = env->GetMethodID(mplayer_class, "renderVideoFrames", "([BI)V");
    int frameDecoded;
    avpicture_fill((AVPicture*) pFrame,
                   buffer,
                   gVideoCodecCtx->pix_fmt,
                   gVideoCodecCtx->width,
                   gVideoCodecCtx->height);
    if (avpkt.stream_index == gVideoStreamIndex) {
        int size = avpkt.size;
        if (debug_mode) {
            LOGD(10, "[DEBUG] Decoding video frame #%d... | Length: %d", total_frames,
                 avpkt.size);
        }
        total_frames++;
        struct SwsContext *img_convert_ctx = NULL;
        avcodec_decode_video2(gVideoCodecCtx, pFrame, &frameDecoded, &avpkt);
        if (!frameDecoded || pFrame == NULL) {
            if (debug_mode) {
                LOGE(10, "[ERROR] Frame #%d not decoded.", total_frames - 1);
            }
            return;
        }

        try {
            AVPixelFormat pxf;
            // RGB565 by default for Android Canvas in pre-Gingerbread devices.
            pxf = AV_PIX_FMT_BGR32;

            int rgbBytes = avpicture_get_size(pxf, gVideoCodecCtx->width,
                                              gVideoCodecCtx->height);

            if (debug_mode) {
                LOGD(10, "[DEBUG] Converting video frame to RGB...");
            }

            buffer = convertYuv2Rgb(pxf, pFrame, rgbBytes);

            if(buffer == NULL) {
                LOGE(10, "[ERROR] Conversion failed");
                return;
            }

            if (debug_mode) {
                LOGD(10, "[DEBUG] Calling renderVideoFrames method to OvkMediaPlayer...");
            }
            buffer2 = env->NewByteArray((jsize) rgbBytes);
            env->SetByteArrayRegion(buffer2, 0, (jsize) rgbBytes,
                                    (jbyte *) buffer);
            env->CallVoidMethod(instance, renderVideoFrames, buffer2, rgbBytes);
            env->DeleteLocalRef(buffer2);
            free(buffer);
        } catch (...) {
            if (debug_mode) {
                LOGE(10, "[ERROR] Render video frames failed");
                return;
            }
        }
    }
}

unsigned char* convertYuv2Rgb(AVPixelFormat pxf, AVFrame* frame, int length) {
    unsigned char *buffer = (unsigned char*) malloc((size_t)length);
    AVFrame* frameRGB = frameRGB = av_frame_alloc();

    AVPixelFormat output_pxf = pxf;

    avpicture_fill((AVPicture *)frameRGB, (uint8_t*)buffer, output_pxf,
                   gVideoCodecCtx->width, gVideoCodecCtx->height);
    SwsContext* sws_ctx = sws_getContext
            (
                    gVideoCodecCtx->width,
                    gVideoCodecCtx->height,
                    gVideoCodecCtx->pix_fmt,
                    gVideoCodecCtx->width,
                    gVideoCodecCtx->height,
                    output_pxf,
                    SWS_BILINEAR,
                    NULL,
                    NULL,
                    NULL
            );
    const int width = gVideoCodecCtx->width, height = gVideoCodecCtx->height;
    SwsContext* img_convert_ctx = sws_getContext(width, height,
                                                 gVideoCodecCtx->pix_fmt,
                                                 width, height, output_pxf, SWS_BICUBIC,
                                                 NULL, NULL, NULL);


    if(img_convert_ctx == NULL) {
        LOGE(10, "Cannot initialize the conversion context!");
        return NULL;
    }
    int Y, Cr, Cb;
    int R, G, B;

    LOGD(10, "Running SWS_Scale...");
    int ret = sws_scale(img_convert_ctx, (const uint8_t* const*)frame->data, frame->linesize, 0,
                        gVideoCodecCtx->height, frameRGB->data, frameRGB->linesize);
    if(frameRGB->data[0] == NULL) {
        LOGE(10, "SWS_Scale failed");
    } else {
        LOGD(10, "SWS_Scale: OK!");
    }

    avpicture_layout((AVPicture*)frameRGB, output_pxf, width, height, (unsigned char*)buffer, length);

    return buffer;
}
