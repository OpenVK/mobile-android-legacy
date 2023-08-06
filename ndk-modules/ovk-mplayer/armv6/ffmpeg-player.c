/* FFmpeg C player example.
 * From: http://web.archive.org/web/20130119153621/http://www.roman10.net/how-to-build-android-applications-based-on-ffmpeg-by-an-example/
*/

#define LOG_TAG "FFmpegPlayer"

#define LOG_LEVEL 10

#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}

#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

char *gFileName;      //the file name of the video

AVFormatContext *gFormatCtx;

int gVideoStreamIndex;    //video stream index

AVCodecContext *gVideoCodecCtx;

static void get_video_info(char *prFilename);

static void get_video_info(char *prFilename) {
    AVCodec *lVideoCodec;
    int lError;
    /*register the codec*/
    extern AVCodec ff_h264_decoder;
    avcodec_register(&ff_h264_decoder);
    /*register demux*/
    extern AVInputFormat ff_mov_demuxer;
    av_register_input_format(&ff_mov_demuxer);

    /*register the protocol*/
    extern URLProtocol ff_file_protocol;
    av_register_protocol2(&ff_file_protocol, sizeof(ff_file_protocol));
    /*open the video file*/
    if ((lError = av_open_input_file(&gFormatCtx, gFileName, NULL, 0, NULL)) !=0 ) {
        LOGE(1, "Error open video file: %d", lError);
        return;    //open file failed
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

    gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIndex]->codec;
    LOGI(10, "open codec: (%d, %d)", gVideoCodecCtx->height, gVideoCodecCtx->width);
    if (avcodec_open(gVideoCodecCtx, lVideoCodec) < 0) {
        LOGE(1, "Error: cannot open the video codec!");
        return;
    }

}
/**
* Uncomment and change function name for your application package.

JNIEXPORT void JNICALL Java_dev_tinelix_ffmpegplayer_MediaPlayer_naInit(JNIEnv *pEnv, jobject pObj, jstring pFileName) {

    int l_mbH, l_mbW;
    gFileName = (char *)(*pEnv)->GetStringUTFChars(pEnv, pFileName, NULL);
    if (gFileName == NULL) {
        LOGE(1, "Error: cannot get the video file name!");
        return;
    }

    LOGI(10, "video file name is %s", gFileName);

    get_video_info(gFileName);

}

JNIEXPORT jstring JNICALL Java_dev_tinelix_ffmpegplayer_MediaPlayer_naGetVideoCodecName(JNIEnv *pEnv, jobject pObj) {
    char* lCodecName = gVideoCodecCtx->codec->name;
    return (*pEnv)->NewStringUTF(pEnv, lCodecName);
}
**/






