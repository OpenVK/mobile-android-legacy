//
// Created by tretdm on 20.04.2024.
//

#ifndef MOBILE_ANDROID_LEGACY_THREAD_H
#define MOBILE_ANDROID_LEGACY_THREAD_H

#include <android/log.h>

extern "C" {
    #include <pthread.h>
}

#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}

class DecoderThread {
    public:
        DecoderThread();
        ~DecoderThread();

        void						start();
        void						startAsync();
        int							wait();

        void 						waitOnNotify();
        void						notify();
        virtual void				stop();

    protected:
        bool						mRunning;

        virtual void                handleThread(void* ptr);

    private:
        pthread_t                   mThread;
        pthread_mutex_t     		mLock;
        pthread_cond_t				mCond;

        static void*				startThread(void* ptr);
};



#endif //MOBILE_ANDROID_LEGACY_THREAD_H
