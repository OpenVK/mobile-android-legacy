//
// Created by tretdm on 20.04.2024.
//

#include "audiodec.h"

#include <../utils/android.h>

#include <stdio.h>
#include <math.h>

#define AV_MAX_AUDIO_FRAME_SIZE 192000;

AVStream*           gStream;

AudioDecoder::AudioDecoder(AVFormatContext *pFormatCtx,
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

bool AudioDecoder::prepare() {
    gBufferSize = AV_MAX_AUDIO_FRAME_SIZE;
    gBuffer = (short*) av_malloc(gBufferSize);
    gSwrCtx = swr_alloc();
    gSwrCtx = swr_alloc_set_opts(
        gSwrCtx, (int64_t) gCodecCtx->channel_layout, AV_SAMPLE_FMT_S16,
        gCodecCtx->sample_rate, gCodecCtx->channel_layout,
        gCodecCtx->sample_fmt, gCodecCtx->sample_rate, 0, NULL
    );
    swr_init(gSwrCtx);
    return gBuffer != NULL;
}

static void *s_decodeInThread(void *arg) {
    return ((AudioDecoder*) arg)->decodeInThread();
}

void *AudioDecoder::decodeInThread() {
    int         status, dataSize, len;
    AVPacket    avPkt;
    AVFrame     *pFrame     = av_frame_alloc();

    while(av_read_frame(gFormatCtx, &avPkt)>=0) {
        // It is from the audio stream?
        if(avPkt.stream_index == gStreamIndex) {
            len = avcodec_decode_audio4(gStream->codec, pFrame, &status, &avPkt);
            if(len < 0) {
            	break;
            }
            if (status) {
                dataSize = av_samples_get_buffer_size(
                    NULL, gCodecCtx->channels, pFrame->nb_samples,
                    gCodecCtx->sample_fmt, 1
                );
                uint8_t* buffer = (uint8_t*)av_malloc(sizeof(uint8_t) * dataSize);
                swr_convert(
                    gSwrCtx, &buffer, dataSize,
                    (const uint8_t **) pFrame->data,
                    pFrame->nb_samples
                );
                memcpy(gBuffer, buffer, dataSize);
                av_free(buffer);
                gInterface->onStreamDecoding((uint8_t*)gBuffer, dataSize / 2, gStreamIndex);
            }
        }
        // Free the packet that was allocated by av_read_frame
        av_free_packet(&avPkt);
        av_packet_unref(&avPkt);
    }
    av_free(pFrame);
    stop();
}

bool AudioDecoder::start() {
    decodeInThread();
    return true;
}

bool AudioDecoder::stop() {
    free(gBuffer);
    swr_free(&gSwrCtx);
    avcodec_close(gCodecCtx);
    return true;
}

double AudioDecoder::getPacketTime(AVPacket avPkt) {
    return (avPkt.dts - gStream->start_time) * av_q2d(gStream->time_base);
}
