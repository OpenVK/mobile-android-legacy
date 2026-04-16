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

package uk.openvk.android.legacy.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Date;

import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.legacy.ui.list.items.PublicPageAboutItem;

public class AboutProfileLayout extends LinearLayout {
    private ArrayList<PublicPageAboutItem> items;
    private PublicPageAboutAdapter aboutAdapter;

    public AboutProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_profile_about, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(!((OvkApplication) getContext().getApplicationContext()).isTablet) {
            if (global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                view.findViewById(R.id.profile_ext_header)
                        .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            } else if (global_prefs.getString("uiTheme", "blue").equals("Black")) {
                view.findViewById(R.id.profile_ext_header)
                        .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            }
        }
    }

    public void setStatus(String status) {
        EditText status_editor = findViewById(R.id.status_editor);
        if(status != null) {
            if(status.length() > 0) {
                status_editor.setText(status);
            } else {
                status_editor.setVisibility(GONE);
            }
        } else {
            status_editor.setVisibility(GONE);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void setBirthdate(String bdate) {

    }

    public void setInterests(User user) {

        if(items == null)
            items = new ArrayList<>();

        if(user.interests.length() > 0)
            items.add(
                    new PublicPageAboutItem(
                            getResources().getString(R.string.profile_interests), user.interests
                    )
            );

        if(user.music.length() > 0)
            items.add(
                new PublicPageAboutItem(
                        getResources().getString(R.string.profile_music), user.music
                )
            );

        if(user.movies.length() > 0)
            items.add(
                    new PublicPageAboutItem(
                            getResources().getString(R.string.profile_movies), user.movies
                    )
            );

        if(user.tv.length() > 0)
            items.add(
                    new PublicPageAboutItem(
                            getResources().getString(R.string.profile_tv), user.tv
                    )
            );

        if(user.books.length() > 0)
            items.add(
                    new PublicPageAboutItem(
                        getResources().getString(R.string.profile_books), user.books
                    )
            );
    }

    public void clear() {
        items.clear();
        if(aboutAdapter != null)
            aboutAdapter.notifyDataSetChanged();
    }

    public void setProfileInfoAdapter() {
        RecyclerView about_rv = findViewById(R.id.about_rv);
        if(aboutAdapter == null) {
            aboutAdapter = new PublicPageAboutAdapter(getContext(), items);
            about_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            about_rv.setAdapter(aboutAdapter);
        } else
            aboutAdapter.notifyDataSetChanged();

        findViewById(R.id.about_profile).setVisibility(VISIBLE);

        if(aboutAdapter.getItemCount() == 0) {
            findViewById(R.id.about_profile).setVisibility(VISIBLE);
            findViewById(R.id.about_rv).setVisibility(GONE);
            findViewById(R.id.no_info).setVisibility(VISIBLE);
        }
    }

    public void setContacts(String city) {
    }

    public void setRegistrationDate(Date regdate) {
        if(items == null)
            items = new ArrayList<>();

        if(regdate != null) {
            items.add(
                    new PublicPageAboutItem(
                            getResources().getString(R.string.profile_regdate),
                            Global.formatTimestamp(getContext(), regdate.getTime())
                    )
            );
        }
    }

    public int getProfileFieldsCount() {
        if(items == null)
            return 0;
        else
            return items.size();
    }
}
