#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_uk_openvk_android_legacy_utils_MediaPlayer_testString(JNIEnv *env, jclass type) {

    char* returnValue = "Hello World from C and JNI";
    return returnValue;
}
