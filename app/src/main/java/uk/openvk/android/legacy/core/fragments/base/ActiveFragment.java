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

package uk.openvk.android.legacy.core.fragments.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;

public class ActiveFragment extends Fragment {
    private boolean mIsActivated;
    protected OpenVKAPI mOpenVK;
    protected SharedPreferences mGlobalPrefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() instanceof NetworkFragmentActivity)
            mOpenVK = ((NetworkFragmentActivity) getActivity()).getOpenVKAPI();

        mGlobalPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean onReceivedAPIResponse(int message, Bundle data) {
        return false;
    }

    public boolean isActivated() {
        return mIsActivated;
    }

    protected void activate() {
        onActivated();
    }

    public void onActivated() {
        mIsActivated = true;
    }

    protected void deactivate() {
        onDeactivated();
    }

    public void onDeactivated() {
        mIsActivated = false;
    }

    public void adjustLayout(int orientation) {

    }

    public int getObjectsSize() {
        return 0;
    }
}
