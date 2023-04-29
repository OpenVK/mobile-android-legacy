package uk.openvk.android.legacy.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.openvk.android.legacy.R;

/** OPENVK LEGACY LICENSE NOTIFICATION
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
        if(bdate.length() > 0) {
            SimpleDateFormat originalFormat = new SimpleDateFormat("d.m.yyyy");
            try {
                Date birthdate = originalFormat.parse(bdate);
                ((TextView) findViewById(R.id.birthdate_label2)).setText(new SimpleDateFormat("dd MMMM yyyy").format(birthdate));
                ((LinearLayout) findViewById(R.id.birthdate_ll)).setVisibility(VISIBLE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            ((LinearLayout) findViewById(R.id.birthdate_ll)).setVisibility(GONE);
        }
    }

    public void setInterests(String interests, String music, String movies, String tv, String books) {
        this.interests = interests;
        this.music = music;
        this.movies = movies;
        this.tv = tv;
        this.books = books;
        if(interests != null) {
            if (interests.length() > 0) {
                ((TextView) findViewById(R.id.interests_label2)).setText(interests);
                ((LinearLayout) findViewById(R.id.interests_layout)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.interests_layout)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout)).setVisibility(GONE);
        }
        if(music != null) {
            if (music.length() > 0) {
                ((TextView) findViewById(R.id.music_label2)).setText(music);
                ((LinearLayout) findViewById(R.id.interests_layout2)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.interests_layout2)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout2)).setVisibility(GONE);
        }
        if(movies != null) {
            if (movies.length() > 0) {
                ((TextView) findViewById(R.id.movies_label2)).setText(movies);
                ((LinearLayout) findViewById(R.id.interests_layout3)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.interests_layout3)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout3)).setVisibility(GONE);
        }
        if(tv != null) {
            if (tv.length() > 0) {
                ((TextView) findViewById(R.id.tvshows_label2)).setText(tv);
                ((LinearLayout) findViewById(R.id.interests_layout4)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.interests_layout4)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout4)).setVisibility(GONE);
        }
        if(books != null) {
            if (books.length() > 0) {
                ((TextView) findViewById(R.id.books_label2)).setText(books);
                ((LinearLayout) findViewById(R.id.interests_layout5)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.interests_layout5)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout5)).setVisibility(GONE);
        }
        if(interests != null && music != null && movies != null && tv != null && books != null) {
            if (interests.length() == 0 && music.length() == 0 && movies.length() == 0 && tv.length() == 0 && books.length() == 0) {
                ((LinearLayout) findViewById(R.id.interests_layout_all)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout_all)).setVisibility(GONE);
        }
    }

    public void setContacts(String city) {
        if (city != null) {
            if (city.length() > 0) {
                ((TextView) findViewById(R.id.city_label2)).setText(city);
                ((LinearLayout) findViewById(R.id.city_layout)).setVisibility(VISIBLE);
                if(interests == null && music == null && movies == null && tv == null && books == null) {
                    ((LinearLayout) findViewById(R.id.about_profile)).setVisibility(GONE);
                } else if(interests.length() == 0 && music.length() == 0 && movies.length() == 0
                        && tv.length() == 0 && books.length() == 0) {
                    ((LinearLayout) findViewById(R.id.about_profile)).setVisibility(GONE);
                }
            } else {
                ((LinearLayout) findViewById(R.id.city_layout)).setVisibility(GONE);
                ((LinearLayout) findViewById(R.id.contacts_layout)).setVisibility(GONE);
            }
        } else {
            if(interests == null && music == null && movies == null && tv == null && books == null) {
                ((LinearLayout) findViewById(R.id.about_profile)).setVisibility(GONE);
            } else if(interests.length() == 0 && music.length() == 0 && movies.length() == 0
                    && tv.length() == 0 && books.length() == 0) {
                ((LinearLayout) findViewById(R.id.about_profile)).setVisibility(GONE);
            }
        }
    }
}
