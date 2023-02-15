package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import uk.openvk.android.legacy.R;

public class ConversationPanel extends RelativeLayout {
    public ConversationPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.conversation_panel_layout, null);

        this.addView(view);

    }
}
