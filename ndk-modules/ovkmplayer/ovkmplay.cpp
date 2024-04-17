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
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>

#include <android.h>

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

// Android implementations headers
#include <android/log.h>

// FFmpeg implementation headers (using LGPLv3.0 model)
extern "C" {
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

/*for Android logs*/
#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char                version[7]                  = "0.0.1";
char                *gFileName;	                // file name of the video
int                 gErrorCode;

AVFormatContext     *gFormatCtx;

int                 gVideoStreamIndex;          // video stream index
int                 gAudioStreamIndex;          // audio stream index

AVCodecContext      *gVideoCodecCtx;
AVCodecContext      *gAudioCodecCtx;

AVCodec             *gVideoCodec;
AVCodec             *gAudioCodec;


bool                debug_mode;
int                 gMinAudioBufferSize;

AVDictionary        *avFormatOptions            = NULL;
AVDictionary        *avCodecOptions             = NULL;

jint                gPlaybackState;
jint                FFMPEG_PLAYBACK_STOPPED     = 0;
jint                FFMPEG_PLAYBACK_PLAYING     = 1;
jint                FFMPEG_PLAYBACK_PAUSED      = 2;
int                 gFramesCount;

struct SwrContext   *swrCtx;

int gFrameCount;

JNIEXPORT void JNICALL naInit(JNIEnv *env, jobject instance) {
    if(debug_mode) {
        LOGD(10, "[DEBUG] Initializing FFmpeg...");
    }
    av_register_all();
    avcodec_register_all();
    avformat_network_init();
    gFormatCtx = avformat_alloc_context();
}

JNIEXPORT jstring JNICALL naShowLogo(JNIEnv *env, jobject instance) {
   char logo[256] = "Logo";
   sprintf(logo, "OpenVK Media Player ver. %s for Android"
                 "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                 "licensed under AGPLv3 or later version."
                 "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
   return env->NewStringUTF(logo);
}

JNIEXPORT void JNICALL naSetDebugMode(JNIEnv *env, jobject instance, jboolean value) {
    if(value == JNI_TRUE) {
        debug_mode = true;
        LOGD(10, "[DEBUG] Enabled Debug Mode");
    } else {
        LOGD(10, "[DEBUG] Disabled Debug Mode");
    }
}

JNIEXPORT void JNICALL naSetMinAudioBufferSize(JNIEnv *env, jobject instance, jint minBuffSize) {
    gMinAudioBufferSize = (int)minBuffSize;
}

JNIEXPORT jint JNICALL naGetPlaybackState(JNIEnv *env, jobject instance) {
    return (jint)gPlaybackState;
}

JNIEXPORT jint JNICALL naOpenFile(JNIEnv *env, jobject instance, jstring filename) {
    AVDictionary    *optionsDict = NULL;
    AVFrame         *decodedFrame = NULL;
    AVFrame         *frameRGBA = NULL;
    jobject			bitmap;
    int             result,
                    aCodecResult,
                    vCodecResult;
    char            errorString[192];

    gFileName = (char *)env->GetStringUTFChars(filename, NULL);
    if((result = avformat_open_input(&gFormatCtx, gFileName, NULL, NULL))!=0){
        if(result == -2) {
            sprintf(errorString, "File not found");
        } else if (av_strerror(result, errorString, 192) < 0) {
            strerror_r(-gErrorCode, errorString, 192);
        }
        LOGE(10, "[ERROR] Cannot open file %s (%d, %s)", gFileName, result, errorString);
        return -1;
    }

    if(avformat_find_stream_info(gFormatCtx, NULL)<0){
        LOGE(10, "FAILED to find stream info %s", gFileName);
        return -1;
    }

    av_dump_format(gFormatCtx, 0, gFileName, 0);

    gVideoStreamIndex = -1;
    for(int i = 0; i<gFormatCtx->nb_streams; i++) {
        if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            gVideoStreamIndex = i;
        } else if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            gAudioStreamIndex = i;
        }
    }
    if(gVideoStreamIndex == -1){
        LOGE(10, "Didn't find a video stream");
    } else if(gAudioStreamIndex == -1){
        LOGE(10, "Didn't find a audio stream");
    } else if(gVideoStreamIndex == -1 && gAudioStreamIndex == -1){
        LOGE(10, "Media streams not found");
        return -1;
    }

    if(gVideoStreamIndex != -1) {
        gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
        gVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
        if(gVideoCodec==NULL) {
            LOGE(10, "Unsupported video codec");
            vCodecResult = -1;
        } else if(avcodec_open2(gVideoCodecCtx, gVideoCodec, &optionsDict)<0){
            LOGE(10, "Could not open video codec");
            vCodecResult = -1;
        }
    }
    if(gAudioStreamIndex != -1) {
        gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIndex]->codec;
        gAudioCodec = avcodec_find_decoder(gAudioCodecCtx->codec_id);
        if(gAudioCodec == NULL) {
            LOGE(10, "Unsupported audio codec");
            aCodecResult = -1;
        } else if(avcodec_open2(gAudioCodecCtx, gAudioCodec, &optionsDict)<0){
           LOGE(10, "Could not open audio codec");
           aCodecResult = -1;
        }
    }

    if(aCodecResult != -1 || vCodecResult != -1) {
        return 0;
    } else {
        return -1;
    }
}

