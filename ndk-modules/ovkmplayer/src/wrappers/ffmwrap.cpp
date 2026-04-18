#include <wrappers/ffmwrap.h>

FFmpegWrapper::FFmpegWrapper(bool pDebugMode) {
	hDebugMode = pDebugMode;
	if(pDebugMode) {
		getAVFormatVersion();
		getAVFormatLicense();
		getAVFormatBuildConfiguration();
	}
}

void FFmpegWrapper::init() {
	av_register_all();
	avcodec_register_all();
	avformat_network_init();
	
	if(hDebugMode)
		LOGD("[DEBUG] FFmpegWrapper is ready");
}

char* FFmpegWrapper::getAVFormatVersion() {
	char avfVersion[16];
	
	int avfVersionI = avformat_version();
	
	sprintf(avfVersion, 
	        "%d.%d.%d", 
	        LIBAVUTIL_VERSION_MAJOR, 
	        LIBAVUTIL_VERSION_MINOR, 
	        LIBAVUTIL_VERSION_MICRO);
	        
	if(hDebugMode)
		LOGD("[DEBUG] libavformat version: %s", avfVersion);
		
	return avfVersion;
}

char* FFmpegWrapper::getAVFormatBuildConfiguration() {
	char* originalConf = (char*)malloc(2048 * sizeof(char));
	char* formattedConf = (char*)malloc(2048 * sizeof(char));
	char* token;
	int result, lines = 0;
	
	sprintf(originalConf, "%s", avformat_configuration());
	
	token = strtok(originalConf, " ");
	
	while(token != NULL) {
		if(hDebugMode && lines > 0) {
        	if(lines == 1)
        		LOGD("[DEBUG] libavformat build configuration:\r\n%s",  formattedConf)
        	else
        		LOGD("%s", formattedConf);
        }

        result = sprintf(formattedConf, "\t%s\r\n", token);
		
		lines++;
		token = strtok(NULL, " ");
	}
	
	free(originalConf);
	free(formattedConf);
	
	return (char*)avformat_configuration();
}

const char* FFmpegWrapper::getAVFormatLicense() {
	const char* license = avformat_license();
	
	if(hDebugMode)
		LOGD("[DEBUG] libavformat license: %s", license);
		
	return license;
}

int FFmpegWrapper::getErrorCode() {
	return hErrorCode;
}

void FFmpegWrapper::setDebugMode(bool value) {
	hDebugMode = value;
}

int FFmpegWrapper::openInput(char* pFileName, bool pFindStreams) {
	int result;
	hCurrentFileName = pFileName;
	
	hFormatCtx = avformat_alloc_context();
	
	if((result = avformat_open_input(&hFormatCtx, pFileName, NULL, NULL)) < 0) {
		hErrorCode = result;
		switch(result) {
			case -2:
				sprintf(hErrorStr, "Input source not found");
				break;
			default:
				if(av_strerror(result, hErrorStr, 192) < 0)
					strerror_r(-hErrorCode, hErrorStr, 192);
				break;
		}
		
		if(hDebugMode)
			LOGE("[ERROR] Cannot open %s (%d, %s)", pFileName, result, hErrorStr);
			
		return result;
	}
	
	if(pFindStreams)
		result = findInputStreams();
	
	return result;
}

int FFmpegWrapper::findInputStreams() {
	int result;
	
	if((result = avformat_find_stream_info(hFormatCtx, NULL)) < 0) {
		if(hDebugMode)
			LOGE("[ERROR] Failed to find stream info from %s", hCurrentFileName);
		return result;
	}
	
	av_dump_format(hFormatCtx, 0, hCurrentFileName, 0);
	
	for(int i = 0; i < hFormatCtx->nb_streams; i++) {
		int codecType = hFormatCtx->streams[i]->codec->codec_type;
		switch(codecType) {
			case AVMEDIA_TYPE_VIDEO:
				hStreamIndexes[0] = i;
				break;
			case AVMEDIA_TYPE_AUDIO:
				hStreamIndexes[1] = i;
				break;
		}
	}
	
	if(hStreamIndexes[0] == -1 && hStreamIndexes[1] == -1) {
		LOGE("[ERROR] Media streams not found");
		return -1;
	}
}

AVStream* FFmpegWrapper::getInputStream(int type) {
	if(type < 2 && hStreamIndexes[type] >= 0)
		return hFormatCtx->streams[hStreamIndexes[type]];
	else
		return NULL;
}

int FFmpegWrapper::openCodec(int type) {
	int result;
	AVDictionary* optDict;
	
	if(hStreamIndexes[type] != -1) {
		hCodecCtx[type] = getInputStream(type)->codec;
		
		if(hCodecCtx[type] == NULL) {
			if(hDebugMode)
				LOGE("[ERROR] Stream #%d not found", type + 1);
			return -1;
		}
		
		hCodecs[type] = avcodec_find_decoder(hCodecCtx[type]->codec_id);
		
		if(hCodecs[type] == NULL) {
			if(hDebugMode)
				LOGE("[ERROR] Unsupported media codec #%d", hCodecCtx[type]->codec_id);
			return -2;
		}
		
		if((result = avcodec_open2(hCodecCtx[type], hCodecs[type], &optDict)) < 0) {
			if(hDebugMode)
				LOGE("[ERROR] Could not open media codec #%d", hCodecCtx[type]->codec_id);
			return result;
		}
	}
}

