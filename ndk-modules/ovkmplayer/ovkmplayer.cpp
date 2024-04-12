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
char *gFileName;	        // file name of the video
int gErrorCode;

AVFormatContext *gFormatCtx;
AVFormatContext *gTempFormatCtx;
int gVideoStreamIndex;      // video stream index
int gAudioStreamIndex;      // audio stream index

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

void naInitFFmpeg(JNIEnv *env, jobject instance) {
    if(debug_mode) {
        LOGD(10, "[DEBUG] Initializing FFmpeg...");
    }
    av_register_all();
    avcodec_register_all();
    avformat_network_init();
    gFormatCtx = avformat_alloc_context();
}

jstring naShowLogo(JNIEnv *env, jobject instance) {
   char logo[256] = "Logo";
   sprintf(logo, "OpenVK Media Player ver. %s for Android"
                 "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                 "licensed under AGPLv3 or later version."
                 "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
   return env->NewStringUTF(logo);
}

void naSetDebugMode(JNIEnv *env, jobject instance, jboolean value) {
    if(value == JNI_TRUE) {
        debug_mode = true;
        LOGD(10, "[DEBUG] Enabled Debug Mode");
    } else {
        LOGD(10, "[DEBUG] Disabled Debug Mode");
    }
}

jint naPlay(JNIEnv *env, jobject instance, jstring filename) {
    AVDictionary    *optionsDict = NULL;
    AVFrame         *decodedFrame = NULL;
    AVFrame         *frameRGBA = NULL;
    jobject			bitmap;

    gFileName = (char *)env->GetStringUTFChars(filename, NULL);
    if(avformat_open_input(&gFormatCtx, gFileName, NULL, NULL)!=0){
        LOGE(10, "FAILED to open input %s", gFileName);
        return -1; // Couldn't open file
    }

    if(avformat_find_stream_info(gFormatCtx, NULL)<0){
        LOGE(10, "FAILED to find stream info %s", gFileName);
        return -1; // Couldn't find stream information
    }

    av_dump_format(gFormatCtx, 0, gFileName, 0);

    gVideoStreamIndex = -1;
    for(int i = 0; i<gFormatCtx->nb_streams; i++) {
        if(gFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO) {
            gVideoStreamIndex = i;
            break;
        }
    }
    if(gVideoStreamIndex == -1){
        LOGE(10, "Didn't find a video stream");
        return -1;
    }

    gVideoCodecCtx=gFormatCtx->streams[gVideoStreamIndex]->codec;

    AVCodec *lVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
    if(lVideoCodec==NULL) {
        LOGE(10, "Unsupported codec");
        return -1;
    }
    // Open codec
    if(avcodec_open2(gVideoCodecCtx, lVideoCodec, &optionsDict)<0){
        LOGE(10, "Could not open codec");
        return -1;
    }
    // Allocate video frame
    decodedFrame = av_frame_alloc();
    // Allocate an AVFrame structure
    frameRGBA = av_frame_alloc();
    if(frameRGBA==NULL){
        LOGE(10, "AVFrame could not be allocated");
        return -1;
    }
    return 0;
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