JNIEXPORT uint8_t* JNICALL convertYuv2Rgb(AVPixelFormat pxf, AVFrame* frame, int length) {
    uint8_t *buffer = (uint8_t*) malloc((size_t)length);
    AVFrame* frameRGB = av_frame_alloc();

    AVPixelFormat output_pxf = pxf;

    avpicture_fill((AVPicture *)frameRGB, (uint8_t*)buffer, output_pxf,
                   gVideoCodecCtx->width, gVideoCodecCtx->height);
    const int width = gVideoCodecCtx->width, height = gVideoCodecCtx->height;
    SwsContext* img_convert_ctx = sws_getContext(width, height,
                                     gVideoCodecCtx->pix_fmt,
                                     width, height, output_pxf, SWS_BICUBIC,
                                     NULL, NULL, NULL);


    if(img_convert_ctx == NULL) {
        LOGE(10, "Cannot initialize the conversion context!");
        sws_freeContext(img_convert_ctx);
        return NULL;
    }

    int ret = sws_scale(img_convert_ctx, (const uint8_t* const*)frame->data, frame->linesize, 0,
                        gVideoCodecCtx->height, frameRGB->data, frameRGB->linesize);
    if(frameRGB->data[0] == NULL) {
        LOGE(10, "SWS_Scale failed");
    }
    av_free(frameRGB);
    av_frame_unref(frameRGB);
    sws_freeContext(img_convert_ctx);
    return buffer;
}

JNIEXPORT void JNICALL decodeAudioFromPacket(       // Decoding audio packets
    JNIEnv* env, jobject instance, jclass jmplay,
    AVPacket avPkt, int tAudioFrames,
    uint8_t* buffer, jbyteArray buffer2,
    int audioBufferLength
) {

    AVFrame             *pFrame                 = av_frame_alloc();
    int                 AUDIO_INBUF_SIZE        = 4096;
    int                 dataSize                = 192000 * 4;
    int                 decodedDataSize         = 0;
    int                 packetSize              = avPkt.size;
    int                 status                  = 0;
    jmethodID           renderAudioMid          = env->GetMethodID(jmplay, "renderAudio", "([BI)V");

    // Allocate audio frame
    pFrame = av_frame_alloc();
    int len = avcodec_decode_audio4(
                                            gAudioCodecCtx,
                                            pFrame,
                                            &status,
                                            &avPkt
                                           );
    if (status) {
        int sampleSize = av_samples_get_buffer_size(NULL,
                                                       gAudioCodecCtx->channels,
                                                       pFrame->nb_samples,
                                                       gAudioCodecCtx->sample_fmt,
                                                       1);
        if(debug_mode) {
            LOGD(10, "[DEBUG] Decoding audio frame #%d | Length: %d of %d",
                     tAudioFrames + 1, len, packetSize);
        }
        buffer = (uint8_t *) pFrame->data[0];
        env->SetByteArrayRegion(buffer2, 0, (jsize) sampleSize, (jbyte *) buffer);
        env->CallVoidMethod(instance, renderAudioMid, buffer2, sampleSize);
    }
    //env->ReleaseByteArrayElements(buffer2, (jbyte *) buffer, 0);
    av_free(pFrame);
    av_frame_unref(pFrame);
    tAudioFrames++;
}

