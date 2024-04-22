//
// Created by tretdm on 20.04.2024.
//

#include "ffwrap.h"

#include <stdio.h>
#include <math.h>

FFmpegWrapper::FFmpegWrapper(bool pDebugMode, IFFmpegWrapper *pInterface) {
    gInterface = pInterface;
    setDebugMode(pDebugMode);
    if(pDebugMode) {
        LOGD(10, "[DEBUG] Initializing FFmpeg...");
        getAVFormatVersion();
        getAVFormatLicense();
        getAVFormatBuildConf();

    }
    av_register_all();
    avcodec_register_all();
    avformat_network_init();
}

char* FFmpegWrapper::getAVFormatVersion() {
    char avfVersion[10];

    int avfVersionI = avformat_version();
    if(gDebugMode) {
        LOGD(10, "[DEBUG] libavformat version: %d.%d.%d",
                LIBAVUTIL_VERSION_MAJOR,
                LIBAVUTIL_VERSION_MINOR,
                LIBAVUTIL_VERSION_MICRO
        );
    }
    sprintf(avfVersion,
            "%d.%d.%d",
            LIBAVUTIL_VERSION_MAJOR,
            LIBAVUTIL_VERSION_MINOR,
            LIBAVUTIL_VERSION_MICRO
    );

    return avfVersion;
}

char* FFmpegWrapper::getAVFormatBuildConf() {
    char* originalBuildConf = (char*)av_malloc(2048 * sizeof(char));
    char* token;
    char* formattedBuildConf  = (char*)av_malloc(2048 * sizeof(char));

    sprintf(originalBuildConf, "%s", avformat_configuration());

    token = strtok(originalBuildConf, " ");
    int result = 0;
    int lines = 0;
    while (token != NULL){
        if(lines % 8 > 0) {
            result += sprintf(formattedBuildConf + result, "\t%s\r\n", token);
        } else {
            if(gDebugMode) {
                if(lines == 8) {
                    LOGD(10, "[DEBUG] libavformat build configuration:\r\n%s",
                        formattedBuildConf
                    )
                } else if(lines > 8) {
                    LOGD(10, "%s",
                        formattedBuildConf
                    )
                }
            }
            result = sprintf(formattedBuildConf, "\t%s\r\n", token);
        }
        lines++;
        token = strtok(NULL, " ");
    }
    av_free(originalBuildConf);
    av_free(formattedBuildConf);
    return (char*)avformat_configuration();
}

char* FFmpegWrapper::getAVFormatLicense() {
    if(gDebugMode) {
        LOGD(10, "[DEBUG] libavformat license: %s",
            avformat_license()
        );
    }
    return (char*)avformat_license();

}

void FFmpegWrapper::setDebugMode(bool pDebugMode) {
    gDebugMode = pDebugMode;
}

int FFmpegWrapper::getPlaybackState() {
    return gPlaybackState;
}

void FFmpegWrapper::setPlaybackState(int pPlaybackState) {
    gPlaybackState = pPlaybackState;
    if(gDebugMode) {
        LOGD(10, "[DEBUG] gPlaybackState now is %d...", gPlaybackState);
    }
}

void FFmpegWrapper::openInputFile(char* pFileName, bool afterFindStreams) {
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
        if(gDebugMode) {
            LOGE(10, "[ERROR] Failed to find stream info %s", gFileName);
        }
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
        if(gDebugMode) {
            LOGE(10, "[ERROR] Didn't find a video stream");
        }
    } else if(gAudioStreamIndex == -1){
        if(gDebugMode) {
            LOGE(10, "[ERROR] Didn't find a audio stream");
        }
    } else if(gVideoStreamIndex == -1 && gAudioStreamIndex == -1){
        if(gDebugMode) {
            LOGE(10, "[ERROR] Media streams not found");
        }
        gInterface->onError(FFMPEG_COMMAND_FIND_STREAMS, -1);
    } else {
        gInterface->onResult(FFMPEG_COMMAND_FIND_STREAMS, 0);
    }
}

void FFmpegWrapper::openCodecs() {
    int             vCodecResult, aCodecResult;
    AVDictionary    *optionsDict = NULL;

    if(gVideoStreamIndex != -1) {
        gVideoCodecCtx = getStream(gVideoStreamIndex)->codec;
        gVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
        if(gVideoCodec==NULL) {
            LOGE(10, "[ERROR] Unsupported video codec");
            vCodecResult = -1;
        } else if(avcodec_open2(gVideoCodecCtx, gVideoCodec, &optionsDict)<0){
            LOGE(10, "[ERROR] Could not open video codec");
            vCodecResult = -1;
        }
    }
    if(gAudioStreamIndex != -1) {
        gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIndex]->codec;
        gAudioCodec = avcodec_find_decoder(gAudioCodecCtx->codec_id);
        if(gAudioCodec == NULL) {
            LOGE(10, "[ERROR] Unsupported audio codec");
            aCodecResult = -1;
        } else if(avcodec_open2(gAudioCodecCtx, gAudioCodec, &optionsDict)<0){
            LOGE(10, "[ERROR] Could not open audio codec");
            aCodecResult = -1;
        }
    }

    if(aCodecResult != -1 || vCodecResult != -1) {
        gInterface->onResult(FFMPEG_COMMAND_OPEN_CODECS, 0);
    } else {
        gInterface->onError(FFMPEG_COMMAND_OPEN_CODECS, -1);
    }
}

AVStream* FFmpegWrapper::getStream(int index) {
    return gFormatCtx->streams[index];
}

static void *audioDecoderThread(void *arg) {
    AudioDecoder *audioDec = (AudioDecoder*) arg;
    audioDec->prepare();
    audioDec->start();
}

static void *videoDecoderThread(void *arg) {
    VideoDecoder *videoDec = (VideoDecoder*) arg;
    videoDec->prepare();
    videoDec->start();
}

void FFmpegWrapper::startDecoding() {
    AudioDecoder *audioDec = new AudioDecoder(
        gFormatCtx,
        gAudioCodecCtx,
        getStream(gAudioStreamIndex),
        gAudioStreamIndex,
        gInterface
    );
    VideoDecoder *videoDec = new VideoDecoder(
        gFormatCtx,
        gVideoCodecCtx,
        getStream(gVideoStreamIndex),
        gVideoStreamIndex,
        gInterface
    );
    pthread_t audioDecThread;
    pthread_create(&audioDecThread, NULL, &audioDecoderThread, (void*)audioDec);
    pthread_t videoDecThread;
    pthread_create(&videoDecThread, NULL, &videoDecoderThread, (void*)videoDec);
}