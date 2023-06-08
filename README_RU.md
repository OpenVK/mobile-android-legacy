# OpenVK Legacy для Android

_[English](https://github.com/openvk/mobile-android-legacy/blob/master/README.md)_

Автор: [Дмитрий Третьяков (Tinelix)](https://github.com/tretdm)

**OpenVK Legacy** - мобильный клиент для ретро-устройств, работающие на Android 2.1 Eclair и выше.\
_Работает на OpenVK API._

мы будем рады принять ваши сообщения об ошибках [в нашем баг-трекере](https://github.com/openvk/mobile-android-legacy/projects/1).

![featureGraphic](https://github.com/openvk/mobile-android-legacy/blob/main/fastlane/metadata/android/en-US/images/featureGraphic.png)

## Скачать APK
* **через F-Droid**
  * **[repo.openvk.uk](https://repo.openvk.uk/repo/)** (намного быстрее, зеркало ~~[без TLS](http://repo.openvk.co/repo/)~~ не оплачен)
  * [f-droid.org](https://f-droid.org/packages/uk.openvk.android.legacy/)
  * [izzysoft.de](https://apt.izzysoft.de/fdroid/index/apk/uk.openvk.android.legacy)
* **через [Telegram-канал](https://t.me/+nPLHBZqAsFlhYmIy)**
* **через [страницу релизов](https://github.com/openvk/mobile-android-legacy/releases/latest)**
* **через [NashStore](https://store.nashstore.ru/store/637cc36cfb3ed38835524503)** _(как бы для российских телефонов 😂)_
* **через [Trashbox](https://trashbox.ru/topics/164477/openvk-legacy)**

## Сборка
Мы советуем использовать [Android Studio 2.3.2](https://developer.android.com/studio/archive) вместе с Java 7 для идеальной поддержки библиотек (такие как устаревший формат ресурсов и Gradle 2.3.2), разработанные для Android 2.1 Eclair и выше.

**ВНИМАНИЕ!** После возникновения ошибки `java.util.zip.ZipException: invalid entry compressed size (expected [m] but got [n] bytes)` в задаче `:[package_name]:mockableAndroidJar`, при использовании Android SDK Build-tools 28 и выше необходимо очистить проект (Clean Project).

## Используемые библиотеки
1. [Android Support Library v24 for 1.6+](https://developer.android.com/topic/libraries/support-library) (Apache License 2.0)
2. [Apache Wrapped HTTP Client 4.1.2](https://mvnrepository.com/artifact/org.jbundle.util.osgi.wrapped/org.jbundle.util.osgi.wrapped.org.apache.http.client/4.1.2#gradle) (Apache License 2.0)
3. [PhotoView 1.2.5](https://github.com/Baseflow/PhotoView/tree/v1.2.5) (Apache License 2.0)
4. [SlidingMenu with Android 10+ patch](https://github.com/tinelix/SlidingMenu) (Apache License 2.0)
5. [OkHttp 3.8.0](https://square.github.io/okhttp/) (Apache License 2.0)
6. [Emojicon 1.2](https://github.com/rockerhieu/emojicon/tree/1.2) (Apache License 2.0)
7. [ijkplayer 0.8.2](https://github.com/bilibili/ijkplayer/tree/k0.6.2) (LGPL 2.1+)
8. [Retro-ActionBar](https://github.com/tinelix/retro-actionbar) (Apache License 2.0)
9. [Retro-PopupMenu](https://github.com/tinelix/retro-popupmenu) (Apache License 2.0)
10. [SystemBarTint](https://github.com/jgilfelt/SystemBarTint) (Apache License 2.0)
11. [SwipeRefreshLayout Mod with classic PTR header](https://github.com/xyxyLiu/SwipeRefreshLayout) (Apache License 2.0)

## Лицензия OpenVK Legacy
[GNU (Affero) GPL v3.0](https://github.com/openvk/mobile-android-legacy/blob/main/COPYING) или более поздней версии.

## Ссылки
[Документация по OpenVK API](https://docs.openvk.su/openvk_engine/api/description/)\
[OpenVK Mobile](https://openvk.uk/app)
