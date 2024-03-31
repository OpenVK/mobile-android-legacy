# OpenVK Legacy для Android

_[English](README.md)_

Автор: [Дмитрий Третьяков (Tinelix)](https://github.com/tretdm)

**OpenVK Legacy** - мобильный клиент для ретро-устройств, работающие на Android 2.1 Eclair и выше.\
_Работает на OpenVK API._

Мы будем рады принять ваши сообщения об ошибках [в нашем баг-трекере](https://github.com/openvk/mobile-android-legacy/projects/1).

![featureGraphic](fastlane/metadata/android/en-US/images/featureGraphic.png)

## Скачать APK
* **через F-Droid**
  * [f-droid.org](https://f-droid.org/packages/uk.openvk.android.legacy/)
  * [izzysoft.de](https://apt.izzysoft.de/fdroid/index/apk/uk.openvk.android.legacy)
  * [tinelix.ru](https://fdroid.tinelix.ru)
* **через [Telegram-канал](https://t.me/+nPLHBZqAsFlhYmIy)**
* **через [страницу релизов](https://github.com/openvk/mobile-android-legacy/releases/latest)**
* **через [NashStore](https://store.nashstore.ru/store/637cc36cfb3ed38835524503)** _(как бы для российских телефонов 😂)_
* **через [Trashbox](https://trashbox.ru/topics/164477/openvk-legacy)**

## Сборка
Мы советуем использовать [Android Studio 3.1.2](https://developer.android.com/studio/archive) вместе с Java JRE 8 для идеальной поддержки библиотек, разработанные для Android 2.1 Eclair и выше.

**ВНИМАНИЕ!** После возникновения ошибки `java.util.zip.ZipException: invalid entry compressed size (expected [m] but got [n] bytes)` в задаче `:[package_name]:mockableAndroidJar`, при использовании Android SDK Build-tools 28 и выше необходимо очистить проект (Clean Project).

## Используемые компоненты приложения
**Большинство совместимых компонентов приложения, включая библиотеки, гарантированно работают в Android 2.1 и выше.**

Возможно, они вам пригодятся для разработки приложений с поддержкой очень старых версий Android, несмотря на проблемы с безопасностью и стабильностью в свежих версиях Android.

#### Библиотеки

1. **[Android Support Library v24](https://developer.android.com/topic/libraries/support-library)** (Apache License 2.0)
2. **[HttpUrlWrapper](https://github.com/tinelix/httpurlwrapper)** (Apache License 2.0)
3. **[PhotoView 1.2.5](https://github.com/Baseflow/PhotoView/tree/v1.2.5)** (Apache License 2.0)
4. **[SlidingMenu с патчем для Android 10+](https://github.com/tinelix/SlidingMenu)** (Apache License 2.0)
5. **[OkHttp 3.8.0](https://square.github.io/okhttp/)** (Apache License 2.0)
6. **[Twemojicon](https://github.com/tinelix/twemoji/tree/1.2)** (Apache License 2.0)
8. **[Retro-ActionBar](https://github.com/tinelix/retro-actionbar)** (Apache License 2.0)
9. **[Retro-PopupMenu](https://github.com/tinelix/retro-popupmenu)** (Apache License 2.0)
10. **[SystemBarTint](https://github.com/jgilfelt/SystemBarTint)** (Apache License 2.0)
11. **[Модификация SwipeRefreshLayout с Pull-to-Refresh](https://github.com/xyxyLiu/SwipeRefreshLayout)** (Apache License 2.0)
12. **[android-i18n-plurals](https://github.com/populov/android-i18n-plurals)** (X11 License)
13. **[Application Crash Reports 4.6.0](https://github.com/ACRA/acra/tree/acra-4.6.0)** (Apache License 2.0) \
    _По поводу применения ACRA в приложении смотрите [issue №153](https://github.com/openvk/mobile-android-legacy/issues/153)._
15. **[Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader/tree/v1.9.5)** (Apache License 2.0)
16. **[NineOldAndroids animation API](https://github.com/JakeWharton/NineOldAndroids)** (Apache License 2.0)

#### Оформление
1. **Оригинальные ресурсы ВКонтакте 3.x** \
   Автор: [Григорий Клюшников](https://grishka.me)
2. **Темы оформления, основанные на ВК3:** "Серая" и "Черная"
3. [**Язык дизайна Holo**](https://web.archive.org/web/20130217132335/http://developer.android.com/design/index.html)

## Лицензия OpenVK Legacy
[GNU Affero GPL v3.0](COPYING) или более поздней версии.

## Ссылки
[Документация по OpenVK API](https://docs.openvk.su/openvk_engine/ru/api/description/)\
[OpenVK Mobile](https://openvk.uk/app)

<a href="https://codeberg.org/OpenVK/mobile-android-legacy">
    <img alt="Get it on Codeberg" src="https://codeberg.org/Codeberg/GetItOnCodeberg/media/branch/main/get-it-on-blue-on-white.png" height="60">
</a>
