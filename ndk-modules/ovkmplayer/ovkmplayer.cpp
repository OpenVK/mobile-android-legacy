/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This file is part of OpenVK Legacy.
 *
 * OpenVK Legacy is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

#include <jni.h>
#include <string.h>
#include <stdio.h>

char version[7] = "0.0.1";

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_uk_openvk_android_legacy_utils_OvkMediaPlayer_showLogo(JNIEnv *env, jobject instance) {
        char* logo = (char *) "Logo";
        sprintf(logo, "OpenVK Media Player ver. %s for Android"
                "\r\nOpenVK Media Player for Android is part of OpenVK Legacy Android-app "
                "licensed under AGPLv3 or later version."
                "\r\nUsing FFmpeg licensed under LGPLv3 or later version.", version);
        return env->NewStringUTF(logo);
    }
};