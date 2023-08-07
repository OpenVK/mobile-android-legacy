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

# argument == ffmpeg version, if not specified compiling all versions
if [[ -z $1 ]]; then
    # Build FFmpeg 0.8.5 & 4.0.4
    ./build-android.sh armv6
    ./build-android.sh armv7
    # FFmpeg 0.8.5 not buildable under ARMv8a 64-bit
    cd ../ffmpeg-builder-4.0.4
    chmod -R 0777 . && chmod +x ./build-android.sh
    ./build-android.sh armv8a
elif [ $1 == "0.8.5" ]; then
    ./build-android.sh armv6
    ./build-android.sh armv7
elif [ $1 == "4.0.4" ]; then
    cd ../ffmpeg-builder-4.0.4
    chmod -R 0777 . && chmod +x ./build-android.sh
    ./build-android.sh armv8a
else
    echo "Wrong argument: ./build-android [version == '0.8.5' or '4.0.4']"
    exit 1;
fi;

# Create directories in jniLibs
mkdir -p ../../app/src/main/jniLibs/armeabi
mkdir -p ../../app/src/main/jniLibs/armeabi-v7a
mkdir -p ../../app/src/main/jniLibs/arm64-v8a

echo "OpenVK Legacy | Copying libraries to project..."
echo;
cp ./ffmpeg/android/arm64-v8a/libffmpeg-v4.0.4.so ../../app/src/main/jniLibs/arm64-v8a
cd ../ffmpeg-builder-0.8.5
cp ./ffmpeg/android/armeabi/libffmpeg-v0.8.5.so ../../app/src/main/jniLibs/armeabi
cp ./ffmpeg/android/armeabi-v7a/libffmpeg-v0.8.5.so ../../app/src/main/jniLibs/armeabi-v7a
echo "OpenVK Legacy | Done!"