JNIEXPORT void JNICALL decodeVideoFromPacket(       // Decoding video packets
    JNIEnv* env, jobject instance, jclass jmplay,
    AVPacket avPkt, int tVideoFrames, uint8_t* buffer,
    jbyteArray buffer2, int videoBufferLength
) {
    int         vWidth                  = gVideoCodecCtx->width;
    int         vHeight                 = gVideoCodecCtx->height;
    int         dataSize                = avpicture_get_size(AV_PIX_FMT_RGB32, vWidth, vHeight);
    int         decodedDataSize         = 0;
    int         packetSize              = avPkt.size;
    int         frameDecoded;
    jmethodID   renderVideoMid          = env->GetMethodID(jmplay, "renderVideo", "([BI)V");
    AVFrame     *pFrame                 = av_frame_alloc(),
                *pFrameRGB              = av_frame_alloc();

    avpicture_fill((AVPicture*) pFrame,
                       (const uint8_t*) buffer,
                       gVideoCodecCtx->pix_fmt,
                       gVideoCodecCtx->width,
                       gVideoCodecCtx->height
    );

    if (avPkt.stream_index == gVideoStreamIndex) {
        int size = avPkt.size;
        if (debug_mode) {
            LOGD(10, "[DEBUG] Decoding video frame #%d... | Length: %d", tVideoFrames, avPkt.size);
        }

        struct SwsContext *img_convert_ctx = NULL;
        avcodec_decode_video2(gVideoCodecCtx, pFrame, &frameDecoded, &avPkt);

        if (!frameDecoded || pFrame == NULL) {
            if (debug_mode) {
                LOGE(10, "[ERROR] Frame #%d not decoded.", tVideoFrames - 1);
            }
            return;
        }

        try {
            AVPixelFormat pxf;

            // RGB565 by default for Android Canvas in pre-Gingerbread devices.
            if(android::getApiLevel(env) >= OS_CODENAME_GINGERBREAD) {
                pxf = AV_PIX_FMT_BGR32;
            } else {
                pxf = AV_PIX_FMT_RGB565;
            }

            buffer = (uint8_t*) convertYuv2Rgb(pxf, pFrame, videoBufferLength);

            if(buffer == NULL) {
                LOGE(10, "[ERROR] Conversion failed");
                return;
            }

            env->SetByteArrayRegion(buffer2, 0, (jsize) videoBufferLength, (jbyte *) buffer);
            env->CallVoidMethod(instance, renderVideoMid, buffer2, videoBufferLength);
        } catch (...) {
            if (debug_mode) {
                LOGE(10, "[ERROR] Render video frames failed");
                return;
            }
        }
    }

    tVideoFrames++;
    gFramesCount = tVideoFrames;
    av_free(pFrame);
    av_frame_unref(pFrame);
    env->ReleaseByteArrayElements(buffer2, (jbyte *) buffer, 0); // clear buffer before next frame
}

JNIEXPORT void JNICALL naPause(JNIEnv *env, jobject instance) {
    gPlaybackState = FFMPEG_PLAYBACK_PAUSED;
}

JNIEXPORT void JNICALL naStop(JNIEnv *env, jobject instance) {
    gPlaybackState = FFMPEG_PLAYBACK_STOPPED;
    if(gVideoStreamIndex != -1) {
        avcodec_close(gVideoCodecCtx);
    }
    if(gAudioStreamIndex != -1) {
        avcodec_close(gAudioCodecCtx);
    }
    avformat_close_input(&gFormatCtx);
}

