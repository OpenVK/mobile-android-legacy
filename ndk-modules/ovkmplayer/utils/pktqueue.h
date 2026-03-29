//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_PKTQUEUE_H
#define MOBILE_ANDROID_LEGACY_PKTQUEUE_H

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
    #include <pthread.h>
}

class PacketQueue {
    public:
        PacketQueue();
        ~PacketQueue();

        void flush();
        int put(AVPacket* pkt);

        int get(AVPacket *pkt, bool block);
        int length();
        void abort();


    private:
        AVPacketList*		mFirst;
    	AVPacketList*		mLast;
        int					mPackets;
        int					mSize;
        bool				mAbortReq;
        pthread_mutex_t     mLock;
        pthread_cond_t		mCond;
};



#endif //MOBILE_ANDROID_LEGACY_PKTQUEUE_H
