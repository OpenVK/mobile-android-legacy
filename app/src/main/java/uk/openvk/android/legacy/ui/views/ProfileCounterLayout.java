package uk.openvk.android.legacy.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class ProfileCounterLayout extends LinearLayout {
    public String action;

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
        if(view != null) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);
        }
    }

    public void setCounter(long count, String label, String action) {
        this.action = action;
        ((TextView) findViewById(R.id.profile_counter_value)).setText(String.valueOf(count));
        ((TextView) findViewById(R.id.profile_counter_title)).setText(label);
    }

    public void setOnCounterClickListener() {
        ((LinearLayout) findViewById(R.id.counter)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Global.openIntentFromCounters(getContext(), action);
            }
        });
    }

    public void setOnCounterClickListener(OnClickListener onClickListener) {
        ((LinearLayout) findViewById(R.id.counter)).setOnClickListener(onClickListener);
    }
}
