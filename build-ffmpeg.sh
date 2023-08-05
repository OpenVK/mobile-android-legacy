#!/bin/bash
git submodule init
git submodule update
cd ndk-modules/ffmpeg-android-builder
chmod +x ./build-android.sh
./build-android.sh
