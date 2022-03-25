package uk.openvk.android.legacy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class ProfileHeader extends RelativeLayout {
    public ProfileHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_head, null);

        this.addView(view);
    }
}
