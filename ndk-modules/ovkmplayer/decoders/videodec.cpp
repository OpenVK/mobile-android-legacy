//
// Created by tretdm on 20.04.2024.
//

#include "videodec.h"

VideoDecoder::VideoDecoder(AVFormatContext *pFormatCtx,
                           AVCodecContext *pCodecCtx,
                           AVStream* pStream,
                           int pStreamIndex,
                           IFFmpegWrapper *pInterface) {
    gFormatCtx = pFormatCtx;
    gCodecCtx = pCodecCtx;
    gStream = pStream;
    gStreamIndex = pStreamIndex;
    gInterface = pInterface;
}

bool VideoDecoder::prepare() {
	gFrame = avcodec_alloc_frame();
	return gFrame != NULL;
}

static void *s_decodeInThread(void *arg) {
    return ((VideoDecoder*) arg)->decodeInThread();
}

void *VideoDecoder::decodeInThread() {
    int         status, dataSize, len;
    AVPacket    avPkt;

    while(av_read_frame(gFormatCtx, &avPkt)>=0) {
        // It is from the video stream?
        if(avPkt.stream_index == gStreamIndex) {

            // TODO: Implement video decoding stages
            //gInterface->onStreamDecoding((uint8_t*)gBuffer, dataSize / 2, gStreamIndex);
        }
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&avPkt);
    }

    stop();
}

bool VideoDecoder::stop() {
    av_free(gFrame);
    avcodec_close(gCodecCtx);
    return true;
}
