//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_AUDIODEC_H
#define MOBILE_ANDROID_LEGACY_AUDIODEC_H

#include <../utils/pktqueue.h>
#include <../interfaces/ffwrap.h>
#include <android/log.h>
#include <android.h>

#define LOG_TAG "FFwrap"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

typedef void (*DecoderHandler) (short*, int);

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

class AudioDecoder {
    public:
        AudioDecoder(AVFormatContext *pFormatCtx,
                     AVCodecContext *pCodecCtx,
                     AVStream* pStream,
                     int pStreamIndex,
                     IFFmpegWrapper *pInterface);
        bool                prepare();
        bool                process();
        bool                decode(void *ptr);
        DecoderHandler		onDecode;
        int                 gBufferSize, gStreamIndex;
        short*              gBuffer;
        bool                gRunning;
        AVFormatContext     *gFormatCtx;
        AVCodecContext      *gCodecCtx;
        IFFmpegWrapper      *gInterface;
        SwrContext          *gSwrCtx;
        bool                start();
        bool                stop();
        void*               decodeInThread();
        double              getPacketTime(AVPacket avPkt);
    private:
        PacketQueue*        gPktQueue;
};



#endif //MOBILE_ANDROID_LEGACY_AUDIODEC_H
