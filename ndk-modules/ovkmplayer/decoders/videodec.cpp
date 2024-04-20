//
// Created by tretdm on 20.04.2024.
//

#include "videodec.h"

VideoDecoder::VideoDecoder(AVStream* stream, PacketQueue *pPktQueue) {

}

bool VideoDecoder::prepare() {
	gFrame = avcodec_alloc_frame();
	return gFrame != NULL;
}

bool VideoDecoder::process(AVPacket *avPkt) {
    int	completed;
    int pts = 0;

	// TODO: Put FFmpeg decoding method here.

	if (avPkt->dts == AV_NOPTS_VALUE && gFrame->opaque
			&& *(uint64_t*) gFrame->opaque != AV_NOPTS_VALUE) {
		pts = *(uint64_t *) gFrame->opaque;
	} else if (avPkt->dts != AV_NOPTS_VALUE) {
		pts = avPkt->dts;
	} else {
		pts = 0;
	}
	pts *= av_q2d(gStream->time_base);

	if (completed) {
		//pts = synchronize(gFrame, pts);
		//onDecode(gFrame, pts);
	}
	return completed;
}

bool VideoDecoder::decode() {
	AVPacket        avPkt;

    while(gRunning) {
        if(gPktQueue->get(&avPkt, true) < 0) {
            gRunning = false;
            return gRunning;
        }
        if(!process(&avPkt)) {
            gRunning = false;
            return gRunning;
        }
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&avPkt);
    }

    // Free the RGB image
    av_free(gFrame);

	return true;
}

int VideoDecoder::getBuffer(struct AVCodecContext *pCodecCtx, AVFrame *pFrame) {
	int ret = avcodec_default_get_buffer(pCodecCtx, pFrame);
	uint64_t *pts = (uint64_t *)av_malloc(sizeof(uint64_t));
	*pts = gPktPts;
	pFrame->opaque = pts;
	return ret;
}

void VideoDecoder::releaseBuffer(struct AVCodecContext *pCodecCtx, AVFrame *pFrame) {
	if(pFrame)
	    av_freep(&pFrame->opaque);
	avcodec_default_release_buffer(pCodecCtx, pFrame);
}
