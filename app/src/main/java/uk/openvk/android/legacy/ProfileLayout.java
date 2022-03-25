package uk.openvk.android.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ProfileLayout extends LinearLayout {
    public ProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_layout, null);

        this.addView(view);
    }
}
