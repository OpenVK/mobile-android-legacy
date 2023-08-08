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
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>

// Non-standard 'stdint' implementation
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
}

/*for Android logs*/
#define LOG_TAG "OVK-MP"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char version[7] = "0.0.1";
char *gFileName;	  //the file name of the video

AVFormatContext *gFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

jobject generateTrackInfo(JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);


extern "C" {
    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android [Legacy Mode]"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo
            (JNIEnv *env, jobject instance, jstring filename, jint type) {
        av_register_all();
        int lError;
        if ((lError = av_open_input_file(&gFormatCtx, gFileName, NULL, 0, NULL)) != 0) {
            LOGE(1, "[ERROR] Can't open file: %d", lError);
            return NULL;    //open file failed
        }
        /*retrieve stream information*/
        if ((lError = av_find_stream_info(gFormatCtx)) < 0) {
            LOGE(1, "[ERROR] Can't find stream information: %d", lError);
            return NULL;
        }
        if (type == 0) {
            AVCodec *lVideoCodec;
            /*some global variables initialization*/
            LOGI(10, "Getting video track info...");


            /*find the video stream and its decoder*/
            gVideoStreamIndex = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                    &lVideoCodec,
                                                    0);
            if (gVideoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                LOGE(1, "[ERROR] Cannot find a video stream");
                return NULL;
            } else {
                LOGI(10, "[INFO] Video codec: %s", lVideoCodec->name);
            }
            if (gVideoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                LOGE(1, "[ERROR] Video stream found, but decoder is unavailable.");
                return NULL;
            }
            /*open the codec*/
            gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
            LOGI(10, "[INFO] Frame size: %dx%d", gVideoCodecCtx->height, gVideoCodecCtx->width);
            #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(gVideoCodecCtx, lVideoCodec) < 0) {
                LOGE(1, "[ERROR] Can't open the video codec!");
                return NULL;
            }
            return generateTrackInfo(
                    env, gFormatCtx->streams[gVideoStreamIndex],
                    lVideoCodec, gVideoCodecCtx, AVMEDIA_TYPE_VIDEO
            );
        } else {
            AVCodec *lAudioCodec;
            /*find the video stream and its decoder*/
            gAudioStreamIndex = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1,
                                                    &lAudioCodec,
                                                    0);
            if (gAudioStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                LOGE(1, "[ERROR] Cannot find a video stream");
                return NULL;
            } else {
                LOGI(10, "[INFO] Audio codec: %s", lAudioCodec->name);
            }
            if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                LOGE(1, "[ERROR] Video stream found, but decoder is unavailable.");
                return NULL;
            }
            /*open the codec*/
            gAudioCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
            LOGI(10, "[INFO] Frame size: %dx%d", gVideoCodecCtx->height, gVideoCodecCtx->width);
            #ifdef SELECTIVE_DECODING
                    gAudioCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(gVideoCodecCtx, lAudioCodec) < 0) {
                LOGE(1, "[ERROR] Can't open the audio codec!");
                return NULL;
            }
            return generateTrackInfo(
                    env, gFormatCtx->streams[gVideoStreamIndex],
                    lAudioCodec, gVideoCodecCtx, AVMEDIA_TYPE_AUDIO
            );
        }
    }
};

jobject generateTrackInfo(
        JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type
) {
    jclass track_class;
    if(type == AVMEDIA_TYPE_VIDEO) {
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
                track_class, "bitrate", "Ljava/lang/Integer;"
        );
        jfieldID frame_rate_field = env->GetFieldID(
                track_class, "frame_rate", "Ljava/lang/Float;"
        );

        // Load OvkVideoTrack values form fields (class variables)
        env->SetObjectField(track_class, codec_name_field, env->NewStringUTF(pCodec->name));
        jintArray array = (jintArray) env->GetObjectField(track_class, frame_size_field);
        jint *frame_size = env->GetIntArrayElements(array, 0);
        frame_size[0] = pCodecCtx->width;
        frame_size[1] = pCodecCtx->height;
        env->ReleaseIntArrayElements(array, frame_size, 0);
        env->SetIntField(track_class, bitrate_field, pCodecCtx->bit_rate);
        env->SetFloatField(track_class, frame_rate_field, pStream->avg_frame_rate.num);
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
                track_class, "bitrate", "Ljava/lang/Integer;"
        );
        jfieldID sample_rate_field = env->GetFieldID(
                track_class, "frame_rate", "Ljava/lang/Integer;"
        );
        jfieldID channels_field = env->GetFieldID(
                track_class, "channels", "Ljava/lang/Integer;"
        );

        // Load OvkAudioTrack values form fields (class variables)
        env->SetObjectField(track_class, codec_name_field, env->NewStringUTF(pCodec->name));
        env->SetIntField(track_class, bitrate_field, pCodecCtx->bit_rate);
        env->SetIntField(track_class, channels_field, pCodecCtx->channels);
    }
    return track_class;
};