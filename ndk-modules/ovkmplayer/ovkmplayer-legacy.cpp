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
    #include <libavutil/pixfmt.h>
}

/*for Android logs*/
#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char version[7] = "0.0.1";
char *gFileName;	  //the file name of the video
int gErrorCode;

AVFormatContext *gFormatCtx;
AVFormatContext *gTempFormatCtx;
int gVideoStreamIndex;    // video stream index
int gAudioStreamIndex;    // audio stream index

AVCodecContext *gVideoCodecCtx;
AVCodecContext *gAudioCodecCtx;

jobject generateTrackInfo(JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type);
jobject createBitmap(JNIEnv* env, int pWidth, int pHeight);

bool debug_mode;

AVDictionary *avFormatOptions = NULL;
AVDictionary *avCodecOptions = NULL;

jint g_playbackState;
jint FFMPEG_PLAYBACK_STOPPED = 0;
jint FFMPEG_PLAYBACK_PLAYING = 1;
jint FFMPEG_PLAYBACK_PAUSED = 2;

int gFrameCount;

AVCodec *gVideoCodec;
AVCodec *gAudioCodec;

extern "C" {
JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_initFFmpeg(JNIEnv *env, jobject instance) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Initializing FFmpeg...");
        }
        av_register_all();
        avcodec_register_all();
    }

    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char logo[256] = "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android [Legacy Mode]"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_setPlaybackState
            (JNIEnv *env, jobject instance, jint state) {
        g_playbackState = state;
        if(state == FFMPEG_PLAYBACK_PLAYING) {
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Playing\"...");
            }
        } else if(state == FFMPEG_PLAYBACK_PAUSED){
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Paused\"...");
            }
        } else if(state == FFMPEG_PLAYBACK_STOPPED) {
            if(debug_mode) {
                LOGD(1, "[DEBUG] Setting playback state to \"Stopped\"...");
            }
        }
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getPlaybackState
            (JNIEnv *env, jobject instance) {
        return g_playbackState;
    }

    JNIEXPORT void JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_setDebugMode(
            JNIEnv *env, jobject instance, jboolean value
    ) {
        if(value == JNI_TRUE) {
            debug_mode = true;
            LOGD(10, "[DEBUG] Enabled Debug Mode");
        } else {
            LOGD(10, "[DEBUG] Disabled Debug Mode");
        }
    }

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile(
            JNIEnv *env, jobject instance,
            jstring filename_) {
        const char *filename = env->GetStringUTFChars(filename_, 0);
        if(filename_ == NULL) {
            LOGE(1, "[ERROR] Invalid filename");
            return -10;
        }
        gFileName = (char *) filename;
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening file %s...", filename);
        }

        gFormatCtx = avformat_alloc_context();

        if ((gErrorCode = avformat_open_input(&gFormatCtx, filename, NULL, 0)) != 0) {
            char error_string[192];
            if(gErrorCode == -2) {
                sprintf(error_string, "File not found");
            } else {
                if (av_strerror(gErrorCode, error_string, 192) < 0) {
                    strerror_r(-gErrorCode, error_string, 192);
                }
            }
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't open file: %d (%s)", gErrorCode, error_string);
            }
            return gErrorCode;    //open file failed
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Searching A/V streams...", filename);
        }
        /*retrieve stream information*/
        if ((gErrorCode = av_find_stream_info(gFormatCtx)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            return gErrorCode;
        }
    }

    AVFormatContext* openTempFile(char* filename) {
        if(filename == NULL) {
            LOGE(1, "[ERROR] Invalid filename");
            return NULL;
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Opening temporary file %s...", filename);
        }
        if ((gErrorCode = av_open_input_file(&gTempFormatCtx, filename, NULL, 0, NULL)) != 0) {
            char error_string[192];
            if(gErrorCode == -2) {
                sprintf(error_string, "File not found");
            } else {
                if (av_strerror(gErrorCode, error_string, 192) < 0) {
                    strerror_r(-gErrorCode, error_string, 192);
                }
            }
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't open file: %d (%s)", gErrorCode, error_string);
            }
            return NULL;    //open file failed
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Searching A/V streams...");
        }
        /*retrieve stream information*/
        if ((gErrorCode = av_find_stream_info(gTempFormatCtx)) < 0) {
            char error_string[192];
            av_strerror(gErrorCode, error_string, 192);
            if(debug_mode) {
                LOGE(1, "[ERROR] Can't find stream information: %s (%d)", error_string, gErrorCode);
            }
            return NULL;
        }
        return gTempFormatCtx;
    }

    JNIEXPORT void JNICALL
        Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_decodeVideo
                (JNIEnv *env, jobject instance, jbyteArray buffer, jint length) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Decoding video stream #%d...", gVideoStreamIndex)
        }
        AVPacket avpkt;
        AVDictionaryEntry *e;
        AVFrame *pFrame, *pFrameRGB;
        jclass mplayer_class = env->GetObjectClass(instance);
        jmethodID renderVideoFrames = env->GetMethodID(mplayer_class, "renderVideoFrames", "([BI)V");
        void *bitmap_buffer = malloc(
                (size_t)gVideoCodecCtx->width * gVideoCodecCtx->height * 24);
        int frameDecoded;
        int received_frame = 0;
        int total_frames = 0;
        e = NULL;
        int decoded_data_size = (gVideoCodecCtx->width * gVideoCodecCtx->height * 3) / 2;
        pFrame = avcodec_alloc_frame();
        // Allocate an AVFrame structure
        pFrameRGB = avcodec_alloc_frame();
        if(pFrameRGB == NULL)
            return;
        avcodec_get_frame_defaults(pFrame);
        if(debug_mode) {
            LOGD(10, "[DEBUG] AVPacket initializing...")
        }
        av_init_packet(&avpkt);

        int data_size = avcodec_decode_video2(gVideoCodecCtx,
                                    pFrame, &frameDecoded, &avpkt);
        int read_frame_status = -1;

        avpicture_fill((AVPicture *)pFrameRGB, (uint8_t*)bitmap_buffer, PIX_FMT_RGBA,
                       gVideoCodecCtx->width, gVideoCodecCtx->height);
        while (av_read_frame(gFormatCtx, &avpkt) >= 0) {
            data_size = pFrame->width * pFrame->height * 4;
            if (avpkt.stream_index == gVideoStreamIndex) {
                if (debug_mode) {
                    LOGD(10, "[DEBUG] Decoding video frame #%d... | Length: %d", total_frames,
                         avpkt.size);
                }
                total_frames++;
                avcodec_decode_video2(gVideoCodecCtx, pFrame, &frameDecoded, &avpkt);
                if (!frameDecoded) {
                    if (debug_mode) {
                        LOGE(10, "[ERROR] Frame #%d not decoded.", total_frames - 1);
                    }
                    break;
                } else {
                    if (debug_mode) {
                        LOGD(10, "[DEBUG] Converting video frame to RGB...");
                    }
                }

                SwsContext* conversion = sws_getContext(gVideoCodecCtx->width,
                                                        gVideoCodecCtx->height,
                                                        (PixelFormat)pFrame->format,
                                                        gVideoCodecCtx->width,
                                                        gVideoCodecCtx->height,
                                                        PIX_FMT_RGBA,
                                                        SWS_FAST_BILINEAR,
                                                        NULL,
                                                        NULL,
                                                        NULL);
                sws_scale(conversion, reinterpret_cast<const uint8_t *const *>(pFrame->data),
                          pFrame->linesize, 0,
                          gVideoCodecCtx->height, pFrameRGB->data, pFrameRGB->linesize);
                sws_freeContext(conversion);

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Setting pFrameRGB...");
                }

                pFrameRGB->format = PIX_FMT_RGBA;
                pFrameRGB->width = pFrame->width;
                pFrameRGB->height = pFrame->height;

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Calling renderVideoFrames method to OvkMediaPlayer...");
                }

                buffer = env->NewByteArray((jsize)data_size);
                env->SetByteArrayRegion(buffer, 0, (jsize)data_size, (jbyte*)pFrameRGB->data[0]);
                env->CallVoidMethod(instance, renderVideoFrames, buffer, length);
                env->DeleteLocalRef(buffer);

                received_frame++;
            }
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Audio decoding result:\r\nReceived frames: %d | Total frames: %d",
                 received_frame, total_frames);
        }
    }

    JNIEXPORT void JNICALL
        Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_decodeAudio
                (JNIEnv *env, jobject instance, jbyteArray buffer, jint length) {
        if(debug_mode) {
            LOGD(10, "[DEBUG] Decoding audio stream #%d...", gAudioStreamIndex)
        }
        jclass mplayer_class = env->GetObjectClass(instance);
        jmethodID renderAudio = env->GetMethodID(mplayer_class, "renderAudio", "([BI)V");
        jmethodID completePlayback = env->GetMethodID(mplayer_class, "completePlayback", "()V");
        int AUDIO_INBUF_SIZE = 4096;
        int output_size;
        int data_size = AVCODEC_MAX_AUDIO_FRAME_SIZE * 4;
        int decoded_data_size = 0;
        short *output_buf, input_buf[AUDIO_INBUF_SIZE + FF_INPUT_BUFFER_PADDING_SIZE];
        AVPacket avpkt;
        int received_frame = 0;
        int total_frames = 0;
        if(debug_mode) {
            LOGD(10, "[DEBUG] AVPacket initializing...")
        }
        av_init_packet(&avpkt);
        if(!gAudioCodec) {
            return;
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Output buffer allocating...");
        }
        output_buf = (short*) malloc((size_t)data_size);
        int read_frame_status = -1;
        avpkt.data = (uint8_t*) input_buf;
        while ((read_frame_status = av_read_frame(gFormatCtx, &avpkt)) >= 0) {
            data_size = AVCODEC_MAX_AUDIO_FRAME_SIZE * 4;
            if (avpkt.stream_index == gAudioStreamIndex) {
                int size = avpkt.size;
                while(size > 0) {
                    int len = avcodec_decode_audio3(gAudioCodecCtx, output_buf, &data_size, &avpkt);
                    if(len < 0) {
                        break;
                    } else {
                        if(debug_mode) {
                            LOGD(10, "[DEBUG] Decoding frame #%d | Length: %d of %d",
                                 total_frames + 1, len, size);
                        }
                        received_frame++;
                    }
                    buffer = env->NewByteArray((jsize)data_size);
                    env->SetByteArrayRegion(buffer, 0, (jsize)data_size, (jbyte*)output_buf);
                    env->CallVoidMethod(instance, renderAudio, buffer, length);
                    env->DeleteLocalRef(buffer);
                    size -= len;
                }

                total_frames++;
            }
        }
        if(read_frame_status < 0) {
            total_frames++;
        }
        if(debug_mode) {
            LOGD(10, "[DEBUG] Audio decoding result:\r\nReceived frames: %d | Total frames: %d",
                 received_frame, total_frames);
        }
        env->CallVoidMethod(instance, completePlayback);
    }


    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo(
            JNIEnv *env, jobject instance,
            jstring filename_,
            jint type) {
        int videoStreamIndex = -1;    // video stream index
        int audioStreamIndex = -1;    // audio stream index

        AVCodecContext *videoCodecCtx = NULL;
        AVCodecContext *audioCodecCtx = NULL;
        const char *filename;
        if(gTempFormatCtx == NULL) {
            if (filename_ == NULL) {
                if (Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_openMediaFile
                            (env, instance, reinterpret_cast<jstring>(gFileName)) < 0) {
                    return NULL;
                } else {
                    gTempFormatCtx = gFormatCtx;
                }
            } else {
                filename = env->GetStringUTFChars(filename_, 0);
                gTempFormatCtx = openTempFile(const_cast<char *>(filename));
                if (gTempFormatCtx == NULL) {
                    return NULL;
                }
            }
        }

        if (type == 0) {
            AVCodec *lVideoCodec;
            /*some global variables initialization*/
            if(debug_mode) {
                LOGD(10, "[DEBUG] Getting video track info...");
            }

            /*find the video stream and its decoder*/
            gVideoStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1,
                                                    &lVideoCodec,
                                                    0);

            if (videoStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Cannot find a video stream");
                }
                return NULL;
            }
            if (videoStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Video stream found, but '%s' decoder is unavailable.",
                         lVideoCodec->name);
                }
                return NULL;
            }

            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d |  Video stream #%d detected. Opening...",
                     gTempFormatCtx->nb_streams, videoStreamIndex + 1);
            }

            /*open the codec*/
            gVideoCodecCtx = gTempFormatCtx->streams[videoStreamIndex]->codec;
            LOGI(10, "[INFO] Codec initialized. Reading...");
            LOGI(10, "[INFO] Video codec: %s | Frame size: %dx%d", videoCodecCtx->codec_name,
                 videoCodecCtx->height, videoCodecCtx->width);
            #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(videoCodecCtx, lVideoCodec) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the video codec!");
                }
                return NULL;
            }
            return generateTrackInfo(
                    env, gTempFormatCtx->streams[videoStreamIndex],
                    lVideoCodec, videoCodecCtx, AVMEDIA_TYPE_VIDEO
            );
        } else {
            AVCodec *lAudioCodec;

            if(debug_mode) {
                LOGD(10, "[DEBUG] Getting audio track info...");
            }

            /*find the audio stream and its decoder*/
            gAudioStreamIndex = av_find_best_stream(gTempFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1,
                                                    &lAudioCodec,
                                                    0);

            if (gAudioStreamIndex == AVERROR_STREAM_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Cannot find a audio stream");
                }
                return NULL;
            }
            if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Audio stream found, but '%s' decoder is unavailable.",
                         lAudioCodec->name);
                }
                return NULL;
            }

            /*open the codec*/
            if(debug_mode) {
                LOGD(10, "[DEBUG] Total streams: %d | Audio stream #%d detected. Opening...",
                     gTempFormatCtx->nb_streams, audioStreamIndex + 1);
            }
            LOGI(10, "[INFO] Duration: %d", gTempFormatCtx->streams[audioStreamIndex]->duration);
            audioCodecCtx = gFormatCtx->streams[audioStreamIndex]->codec;
            LOGI(10, "[INFO] Codec initialized. Reading...");
            LOGI(10, "[INFO] Audio codec: %s | Sample rate: %d Hz", audioCodecCtx->codec_name,
                 audioCodecCtx->sample_rate);
            #ifdef SELECTIVE_DECODING
                    gAudioCodecCtx->allow_selective_decoding = 1;
            #endif
            if (avcodec_open(audioCodecCtx, lAudioCodec) < 0) {
                if(debug_mode) {
                    LOGE(1, "[ERROR] Can't open the audio codec!");
                }
                return NULL;
            }
            return generateTrackInfo(
                    env, gTempFormatCtx->streams[audioStreamIndex],
                    lAudioCodec, audioCodecCtx, AVMEDIA_TYPE_AUDIO
            );
        }
        if(filename_ != NULL)
        env->ReleaseStringUTFChars(filename_, filename);
    };

    JNIEXPORT jobject JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getTrackInfo2(
            JNIEnv *env, jobject instance,
            jint type) {

        int videoStreamIndex = -1;    // video stream index
        int audioStreamIndex = -1;    // audio stream index

        const char *filename;
        try {
            if (type == 0) {
                /*some global variables initialization*/
                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting video track info...");
                }

                /*find the video stream and its decoder*/
                gVideoStreamIndex = -1;
                for(int i = 0; i < gFormatCtx->nb_streams; i++) {
                    if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                        gVideoStreamIndex = i;
                        gVideoCodecCtx = gFormatCtx->streams[i]->codec;
                        if (debug_mode) {
                            LOGD(10, "[DEBUG] Total streams: %d | Video stream #%d detected. Opening...",
                                 gFormatCtx->nb_streams, gVideoStreamIndex);
                        }
                    }
                }

                if (gVideoStreamIndex < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a video stream");
                    }
                    return NULL;
                }

                /*open the codec*/
                gVideoCodec = avcodec_find_decoder(
                        gFormatCtx->streams[gVideoStreamIndex]->codec->codec_id);
                LOGI(10, "[INFO] Codec initialized. Reading...");
                #ifdef SELECTIVE_DECODING
                    gVideoCodecCtx->allow_selective_decoding = 1;
                #endif
                if (avcodec_open(gVideoCodecCtx, gVideoCodec) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the video codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gFormatCtx->streams[gVideoStreamIndex],
                        gVideoCodec, gVideoCodecCtx, AVMEDIA_TYPE_VIDEO
                );
            } else {

                if (debug_mode) {
                    LOGD(10, "[DEBUG] Getting audio track info...");
                }

                /*find the audio stream and its decoder*/
                gAudioStreamIndex = -1;
                for(int i = 0; i < gFormatCtx->nb_streams; i++) {
                    if(gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
                        gAudioStreamIndex = i;
                        gAudioCodecCtx = gFormatCtx->streams[i]->codec;
                        if (debug_mode) {
                            LOGD(10, "[DEBUG] Total streams: %d | Audio stream #%d detected. Opening...",
                                 gFormatCtx->nb_streams, gAudioStreamIndex);
                        }
                    }
                }

                if (gAudioStreamIndex < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Cannot find a audio stream");
                    }
                    return NULL;
                }

                if (gAudioStreamIndex == AVERROR_DECODER_NOT_FOUND) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Audio stream found, but '%s' decoder is unavailable.",
                             gAudioCodec->name);
                    }
                    return NULL;
                }

                /*open the codec*/
                gAudioCodec = avcodec_find_decoder(
                        gFormatCtx->streams[gAudioStreamIndex]->codec->codec_id);
                LOGI(10, "[INFO] Codec initialized. Reading...");
                #ifdef SELECTIVE_DECODING
                                gAudioCodecCtx->allow_selective_decoding = 1;
                #endif
                if (avcodec_open(gAudioCodecCtx, gAudioCodec) < 0) {
                    if (debug_mode) {
                        LOGE(1, "[ERROR] Can't open the audio codec!");
                    }
                    return NULL;
                }
                return generateTrackInfo(
                        env, gFormatCtx->streams[gAudioStreamIndex],
                        gAudioCodec, gAudioCodecCtx, AVMEDIA_TYPE_AUDIO
                );
            }
        } catch (const char* error_message) {
            return NULL;
        }
    };

    JNIEXPORT jint JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorCode(
            JNIEnv *env, jobject instance
    ) {
        return gErrorCode;
    }

    JNIEXPORT jlong JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getFrameCount(
            JNIEnv *env, jobject instance
    ) {
        return gFrameCount;
    }


    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_media_OvkMediaPlayer_getLastErrorString(
            JNIEnv *env, jobject instance
    ) {
        char error_str[192];
        av_strerror(gErrorCode, error_str, 192);
        return env->NewStringUTF(error_str);
    }

}

