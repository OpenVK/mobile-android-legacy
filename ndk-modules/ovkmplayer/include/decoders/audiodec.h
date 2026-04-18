#ifndef OVKMPLAYER_DECODERS_AUDIODEC_H
#define OVKMPLAYER_DECODERS_AUDIODEC_H

#include <extcpp.h>

#define AV_MAX_AUDIO_FRAME_SIZE 192000;

extern "C" {
	#include <libavutil/dict.h>
	#include <libavformat/avformat.h>
	#include <libavformat/avio.h>
	#include <libavutil/samplefmt.h>
	#include <libavutil/frame.h>
	#include <libavcodec/avcodec.h>
	#include <libavcodec/avfft.h>
	#include <libavdevice/avdevice.h>
	#include <libswresample/swresample.h>
}

class AudioDecoder {
	public:
		AudioDecoder(
			AVFormatContext* pFormatCtx, 
			AVCodecContext*  pCodecCtx, 
			AVStream* 		 pStream, 
			int 			 pStreamIndex
		);
		bool 			 prepare();
		int 			 decodeFrame(AVPacket* pPkt, AVFrame* pFrame);
		uint8_t* 		 getFrameBuffer();
		
	private:
		AVFormatContext* hFormatCtx;
		AVCodecContext*  hCodecCtx;
		AVStream*		 hStream;
		int			     hStreamIndex;
		int				 hBufferSize;
		uint8_t*		 hFrameBuffer;
		SwrContext*		 hSwrCtx;
};

#endif