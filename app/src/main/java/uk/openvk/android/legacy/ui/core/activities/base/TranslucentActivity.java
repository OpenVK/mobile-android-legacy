package uk.openvk.android.legacy.ui.core.activities.base;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import uk.openvk.android.legacy.R;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
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
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int statusbar_color = R.color.transparent_statusbar_color;
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            statusbar_color = R.color.transparent_statusbar_color_gray;
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            statusbar_color = R.color.transparent_statusbar_color_black;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(statusbar_color));
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            tintManager.setTintDrawable(
                    getResources().getDrawable(statusbar_color));
        }
    }

    public void setTranslucentStatusBar(int type, int res) {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        if(type == 0) { // Drawable (or color resource ID)
            tintManager.setTintDrawable(
                    getResources().getDrawable(res));
        } else {       // Color
            tintManager.setTintColor(res);
        }
    }
}
