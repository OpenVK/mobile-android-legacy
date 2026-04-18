#include <decoders/audiodec.h>

AudioDecoder::AudioDecoder(
	AVFormatContext* pFormatCtx, 
	AVCodecContext*  pCodecCtx, 
	AVStream* 		 pStream, 
	int 			 pStreamIndex
) {
	hFormatCtx   = pFormatCtx;
	hCodecCtx    = pCodecCtx;
	hStream	     = pStream;
	hStreamIndex = hStreamIndex;
}

bool AudioDecoder::prepare() {
	hBufferSize = AV_MAX_AUDIO_FRAME_SIZE;
	hFrameBuffer = (uint8_t*)malloc(hBufferSize);
	
	hSwrCtx = swr_alloc();
	hSwrCtx = swr_alloc_set_opts(
		hSwrCtx, 
		(int64_t)hCodecCtx->channel_layout,
		AV_SAMPLE_FMT_S16, 
		hCodecCtx->sample_rate, 
		hCodecCtx->channel_layout, 
		hCodecCtx->sample_fmt, 
		hCodecCtx->sample_rate, 
		0,  NULL
	);
	
	swr_init(hSwrCtx);
	return hFrameBuffer != NULL;
}

int AudioDecoder::decodeFrame(AVPacket* pPkt, AVFrame* pFrame) {
	int status, dataSize, len, len2, gotFrame;
	
	if(pPkt->stream_index != hStreamIndex)
		return -1;
	
	do {
		len = avcodec_decode_audio4(hStream->codec, pFrame, &gotFrame, pPkt);
		if(len >= 0) {
			pPkt->dts = pPkt->pts = AV_NOPTS_VALUE;
			
			if(pPkt->data) {
				pPkt->data += len;
				pPkt->size -= len;
				
				if(pPkt->size <= 0)
					break;
			} else if(!gotFrame)
				return -3;
		} else {
			return -2;
		}
	} while(!gotFrame);
	
	int64_t pts   = pPkt->pts;
	hFrameBuffer  = (uint8_t*)malloc(sizeof(uint8_t) * dataSize);
	
	dataSize = av_samples_get_buffer_size(
		NULL, hCodecCtx->channels, pFrame->nb_samples,
		hCodecCtx->sample_fmt, 1
	);
	
	len2 = swr_convert(hSwrCtx,
					&hFrameBuffer, 
					dataSize, 
					(const uint8_t**)pFrame->data, 
					pFrame->nb_samples);
	if(len2 < 0)
		return -4;
		
	return 0;
}

uint8_t* AudioDecoder::getFrameBuffer() {
	return hFrameBuffer;
}