//
// Created by tretdm on 20.04.2024.
//

#include "audiodec.h"

#include <../utils/android.h>

#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

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

    /*AVFrame             *pFrame                 = av_frame_alloc();
    int                 AUDIO_INBUF_SIZE        = 4096;
    int                 decodedDataSize         = 0,
                        packetSize              = avPkt.size,
                        status                  = 0,
                        tAudioFrames            = 0;
    /*jclass              jmPlay                  = env->GetObjectClass(instance);
    jmethodID           renderAudioMid          = env->GetMethodID(jmPlay, "renderAudio", "([BI)V");
    short*              buffer                  = (short*)malloc(aBuffLength);
    jbyteArray          jBuffer                 = env->NewByteArray(aBuffLength);

    pFrame = av_frame_alloc();

    if (debug_mode) {
        LOGD(10, "[DEBUG] Starting audio decoder...");
    }*/

    /*while (gPlaybackState == FFMPEG_PLAYBACK_PLAYING &&
                    (status = av_read_frame(gFormatCtx, &avPkt)) >= 0) {
        int len = avcodec_decode_audio4(
                                        gAudioCodecCtx,
                                        pFrame,
                                        &status,
                                        &avPkt
                                       );

        if (status) {
                int dataSize = av_samples_get_buffer_size(NULL,
                                                              gAudioCodecCtx->channels,
                                                              pFrame->nb_samples,
                                                              gAudioCodecCtx->sample_fmt,
                                                              1);

            if (debug_mode) {
                LOGD(10, "[DEBUG] Decoding audio frame #%d... | Length: %d of %d",
                    tAudioFrames + 1, dataSize, aBuffLength
                );
            }

            buffer = (short *) pFrame->data[0];
            env->SetByteArrayRegion(jBuffer, 0, (jsize) dataSize / gAudioCodecCtx->channels, (jbyte *) buffer);
            env->CallVoidMethod(instance, renderAudioMid, jBuffer, dataSize / gAudioCodecCtx->channels);
        }

        tAudioFrames++;

        av_free_packet(&avPkt);
        av_packet_unref(&avPkt);
    }

    while (gPlaybackState == FFMPEG_PLAYBACK_PLAYING &&
                    (status = av_read_frame(gFormatCtx, &avPkt)) >= 0) {
        if(avPkt->stream_index == pStreamIndex) {

        }
    }*/
}
