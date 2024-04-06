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

package uk.openvk.android.legacy.core.activities.base;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;
import java.util.HashMap;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.utils.SecureCredentialsStorage;

public class TranslucentActivity extends Activity {

    protected HashMap<String, Object> client_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar();
        client_info = SecureCredentialsStorage.generateClientInfo(
                this, new HashMap<String, Object>()
        );
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
        Window window = getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(statusbar_color));
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            tintManager.setTintDrawable(
                    getResources().getDrawable(statusbar_color));
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB) {
            int vendor_translucent_sb_flag = getDeviceVendorTranslucentStatusBarFlag();
            View decor = window.getDecorView();
            if (decor != null && vendor_translucent_sb_flag != 0)
                decor.setSystemUiVisibility(vendor_translucent_sb_flag);
        }
    }

    /**
     *  The command is responsible for enabling the translucent status bar
     *  at the system level on pre-KitKat devices, if the device vendor has
     *  equipped this option.
     *  <p>
     *  <b>Note:</b> Works only in Samsung and Sony devices with stock
     *  firmware.
     */

    int getDeviceVendorTranslucentStatusBarFlag() {

        String[] libs = getPackageManager().getSystemSharedLibraryNames();
        String reflect = null;

        if (libs == null)
            return 0;

        for (String lib : libs) {
            if (lib.equals("touchwiz")) // if Samsung TouchWiz SystemUI
                reflect = "SYSTEM_UI_FLAG_TRANSPARENT_BACKGROUND";
            else if (lib.startsWith("com.sonyericsson.navigationbar")) // if Sony SystemUI
                reflect = "SYSTEM_UI_FLAG_TRANSPARENT";
        }

        if (reflect == null)
            return 0;

        try {
            Field field = View.class.getField(reflect);
            if (field.getType() == Integer.TYPE)
                return field.getInt(null);
        } catch (Exception ignored) { }

        return 0;
    }

    public void setTranslucentStatusBar(int type, int res) {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(res)); // color resource ID
        } else {
            if (type == 0) { // Drawable (or color resource ID)
                tintManager.setTintDrawable(
                        getResources().getDrawable(res));
            } else {       // Color
                tintManager.setTintColor(res);
            }
        }
    }
}
