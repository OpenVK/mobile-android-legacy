package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.openvk.android.legacy.R;

public class AboutProfileLayout extends LinearLayout {
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
        if(interests.length() > 0) {
            ((TextView) findViewById(R.id.interests_label2)).setText(interests);
            ((LinearLayout) findViewById(R.id.interests_layout)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout)).setVisibility(GONE);
        }
        if(music.length() > 0) {
            ((TextView) findViewById(R.id.music_label2)).setText(music);
            ((LinearLayout) findViewById(R.id.interests_layout2)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout2)).setVisibility(GONE);
        }
        if(movies.length() > 0) {
            ((TextView) findViewById(R.id.movies_label2)).setText(movies);
            ((LinearLayout) findViewById(R.id.interests_layout3)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout3)).setVisibility(GONE);
        }
        if(tv.length() > 0) {
            ((TextView) findViewById(R.id.tvshows_label2)).setText(tv);
            ((LinearLayout) findViewById(R.id.interests_layout4)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout4)).setVisibility(GONE);
        }
        if(books.length() > 0) {
            ((TextView) findViewById(R.id.books_label2)).setText(books);
            ((LinearLayout) findViewById(R.id.interests_layout5)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.interests_layout5)).setVisibility(GONE);
        }
        if(interests.length() == 0 && music.length() == 0 && movies.length() == 0 && tv.length() == 0 && books.length() == 0) {
            ((LinearLayout) findViewById(R.id.interests_layout_all)).setVisibility(GONE);
        }
    }

    public void setContacts(String city) {
        if(city.length() > 0) {
            ((TextView) findViewById(R.id.city_label2)).setText(city);
            ((LinearLayout) findViewById(R.id.city_layout)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.city_layout)).setVisibility(GONE);
            ((LinearLayout) findViewById(R.id.contacts_layout)).setVisibility(GONE);
        }
    }
}
