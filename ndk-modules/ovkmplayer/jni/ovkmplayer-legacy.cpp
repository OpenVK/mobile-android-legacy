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
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <math.h>
#include <limits.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>

// Non-standard 'stdint' implementation
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
extern "C"{
    #ifdef __cplusplus
    #define __STDC_CONSTANT_MACROS
    #ifdef _STDINT_H
    #undef _STDINT_H
    #endif
    # include <stdint.h>
    #endif
}
#ifndef INT64_C
#define INT64_C(c) (c ## LL)
#define UINT64_C(c) (c ## ULL)
#endif

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

char version[7] = "0.0.1";
char *gFileName;	  //the file name of the video
int gErrorCode;

AVFormatContext *gFormatCtx;
AVFormatContext *gTempFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

jobject generateTrackInfo(JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);

bool debug_mode;

AVDictionary *avFormatOptions = NULL;
AVDictionary *avCodecOptions = NULL;

jint g_playbackState;
jint FFMPEG_PLAYBACK_STOPPED = 0;
jint FFMPEG_PLAYBACK_PLAYING = 1;
jint FFMPEG_PLAYBACK_PAUSED = 2;

int gFrameCount;

extern "C" {
JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_initFFmpeg(JNIEnv *env, jobject instance) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Initializing FFmpeg...");
        }
        av_register_all();
        avcodec_register_all();

    }

    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android [Legacy Mode]"
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
            LOGD(10, "[DEBUG] Enabled Debug Mode");
        } else {
            LOGD(10, "[DEBUG] Disabled Debug Mode");
        }
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile(
            JNIEnv *env, jobject instance,
            jstring filename_) {
        const char *filename = env->GetStringUTFChars(filename_, 0);
        if(filename_ == NULL) {
            LOGE(1, "[ERROR] Invalid filename");
            return -10;
        }
        gFileName = (char *) filename;
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening file %s...", filename);
        }

        gFormatCtx = avformat_alloc_context();

        if ((gErrorCode = avformat_open_input(&gFormatCtx, filename, NULL, 0)) != 0) {
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
            return gErrorCode;    //open file failed
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Searching A/V streams...", filename);
        }
        /*retrieve stream information*/
        if ((gErrorCode = av_find_stream_info(gFormatCtx)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
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
        if ((gErrorCode = av_open_input_file(&gTempFormatCtx, filename, NULL, 0, NULL)) != 0) {
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
        if ((gErrorCode = av_find_stream_info(gTempFormatCtx)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            return NULL;
        }
        return gTempFormatCtx;
    }

    JNIEXPORT jint JNICALL
        Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_renderFrames
                (JNIEnv *env, jobject instance, jobject buffer, jlong gFrameNumber) {
        uint8_t* pFrameBuffer = (uint8_t *) (env)->GetDirectBufferAddress(buffer);
        if(g_playbackState == FFMPEG_PLAYBACK_PLAYING) {
            int               err, i, got_frame, frame_size;
            AVDictionaryEntry *e;
            AVCodecContext    *pCodecCtx = NULL;
            AVCodec           *pCodec = NULL;
            AVFrame           *pFrame = NULL;
            AVFrame           *pFrameRGB = NULL;
            AVPacket          packet;
            int               endOfVideo;
            uint8_t *output_buf;

            AVStream *pVideoStream = gFormatCtx->streams[gVideoStreamIndex];
            pCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
            pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

            if(pCodec == NULL) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Video stream found, but '%s' decoder is unavailable.",
                         gVideoCodecCtx->codec_name);
                }
                g_playbackState = FFMPEG_PLAYBACK_STOPPED;
                gErrorCode = -2;
                return gErrorCode;
            }

            e = NULL;
            while ((e = av_dict_get(avCodecOptions, "", e, AV_DICT_IGNORE_SUFFIX))) {
                if(debug_mode) {
                    LOGE(10, "avcodec_open2: option \"%s\" not recognized", e->key);
                    gErrorCode = -2;
                    return gErrorCode;
                }
            }

            pFrame = avcodec_alloc_frame();
            pFrameRGB = avcodec_alloc_frame();
            frame_size = avpicture_get_size(PIX_FMT_RGB24, pCodecCtx->width,
                                        pCodecCtx->height);
            pFrameBuffer = (uint8_t*) av_malloc(frame_size * sizeof(uint8_t));
            if(pFrame == NULL || pFrameRGB == NULL) {
                if(debug_mode) {
                    LOGE(10, "[ERROR] Cannot allocate video frames");
                }
                g_playbackState = FFMPEG_PLAYBACK_STOPPED;
                gErrorCode = -3;
                return gErrorCode;
            } else if (packet.stream_index == gVideoStreamIndex) {
                int ret = av_read_frame(gFormatCtx, &packet);
                if(ret >= 0) {
                    avpicture_fill((AVPicture *) pFrameRGB, pFrameBuffer, PIX_FMT_RGB24,
                                   pCodecCtx->width, pCodecCtx->height);
                    avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, &packet);
                    if (got_frame) {
                        pFrameBuffer = (uint8_t *) pFrameRGB->data;
                    }
                    av_free_packet(&packet);
                    return 1;
                } else if(ret == AVERROR_EOF){
                    g_playbackState = FFMPEG_PLAYBACK_STOPPED;
                    return 0;
                }
            }
        }
    }

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

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo(
            JNIEnv *env, jobject instance,
            jstring filename_,
            jint type) {
        int videoStreamIndex = -1;    // video stream index
        int audioStreamIndex = -1;    // audio stream index

        AVCodecContext *videoCodecCtx = NULL;
        AVCodecContext *audioCodecCtx = NULL;
        const char *filename;
        if(gTempFormatCtx == NULL) {
            if (filename_ == NULL) {
                if (Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile
                            (env, instance, reinterpret_cast<jstring>(gFileName)) < 0) {
                    return NULL;
                } else {
                    gTempFormatCtx = gFormatCtx;
                }
            } else {
                filename = env->GetStringUTFChars(filename_, 0);
                gTempFormatCtx = openTempFile(const_cast<char *>(filename));
                if (gTempFormatCtx == NULL) {
                    return NULL;
                }
            }
        }

        if (type == 0) {
            AVCodec *lVideoCodec;
            /*some global variables initialization*/
            if(debug_mode) {
                LOGD(10, "[DEBUG] Getting video track info...");
            }

            /*find the video stream and its decoder*/
            gVideoStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                    &lVideoCodec,
                                                    0);

            if (videoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Cannot find a video stream");
                }
                return NULL;
            }
            if (videoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Video stream found, but '%s' decoder is unavailable.",
                         lVideoCodec->name);
                }
                return NULL;
            }

            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d |  Video stream #%d detected. Opening...",
                     gTempFormatCtx->nb_streams, videoStreamIndex + 1);
            }

            /*open the codec*/
            gVideoCodecCtx = gTempFormatCtx->streams[videoStreamIndex]->codec;
            LOGI(10, "[INFO] Video codec: %s | Frame size: %dx%d", videoCodecCtx->codec_name,
                 videoCodecCtx->height, videoCodecCtx->width);
            #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(videoCodecCtx, lVideoCodec) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the video codec!");
                }
                return NULL;
            }
            return generateTrackInfo(
                    env, gTempFormatCtx->streams[videoStreamIndex],
                    lVideoCodec, videoCodecCtx, AVMEDIA_TYPE_VIDEO
            );
        } else {
            AVCodec *lAudioCodec;

            if(debug_mode) {
                LOGD(10, "[DEBUG] Getting audio track info...");
            }

            /*find the audio stream and its decoder*/
            gAudioStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1,
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

            /*open the codec*/
            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d | Audio stream #%d detected. Opening...",
                     gTempFormatCtx->nb_streams, audioStreamIndex + 1);
            }
            LOGI(10, "[INFO] Duration: %d", gTempFormatCtx->streams[audioStreamIndex]->duration);
            audioCodecCtx = gFormatCtx->streams[audioStreamIndex]->codec;
            LOGI(10, "[INFO] Audio codec: %s | Sample rate: %d Hz", audioCodecCtx->codec_name,
                 audioCodecCtx->sample_rate);
            #ifdef SELECTIVE_DECODING
                    gAudioCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(audioCodecCtx, lAudioCodec) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the audio codec!");
                }
                return NULL;
            }
            return generateTrackInfo(
                    env, gTempFormatCtx->streams[audioStreamIndex],
                    lAudioCodec, audioCodecCtx, AVMEDIA_TYPE_AUDIO
            );
        }
        if(filename_ != NULL)
        env->ReleaseStringUTFChars(filename_, filename);
    };

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo2(
            JNIEnv *env, jobject instance,
            jint type) {

        int videoStreamIndex = -1;    // video stream index
        int audioStreamIndex = -1;    // audio stream index

        AVCodecContext *videoCodecCtx = NULL;
        AVCodecContext *audioCodecCtx = NULL;
        const char *filename;
        if(gFormatCtx == NULL) {
            if (gTempFormatCtx == NULL) {
                if (openTempFile(gFileName) == NULL) {
                    return NULL;
                }
            }
        } else {
            gTempFormatCtx = gFormatCtx;
        }
        try {
            if (type == 0) {
                AVCodec *lVideoCodec;
                /*some global variables initialization*/
                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting video track info...");
                }

                /*find the video stream and its decoder*/
                gVideoStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                        &lVideoCodec,
                                                        0);

                if (videoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a video stream");
                    }
                    return NULL;
                }
                if (videoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Video stream found, but '%s' decoder is unavailable.",
                             lVideoCodec->name);
                    }
                    return NULL;
                }

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Total streams: %d | Video stream #%d detected. Opening...",
                         gTempFormatCtx->nb_streams, videoStreamIndex + 1);
                }

                /*open the codec*/
                gVideoCodecCtx = gTempFormatCtx->streams[videoStreamIndex]->codec;
                LOGI(10, "[INFO] Video codec: %s | Frame size: %dx%d", videoCodecCtx->codec_name,
                     videoCodecCtx->height, videoCodecCtx->width);
                #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
                #endif
                if (avcodec_open(videoCodecCtx, lVideoCodec) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the video codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gTempFormatCtx->streams[videoStreamIndex],
                        lVideoCodec, videoCodecCtx, AVMEDIA_TYPE_VIDEO
                );
            } else {
                AVCodec *lAudioCodec;

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting audio track info...");
                }

                /*find the audio stream and its decoder*/
                gAudioStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1,
                                                        &lAudioCodec,
                                                        0);

                if (gAudioStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a audio stream");
                    }
                    return NULL;
                }
                if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Audio stream found, but '%s' decoder is unavailable.",
                             lAudioCodec->name);
                    }
                    return NULL;
                }

                /*open the codec*/
                if (debug_mode) {
                    LOGD(10, "[DEBUG] Total streams: %d | Audio stream #%d detected. Opening...",
                         gTempFormatCtx->nb_streams, audioStreamIndex + 1);
                }
                LOGI(10, "[INFO] Duration: %d",
                     gTempFormatCtx->streams[audioStreamIndex]->duration);
                lAudioCodec = avcodec_find_decoder(
                        gTempFormatCtx->streams[audioStreamIndex]->codec->codec_id);

                audioCodecCtx = lAudioCodec->;
                LOGI(10, "[INFO] Audio codec: %s | Sample rate: %d Hz", audioCodecCtx->codec_name,
                     audioCodecCtx->sample_rate);
                #ifdef SELECTIVE_DECODING
                                gAudioCodecCtx->allow_selective_decoding = 1;
                #endif
                if (avcodec_open(audioCodecCtx, lAudioCodec) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the audio codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gTempFormatCtx->streams[audioStreamIndex],
                        lAudioCodec, audioCodecCtx, AVMEDIA_TYPE_AUDIO
                );
            }
        } catch (const char* error_message) {
            return NULL;
        }
    };

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorCode(
            JNIEnv *env, jobject instance
    ) {
        return gErrorCode;
    }

    JNIEXPORT jlong JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getFrameCount(
            JNIEnv *env, jobject instance
    ) {
        return gFrameCount;
    }


    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorString(
            JNIEnv *env, jobject instance
    ) {
        char error_str[192];
        av_strerror(gErrorCode, error_str, 192);
        return env->NewStringUTF(error_str);
    }
};

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
    try {
        if (type == AVMEDIA_TYPE_VIDEO) {
            // Load OvkVideoTrack class
            track_class = env->FindClass(
                    "uk/openvk/android/legacy/utils/media/OvkVideoTrack"
            );
            // Load OvkVideoTrack class method
            jmethodID video_track_init = env->GetMethodID(
                    track_class, "<init>", "()V"
            );
            jfieldID codec_name_field = env->GetFieldID(
                    track_class, "codec_name", "Ljava/lang/String;"
            );
            jfieldID frame_size_field = env->GetFieldID(track_class, "frame_size", "[I");
            jfieldID bitrate_field = env->GetFieldID(
                    track_class, "bitrate", "I"
            );
            jfieldID frame_rate_field = env->GetFieldID(
                    track_class, "frame_rate", "F"
            );

            jobject track = env->NewObject(track_class, video_track_init);

            // Load OvkVideoTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            jintArray array = (jintArray) env->GetObjectField(track, frame_size_field);
            jint *frame_size = env->GetIntArrayElements(array, 0);
            frame_size[0] = pCodecCtx->width;
            frame_size[1] = pCodecCtx->height;
            env->ReleaseIntArrayElements(array, frame_size, 0);
            env->SetIntField(track, bitrate_field, pCodecCtx->bit_rate);
            env->SetFloatField(track, frame_rate_field, pStream->avg_frame_rate.num);
            return track;
        } else {
            // Load OvkAudioTrack class
            track_class = env->FindClass(
                    "uk/openvk/android/legacy/utils/media/OvkAudioTrack"
            );
            // Load OvkVideoTrack class methods
            jmethodID audio_track_init = env->GetMethodID(
                    track_class, "<init>", "()V"
            );

            jobject track = env->NewObject(track_class, audio_track_init);

            jfieldID codec_name_field = env->GetFieldID(
                    track_class, "codec_name", "Ljava/lang/String;"
            );
            jfieldID sample_rate_field = env->GetFieldID(
                    track_class, "sample_rate", "J"
            );
            jfieldID bitrate_field = env->GetFieldID(
                    track_class, "bitrate", "J"
            );
            jfieldID channels_field = env->GetFieldID(
                    track_class, "channels", "I"
            );

            // Load OvkAudioTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            env->SetLongField(track, sample_rate_field, pCodecCtx->sample_rate);
            env->SetLongField(track, bitrate_field, pCodecCtx->bit_rate);
            env->SetIntField(track, channels_field, pCodecCtx->channels);
            return track;
        }
    } catch (...) {
        if(debug_mode) {
            LOGE(1, "[ERROR] Track not found");
        }
        return NULL;
    }
    return NULL;
}
#pragma clang diagnostic pop
