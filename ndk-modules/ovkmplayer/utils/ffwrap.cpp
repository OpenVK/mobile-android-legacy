//
// Created by tretdm on 20.04.2024.
//

#include "ffwrap.h"

FFmpegWrapper::FFmpegWrapper(bool pDebugMode, IFFmpegWrapper *pInterface) {
    gInterface = pInterface;
    setDebugMode(pDebugMode);
    if(pDebugMode) {
        LOGD(10, "[DEBUG] Initializing FFmpeg...");
    }
    av_register_all();
    avcodec_register_all();
    avformat_network_init();
}

void FFmpegWrapper::setDebugMode(bool pDebugMode) {
    gDebugMode = pDebugMode;
}

int FFmpegWrapper::getPlaybackState() {
    return gPlaybackState;
}

void FFmpegWrapper::openInputFile(char* pFileName, bool afterFindStreams) {
    AVDictionary    *optionsDict = NULL;
    int             result;
    char            errorString[192] = "No error";

    gFileName = pFileName;

    gFormatCtx = avformat_alloc_context();

    if(gDebugMode) {
        LOGD(10, "[DEBUG] Opening file %s...", pFileName);
    }

    if((result = avformat_open_input(&gFormatCtx, pFileName, NULL, NULL))!=0){
        if(result == -2) {
            sprintf(errorString, "File not found");
        } else if (av_strerror(result, errorString, 192) < 0) {
            strerror_r(-gErrorCode, errorString, 192);
        }
        if(gDebugMode) {
            LOGE(10, "[ERROR] Cannot open file %s (%d, %s)", pFileName, result, errorString);
        }
        gInterface->onError(FFMPEG_COMMAND_OPEN_INPUT, result);
        return;
    }
    if(afterFindStreams) {
        findStreams();
    } else {
        gInterface->onResult(FFMPEG_COMMAND_OPEN_INPUT, result);
    }
}

void FFmpegWrapper::findStreams() {
    if(avformat_find_stream_info(gFormatCtx, NULL)<0){
        LOGE(10, "FAILED to find stream info %s", gFileName);
        gInterface->onError(FFMPEG_COMMAND_FIND_STREAMS, -1);
    }

    av_dump_format(gFormatCtx, 0, gFileName, 0);

    gVideoStreamIndex = -1;
    for(int i = 0; i<gFormatCtx->nb_streams; i++) {
        if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            gVideoStreamIndex = i;
        } else if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            gAudioStreamIndex = i;
        }
    }

    if(gVideoStreamIndex == -1){
        LOGE(10, "Didn't find a video stream");
    } else if(gAudioStreamIndex == -1){
        LOGE(10, "Didn't find a audio stream");
    } else if(gVideoStreamIndex == -1 && gAudioStreamIndex == -1){
        LOGE(10, "Media streams not found");
        gInterface->onError(FFMPEG_COMMAND_FIND_STREAMS, -1);
    }
}


