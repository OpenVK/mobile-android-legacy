package uk.openvk.android.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import java.util.Timer;

public class SlidingMenuLayout extends LinearLayout {
    public SlidingMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.sliding_menu_layout, null);

        this.addView(view);
    }
}
