#ifndef OVKMPLAYER_UTILS_PKTQUEUE_H
#define OVKMPLAYER_UTILS_PKTQUEUE_H

class AVPacketQueue {
	private:
		AVPacketList* 	hFirst, hLast;
		int			  	hPacketsCount;
		int			  	hSize;
		bool		  	hAboutRequest;
		pthread_mutex_t hMutex;
		pthread_cond_t  hCond;
	public:
		AVPacketQueue();
		~AVPacketQueue();
		
		put(AVPacket* pPkt);
		flush();
		get(AVPacket* pPkt,  bool block);
		abort();
}

#endif