#!/bin/bash
#
#  Copyright © 2023 Dmitry Tretyakov (aka. Tinelix)
#
#  This program is free software: you can redistribute it and/or modify it under the terms of
#  the GNU Lesser General Public License as published by the Free Software Foundation, either
#  version 3 of the License, or (at your option) any later version.
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
#  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#  See the GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License along with this
#  program. If not, see https://www.gnu.org/licenses/.
#
#  Source code: https://github.com/tinelix/ffmpeg-android-builder

FFMPEG_VERSION="$(cat ./ffmpeg-4.0.4/RELEASE)"

echo "FFmpeg custom builder for Android"
echo "Copyright (c) Dmitry Tretyakov (aka. Tinelix), 2023"
echo "Licensed under LGPLv3 or later version.";
echo "";

FFMPEG_INPUT_ARCH="ARCH";

if [[ -z $1 ]];
then
    read -p "Specify architecture [armv6, armv7, armv8a]: " FFMPEG_INPUT_ARCH
else
    FFMPEG_INPUT_ARCH=$1
fi

if [ ! -d "ffmpeg-4.0.4" ]; then
  echo "[ERROR] FFmpeg 4.0.4 source code not found. Please download it from https://github.com/ffmpeg/ffmpeg.";
  exit 1
fi

echo "Creating output directories...";
chmod -R 0777 .
mkdir -p ffmpeg-4.0.4/android
mkdir -p ffmpeg-4.0.4/android/armeabi
mkdir -p ffmpeg-4.0.4/android/armeabi-v7a
mkdir -p ffmpeg-4.0.4/android/arm64-v8a
mkdir -p ffmpeg-4.0.4/android/x86

echo "Configuring FFmpeg v$FFMPEG_VERSION build...";

FFMPEG_BUILD_PLATFORM="linux";

if [ $FFMPEG_INPUT_ARCH == "armv6" ]; then
	FFMPEG_TARGET_ARCH="armv6";
	FFMPEG_TARGET_CPU="armv6";
	ANDROID_TARGET_ARCH="armeabi";
	ANDROID_TOOLCHAIN_CPUABI="arm";
	ANDROID_TARGET_API=9;
elif [ $FFMPEG_INPUT_ARCH == "armv7" ]; then
	FFMPEG_TARGET_ARCH="armv7";
	FFMPEG_TARGET_CPU="armv7";
	ANDROID_TARGET_ARCH="armeabi-v7a";
	ANDROID_TOOLCHAIN_CPUABI="arm";
	ANDROID_TARGET_API=9;
elif [ $FFMPEG_INPUT_ARCH == "armv8a" ]; then
	FFMPEG_TARGET_ARCH="aarch64";
	FFMPEG_TARGET_CPU="armv8-a";
	ANDROID_TARGET_ARCH="arm64-v8a";
	ANDROID_TOOLCHAIN_CPUABI="aarch64";
	ANDROID_TARGET_API=21;
else
	echo "Canceling...";
	exit 1;
fi;

if [ $FFMPEG_INPUT_ARCH == "armv8a" ]; then
	FFMPEG_CFLAGS="-std=c99 -Os -Wall -pipe -fpic -fasm \
		-finline-limit=300 -ffast-math \
		-fstrict-aliasing -Werror=strict-aliasing \
		-Wno-psabi \
		-fdiagnostics-color=always \
		-DANDROID -DNDEBUG"
	ANDROID_NDK_SYSROOT="${ANDROID_NDK_HOME}/platforms/android-${ANDROID_TARGET_API}/arch-arm64"
else
	FFMPEG_CFLAGS="-std=c99 -Os -Wall -pipe -fpic -fasm \
		-finline-limit=300 -ffast-math \
		-fstrict-aliasing -Werror=strict-aliasing \
		-Wno-psabi \
		-fdiagnostics-color=always \
		-msoft-float \
		-DANDROID -DNDEBUG"
	ANDROID_NDK_SYSROOT="${ANDROID_NDK_HOME}/platforms/android-${ANDROID_TARGET_API}/arch-${ANDROID_TOOLCHAIN_CPUABI}"
fi;

if [ -z "$ANDROID_NDK_HOME" ]; then # requires NDK r10e+
	echo "[ERROR] NDK (requires r10e+) not installed or ANDROID_NDK_HOME variable is not defined.";
	exit 1
else
	if [ $FFMPEG_INPUT_ARCH == "armv8a" ]; then
		ANDROID_NDK_TOOLCHAINS="${ANDROID_NDK_HOME}/toolchains/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-android-4.9/prebuilt/${FFMPEG_BUILD_PLATFORM}-x86_64/bin/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-android-"
		ANDROID_NDK_GCC="${ANDROID_NDK_HOME}/toolchains/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-android-4.9/prebuilt/${FFMPEG_BUILD_PLATFORM}-x86_64/lib/gcc/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-android/4.9"
	else
		ANDROID_NDK_TOOLCHAINS="${ANDROID_NDK_HOME}/toolchains/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-androideabi-4.9/prebuilt/${FFMPEG_BUILD_PLATFORM}-x86_64/bin/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-androideabi-"
		ANDROID_NDK_GCC="${ANDROID_NDK_HOME}/toolchains/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-androideabi-4.9/prebuilt/${FFMPEG_BUILD_PLATFORM}-x86_64/lib/gcc/${ANDROID_TOOLCHAIN_CPUABI}-${FFMPEG_BUILD_PLATFORM}-androideabi/4.9"
	fi;
fi

