#ifndef OVKMPLAYER_WRAPPERS_FFMWRAP_H
#define OVKMPLAYER_WRAPPERS_FFMWRAP_H

#include <stdio.h>
#include <math.h>
#include <utils/android.h>
#include <extcpp.h>

extern "C" {
	#include <libavutil/version.h>
	#include <libavutil/avstring.h>
	#include <libavutil/dict.h>
	#include <libavformat/avformat.h>
	#include <libavformat/url.h>
	#include <libavformat/avio.h>
	#include <libavcodec/avcodec.h>
	#include <libavdevice/avdevice.h>
}

class FFmpegWrapper {
	private:
		bool  				hDebugMode;
		int	  				hErrorCode;
		char  				hErrorStr[192];
		char* 				hCurrentFileName;
		AVFormatContext*	hFormatCtx;
		AVCodecContext*		hCodecCtx[2];
		AVCodec*			hCodecs[2];
		int					hStreamIndexes[2]; // 0 - video, 1 - audio
		// TODO: Write private functions
		
	public:
		FFmpegWrapper(bool pDebugMode);
		void		init();
		char*		getAVFormatVersion();
		char*		getAVFormatBuildConfiguration();
		const char* getAVFormatLicense();
		int			getErrorCode();
		void		setDebugMode(bool pDebugMode);
		int			openInput(char* pFileName, bool pFindStreams);
		int         openCodec(int type);
		int			findInputStreams();
		AVStream*   getInputStream(int type);
		int			getInputStreamIndex(int type);
};

#endif