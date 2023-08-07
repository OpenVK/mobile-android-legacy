#!/bin/bash
#  Automatic FFmpeg 0.8.5 for Android build script using builder by Tinelix
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

echo "OpenVK Legacy | FFmpeg for Android building..."
echo;
git submodule init
git submodule update
cd ndk-modules/ffmpeg-android-builder
git checkout -b ffmpeg-0.8.5
chmod -R 0777 . && chmod +x ./build-android.sh
./build-android.sh armv6
./build-android.sh armv7
# FFmpeg 0.8.5 not buildable under ARMv8a 64-bit
git reset --hard
git checkout -b ffmpeg-4.0.4
./build-android.sh armv8a
cp -R *.so ./ffmpeg/android ../../app/src/main/jniLibs
echo "OpenVK Legacy | Done!"
