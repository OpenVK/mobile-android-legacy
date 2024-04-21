//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_FFWRAP_H
#define MOBILE_ANDROID_LEGACY_FFWRAP_H

#include <android/log.h>
#include <utils/android.h>

// Non-standard 'stdint' implementation
#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
extern "C"{
    #ifdef __cplusplus
    #define __STDC_CONSTANT_MACROS
    #ifdef _STDINT_H
    #undef _STDINT_H
    #endif
    # include <stdint.h>
    #endif
}
#ifndef INT64_C
#define INT64_C(c) (c ## LL)
#define UINT64_C(c) (c ## ULL)
#endif

#include <../interfaces/ffwrap.h>
#include <../decoders/audiodec.h>
#include <../decoders/videodec.h>

// FFmpeg implementation headers (using LGPLv3.0 model)
extern "C" {
    #include <libavutil/avstring.h>
    #include <libavutil/pixdesc.h>
    #include <libavutil/imgutils.h>
    #include <libavutil/samplefmt.h>
    #include <libavformat/avformat.h>
    #include <libavformat/url.h>
    #include <libavformat/avio.h>
    #include <libswscale/swscale.h>
    #include <libavcodec/avcodec.h>
    #include <libavcodec/avfft.h>
    #include <libavdevice/avdevice.h>
    #include <libswresample/swresample.h>
}

/*for Android logs*/
#define LOG_TAG "FFmpegWrapper"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

const int FFMPEG_COMMAND_OPEN_INPUT             = 0x2000;
const int FFMPEG_COMMAND_FIND_STREAMS           = 0x2001;
const int FFMPEG_COMMAND_OPEN_CODECS            = 0x2002;
const int FFMPEG_PLAYBACK_ERROR                 = 0x7fff;
const int FFMPEG_PLAYBACK_STOPPED               = 0x8000;
const int FFMPEG_PLAYBACK_PLAYING               = 0x0801;
const int FFMPEG_PLAYBACK_PAUSED                = 0x8002;

class FFmpegWrapper {
    public:
        FFmpegWrapper(bool pDebugMode, IFFmpegWrapper *pInterface);
        AVFormatContext     *gFormatCtx;
        AVCodecContext      *gVideoCodecCtx;
        AVCodecContext      *gAudioCodecCtx;

        int                 gAudioStreamIndex,
                            gVideoStreamIndex;

        AVCodec             *gVideoCodec;
        AVCodec             *gAudioCodec;
        void setDebugMode(bool pDebugMode);
        int getPlaybackState();
        void setPlaybackState(int pPlaybackState);
        void openInputFile(char* pFileName, bool afterFindStreams);
        void findStreams();
        AVStream* getStream(int index);
        void openCodecs();
        void startDecoding();

    private:
        IFFmpegWrapper      *gInterface;

        bool    gDebugMode;
        int     gPlaybackState,
                gErrorCode;
        char*   gFileName;
};



#endif //MOBILE_ANDROID_LEGACY_FFWRAP_H
