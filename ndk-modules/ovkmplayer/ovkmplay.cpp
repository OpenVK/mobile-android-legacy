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
    #include <libswresample/swresample.h>
}

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

int                 gVideoStreamIndex;          // video stream index
int                 gAudioStreamIndex;          // audio stream index

int                 gFramesCount;

FFmpegWrapper       *gWrapper;

class IPlayerWrapper : public IFFmpegWrapper {
    public:
        void onError(int cmdId, int errorCode);
        void onResult(int cmdId,
                              int resultCode) {};
        void onStreamDecoding(uint8_t *buffer,
                                      int bufferLen,
                                      int streamIndex) {};
        void onChangePlaybackState(int playbackState) {};
};

JNIEXPORT void JNICALL naInit(JNIEnv *env, jobject instance) {
    IFFmpegWrapper* interface = new IPlayerWrapper();
    //IFFmpegWrapper *interface2 = (IFFmpegWrapper*) new IPlayerWrapper();
    //gWrapper = new FFmpegWrapper(gDebugMode, NULL);
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
    gWrapper->setDebugMode(value == JNI_TRUE);
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

}

/*env->SetByteArrayRegion(jBuffer, 0, (jsize) dataSize / gAudioCodecCtx->channels, (jbyte *) buffer);
    env->CallVoidMethod(instance, renderAudioMid, jBuffer, dataSize / gAudioCodecCtx->channels);*/

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[12];

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

    nm[10].name = "naGetPlaybackState";
    nm[10].signature = "()I";
    nm[10].fnPtr = (void*)naGetPlaybackState;

	jclass cls = env->FindClass("uk/openvk/android/legacy/utils/media/OvkMediaPlayer");
	//Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 12);
	return JNI_VERSION_1_6;
}
