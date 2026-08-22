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

package uk.openvk.android.legacy.ui.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.R;

public class HeaderPreference extends Preference {

    public HeaderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        view.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        TextView versionInfo = view.findViewById(R.id.app_version_text);
        if(versionInfo != null)
            versionInfo.setText(
                    getContext().getResources().getString(
                            R.string.app_version_text,
                            BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT
                    )
            );
    }
}
