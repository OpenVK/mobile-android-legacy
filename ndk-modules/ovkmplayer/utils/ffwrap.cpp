//
// Created by tretdm on 20.04.2024.
//

#include "ffwrap.h"

FFmpegWrapper::FFmpegWrapper(bool pDebugMode, IFFmpegWrapper *pInterface) {

}

void FFmpegWrapper::setDebugMode(bool pDebugMode) {
    gDebugMode = pDebugMode;
}

int FFmpegWrapper::getPlaybackState() {
    return gPlaybackState;
}

void FFmpegWrapper::openInputFile(char* pFileName, bool afterFindStreams) {

}
