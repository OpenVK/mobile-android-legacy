//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_FFWRAP_H
#define MOBILE_ANDROID_LEGACY_FFWRAP_H

#include <../interfaces/ffwrap.h>

class FFmpegWrapper {
    public:
        FFmpegWrapper(bool pDebugMode, IFFmpegWrapper *pInterface);
        void setDebugMode(bool pDebugMode);
        int getPlaybackState();
        void openInputFile(char* pFileName, bool afterFindStreams);
    private:
        bool gDebugMode;
        int gPlaybackState;
};



#endif //MOBILE_ANDROID_LEGACY_FFWRAP_H