if [ -z "$FFMPEG_GNUTLS" ]; then
	echo "[WARNING] FFMPEG_GNUTLS variable is not defined."
	echo "          Streaming playback may be limited.";

	FFMPEG_FLAGS="--target-os=linux \
				--prefix=./android/${ANDROID_TARGET_ARCH} \
				--disable-everything \
				--enable-cross-compile \
				--arch=${FFMPEG_TARGET_ARCH} \
				--cc=${ANDROID_NDK_TOOLCHAINS}gcc \
				--cross-prefix=${ANDROID_NDK_TOOLCHAINS} \
				--nm=${ANDROID_NDK_TOOLCHAINS}nm \
				--sysroot=${ANDROID_NDK_SYSROOT} \
				--disable-gpl \
				--enable-version3 \
				--disable-nonfree \
				--enable-avcodec \
				--enable-avformat \
				--enable-avutil \
				--enable-swscale \
				--enable-avfilter \
				--enable-yasm \
				--enable-asm \
				--disable-programs \
				--disable-ffmpeg \
				--disable-ffplay \
				--disable-ffprobe \
				--disable-doc \
				--disable-htmlpages \
				--disable-d3d11va \
				--disable-dxva2 \
				--disable-vaapi \
				--disable-vdpau \
				--disable-videotoolbox \
				--disable-encoders \
				--disable-decoders \
				--disable-demuxers \
				--disable-parsers \
				--disable-muxers \
				--disable-filters \
				--disable-iconv \
				--disable-debug \
				--enable-network \
				--enable-protocol=file,http,async \
				--enable-armv5te \
				--enable-parser=h263 \
				--enable-parser=h264 \
				--enable-parser=vp8 \
				--enable-parser=flac \
				--enable-parser=aac \
				--enable-parser=vorbis \
				--enable-parser=ogg \
				--enable-demuxer=flv \
				--enable-demuxer=mp3 \
				--enable-demuxer=data \
				--enable-decoder=mp3 \
				--enable-decoder=aac \
				--enable-decoder=vp8 \
				--enable-decoder=h263 \
				--enable-decoder=h264 \
				--enable-decoder=theora \
				--enable-decoder=flac \
				--enable-decoder=vorbis \
				--enable-decoder=aac \
				--enable-encoder=libmp3lame \
				--enable-encoder=vorbis \
				--enable-muxer=mp4 \
				--enable-muxer=ogg \
				--enable-muxer=mp3 \
				--enable-small \
				--enable-inline-asm \
				--enable-optimizations"
else
	FFMPEG_FLAGS="--target-os=linux \
					--prefix=./android/${ANDROID_TARGET_ARCH} \
					--enable-cross-compile \
					--arch=${FFMPEG_TARGET_ARCH} \
					--cc=${ANDROID_NDK_TOOLCHAINS}gcc \
					--cross-prefix=${ANDROID_NDK_TOOLCHAINS} \
					--nm=${ANDROID_NDK_TOOLCHAINS}nm \
					--sysroot=${ANDROID_NDK_SYSROOT} \
					--disable-gpl \
					--enable-version3 \
					--disable-nonfree \
					--enable-avcodec \
					--enable-avformat \
					--enable-avutil \
					--enable-swscale \
					--enable-avfilter \
					--enable-yasm \
					--enable-asm \
					--disable-programs \
					--disable-ffmpeg \
					--disable-ffplay \
					--disable-ffprobe \
					--disable-doc \
					--disable-htmlpages \
					--disable-d3d11va \
					--disable-dxva2 \
					--disable-vaapi \
					--disable-vdpau \
					--disable-videotoolbox \
					--disable-encoders \
					--disable-encoders \
					--disable-decoders \
					--disable-demuxers \
					--disable-parsers \
					--disable-muxers \
					--disable-filters \
					--disable-iconv \
					--disable-debug \
					--enable-network \
					--enable-protocol=file,http,async \
					--enable-armv5te \
					--enable-parser=h263 \
					--enable-parser=h264 \
					--enable-parser=vp8 \
					--enable-parser=flac \
					--enable-parser=aac \
					--enable-demuxer=flv \
					--enable-demuxer=mp3 \
					--enable-demuxer=data \
					--enable-decoder=mp3 \
					--enable-decoder=aac \
					--enable-decoder=vp8 \
					--enable-decoder=h263 \
					--enable-decoder=h264 \
					--enable-decoder=theora \
					--enable-decoder=flac \
					--enable-decoder=vorbis \
					--enable-decoder=aac \
					--enable-encoder=libmp3lame \
					--enable-encoder=vorbis \
					--enable-muxer=mp4 \
					--enable-muxer=ogg \
					--enable-small \
					--enable-inline-asm \
					--enable-optimizations"
fi

cd ffmpeg-4.0.4

./configure $FFMPEG_FLAGS --extra-cflags="$FFMPEG_CFLAGS"
echo;
echo "Build starts in 15 seconds. Wait or press CTRL+Z for cancel.";
sleep 15s;
echo;
echo "Building FFmpeg for ${ANDROID_TARGET_ARCH}...";
make clean
make -j8
make install

echo;
echo "Linking FFmpeg libraries...";

${ANDROID_NDK_TOOLCHAINS}ld \
-rpath-link=${ANDROID_NDK_SYSROOT}/usr/lib \
-L${ANDROID_NDK_SYSROOT}/usr/lib \
-L./android/${ANDROID_TARGET_ARCH}/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
./android/${ANDROID_TARGET_ARCH}/libffmpeg.so \
libavcodec/libavcodec.a \
libavfilter/libavfilter.a \
libswresample/libswresample.a \
libavformat/libavformat.a \
libavutil/libavutil.a \
libswscale/libswscale.a \
-lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
${ANDROID_NDK_GCC}/libgcc.a

echo;
echo "FFmpeg successfully builded!";
echo;
echo "Copy *.so file to '[app module]/src/main/jniLibs' of your Android project."
echo "*.so file and headers placed in './ffmpeg/android/${ANDROID_TARGET_ARCH}' directory."


