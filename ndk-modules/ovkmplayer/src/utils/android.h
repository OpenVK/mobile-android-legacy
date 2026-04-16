/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

#ifndef OVKMPLAYER_UTILS_ANDROID_H
#define OVKMPLAYER_UTILS_ANDROID_H

#include <jni.h>

// OS codenames ("1.0" - "16" / 1 - 36)

const int OS_CODENAME_A                                =  1;
const int OS_CODENAME_B                                =  2;
const int OS_CODENAME_CUPCAKE                          =  3;
const int OS_CODENAME_DONUT                            =  4;
const int OS_CODENAME_ECLAIR                           =  5;
const int OS_CODENAME_ECLAIR_2_0_1                     =  6;
const int OS_CODENAME_ECLAIR_2_1                       =  7;
const int OS_CODENAME_FROYO                            =  8;
const int OS_CODENAME_GINGERBREAD                      =  9;
const int OS_CODENAME_GINGERBREAD_2_3_6                = 10;
const int OS_CODENAME_HONEYCOMB                        = 11;
const int OS_CODENAME_HONEYCOMB_3_1                    = 12;
const int OS_CODENAME_HONEYCOMB_3_2                    = 13;
const int OS_CODENAME_ICS                              = 14;
const int OS_CODENAME_ICS_4_0_3                        = 15;
const int OS_CODENAME_JB                               = 16;
const int OS_CODENAME_JB_4_2                           = 17;
const int OS_CODENAME_JB_4_3                           = 18;
const int OS_CODENAME_KITKAT                           = 19;
const int OS_CODENAME_KITKAT_W                         = 20;
const int OS_CODENAME_LOLLIPOP                         = 21;
const int OS_CODENAME_LOLLIPOP_5_1                     = 22;
const int OS_CODENAME_MARSHMALLOW                      = 23;
const int OS_CODENAME_NOUGAT                           = 24;
const int OS_CODENAME_NOUGAT_7_1                       = 25;
const int OS_CODENAME_OREO                             = 26;
const int OS_CODENAME_OREO_8_1                         = 27;
const int OS_CODENAME_PIE                              = 28;
const int OS_CODENAME_Q                                = 29;
const int OS_CODENAME_R                                = 30;
const int OS_CODENAME_S                                = 31;
const int OS_CODENAME_S_12_1                           = 32;
const int OS_CODENAME_TIRAMISU                         = 33;
const int OS_CODENAME_UDC                              = 34;
const int OS_CODENAME_VIC                              = 35;
const int OS_CODENAME_BAKLAVA                          = 36;

class android {
public:
    static int getApiLevel(JNIEnv *env);
};

#define LOG_TAG "OVK-MPLAY-LIB"
#define LOG_LEVEL 10
#define LOGD(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}


#endif //OVKMPLAYER_UTILS_ANDROID_H
