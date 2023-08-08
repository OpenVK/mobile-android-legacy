/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This file is part of OpenVK Legacy.
 *
 * OpenVK Legacy is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

// Java/C++ standard implementations headers
#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <math.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>

// Android implementations headers
#include <android/log.h>
#include <android/bitmap.h>

// FFmpeg implementation headers (using LGPLv3.0 model)
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

/*for Android logs*/
#define LOG_TAG "OVK-MP"
#define LOG_LEVEL 10
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char version[7] = "0.0.1";
char *gFileName;	  //the file name of the video

AVFormatContext *gFormatCtx;
int gVideoStreamIndex;    //video stream index

AVCodecContext *gVideoCodecCtx;

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }

    JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_OvkMediaPlayer_getVideoInfo
            (JNIEnv *env, jobject instance, char* filename) {
        AVCodec *lVideoCodec;
        int lError;
        /*some global variables initialization*/
        LOGI(10, "get video info starts!");
        /*register the codec*/
        extern AVCodec ff_h263_decoder;
        avcodec_register(&ff_h263_decoder);
        extern AVCodec ff_h264_decoder;
        avcodec_register(&ff_h264_decoder);
        extern AVCodec ff_mpeg4_decoder;
        avcodec_register(&ff_mpeg4_decoder);
        extern AVCodec ff_mjpeg_decoder;
        avcodec_register(&ff_mjpeg_decoder);
        /*register parsers*/
        //extern AVCodecParser ff_h264_parser;
        //av_register_codec_parser(&ff_h264_parser);
        //extern AVCodecParser ff_mpeg4video_parser;
        //av_register_codec_parser(&ff_mpeg4video_parser);
        /*register demux*/
        extern AVInputFormat ff_mov_demuxer;
        av_register_input_format(&ff_mov_demuxer);
        //extern AVInputFormat ff_h264_demuxer;
        //av_register_input_format(&ff_h264_demuxer);
        /*register the protocol*/
        extern URLProtocol ff_file_protocol;
        av_register_protocol2(&ff_file_protocol, sizeof(ff_file_protocol));
        /*open the video file*/
        if ((lError = av_open_input_file(&gFormatCtx, gFileName, NULL, 0, NULL)) !=0 ) {
            LOGE(1, "Error open video file: %d", lError);
            return;	//open file failed
        }
        /*retrieve stream information*/
        if ((lError = av_find_stream_info(gFormatCtx)) < 0) {
            LOGE(1, "Error find stream information: %d", lError);
            return;
        }
        /*find the video stream and its decoder*/
        gVideoStreamIndex = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &lVideoCodec, 0);
        if (gVideoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
            LOGE(1, "Error: cannot find a video stream");
            return;
        } else {
            LOGI(10, "video codec: %s", lVideoCodec->name);
        }
        if (gVideoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
            LOGE(1, "Error: video stream found, but no decoder is found!");
            return;
        }
        /*open the codec*/
        gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
        LOGI(10, "open codec: (%d, %d)", gVideoCodecCtx->height, gVideoCodecCtx->width);
#ifdef SELECTIVE_DECODING
        gVideoCodecCtx->allow_selective_decoding = 1;
#endif
        if (avcodec_open(gVideoCodecCtx, lVideoCodec) < 0) {
            LOGE(1, "Error: cannot open the video codec!");
            return;
        }
        LOGI(10, "get video info ends");
    }
};