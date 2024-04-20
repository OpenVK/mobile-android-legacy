//
// Created by tretdm on 20.04.2024.
//

#include "audiodec.h"

#include <../utils/android.h>

#define AV_MAX_AUDIO_FRAME_SIZE 192000;

AVStream*           gStream;

AudioDecoder::AudioDecoder(AVStream* pStream, PacketQueue *pPktQueue) {
    gStream = pStream;
    gPktQueue = pPktQueue;
}

bool AudioDecoder::prepare() {
    gBufferSize = AV_MAX_AUDIO_FRAME_SIZE;
    gBuffer = (short*) av_malloc(gBufferSize);
    return gBuffer != NULL;
}

bool AudioDecoder::decode(void* ptr) {
    AVPacket            avPkt;

    while(gRunning) {
        if(gPktQueue->get(&avPkt, true) < 0) {
            gRunning = false;
            return gRunning;
        }
        /*if(!process()) {
            gRunning = false;
            return gRunning;
        }*/
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&avPkt);
    }

    av_free(gBuffer);

    return true;
}
