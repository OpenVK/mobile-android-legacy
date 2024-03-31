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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

public class AboutGroupLayout extends LinearLayout {

    private String description;
    private String site;

    public AboutGroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_group_about, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            if (global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                view.findViewById(R.id.profile_ext_header)
                        .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            } else if (global_prefs.getString("uiTheme", "blue").equals("Black")) {
                view.findViewById(R.id.profile_ext_header)
                        .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void setGroupInfo(String description, String site) {
        this.description = description;
        this.site = site;
        if(description != null) {
            if (description.length() > 0) {
                ((TextView) findViewById(R.id.description_label2)).setText(description);
                ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(GONE);
        }
        if(site != null) {
            if (site.length() > 0) {
                ((TextView) findViewById(R.id.site_label2)).setText(site);
                ((TextView) findViewById(R.id.site_label2)).setMovementMethod(LinkMovementMethod.getInstance());
                ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(GONE);
        }
        if(description == null && site == null) {
            findViewById(R.id.about_group_layout).setVisibility(GONE);
        } else if(description.length() == 0 && site.length() == 0) {
            findViewById(R.id.about_group_layout).setVisibility(GONE);
        }
    }

/*  public void setContacts() {

    }
*/
}
