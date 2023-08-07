#!/bin/bash
#  FFmpeg 0.8.5 & 4.0.4 for Android automatic build script using builder by Tinelix
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

echo "OpenVK Legacy | FFmpeg for Android building..."
echo;
git submodule init
git submodule update
cd ndk-modules/ffmpeg-builder-0.8.5
git checkout -b ffmpeg-0.8.5
chmod -R 0777 . && chmod +x ./build-android.sh

# Build FFmpeg 0.8.5 & 4.0.4
./build-android.sh armv6
./build-android.sh armv7
# FFmpeg 0.8.5 not buildable under ARMv8a 64-bit
cd ../ffmpeg-builder-4.0.4
chmod -R 0777 . && chmod +x ./build-android.sh
./build-android.sh armv8a

# Create directories in jniLibs
mkdir ../../app/src/main/jniLibs/armeabi
mkdir ../../app/src/main/jniLibs/armeabi-v7a
mkdir ../../app/src/main/jniLibs/arm64-v8a

echo "OpenVK Legacy | Copying libraries to project..."
echo;
cp ./ffmpeg/android/armeabi/libffmpeg-0.8.5.so ../../app/src/main/jniLibs/armeabi
cp ./ffmpeg/android/armeabi-v7a/libffmpeg-0.8.5.so ../../app/src/main/jniLibs/armeabi-v7a
cd ../ffmpeg-builder-0.8.5
cp ./ffmpeg/android/arm64-v8a/libffmpeg-4.0.4.so ../app/src/main/jniLibs/arm64-v8a
echo "OpenVK Legacy | Done!"
