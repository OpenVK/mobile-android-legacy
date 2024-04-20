//
// Created by tretdm on 20.04.2024.
//

#include "pktqueue.h"

PacketQueue::PacketQueue() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCond, NULL);
    mFirst = NULL;
    mLast = NULL;
    mPackets = 0;
    mSize = 0;
    mAbortReq = false;
}

PacketQueue::~PacketQueue()
{
	flush();
	pthread_mutex_destroy(&mLock);
	pthread_cond_destroy(&mCond);
}

void PacketQueue::flush() {
	AVPacketList *pkt, *pkt1;

    pthread_mutex_lock(&mLock);

    for(pkt = mFirst; pkt != NULL; pkt = pkt1) {
        av_free_packet(&pkt->pkt);
        av_freep(&pkt);
		pkt1 = pkt->next;
    }
    mLast = NULL;
    mFirst = NULL;
    mPackets = 0;
    mSize = 0;

    pthread_mutex_unlock(&mLock);
}

int PacketQueue::length() {
	pthread_mutex_lock(&mLock);
    int size = mPackets;
    pthread_mutex_unlock(&mLock);
	return size;
}

int PacketQueue::put(AVPacket* pkt) {
	AVPacketList *pktList;

    if (av_dup_packet(pkt) < 0)
        return -1;

    pktList = (AVPacketList *) av_malloc(sizeof(AVPacketList));
    if (!pktList)
        return -1;
    pktList->pkt = *pkt;
    pktList->next = NULL;

    pthread_mutex_lock(&mLock);

    if (!mLast) {
        mFirst = pktList;
	} else {
        mLast->next = pktList;
	}

    mLast = pktList;
    mPackets++;
    mSize += pktList->pkt.size + sizeof(*pktList);

	pthread_cond_signal(&mCond);
    pthread_mutex_unlock(&mLock);

    return 0;
}

int PacketQueue::get(AVPacket *pkt, bool block) {
	AVPacketList *pktList;
    int ret;

    pthread_mutex_lock(&mLock);

    for(;;) {
        if (mAbortReq) {
            ret = -1;
            break;
        }

        pktList = mFirst;
        if (pktList) {
            mFirst = pktList->next;
            if (!mFirst)
                mLast = NULL;
            mPackets--;
            mSize -= pktList->pkt.size + sizeof(*pktList);
            *pkt = pktList->pkt;
            av_free(pktList);
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
			pthread_cond_wait(&mCond, &mLock);
		}
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

void PacketQueue::abort() {
    pthread_mutex_lock(&mLock);
    mAbortReq = true;
    pthread_cond_signal(&mCond);
    pthread_mutex_unlock(&mLock);
}



