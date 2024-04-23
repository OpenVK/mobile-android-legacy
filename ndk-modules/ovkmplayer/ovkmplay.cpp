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
#include <pthread.h>

#include <utils/android.h>

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


#include <utils/ffwrap.h>
#include <interfaces/ffwrap.h>

/*for Android logs*/
#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char                version[7]                  = "0.0.1";
char                *gFileName;	                // file name of the video
int                 gErrorCode;

bool                gDebugMode                  = true;

int                 gFramesCount,
                    gAttachResult               = -54;

FFmpegWrapper       *gWrapper;

JavaVM*             gVM;
JavaVMAttachArgs    gVMArgs;
jobject             gInstance;
JNIEnv*             gEnv;

jbyteArray jBuffer;

class IPlayerWrapper : public IFFmpegWrapper {
    public:
        void onError(int cmdId, int errorCode);
        void onResult(int cmdId, int resultCode);
        void onStreamDecoding(uint8_t *buffer,
                                      int bufferLen,
                                      int streamIndex);
        void onChangePlaybackState(int playbackState) {};
        void onChangeWrapperState(int wrapperState);
        JNIEnv *env;
        jobject instance;
};

IPlayerWrapper* gInterface;

int attachEnv(JNIEnv **pEnv) {
    int getEnvStat = gVM->GetEnv((void **) pEnv, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        if (gVM->AttachCurrentThread(pEnv, NULL) != 0) {
            LOGE(10, "Failed to attach thread with JNIEnv*");
            return 2; //Failed to attach
        }
        return 1; //Attached. Need detach
    } else if (JNI_OK == getEnvStat) {
        return 0;//Already attached
    } else {
        return 3;
    }
}

JNIEXPORT void JNICALL naInit(JNIEnv *env, jobject instance) {
    gInterface = new IPlayerWrapper();
    gInterface->instance = env->NewGlobalRef(instance);
    gWrapper = new FFmpegWrapper(gDebugMode, gInterface);
}

JNIEXPORT void JNICALL naPlay(JNIEnv *env, jobject instance, int streamType) {
    gVMArgs.version = JNI_VERSION_1_6;
    gVMArgs.name = NULL;
    gVMArgs.group = NULL;
    gWrapper->setPlaybackState(FFMPEG_PLAYBACK_PLAYING);
    //gWrapper->startDecoding();
}

JNIEXPORT void JNICALL naStartAudioDecoding(JNIEnv *env, jobject instance) {
    gWrapper->startDecoding(gWrapper->gAudioStreamIndex);
}

JNIEXPORT void JNICALL naStartVideoDecoding(JNIEnv *env, jobject instance) {
    gWrapper->startDecoding(gWrapper->gVideoStreamIndex);
}

JNIEXPORT void JNICALL naPause(JNIEnv *env, jobject instance) {
    gWrapper->setPlaybackState(FFMPEG_PLAYBACK_PAUSED);
}

JNIEXPORT void JNICALL naStop(JNIEnv *env, jobject instance) {
    gWrapper->setPlaybackState(FFMPEG_PLAYBACK_STOPPED);
}