int decodeMediaFile(JNIEnv* env, jobject instance) {
    if(debug_mode) {
        LOGD(10, "[DEBUG] Decoding audio stream #%d and video stream #%d...",
                    gAudioStreamIndex + 1, gVideoStreamIndex + 1
        )
    }

    int             aBuffLength                 =   8192;
    int             vWidth                      =   0;
    int             vHeight                     =   0;

    if(gVideoStreamIndex != -1) {
            vWidth = gVideoCodecCtx->width;
            vHeight = gVideoCodecCtx->height;
    } else {
            vWidth = 320;
            vHeight = 240;
    }

    jclass          jmplay = env->GetObjectClass(instance);
    jmethodID       completePlayback            = env->GetMethodID(jmplay, "completePlayback", "()V");
    AVPacket        avPkt;
    uint8_t         *audioBuffer,
                    *videoBuffer;
    int             receivedFrame               =   0;
    int             tAudioFrames                =   0;
    int             tVideoFrames                =   0;
    int             status                      =  -1;
    int             vBuffLength                 = avpicture_get_size(AV_PIX_FMT_RGB32, vWidth, vHeight);
    av_init_packet(&avPkt);
    if(!gVideoCodec && !gAudioCodec) {
        return -1;
    }

    jbyteArray jVideoBuffer = env->NewByteArray((jsize) vBuffLength);;
    jbyteArray jAudioBuffer = env->NewByteArray((jsize) aBuffLength);

    while (gPlaybackState == FFMPEG_PLAYBACK_PLAYING &&
    (status = av_read_frame(gFormatCtx, &avPkt)) >= 0) {
        if(avPkt.size > 0) {
            if (avPkt.stream_index == gAudioStreamIndex) {
                if(gAudioStreamIndex != -1) {
                    audioBuffer = (uint8_t*)malloc(aBuffLength);
                    decodeAudioFromPacket(
                        env, instance, jmplay,
                        avPkt, tAudioFrames++,
                        audioBuffer, jAudioBuffer,
                        aBuffLength
                    );
                    free(audioBuffer);
                }
            } else if(avPkt.stream_index == gVideoStreamIndex) {
                if(gVideoStreamIndex != -1) {
                    videoBuffer = (uint8_t*)malloc(vBuffLength);
                    decodeVideoFromPacket(
                        env, instance, jmplay,
                        avPkt, tVideoFrames++,
                        videoBuffer, jVideoBuffer,
                        vBuffLength
                    );
                    free(videoBuffer);
                }
            }
        }

        // Clear packet from memory
        av_free_packet(&avPkt);
        av_packet_unref(&avPkt);
    }
    jmethodID completePbMid = env->GetMethodID(jmplay, "completePlayback", "()V");
    env->CallVoidMethod(instance, completePbMid);
    env->DeleteLocalRef(jmplay);
    env->DeleteLocalRef(jAudioBuffer);
    env->DeleteLocalRef(jVideoBuffer);
    if(status < 0) {
        naStop(env, instance);
    }
    return 0;
}

JNIEXPORT jint JNICALL naPlay(JNIEnv *env, jobject instance) {
    gPlaybackState = FFMPEG_PLAYBACK_PLAYING;
    return (jint)decodeMediaFile(env, instance);
}

