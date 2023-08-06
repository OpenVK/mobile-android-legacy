/* Not yet implemented! */

#include <jni.h>


#define LOG_TAG "FFmpegPlayer"

#define LOG_LEVEL 10

#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}

#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

JNIEXPORT char* JNICALL Java_uk_openvk_android_legacy_utils_MediaPlayer_testString(JNIEnv *pEnv, jobject pObj) {
    char* hello = "Hello World from C++!";
    return hello;
}






