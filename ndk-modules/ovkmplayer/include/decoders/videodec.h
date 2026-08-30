#ifndef OVKMPLAYER_DECODERS_VIDEODEC_H
#define OVKMPLAYER_DECODERS_VIDEODEC_H

/*

class VideoDecoder {
	public:
		VideoDecoder(
			AVFormatContext* pFormatCtx, 
			AVCodecContext*  pCodecCtx, 
			AVStream* 		 pStream, 
			int 			 pStreamIndex
		);
		bool 			 prepare();
		int 			 decodeFrame(AVPacket* pPkt, AVFrame* pFrame);
		uint8_t*		 YUVtoRGB(AVPixelFormat pxf, AVFrame* frame, int length);
		uint8_t* 		 getFrameBuffer();
		
	private:
		AVFormatContext*  hFormatCtx;
		AVCodecContext*   hCodecCtx;
		AVStream*		  hStream;
		int			      hStreamIndex;
		int				  hBufferSize;
		uint8_t*		  hFrameBuffer;
};


*/

#endif