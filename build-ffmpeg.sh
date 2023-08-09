#!/bin/bash
#  FFmpeg 0.8.5 & 4.0.4 for Android automatic build script using builder by Tinelix
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

echo "OpenVK Legacy | Downloading from https://github.com/tinelix/ffmpeg-android-builder..."
cd ndk-modules/ovkmplayer
git clone https://github.com/tinelix/ffmpeg-android-builder builder
cd builder
chmod -R 0777 . && chmod +x ./build-android-2.6.sh && chmod +x ./build-android-4.0.4.sh

echo "OpenVK Legacy | FFmpeg for Android building..."
# argument == ffmpeg version, if not specified compiling all versions
if [[ -z $1 ]]; then
    # Build FFmpeg 0.8.5 & 2.3
    ./build-android-0.8.5.sh armv6
    ./build-android-0.8.5.sh armv7
    # FFmpeg 0.8.5 not buildable under ARMv8a 64-bit
    ./build-android-2.3.sh armv8a
elif [ $1 == "0.8.5" ]; then
    ./build-android-0.8.5.sh armv6 r8e
    ./build-android-0.8.5.sh armv7 r8e
elif [ $1 == "2.6" ]; then
    ./build-android-2.6.sh armv8a
else
    echo;
    echo "[ERROR] Wrong argument: ./build-ffmpeg.sh [version == '2.6' or '4.0.4']"
    exit 1;
fi;

# Create directories in jniLibs and ovkmplayer
mkdir -p ../../app/src/main/jniLibs/armeabi
mkdir -p ../../app/src/main/jniLibs/armeabi-v7a
mkdir -p ../../app/src/main/jniLibs/arm64-v8a

echo "OpenVK Legacy | Copying libraries to project..."
echo;

if [[ -z $1 ]]; then
    cp ./ffmpeg-2.6/android/arm64-v8a/libffmpeg.so ../../../app/src/main/jniLibs/arm64-v8a
    cp ./ffmpeg-0.8.5/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
    cp ./ffmpeg-0.8.5/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
elif [ $1 == "0.8.5" ]; then
    cp ./ffmpeg-0.8.5/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
    cp ./ffmpeg-0.8.5/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
else
    cp ./ffmpeg-2.6/android/arm64-v8a/libffmpeg.so ../../../app/src/main/jniLibs/arm64-v8a
fi;

echo "OpenVK Legacy | Done!"
