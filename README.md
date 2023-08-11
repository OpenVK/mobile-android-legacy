# OpenVK Legacy for Android

_[Русский](README_RU.md)_

Author: [Dmitry Tretyakov (Tinelix)](https://github.com/tretdm)

**OpenVK Legacy** is mobile client for retro devices running Android 2.1 Eclair and higher.\
_Powered by OpenVK API._

We will be happy to accept your bugreports [in our bug-tracker](https://github.com/openvk/mobile-android-legacy/projects/1).

![featureGraphic](fastlane/metadata/android/en-US/images/featureGraphic.png)

## Download APK
* **via F-Droid**
  * **[repo.openvk.uk](https://repo.openvk.uk/repo/)** (much faster, mirror ~~[without TLS](http://repo.openvk.co/repo/)~~ not paid)
  * [f-droid.org](https://f-droid.org/packages/uk.openvk.android.legacy/)
  * [izzysoft.de](https://apt.izzysoft.de/fdroid/index/apk/uk.openvk.android.legacy)
* **via [Telegram channel](https://t.me/+nPLHBZqAsFlhYmIy)**
* **via [Releases page](https://github.com/openvk/mobile-android-legacy/releases/latest)**
* **via [NashStore](https://store.nashstore.ru/store/637cc36cfb3ed38835524503)** _(for Russian phones kinda 😂)_
* **via [Trashbox](https://trashbox.ru/topics/164477/openvk-legacy)**

## Building
We recommend using [Android Studio 2.3.2](https://developer.android.com/studio/archive), Android NDK r10e and Java 7 for perfect support of libraries developed for Android 2.1 Eclair and above.

To ensure non-native codecs support (Theora, VP8, Opus, MP3onMP4), two versions of FFmpeg are used:
* FFmpeg 0.8.12 for 32-bit CPU architectures - ARMv5/v6, ARMv7a, x86
* FFmpeg 3.1.4 for 64-bit CPU architectures - ARMv8a

To compile them you need:
+ **GNU/Linux distro or WSL2** \
  Yeah, it is still possible to build libraries on Linux/WSL2, perhaps an assembly will be added to Windows/Cygwin and macOS.

  Tested on Ubuntu 12.04 LTS and Debian 8.7.1, can be built in the latest distributions. _FFmpeg old version build on Ubuntu 22.04 LTS? 🤔_
+ **[Android NDK r8e](http://web.archive.org/web/20130501232214/http://developer.android.com/tools/sdk/ndk/index.html) and [Android NDK r11c](https://github.com/android/ndk/wiki/Unsupported-Downloads#r11c)** \
  If already there, you need to specify the path to your NDK via the `ANDROID_NDK_HOME` variable.

  Android NDK `r8e` is highly recommended for providing FFmpeg 0.8.12 support in older versions of Android.

  Also, the absolute path to the NDK (which contains the `platforms` directory) in the project settings must be changed so that synchronization with Gradle goes correctly.
+ **Installed dependencies** \
  See packages listing for [Ubuntu/Debian/Linux Mint](https://trac.ffmpeg.org/wiki/CompilationGuide/Ubuntu) or [CentOS/Fedora](https://trac.ffmpeg.org/wiki/CompilationGuide/Centos).

  **OPTIONAL:** To run 32-bit binaries, including NDK r7, you need to install the following packages: `libstdc++6:i386`, `libgcc1:i386`, `zlib1g:i386`, `libncurses5:i386` for Ubuntu/Debian/Linux Mint.
+ **Scripts that build FFmpeg from source** \
  Run the command inside the OpenVK Legacy repository in terminal:
  ```sh
   chmod +x ./build-ffmpeg.sh && \
   ANDROID_NDK_HOME=[path/to/ndk-r8e] ./build-ffmpeg.sh 0.8.12 && \
   ANDROID_NDK_HOME=[path/to/ndk-r11c] ./build-ffmpeg.sh 3.1.4
  ```

  The source codes of the FFmpeg libraries, as well as the code of builder for Android, are located in the `builder` submodule of the [`./ndk-modules/ovkmplayer` directory](https://github.com/openvk/mobile-android-legacy/tree/main/ndk-modules/ovkmplayer).

**ATTENTION!** After an `java.util.zip.ZipException: invalid entry compressed size (expected [m] but got [n] bytes)` error occurs in the `:[package_name]:mockableAndroidJar` task when using Android SDK Build-tools 28 and higher, be sure to clean the project.

## Used Libraries
1. [Android Support Library v24 for 1.6+](https://developer.android.com/topic/libraries/support-library) (Apache License 2.0)
2. [HttpUrlWrapper](https://github.com/tinelix/httpurlwrapper) (Apache License 2.0)
3. [PhotoView 1.2.5](https://github.com/Baseflow/PhotoView/tree/v1.2.5) (Apache License 2.0)
4. [SlidingMenu with Android 10+ patch](https://github.com/tinelix/SlidingMenu) (Apache License 2.0)
5. [OkHttp 3.8.0](https://square.github.io/okhttp/) (Apache License 2.0)
6. [Twemojicon (Emojicon with Twemoji pack)](https://github.com/tinelix/twemojicon) (Apache License 2.0)
7. [FFmpeg 3.1.4](https://github.com/tinelix/ffmpeg-android-builder/tree/main/ffmpeg-3.1.4) and [FFmpeg 0.8.12](https://github.com/tinelix/ffmpeg-android-builder/tree/main/ffmpeg-0.8.12) with [builder](https://github.com/tinelix/ffmpeg-android-builder/tree/42c67d80bc924c9709a7648e2d12f04ddf43b32b) (LGPLv3.0)
8. [Retro-ActionBar](https://github.com/tinelix/retro-actionbar) (Apache License 2.0)
9. [Retro-PopupMenu](https://github.com/tinelix/retro-popupmenu) (Apache License 2.0)
10. [SystemBarTint](https://github.com/jgilfelt/SystemBarTint) (Apache License 2.0)
11. [SwipeRefreshLayout Mod with classic PTR header](https://github.com/xyxyLiu/SwipeRefreshLayout) (Apache License 2.0)
12. [android-i18n-plurals](https://github.com/populov/android-i18n-plurals) (X11 License)
13. [Application Crash Reports 4.6.0](https://github.com/ACRA/acra/tree/acra-4.6.0) (Apache License 2.0)
14. [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader/tree/v1.9.5) (Apache License 2.0)
15. [NineOldAndroids animation API](https://github.com/JakeWharton/NineOldAndroids) (Apache License 2.0)

## OpenVK Legacy License
[GNU Affero GPL v3.0](COPYING) or later version.

## Links
[OpenVK API docs](https://docs.openvk.su/openvk_engine/api/description/)\
[OpenVK Mobile](https://openvk.uk/app)

<a href="https://codeberg.org/OpenVK/mobile-android-legacy">
    <img alt="Get it on Codeberg" src="https://codeberg.org/Codeberg/GetItOnCodeberg/media/branch/main/get-it-on-blue-on-white.png" height="60">
</a>
