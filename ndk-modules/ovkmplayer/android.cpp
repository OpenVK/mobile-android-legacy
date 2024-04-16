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

#include <stdint.h>
#include "android.h"

int android::getApiLevel(JNIEnv *env) {
    bool result = false;
    jclass versionClass = env->FindClass("android/os/Build$VERSION");
    if (NULL != versionClass)
        result = true;

    jfieldID sdkIntFieldID = NULL;
    if (result)
        result = (NULL != (sdkIntFieldID = env->GetStaticFieldID(versionClass, "SDK_INT", "I")));

    int version = env->GetStaticIntField(versionClass, sdkIntFieldID);
    env->DeleteLocalRef(versionClass);
    if (result) {
        return version;
    } else {
        return -1;
    }
}
