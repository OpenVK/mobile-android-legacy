package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import uk.openvk.android.legacy.R;

public class CommentPanel extends RelativeLayout {
    public CommentPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.comment_panel_layout, null);

        this.addView(view);

    }
}
