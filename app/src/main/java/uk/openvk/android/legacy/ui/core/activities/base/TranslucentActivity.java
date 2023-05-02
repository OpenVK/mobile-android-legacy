package uk.openvk.android.legacy.ui.core.activities.base;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import uk.openvk.android.legacy.R;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of
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
 **/
public class TranslucentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar();
    }

    private void setTranslucentStatusBar() {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tintManager.setTintDrawable(
                    getResources().getDrawable(R.color.statusbar_color));
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            tintManager.setTintDrawable(
                    getResources().getDrawable(R.color.transparent_statusbar_color));
        }
    }
}
