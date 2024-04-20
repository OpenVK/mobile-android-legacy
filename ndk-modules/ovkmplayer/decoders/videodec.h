//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_VIDEODEC_H
#define MOBILE_ANDROID_LEGACY_VIDEODEC_H

#include <../utils/pktqueue.h>

#define LOG_TAG "FFwrap"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

// FFmpeg implementation headers (using LGPLv3.0 model)
extern "C" {
    #define __STDC_CONSTANT_MACROS          // workaround for compiler
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


class VideoDecoder {
    public:
        VideoDecoder(AVStream* pStream, PacketQueue *pPktQueue);
        AVFrame *gFrame;
        AVStream *gStream;
        bool gRunning;
        uint64_t gPktPts;
        bool prepare();
        bool process(AVPacket *avPkt);
        bool decode();
        int getBuffer(struct AVCodecContext *pCodecCtx, AVFrame *pFrame);
        void releaseBuffer(struct AVCodecContext *pCodecCtx, AVFrame *pFrame);
    private:
        PacketQueue*        gPktQueue;
};

#endif //MOBILE_ANDROID_LEGACY_VIDEODEC_H
