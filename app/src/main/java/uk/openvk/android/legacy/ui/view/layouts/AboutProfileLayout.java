package uk.openvk.android.legacy.ui.view.layouts;

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
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.legacy.ui.list.items.PublicPageAboutItem;

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

public class AboutProfileLayout extends LinearLayout {
    private String interests;
    private String music;
    private String movies;
    private String tv;
    private String books;

    public AboutProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.profile_about, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
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

    public void setInterests(String interests, String music, String movies, String tv, String books) {
        this.interests = interests;
        this.music = music;
        this.movies = movies;
        this.tv = tv;
        this.books = books;
        ArrayList<PublicPageAboutItem> items = new ArrayList<>();
        if(this.interests != null && this.music != null && this.movies != null && this.tv != null
                && this.books != null) {
            if(this.interests.length() > 0) {
                items.add(new PublicPageAboutItem(
                        getResources().getString(R.string.profile_interests), interests));
            }
            if(this.music.length() > 0) {
                items.add(new PublicPageAboutItem(
                        getResources().getString(R.string.profile_music), music));
            }
            if(this.movies.length() > 0) {
                items.add(new PublicPageAboutItem(
                        getResources().getString(R.string.profile_movies), movies));
            }
            if(this.tv.length() > 0) {
                items.add(new PublicPageAboutItem(
                        getResources().getString(R.string.profile_tv), tv));
            }
            if(this.books.length() > 0) {
                items.add(new PublicPageAboutItem(
                        getResources().getString(R.string.profile_books), books));
            }
            RecyclerView about_rv = findViewById(R.id.about_rv);
            PublicPageAboutAdapter aboutAdapter = new PublicPageAboutAdapter(getContext(), items);
            about_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            about_rv.setAdapter(aboutAdapter);
            findViewById(R.id.about_profile).setVisibility(VISIBLE);
            if(aboutAdapter.getItemCount() == 0) {
                findViewById(R.id.about_profile).setVisibility(VISIBLE);
                findViewById(R.id.about_rv).setVisibility(GONE);
                findViewById(R.id.no_info).setVisibility(VISIBLE);
            }
        } else {
            findViewById(R.id.about_profile).setVisibility(VISIBLE);
            findViewById(R.id.about_rv).setVisibility(GONE);
            findViewById(R.id.no_info).setVisibility(VISIBLE);
        }
    }

    public void setContacts(String city) {
    }
}
