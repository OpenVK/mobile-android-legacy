/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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

// Java/C++ standard implementations headers
#include <jni.h>
#include <string.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>

#include <utils/android.h>

// Non-standard 'stdint' implementation

// Android implementations headers
#include <android/log.h>

/*for Android logs*/

char                version[7]                  = "0.0.1";
char                *gFileName;	                // file name of the video
int                 gErrorCode;

bool                gDebugMode                  = true;

JavaVM*             gVM;
JavaVMAttachArgs    gVMArgs;
jobject             gInstance;
JNIEnv*             gEnv;

jbyteArray jBuffer;

int attachEnv(JNIEnv **pEnv) {
    return 3;
}

JNIEXPORT void JNICALL naInit(JNIEnv *env, jobject instance) {
}

JNIEXPORT void JNICALL naPlay(JNIEnv *env, jobject instance, int streamType) {
    gVMArgs.version = JNI_VERSION_1_6;
    gVMArgs.name = NULL;
    gVMArgs.group = NULL;
}

JNIEXPORT void JNICALL naStartAudioDecoding(JNIEnv *env, jobject instance) {

}

JNIEXPORT void JNICALL naStartVideoDecoding(JNIEnv *env, jobject instance) {

}

JNIEXPORT void JNICALL naPause(JNIEnv *env, jobject instance) {

}

JNIEXPORT void JNICALL naStop(JNIEnv *env, jobject instance) {

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
}

JNIEXPORT jint JNICALL naGetPlaybackState(JNIEnv *env, jobject instance) {
    return 0;
}

JNIEXPORT jint JNICALL naOpenFile(JNIEnv *env, jobject instance, jstring filename) {
    return 0;
}

JNIEXPORT jobject JNICALL naGenerateTrackInfo(
        JNIEnv* env, jobject instance, jint type
) {
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
