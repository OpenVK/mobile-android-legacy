#!/bin/bash
#
#  FFmpeg 2.5.8 for Android automatic build script using builder by Tinelix
#
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

FFMPEG_BUILDER_REPO="https://github.com/tinelix/ffmpeg-android-builder"

echo "OpenVK Legacy | Downloading from ${FFMPEG_BUILDER_REPO}..."
git submodule init
git submodule update
cd ndk-modules/ovkmplayer
cd builder
git checkout origin/main
chmod -R 0777 . && chmod +x ./build-android-2.5.8.sh

echo "OpenVK Legacy | Starting FFmpeg builder..."

ANDROID_NDK_HOME=${ANDROID_NDK_R10E} ./build-android-2.5.8.sh armv8a r10e
ANDROID_NDK_HOME=${ANDROID_NDK_R8E} ./build-android-2.5.8.sh armv7 r8e
ANDROID_NDK_HOME=${ANDROID_NDK_R8E} ./build-android-2.5.8.sh armv6 r8e
ANDROID_NDK_HOME=${ANDROID_NDK_R8E} ./build-android-2.5.8.sh x86 r8e

# Create directories in jniLibs and ovkmplayer
mkdir -p ../../../app/src/main/jniLibs/armeabi
mkdir -p ../../../app/src/main/jniLibs/armeabi-v7a
mkdir -p ../../../app/src/main/jniLibs/arm64-v8a
mkdir -p ../../../app/src/main/jniLibs/x86

echo "OpenVK Legacy | Copying libraries to project..."
echo;

cp ./ffmpeg-2.5.8/android/arm64-v8a/libffmpeg.so ../../../app/src/main/jniLibs/arm64-v8a
cp ./ffmpeg-2.5.8/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
cp ./ffmpeg-2.5.8/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
cp ./ffmpeg-2.5.8/android/x86/libffmpeg.so ../../../app/src/main/jniLibs/x86

echo "OpenVK Legacy | Done!"
