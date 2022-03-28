package uk.openvk.android.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class AboutProfileLinearLayout extends LinearLayout {
    public AboutProfileLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.profile_about, null);

        this.addView(view);
    }
}
