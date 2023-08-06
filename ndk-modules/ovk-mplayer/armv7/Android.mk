#  Copyright © 2023 Dmitry Tretyakov (aka. Tinelix)
#
#  This file as part of FFmpeg custom builder for Android.
#
#  FFmpeg custom builder for Android is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Lesser General Public License as published by the Free Software
#  Foundation, either version 3 of the License, or (at your option) any later version.
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
#  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#  See the GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License along with this
#  program. If not, see https://www.gnu.org/licenses/.
#
#  Source code: https://github.com/tinelix/ffmpeg-android-builder

LOCAL_PATH := $(call my-dir)

#declare the prebuilt library

include $(CLEAR_VARS)

FFMPEG_VERSION := 4.0.4

LOCAL_MODULE := ffmpeg-prebuilt

LOCAL_SRC_FILES := ../ffmpeg/android/armv7/libffmpeg-v${FFMPEG_VERSION}.so

LOCAL_EXPORT_C_INCLUDES := ../ffmpeg/android/armv6/include

LOCAL_EXPORT_LDLIBS := ../ffmpeg/android/armv7/libffmpeg-v${FFMPEG_VERSION}.so

LOCAL_PRELINK_MODULE := true

include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)

LOCAL_ALLOW_UNDEFINED_SYMBOLS=false
LOCAL_MODULE := ffmpeg-player
LOCAL_SRC_FILES := ffmpeg-player.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/android/armv7-a/include
LOCAL_SHARED_LIBRARY := ffmpeg-prebuilt
LOCAL_LDLIBS    := -llog -ljnigraphics -lz -lm $(LOCAL_PATH)/../ffmpeg/android/armv7/libffmpeg-v${FFMPEG_VERSION}.so

include $(BUILD_SHARED_LIBRARY)

