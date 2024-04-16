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
* **via [NashStore](https://store.nashstore.ru/store/637cc36cfb3ed38835524503)** _(why not?)_
* **via [Trashbox](https://trashbox.ru/topics/164477/openvk-legacy)**
* **via [4PDA](https://4pda.to/forum/index.php?showtopic=1057695)**

## Building
We recommend opening the project in [Android Studio 3.1.2](https://developer.android.com/studio/archive) along with Java 7 already installed for perfect support of libraries developed for Android 2.1 Eclair and above.

To provide support for non-native codecs (Theora, VP8, Opus), **FFmpeg v. 2.2.4** is used.

To compile them you need:
+ **GNU/Linux distro or WSL2** \
  Yeah, it is still possible to build libraries on Linux/WSL2, perhaps an assembly will be added to Windows/Cygwin and macOS.

  Tested on Debian 8.11.0, can be built in the latest distributions.
+ **[Android NDK r8e](http://web.archive.org/web/20130501232214/http://developer.android.com/tools/sdk/ndk/index.html) and [Android NDK r10e](https://github.com/android/ndk/wiki/Unsupported-Downloads#r10e)** \
  If already there, you need to specify the path to your NDK via the `ANDROID_NDK_R8E` variable.

  Android NDK `r8e` is highly recommended for providing FFmpeg support in Android 2.2 and below.

  Also, in the project settings, specify the path to Android NDK r10e.
+ **Installed dependencies** \
  See packages listing for [Ubuntu/Debian/Linux Mint](https://trac.ffmpeg.org/wiki/CompilationGuide/Ubuntu) or [CentOS/Fedora](https://trac.ffmpeg.org/wiki/CompilationGuide/Centos).

+ **Scripts that build FFmpeg from source** \
  Run the command inside the OpenVK Legacy repository in terminal:
  ```sh
   chmod +x ./build-ffmpeg.sh
   ANDROID_NDK_R8E=[path/to/ndk-r8e] ANDROID_NDK_R10E=[path/to/ndk-r10e] ./build-ffmpeg.sh
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
7. [FFmpeg 2.2.4](https://github.com/tinelix/ffmpeg-android-builder/tree/main/ffmpeg-2.2.4)  with [builder](https://github.com/tinelix/ffmpeg-android-builder) (LGPLv3.0)
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