JNIEXPORT jobject JNICALL naGenerateTrackInfo(
        JNIEnv* env, jobject instance, jint type
) {

    jclass track_class;
    try {
        AVStream *pStream;
        if (type == AVMEDIA_TYPE_VIDEO) {
            if(gVideoStreamIndex != -1) {
                pStream = gFormatCtx->streams[gVideoStreamIndex];
                // Load OvkVideoTrack class
                track_class = env->FindClass(
                        "uk/openvk/android/legacy/utils/media/OvkVideoTrack"
                );
                // Load OvkVideoTrack class method
                jmethodID video_track_init = env->GetMethodID(
                        track_class, "<init>", "()V"
                );
                jfieldID codec_name_field = env->GetFieldID(
                        track_class, "codec_name", "Ljava/lang/String;"
                );
                jfieldID frame_size_field = env->GetFieldID(track_class, "frame_size", "[I");
                jfieldID bitrate_field = env->GetFieldID(
                        track_class, "bitrate", "J"
                );
                jfieldID frame_rate_field = env->GetFieldID(
                        track_class, "frame_rate", "F"
                );

                jobject track = env->NewObject(track_class, video_track_init);

                // Load OvkVideoTrack values form fields (class variables)
                env->SetObjectField(track, codec_name_field, env->NewStringUTF(gVideoCodec->name));
                jintArray array = (jintArray) env->GetObjectField(track, frame_size_field);
                jint *frame_size = env->GetIntArrayElements(array, 0);
                frame_size[0] = gVideoCodecCtx->width;
                frame_size[1] = gVideoCodecCtx->height;
                env->ReleaseIntArrayElements(array, frame_size, 0);
                env->SetLongField(track, bitrate_field, gVideoCodecCtx->bit_rate);
                env->SetFloatField(track, frame_rate_field, pStream->avg_frame_rate.num);
                return track;
            } else {
                return NULL;
            }
        } else {
            if(gAudioStreamIndex != -1) {
                pStream = gFormatCtx->streams[gAudioStreamIndex];
                // Load OvkAudioTrack class
                track_class = env->FindClass(
                        "uk/openvk/android/legacy/utils/media/OvkAudioTrack"
                );
                // Load OvkVideoTrack class methods
                jmethodID audio_track_init = env->GetMethodID(
                        track_class, "<init>", "()V"
                );

                jobject track = env->NewObject(track_class, audio_track_init);

                jfieldID codec_name_field = env->GetFieldID(
                        track_class, "codec_name", "Ljava/lang/String;"
                );
                jfieldID sample_rate_field = env->GetFieldID(
                        track_class, "sample_rate", "J"
                );
                jfieldID bitrate_field = env->GetFieldID(
                        track_class, "bitrate", "J"
                );
                jfieldID channels_field = env->GetFieldID(
                        track_class, "channels", "I"
                );

                // Load OvkAudioTrack values form fields (class variables)
                env->SetObjectField(track, codec_name_field, env->NewStringUTF(gAudioCodec->name));
                env->SetLongField(track, sample_rate_field, gAudioCodecCtx->sample_rate);
                env->SetLongField(track, bitrate_field, gAudioCodecCtx->bit_rate);
                env->SetIntField(track, channels_field, gAudioCodecCtx->channels);
                return track;
            } else {
                return NULL;
            }
        }
    } catch (...) {
        if(debug_mode) {
            LOGE(1, "[ERROR] Track not found");
        }
        return NULL;
    }
    return NULL;
}
#pragma clang diagnostic pop

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[10];
	nm[0].name = "naInit";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naInit;

	nm[1].name = "naShowLogo";
    nm[1].signature = "()Ljava/lang/String;";
    nm[1].fnPtr = (void*)naShowLogo;

    nm[2].name = "naSetDebugMode";
    nm[2].signature = "(Z)V";
    nm[2].fnPtr = (void*)naSetDebugMode;

    nm[3].name = "naOpenFile";
    nm[3].signature = "(Ljava/lang/String;)I";
    nm[3].fnPtr = (void*)naOpenFile;

    nm[4].name = "naGenerateTrackInfo";
    nm[4].signature = "(I)Ljava/lang/Object;";
    nm[4].fnPtr = (void*)naGenerateTrackInfo;

	nm[5].name = "naPlay";
    nm[5].signature = "()I";
    nm[5].fnPtr = (void*)naPlay;

    nm[6].name = "naPause";
    nm[6].signature = "()V";
    nm[6].fnPtr = (void*)naPause;

    nm[7].name = "naStop";
    nm[7].signature = "()V";
    nm[7].fnPtr = (void*)naStop;

    nm[8].name = "naGetPlaybackState";
    nm[8].signature = "()I";
    nm[8].fnPtr = (void*)naGetPlaybackState;

    nm[9].name = "naSetMinAudioBufferSize";
    nm[9].signature = "(I)V";
    nm[9].fnPtr = (void*)naSetMinAudioBufferSize;

	jclass cls = env->FindClass("uk/openvk/android/legacy/utils/media/OvkMediaPlayer");
	//Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 10);
	return JNI_VERSION_1_6;
}