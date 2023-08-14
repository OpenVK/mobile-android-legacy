#!/bin/bash
#  FFmpeg 0.8.12 & 3.1.4 for Android automatic build script using builder by Tinelix
#  NOTICE: FFmpeg and FFmpeg for Android builder by Tinelix licensed under LGPLv3 or later version.

echo "OpenVK Legacy | Downloading from https://github.com/tinelix/ffmpeg-android-builder..."
git submodule init
git submodule update
cd ndk-modules/ovkmplayer
cd builder
chmod -R 0777 . && chmod +x ./build-android-3.1.4.sh && chmod +x ./build-android-0.8.5.sh

echo "OpenVK Legacy | FFmpeg for Android building..."
# argument == ffmpeg version, if not specified compiling all versions
if [[ -z $1 ]]; then
    # Build FFmpeg 0.8.12 & 3.1.4 for 32-bit architecture
    ./build-android-0.8.12.sh armv6 r8e
    ./build-android-0.8.12.sh armv7 r8e
    ./build-android-0.8.12.sh x86 r8e
    # FFmpeg 0.8.12 not buildable under ARMv8a 64-bit and x64
    ./build-android-3.1.4.sh armv8a
elif [ $1 == "0.8.12" ]; then
    ./build-android-0.8.12.sh armv6 r8e
    ./build-android-0.8.12.sh armv7 r8e
    ./build-android-0.8.12.sh x86 r8e
elif [ $1 == "0.8.12-r6" ]; then
    ./build-android-0.8.12.sh armv6 r6
    ./build-android-0.8.12.sh armv7 r6
    ./build-android-0.8.12.sh x86 r6
elif [ $1 == "3.1.4" ]; then
    ./build-android-3.1.4.sh armv8a
else
    echo;
    echo "[ERROR] Wrong argument: ./build-ffmpeg.sh [version == '3.1.4' or '0.8.12' or '0.8.12-r6']"
    exit 1;
fi;

# Create directories in jniLibs and ovkmplayer
mkdir -p ../../../app/src/main/jniLibs/armeabi
mkdir -p ../../../app/src/main/jniLibs/armeabi-v7a
mkdir -p ../../../app/src/main/jniLibs/arm64-v8a
mkdir -p ../../../app/src/main/jniLibs/x86

echo "OpenVK Legacy | Copying libraries to project..."
echo;

if [[ -z $1 ]]; then
    cp ./ffmpeg-3.1.4/android/arm64-v8a/libffmpeg.so ../../../app/src/main/jniLibs/arm64-v8a
    cp ./ffmpeg-0.8.12/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
    cp ./ffmpeg-0.8.12/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
    cp ./ffmpeg-0.8.12/android/x86/libffmpeg.so ../../../app/src/main/jniLibs/x86
elif [ $1 == "0.8.12" ]; then
    cp ./ffmpeg-0.8.12/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
    cp ./ffmpeg-0.8.12/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
    cp ./ffmpeg-0.8.12/android/x86/libffmpeg.so ../../../app/src/main/jniLibs/x86
elif [ $1 == "0.8.12-r6" ]; then
    cp ./ffmpeg-0.8.12/android/armeabi/libffmpeg.so ../../../app/src/main/jniLibs/armeabi
    cp ./ffmpeg-0.8.12/android/armeabi-v7a/libffmpeg.so ../../../app/src/main/jniLibs/armeabi-v7a
    cp ./ffmpeg-0.8.12/android/x86/libffmpeg.so ../../../app/src/main/jniLibs/x86
else
    cp ./ffmpeg-3.1.4/android/arm64-v8a/libffmpeg.so ../../../app/src/main/jniLibs/arm64-v8a
fi;

echo "OpenVK Legacy | Done!"
