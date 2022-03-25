package uk.openvk.android.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class ProfileCounterLayout extends LinearLayout {
    public ProfileCounterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_counter, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }
}
