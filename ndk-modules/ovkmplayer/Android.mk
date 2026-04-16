#
#  Copyleft © 2022-24, 2026 OpenVK Team
#  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
#
#  This file is part of OpenVK Legacy for Android.
#
#  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under 
#  the terms of the GNU Affero General Public License as published by the Free Software Foundation, 
#  either version 3 of the License, or (at your option) any later version.
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

LOCAL_PATH 						:= $(call my-dir)
PROJECT_PATH 					:= $(call my-dir)/../..
FFMPEG_VERSION 					=  2.8.11

FFMPEG_PATH 					=  $(call my-dir)/builder/ffmpeg-$(FFMPEG_VERSION)

# FFmpeg core library 
include $(CLEAR_VARS)

LOCAL_MODULE 					:= ffmpeg-prebuilt
LOCAL_SRC_FILES 				:=  $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_EXPORT_C_INCLUDES 		:=  $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION)/android/$(TARGET_ARCH_ABI)/include
LOCAL_EXPORT_LDLIBS 			:=  $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_PRELINK_MODULE 			:=  true
LOCAL_CFLAGS 					+= -std=c++98
LOCAL_LDFLAGS 					+= -ljnigraphics

include $(PREBUILT_SHARED_LIBRARY)

# FFmpeg-based MediaPlayer library 
include $(CLEAR_VARS)

LOCAL_ALLOW_UNDEFINED_SYMBOLS	=  false
LOCAL_MODULE 					:= ovkmplayer

LOCAL_SRC_DIR 					:= src

LOCAL_SRC_FILES 				:= 	$(LOCAL_SRC_DIR)/ovkmplay.cpp \
									$(LOCAL_SRC_DIR)/utils/android.cpp
					
LOCAL_C_INCLUDES 				:= $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION)/android/$(TARGET_ARCH_ABI)/include
LOCAL_C_INCLUDES 				+= $(PROJECT_PATH)/ndk-modules/ovkmplayer/builder/ffmpeg-$(FFMPEG_VERSION) \
								   $(PROJECT_PATH)/ndk-modules/ovkmplayer/include

LOCAL_CFLAGS 					+= -std=c++98
LOCAL_CPP_FEATURES 				:= exceptions
LOCAL_SHARED_LIBRARIES 			:= ffmpeg-prebuilt
LOCAL_LDLIBS    				:= -llog -lz -lm  $(PROJECT_PATH)/app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libffmpeg.so

include $(BUILD_SHARED_LIBRARY)