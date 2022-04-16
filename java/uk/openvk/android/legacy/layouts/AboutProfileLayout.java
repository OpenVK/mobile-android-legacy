package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import uk.openvk.android.legacy.R;

public class AboutProfileLayout extends LinearLayout {
    public AboutProfileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.profile_about, null);

        this.addView(view);
    }
}
