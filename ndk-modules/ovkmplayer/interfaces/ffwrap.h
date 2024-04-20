#ifndef MOBILE_ANDROID_LEGACY_INTERFACES_FFWRAP_H
#define MOBILE_ANDROID_LEGACY_INTERFACES_FFWRAP_H

#include <stdint.h>

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
    private:
        bool gDebugMode;
};

#endif