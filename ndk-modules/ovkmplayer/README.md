# FFmpeg custom builder for Android
This is a special script that makes it easy to build [FFmpeg 0.8.5](https://github.com/FFmpeg/FFmpeg/tree/n0.8.5) for Android. Builds without problems in Android NDK r9d and r10e with Android 2.0 NDK platform.

### Building
1. If there is none in-the-box, install the missing packages in your package manager: `gcc` `g++` `yasm` `gettext` `autoconf` `automake` `cmake` `git` `git-core` `libass-dev` `libfreetype6-dev` `libmp3lame-dev` `libsdl2-dev` `libtool` `libvdpau-dev` `libvorbis-dev` `pkg-config` `wget` `zlib1g-dev` `texinfo` for Ubuntu/Debian and their based distributions.
2. Download Android NDK (r9d-r10e): [Unsupported NDK Versions](https://github.com/android/ndk/wiki/Unsupported-Downloads)
3. Clone repo.
   `git clone https://github.com/tinelix/ffmpeg-android-builder.git`
4. `cd ffmpeg-android-builder`
5. Change `./build-android.sh` file and `ffmpeg` directory permissions to `0777` (`chmod -R 0777 .`) and run it.

### License
This builder using FFmpeg (modified) source code licensed under LGPLv3 or later version. Scripts are also licensed under this same license.
