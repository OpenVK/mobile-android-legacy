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
    #include <libavutil/imgutils.h>
    #include <libavformat/avformat.h>
    #include <libavformat/url.h>
}

/*for Android logs*/
#define LOG_TAG "OVK-MP"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char version[7] = "0.0.1";
char *gFileName;	      //file name of the video
int gErrorCode;

AVFormatContext *gFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

bool debug_mode;

jobject generateTrackInfo(JNIEnv* env, jobject instance,
                          AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);

extern "C" {
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
            LOGD(10, "[DEBUG] Enabled Debug Mode");
        } else {
            LOGD(10, "[DEBUG] Disabled Debug Mode");
        }
    }

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo(
            JNIEnv *env, jobject instance,
            jstring filename_,
            jint type) {
        const char *filename = env->GetStringUTFChars(filename_, 0);
        if(debug_mode) {
            LOGD(10, "[DEBUG] Registering FFmpeg (en/de)codecs/protocols...");
        }
        av_register_all();
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening file...");
        }
        if ((gErrorCode = avformat_open_input(&gFormatCtx, filename, NULL, NULL)) != 0) {
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't open file: %d", gErrorCode);
            }
            return NULL;    //open file failed
        }
        LOGD(10, "[DEBUG] Searching stream info...");
        /*retrieve stream information*/
        if ((gErrorCode = avformat_find_stream_info(gFormatCtx, NULL)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            return NULL;
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
                return NULL;
            } else {
                if(debug_mode) {
                    LOGI(10, "[INFO] Video codec: %s", lVideoCodec->name);
                }
            }
            if (gVideoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Video stream found, but decoder is unavailable.");
                }
                return NULL;
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
                return NULL;
            }
            return generateTrackInfo(
                    env, instance, gFormatCtx->streams[gVideoStreamIndex],
                    lVideoCodec, gVideoCodecCtx, AVMEDIA_TYPE_VIDEO
            );
        } else {
            AVCodec *lAudioCodec;
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
                    LOGE(1, "[ERROR] Video stream found, but decoder is unavailable.");
                }
                return NULL;
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
                    env, instance, gFormatCtx->streams[gAudioStreamIndex],
                    lAudioCodec, gAudioCodecCtx, AVMEDIA_TYPE_AUDIO
            );
        }

        env->ReleaseStringUTFChars(filename_, filename);
    };

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
        JNIEnv* env, jobject instance, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type
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

            jobject track = env->NewObject(track_class, audio_track_init);

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
    return track_class;
};