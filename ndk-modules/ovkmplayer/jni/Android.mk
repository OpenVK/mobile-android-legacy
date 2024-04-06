#  OPENVK LEGACY LICENSE NOTIFICATION
#
#  This program is free software: you can redistribute it and/or modify it under the terms of
#  the GNU Affero General Public License as published by the Free Software Foundation, either
#  version 3 of the License, or (at your option) any later version.
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
#  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#  See the GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License along with this
#  program. If not, see https://www.gnu.org/licenses/.
#
#  Source code: https://github.com/openvk/mobile-android-legacy
#
#  NOTICE: FFmpeg and FFmpeg custom builder for Android licensed under LGPLv3.0 or later version.

LOCAL_PATH := $(call my-dir)
PROJECT_PATH := $(call my-dir)/../../..
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
   FFMPEG_VERSION = 3.1.4
else
   FFMPEG_VERSION = 0.8.12
endif
FFMPEG_PATH = $(call my-dir)/builder/ffmpeg-$(FFMPEG_VERSION)
#declare the prebuilt library

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_EXPORT_C_INCLUDES := $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION)/android/$(TARGET_ARCH_ABI)/include
LOCAL_EXPORT_LDLIBS :=  $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_PRELINK_MODULE := true
LOCAL_LDFLAGS += -ljnigraphics

include $(PREBUILT_SHARED_LIBRARY)

#the andzop library
include $(CLEAR_VARS)
LOCAL_ALLOW_UNDEFINED_SYMBOLS=false
LOCAL_MODULE := ovkmplayer
ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := ovkmplayer.cpp
else
    LOCAL_SRC_FILES := ovkmplayer-legacy.cpp
endif
LOCAL_C_INCLUDES := $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION)/android/$(TARGET_ARCH_ABI)/include
LOCAL_C_INCLUDES += $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION)
LOCAL_CPP_FEATURES := exceptions
LOCAL_SHARED_LIBRARY := ffmpeg-prebuilt
LOCAL_LDLIBS    := -llog -lz -lm  $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so

include $(BUILD_SHARED_LIBRARY)

