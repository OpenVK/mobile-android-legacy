#!/bin/bash
#  Automatic FFmpeg 0.8.5 for Android build script using builder by Tinelix
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

echo "OpenVK Legacy | FFmpeg for Android building..."
echo;
git submodule init
git submodule update
cd ndk-modules/ffmpeg-builder-0.8.5
git checkout -b ffmpeg-0.8.5
chmod -R 0777 . && chmod +x ./build-android.sh
./build-android.sh armv6
./build-android.sh armv7
# FFmpeg 0.8.5 not buildable under ARMv8a 64-bit
cd ../ffmpeg-builder-4.0.4
chmod -R 0777 . && chmod +x ./build-android.sh
./build-android.sh armv8a
cp -R *.so ./ffmpeg/android ../../app/src/main/jniLibs
cd ../ffmpeg-builder-0.8.5
cp -R *.so ./ffmpeg/android ../../app/src/main/jniLibs
echo "OpenVK Legacy | Done!"
