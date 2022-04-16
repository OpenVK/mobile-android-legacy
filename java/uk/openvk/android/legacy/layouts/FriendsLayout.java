package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;

import uk.openvk.android.legacy.R;

public class FriendsLayout extends LinearLayout {
    public TextView titlebar_title;
    public String state;
    public JSONArray friends;
    public String send_request;
    public SharedPreferences global_sharedPreferences;

    public FriendsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.friends_layout, null);

        this.addView(view);

    }
}
