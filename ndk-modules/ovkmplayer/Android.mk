# NDK Project Example (Android.mk).
# NOTICE: FFmpeg and FFmpeg custom builder for Android licensed under LGPLv3.0 or later version.

LOCAL_PATH := $(call my-dir)

#declare the prebuilt library

include $(CLEAR_VARS)

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    FFMPEG_VERSION := 4.0.4
else
    FFMPEG_VERSION := 0.8.5
fi

LOCAL_MODULE := ffmpeg-prebuilt

LOCAL_SRC_FILES := ../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg-v${FFMPEG_VERSION}.so

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_EXPORT_LDLIBS := ../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg-v${FFMPEG_VERSION}.so

LOCAL_PRELINK_MODULE := true

LOCAL_LDFLAGS += -ljnigraphics

include $(PREBUILT_SHARED_LIBRARY)

#the andzop library
include $(CLEAR_VARS)

LOCAL_ALLOW_UNDEFINED_SYMBOLS=false
LOCAL_MODULE := ovkmplayer
LOCAL_SRC_FILES := ovkmplayer.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/include
LOCAL_SHARED_LIBRARY := ffmpeg-prebuilt
LOCAL_LDLIBS    := -llog -lz -lm $(LOCAL_PATH)/../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg-v${FFMPEG_VERSION}.so

include $(BUILD_SHARED_LIBRARY)

