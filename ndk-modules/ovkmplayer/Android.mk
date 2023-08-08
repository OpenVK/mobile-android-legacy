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

#declare the prebuilt library

include $(CLEAR_VARS)

LOCAL_MODULE := ffmpeg-prebuilt
LOCAL_SRC_FILES := ../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_EXPORT_LDLIBS := ../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
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
LOCAL_LDLIBS    := -llog -lz -lm $(LOCAL_PATH)/../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so

include $(BUILD_SHARED_LIBRARY)