JNIEXPORT jstring JNICALL naShowLogo(JNIEnv *env, jobject instance) {
   char logo[256] = "Logo";
   sprintf(logo, "OpenVK Media Player ver. %s for Android"
                 "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                 "licensed under AGPLv3 or later version."
                 "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
   return env->NewStringUTF(logo);
}

JNIEXPORT void JNICALL naSetDebugMode(JNIEnv *env, jobject instance, jboolean value) {
    if(gWrapper != NULL) {
        gWrapper->setDebugMode(value == JNI_TRUE);
    }
}

JNIEXPORT jint JNICALL naGetPlaybackState(JNIEnv *env, jobject instance) {
    return (jint)gWrapper->getPlaybackState();
}

JNIEXPORT jint JNICALL naOpenFile(JNIEnv *env, jobject instance, jstring filename) {
    gFileName = (char *)env->GetStringUTFChars(filename, NULL);
    gWrapper->openInputFile(gFileName, true);
    return 0;
}

void IPlayerWrapper::onError(int cmdId, int errorCode) {
    LOGE(10, "Error Callback Test: %d | %d", cmdId, errorCode);
}

void IPlayerWrapper::onResult(int cmdId, int resultCode) {
    JNIEnv* env;
    int attachResult = attachEnv(&env);
    if(attachResult < 2) {
        jclass jmPlay = env->GetObjectClass(instance);
        if(cmdId == FFMPEG_COMMAND_FIND_STREAMS) {
            gWrapper->openCodecs();
        } else if(cmdId == FFMPEG_COMMAND_OPEN_CODECS) {
            jmethodID onResultMid = env->GetMethodID(jmPlay, "onResult", "(II)V");
            env->CallVoidMethod(instance, onResultMid, (jint)cmdId, (jint)resultCode);
        }
        if(attachResult == 1) {
            gVM->DetachCurrentThread();
        }
    }
}

void IPlayerWrapper::onStreamDecoding(uint8_t* buffer, int bufferLen, int streamIndex) {
    JNIEnv* env;
    int attachResult = attachEnv(&env);
    if(attachResult < 2) {
        jclass jmPlay = env->GetObjectClass(instance);
        jBuffer = env->NewByteArray((jsize) bufferLen);
        env->SetByteArrayRegion(jBuffer, 0, (jsize) bufferLen, (jbyte *) buffer);
        if(streamIndex == gWrapper->gAudioStreamIndex) {
            jmethodID renderAudioMid = env->GetMethodID(jmPlay, "renderAudio", "([BI)V");
            env->CallVoidMethod(instance, renderAudioMid, jBuffer, bufferLen);
        } else if(streamIndex == gWrapper->gVideoStreamIndex) {
            jmethodID renderVideoMid = env->GetMethodID(jmPlay, "renderVideo", "([BI)V");
            env->CallVoidMethod(instance, renderVideoMid, jBuffer, bufferLen);
        }
        env->ReleaseByteArrayElements(jBuffer, (jbyte *)env->GetByteArrayElements(jBuffer, NULL), JNI_ABORT);
        env->DeleteLocalRef(jBuffer);
        env->DeleteLocalRef(jmPlay);
        if(attachResult == 1) {
            gVM->DetachCurrentThread();
        }
    }
}

void IPlayerWrapper::onChangeWrapperState(int wrapperState) {
}

JNIEXPORT jobject JNICALL naGenerateTrackInfo(
        JNIEnv* env, jobject instance, jint type
) {
    jclass track_class;
    try {
        AVStream *pStream;
        if (type == 0 && gWrapper->gVideoStreamIndex != -1) {
            if(gDebugMode) {
                LOGD(1, "[DEBUG] Searching video stream...");
            }
            pStream = gWrapper->getStream(gWrapper->gVideoStreamIndex);

            if(gDebugMode) {
                LOGD(1, "[DEBUG] Searching video stream... OK");
            }
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
                track_class, "bitrate", "J"
            );
            jfieldID frame_rate_field = env->GetFieldID(
                track_class, "frame_rate", "F"
            );
            jobject track = env->NewObject(track_class, video_track_init);

            // Load OvkVideoTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(gWrapper->gVideoCodec->name));
            jintArray array = (jintArray) env->GetObjectField(track, frame_size_field);
            jint *frame_size = env->GetIntArrayElements(array, 0);
            frame_size[0] = gWrapper->gVideoCodecCtx->width;
            frame_size[1] = gWrapper->gVideoCodecCtx->height;
            env->ReleaseIntArrayElements(array, frame_size, 0);
            env->SetLongField(track, bitrate_field, gWrapper->gVideoCodecCtx->bit_rate);
            env->SetFloatField(track, frame_rate_field, pStream->avg_frame_rate.num);
            return track;
        } else if(type == 1 && gWrapper->gAudioStreamIndex != -1) {
            pStream = gWrapper->getStream(gWrapper->gAudioStreamIndex);
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
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(gWrapper->gAudioCodec->name));
            env->SetLongField(track, sample_rate_field, gWrapper->gAudioCodecCtx->sample_rate);
            env->SetLongField(track, bitrate_field, gWrapper->gAudioCodecCtx->bit_rate);
            env->SetIntField(track, channels_field, gWrapper->gAudioCodecCtx->channels);
            return track;
        } else {
            if(gDebugMode) {
                LOGE(1, "[ERROR] Track not found");
            }
        }
    } catch (...) {
        if(gDebugMode) {
            LOGE(1, "[ERROR] Track not found");
        }
        return NULL;
    }
    return NULL;
}

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[11];

	nm[0].name = "naInit";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naInit;

	nm[1].name = "naShowLogo";
    nm[1].signature = "()Ljava/lang/String;";
    nm[1].fnPtr = (void*)naShowLogo;

    nm[2].name = "naSetDebugMode";
    nm[2].signature = "(Z)V";
    nm[2].fnPtr = (void*)naSetDebugMode;

    nm[3].name = "naOpenFile";
    nm[3].signature = "(Ljava/lang/String;)I";
    nm[3].fnPtr = (void*)naOpenFile;

    nm[4].name = "naGenerateTrackInfo";
    nm[4].signature = "(I)Ljava/lang/Object;";
    nm[4].fnPtr = (void*)naGenerateTrackInfo;

    nm[5].name = "naGetPlaybackState";
    nm[5].signature = "()I";
    nm[5].fnPtr = (void*)naGetPlaybackState;

    nm[6].name = "naPlay";
    nm[6].signature = "()V";
    nm[6].fnPtr = (void*)naPlay;

    nm[7].name = "naPause";
    nm[7].signature = "()V";
    nm[7].fnPtr = (void*)naPause;

    nm[8].name = "naStop";
    nm[8].signature = "()V";
    nm[8].fnPtr = (void*)naStop;

    nm[9].name = "naStartAudioDecoding";
    nm[9].signature = "()V";
    nm[9].fnPtr = (void*)naStartAudioDecoding;

    nm[10].name = "naStartVideoDecoding";
    nm[10].signature = "()V";
    nm[10].fnPtr = (void*)naStartVideoDecoding;

	jclass cls = env->FindClass("uk/openvk/android/legacy/utils/media/OvkMediaPlayer");
	//Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 11);

	gVM = pVm;

	return JNI_VERSION_1_6;
}
