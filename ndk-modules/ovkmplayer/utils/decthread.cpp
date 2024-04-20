//
// Created by tretdm on 20.04.2024.
//

#include "decthread.h"

DecoderThread::DecoderThread() {
	pthread_mutex_init(&mLock, NULL);
	pthread_cond_init(&mCond, NULL);
}

DecoderThread::~DecoderThread() {

}

void DecoderThread::start() {
    handleThread(NULL);
}

void DecoderThread::startAsync() {
    pthread_create(&mThread, NULL, startThread, this);
}

int DecoderThread::wait() {
	if(!mRunning)
	{
		return 0;
	}
    return pthread_join(mThread, NULL);
}

void DecoderThread::stop() {
}

void* DecoderThread::startThread(void* ptr) {
    LOGD(10, "Thread starting...");
	DecoderThread* thread = (DecoderThread *) ptr;
	thread->mRunning = true;
    thread->handleThread(ptr);
	thread->mRunning = false;
	LOGD(10, "Thread ended.");
}

void DecoderThread::waitOnNotify() {
	pthread_mutex_lock(&mLock);
	pthread_cond_wait(&mCond, &mLock);
	pthread_mutex_unlock(&mLock);
}

void DecoderThread::notify() {
	pthread_mutex_lock(&mLock);
	pthread_cond_signal(&mCond);
	pthread_mutex_unlock(&mLock);
}

void DecoderThread::handleThread(void* ptr) {
}
