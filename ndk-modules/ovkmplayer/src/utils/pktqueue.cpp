#include <utils/pktqueue.h>

AVPacketQueue::AVPacketQueue() {
	pthread_mutex_init(&hMutex, NULL);
	pthread_cond_init(&hCond, NULL);
	
	hFirst 			= NULL;
	hLast 			= NULL;
	hPacketsCount 	= 0;
	hSize 			= 0;
	hAboutRequest 	= false;
}

AVPacketQueue::~AVPacketQueue() {
	flush();
	pthread_mutex_destroy(&hMutex);
	pthread_cond_destroy(&hCond);
}

AVPacketQueue::put(AVPacket* pPkt) {
	AVPacketList *pktList;
	
	if(av_dup_packet(pPkt) < 0)
		return -1;
		
	pktList = (AVPacketList*) av_malloc(sizeof(AVPacketList));
	
	if(!pktList1)
		return -2;
		
	pktList->pkt 	= *pkt;
	pktList->next 	= NULL;
	
	pthread_mutex_lock(&hMutex);
	
	if(!hLast) {
		hFirst = pktList;
	} else {
		hLast->next = pktList;
	}
	
	hLast = pktList;
	
	hPacketsCount++;
	hSize += pktList->pkt.size + sizeof(*pktList);
	
	pthread_cond_signal(&hCond);
	pthread_mutex_unlock(&hLock);
	
	return 0;
}

AVPacketQueue::flush() {
	AVPacketList *pktList1, pktList2;
	
	pthread_mutex_lock(&hMutex);
	
	for(pktList1 = hFirst; pktList1 != NULL; pktList1 = pktList2) {
		av_free_packet(&pktList1->pkt);
		av_freep(&pktList1);
		pktList2 = pktList1->next;
	}
	
	hFirst 			= NULL;
	hLast 			= NULL;
	hPacketsCount 	= 0;
	hSize 			= 0;
	
	pthread_mutex_unlock(&hMutex);
}

int AVPacketQueue::get(AVPacket* pPkt, bool block) {
	AVPacketList *pktList;
	int result;
	
	pthread_mutex_lock(&hMutex);
	
	while(true) {
		if(hAboutRequest) {
			result = -1;
			break;
		}
		
		pktList = hFirst;
		
		if(pktList) {
			hFirst = pktList->next;
			
			if(!hFirst)
				hLast = NULL;
			
			hPacketsCount--;
			hSize -= pktList->pkt.size + sizeof(*pktList);
			*pPkt = pktList->pkt;
			
			av_free(pktList);
			result = 1;
			
			break;
		} else if(!block) {
			result = 0;
			break;
		} else {
			pthread_cond_wait(&hCond, &hMutex);
		}
	}
	
	pthread_mutex_unlock(&hMutex);
}

void AVPacketQueue::abort() {
	pthread_mutex_lock(&hMutex);
	hAboutRequest = true;
	pthread_cond_signal(&hCond);
	pthread_mutex_unlock(&hMutex);
}