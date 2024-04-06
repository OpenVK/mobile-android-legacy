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

package uk.openvk.android.legacy.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;

public class ProfileCounterLayout extends LinearLayout {
    public String action;

    @SuppressLint("InflateParams")
    public ProfileCounterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = null;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            if (!((OvkApplication) getContext().getApplicationContext()).isTablet) {
                switch (global_prefs.getString("uiTheme", "blue")) {
                    case "Gray":
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter_gray, null);
                        break;
                    case "Black":
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter_black, null);
                        break;
                    default:
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter, null);
                        break;
                }
            } else {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.profile_counter_light, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.profile_counter, null);
        }
        this.addView(view);

        float dp = context.getResources().getDisplayMetrics().scaledDensity;

        if(view != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.width =  (int) (92 * dp);
            view.setLayoutParams(layoutParams);
        }
    }

    @SuppressLint("InflateParams")
    public ProfileCounterLayout(Context context) {
        super(context);
        View view = null;
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            if (!((OvkApplication) getContext().getApplicationContext()).isTablet) {
                switch (global_prefs.getString("uiTheme", "blue")) {
                    case "Gray":
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter_gray, null);
                        break;
                    case "Black":
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter_black, null);
                        break;
                    default:
                        view = LayoutInflater.from(getContext()).inflate(
                                R.layout.profile_counter, null);
                        break;
                }
            } else {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.profile_counter_light, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.profile_counter, null);
        }
        this.addView(view);

        if(view != null) {
            adjustSize(view, context, context.getResources().getConfiguration().orientation);
        }
    }

    private void adjustSize(View view, Context ctx, int orientation) {
        float dp = ctx.getResources().getDisplayMetrics().scaledDensity;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int screen_width = ctx.getResources().getDisplayMetrics().widthPixels;
        if(((OvkApplication) ctx.getApplicationContext()).isTablet) {
            layoutParams.width = (int) (100 * dp);
        } else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.width = screen_width / 3 - (int) (12 * dp);
        }
        view.setLayoutParams(layoutParams);
    }

    public void setCounter(long count, String label, final String action) {
        this.action = action;
        if(action != null) {
            findViewById(R.id.counter).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Global.openIntentFromCounters(getContext(), action);
                }
            });
        }
        ((TextView) findViewById(R.id.profile_counter_value)).setText(String.valueOf(count));
        ((TextView) findViewById(R.id.profile_counter_title)).setText(label);
    }

    public void setOnCounterClickListener(OnClickListener onClickListener) {
        findViewById(R.id.counter).setOnClickListener(onClickListener);
    }
}
