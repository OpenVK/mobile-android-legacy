#include <jni.h>
#include <string.h>

JNIEXPORT jstring JNICALL Java_uk_openvk_android_legacy_utils_MediaPlayer_testString(
        JNIEnv* env,
        jobject /* this */) {
    char hello[72] = "Hello from C++";
    return env->NewStringUTF(hello);
}
