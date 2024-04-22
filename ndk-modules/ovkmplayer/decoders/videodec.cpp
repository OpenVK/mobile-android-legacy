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
	return true;
}

static void *s_decodeInThread(void *arg) {
    return ((VideoDecoder*) arg)->decodeInThread();
}

void *VideoDecoder::decodeInThread() {
    AVPacket            avPkt;
    int                 vWidth = gCodecCtx->width,
                        vHeight = gCodecCtx->height,
                        status, len,
                        dataSize = avpicture_get_size(AV_PIX_FMT_RGB32, vWidth, vHeight),
                        packetSize, tVideoFrames;
    struct SwsContext   *img_convert_ctx = NULL;

    gBuffer = (short*) av_mallocz((size_t)dataSize);

    while(av_read_frame(gFormatCtx, &avPkt)>=0) {
        gFrame = avcodec_alloc_frame();
        // It is from the video stream?
        if(avPkt.stream_index == gStreamIndex) {
            packetSize = avPkt.size;
            struct SwsContext *img_convert_ctx = NULL;
            avpicture_fill((AVPicture*) gFrame,
                (const uint8_t*) gBuffer,
                gCodecCtx->pix_fmt,
                gCodecCtx->width,
                gCodecCtx->height
            );

            avcodec_decode_video2(gCodecCtx, gFrame, &status, &avPkt);
            if(!status || gFrame == NULL || packetSize == 0) {
                tVideoFrames++;
                continue;
            }
            AVPixelFormat pxf;

            pxf = AV_PIX_FMT_BGR32;

            convertYuv2Rgb(pxf, gFrame, dataSize);
            tVideoFrames++;
            gInterface->onStreamDecoding((uint8_t*)gBuffer, dataSize, gStreamIndex);
        }
        av_free(gFrame);
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&avPkt);
        av_packet_unref(&avPkt);
    }

            av_free(gBuffer);

    stop();
}

bool VideoDecoder::start() {
    decodeInThread();
    return true;
}

short* VideoDecoder::convertYuv2Rgb(AVPixelFormat pxf, AVFrame* frame, int length) {
    AVFrame         *frameRGB   = av_frame_alloc();
    AVPixelFormat   output_pxf  = pxf;

    avpicture_fill((AVPicture *)frameRGB, (uint8_t*)gBuffer, output_pxf,
                   gCodecCtx->width, gCodecCtx->height);
    const int width = gCodecCtx->width, height = gCodecCtx->height;
    SwsContext* img_convert_ctx = sws_getContext(width, height,
                                     gCodecCtx->pix_fmt,
                                     width, height, output_pxf, SWS_BICUBIC,
                                     NULL, NULL, NULL);


    if(img_convert_ctx == NULL) {
        LOGE(10, "Cannot initialize the conversion context!");
        sws_freeContext(img_convert_ctx);
        return NULL;
    }

    int ret = sws_scale(img_convert_ctx, (const uint8_t* const*)frame->data, frame->linesize, 0,
                        gCodecCtx->height, frameRGB->data, frameRGB->linesize);
    if(frameRGB->data[0] == NULL) {
        LOGE(10, "SWS_Scale failed");
    }
    av_free(frameRGB);
    av_frame_unref(frameRGB);
    sws_freeContext(img_convert_ctx);
    return gBuffer;
}

bool VideoDecoder::stop() {
    av_free(gFrame);
    avcodec_close(gCodecCtx);
    return true;
}