jobject generateTrackInfo(
        JNIEnv* env, AVStream* pStream, AVCodec *pCodec, AVCodecContext *pCodecCtx, int type
) {
    // JNI field types (bad stuff, don't you agree?)
    // [JNI] => [java]
    // "[?"  => ?[] == int[], bool[], long[] and etc.
    // "I"   => int
    // "J"   => long (aka. int64)
    // "Z"   => boolean
    // "D"   => double
    // "F"   => float
    // "[BI" => byte array

    jclass track_class;
    try {
        if (type == AVMEDIA_TYPE_VIDEO) {
            // Load OvkVideoTrack class
            track_class = env->FindClass(
                    "uk/openvk/android/legacy/utils/media/OvkVideoTrack"
            );
            // Load OvkVideoTrack class method
            jmethodID video_track_init = env->GetMethodID(
                    track_class, "<init>", "()V"
            );
            jobject track = env->NewObject(track_class, video_track_init);

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
            jfieldID sample_rate_field = env->GetFieldID(
                    track_class, "sample_rate", "J"
            );

            // Load OvkVideoTrack values form fields (class variables)
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            jintArray array = (jintArray) env->GetObjectField(track, frame_size_field);
            jint *frame_size = env->GetIntArrayElements(array, 0);
            frame_size[0] = pCodecCtx->width;
            frame_size[1] = pCodecCtx->height;
            env->ReleaseIntArrayElements(array, frame_size, 0);
            env->SetLongField(track, bitrate_field, pCodecCtx->bit_rate);

            // Referenced from:
            // https://en.wikipedia.org/wiki/Rec.709
            // https://en.wikipedia.org/wiki/Sampling_(signal_processing)#Applications
            //
            // 1920x1080@60fps = 1920 (active) lpf * 1080 spl * 60 fps = 124.416 MHz
            long sample_rate = pCodecCtx->width * pCodecCtx->height * pStream->avg_frame_rate.num;
            env->SetLongField(track, sample_rate_field, sample_rate);

            env->SetFloatField(track, frame_rate_field, pStream->avg_frame_rate.num);
            return track;
        } else {
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
            env->SetObjectField(track, codec_name_field, env->NewStringUTF(pCodec->name));
            env->SetLongField(track, sample_rate_field, pCodecCtx->sample_rate);
            env->SetLongField(track, bitrate_field, pCodecCtx->bit_rate);
            env->SetIntField(track, channels_field, pCodecCtx->channels);
            return track;
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

jobject createBitmap(JNIEnv *env, int pWidth, int pHeight) {
    int i;
    //get Bitmap class and createBitmap method ID
    jclass javaBitmapClass = (jclass)env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetStaticMethodID(
            javaBitmapClass, "createBitmap",
            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    //create Bitmap.Config
    //reference: https://forums.oracle.com/thread/1548728
    const wchar_t* configName = L"ARGB_8888";
    int len = wcslen(configName);
    jstring jConfigName;
    if (sizeof(wchar_t) != sizeof(jchar)) {
        //wchar_t is defined as different length than jchar(2 bytes)
        jchar* str = (jchar*)malloc((len+1)*sizeof(jchar));
        for (i = 0; i < len; ++i) {
            str[i] = (jchar)configName[i];
        }
        str[len] = 0;
        jConfigName = env->NewString((const jchar*)str, len);
    } else {
        //wchar_t is defined same length as jchar(2 bytes)
        jConfigName = (env)->NewString((const jchar*)configName, len);
    }
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID bmpConfigValueOf =
            env->GetStaticMethodID(bitmapConfigClass, "valueOf",
                                   "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject javaBitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass,
                                                               bmpConfigValueOf, jConfigName);
    //create the bitmap
    return env->CallStaticObjectMethod(javaBitmapClass, mid, pWidth, pHeight, javaBitmapConfig);
}
