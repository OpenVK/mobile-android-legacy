#ifndef MOBILE_ANDROID_LEGACY_INTERFACES_FFWRAP_H
#define MOBILE_ANDROID_LEGACY_INTERFACES_FFWRAP_H

#include <stdint.h>

#include <jni.h>

class IFFmpegWrapper {
    public:
        IFFmpegWrapper() {};
        virtual ~IFFmpegWrapper(){};
        virtual void onError(int cmdId,
                             int errorCode) = 0;
        virtual void onResult(int cmdId,
                              int resultCode) = 0;
        virtual void onStreamDecoding(uint8_t *buffer,
                                      int bufferLen,
                                      int streamIndex) = 0;
        virtual void onChangePlaybackState(int playbackState) = 0;
        virtual void onChangeWrapperState(int wrapperState) = 0;
        JNIEnv *env;
        jobject instance;
    private:
        bool gDebugMode;
};

#endif
