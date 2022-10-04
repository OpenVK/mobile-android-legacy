package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

public class OutcomingMessageLayout extends LinearLayout {
    public OutcomingMessageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.message_out, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        LinearLayout msg_block = (LinearLayout) view.findViewById(R.id.msg_wrap);
        TextView msg_text = (TextView) msg_block.findViewById(R.id.msg_text);
        layoutParams = (LinearLayout.LayoutParams) msg_text.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        msg_text.setLayoutParams(layoutParams);
    }
}
