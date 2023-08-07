# OpenVK Legacy для Android

_[English](README.md)_

Автор: [Дмитрий Третьяков (Tinelix)](https://github.com/tretdm)

**OpenVK Legacy** - мобильный клиент для ретро-устройств, работающие на Android 2.1 Eclair и выше.\
_Работает на OpenVK API._

Мы будем рады принять ваши сообщения об ошибках [в нашем баг-трекере](https://github.com/openvk/mobile-android-legacy/projects/1).

![featureGraphic](fastlane/metadata/android/en-US/images/featureGraphic.png)

## Скачать APK
* **через F-Droid**
  * **[repo.openvk.uk](https://repo.openvk.uk/repo/)** (намного быстрее, зеркало ~~[без TLS](http://repo.openvk.co/repo/)~~ не оплачено)
  * [f-droid.org](https://f-droid.org/packages/uk.openvk.android.legacy/)
  * [izzysoft.de](https://apt.izzysoft.de/fdroid/index/apk/uk.openvk.android.legacy)
* **через [Telegram-канал](https://t.me/+nPLHBZqAsFlhYmIy)**
* **через [страницу релизов](https://github.com/openvk/mobile-android-legacy/releases/latest)**
* **через [NashStore](https://store.nashstore.ru/store/637cc36cfb3ed38835524503)** _(как бы для российских телефонов 😂)_
* **через [Trashbox](https://trashbox.ru/topics/164477/openvk-legacy)**

## Сборка
Мы советуем использовать [Android Studio 2.3.2](https://developer.android.com/studio/archive) вместе с Java 7 для идеальной поддержки библиотек (такие как устаревший формат ресурсов и Gradle 2.3.2), разработанные для Android 2.1 Eclair и выше.

Для обеспечения поддержки ненативных кодеков (Theora, VP8, Opus, MP3onMP4) используются две версии FFmpeg - 0.8.5 (рекомендован для совместимости с Android 2.x) и 4.0.4.

Чтобы собрать их, вам понадобится:
+ **Дистрибутив Linux или WSL2**
  Да, на данный момент возможна сборка библиотек на Linux/WSL2, не исключаем добавления сборки под Windows/Cygwin и macOS. Проверено на Ubuntu 12.04 LTS, может быть также возможна в последних версиях.
+ **[Android NDK r10e](https://github.com/android/ndk/wiki/Unsupported-Downloads#r10e)**
  Если у вас уже есть, то вам нужно указать путь к NDK через переменную `ANDROID_NDK_HOME`. Сборка FFmpeg 0.8.5 на `r11c` невозможна из-за ошибки связывания статических библиотек.
+ **Установленные зависимости**
  Посмотрите список пакетов для [Ubuntu/Debian/Linux Mint](https://trac.ffmpeg.org/wiki/CompilationGuide/Ubuntu) или [CentOS/Fedora](https://trac.ffmpeg.org/wiki/CompilationGuide/Centos)
+ **Скрипты, собирающие FFmpeg из исходных кодов**
  Запустите команду внутри репозитория OpenVK Legacy в терминале: `chmod +x ./build-ffmpeg.sh && ./build-ffmpeg.sh`. Исходные коды библиотек FFmpeg, а также сборщика под Android находятся в подмодулях директории `ndk-modules`.

**ВНИМАНИЕ!** После возникновения ошибки `java.util.zip.ZipException: invalid entry compressed size (expected [m] but got [n] bytes)` в задаче `:[package_name]:mockableAndroidJar`, при использовании Android SDK Build-tools 28 и выше необходимо очистить проект (Clean Project).

## Используемые библиотеки
1. [Android Support Library v24 for 1.6+](https://developer.android.com/topic/libraries/support-library) (Apache License 2.0)
2. [HttpUrlWrapper](https://github.com/tinelix/httpurlwrapper) (Apache License 2.0)
3. [PhotoView 1.2.5](https://github.com/Baseflow/PhotoView/tree/v1.2.5) (Apache License 2.0)
4. [SlidingMenu with Android 10+ patch](https://github.com/tinelix/SlidingMenu) (Apache License 2.0)
5. [OkHttp 3.8.0](https://square.github.io/okhttp/) (Apache License 2.0)
6. [Twemojicon (Emojicon с пакетом Twemoji)](https://github.com/rockerhieu/emojicon/tree/1.2) (Apache License 2.0)
7. [ijkplayer 0.8.2](https://github.com/bilibili/ijkplayer/tree/k0.6.2) (LGPL 2.1+)
8. [Retro-ActionBar](https://github.com/tinelix/retro-actionbar) (Apache License 2.0)
9. [Retro-PopupMenu](https://github.com/tinelix/retro-popupmenu) (Apache License 2.0)
10. [SystemBarTint](https://github.com/jgilfelt/SystemBarTint) (Apache License 2.0)
11. [Модификация SwipeRefreshLayout с классическим PTR](https://github.com/xyxyLiu/SwipeRefreshLayout) (Apache License 2.0)
12. [android-i18n-plurals](https://github.com/populov/android-i18n-plurals) (X11 License)
13. [Application Crash Reports 4.6.0](https://github.com/ACRA/acra/tree/acra-4.6.0) (Apache License 2.0)
14. [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader/tree/v1.9.5) (Apache License 2.0)
15. [NineOldAndroids animation API](https://github.com/JakeWharton/NineOldAndroids) (Apache License 2.0)

## Пока что неиспользованные библиотеки
1. [FFmpeg 4.0.4](https://github.com/FFmpeg/FFmpeg/tree/n4.0.4) (LGPLv3.0)

## Лицензия OpenVK Legacy
[GNU Affero GPL v3.0](COPYING) или более поздней версии.

## Ссылки
[Документация по OpenVK API](https://docs.openvk.su/openvk_engine/api/description/)\
[OpenVK Mobile](https://openvk.uk/app)

<a href="https://codeberg.org/OpenVK/mobile-android-legacy">
    <img alt="Get it on Codeberg" src="https://codeberg.org/Codeberg/GetItOnCodeberg/media/branch/main/get-it-on-blue-on-white.png" height="60">
</a>
