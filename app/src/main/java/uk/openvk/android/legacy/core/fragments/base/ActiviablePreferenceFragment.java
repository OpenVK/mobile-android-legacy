/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.core.fragments.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

public class ActiviablePreferenceFragment extends PreferenceFragmentCompatDividers {
    private boolean isActivated;

    public boolean isActivated() {
        return isActivated;
    }

    protected void activate() {
        onActivated();
    }

    public void onActivated() {
        isActivated = true;
    }

    public void deactivate() {
        onDeactivated();
    }

    public  void onDeactivated() {
        isActivated = false;
    }

    @Override
    public void onCreatePreferencesFix(Bundle bundle, String s) {

    }
}
