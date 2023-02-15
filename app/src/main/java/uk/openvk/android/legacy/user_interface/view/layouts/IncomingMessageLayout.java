package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import uk.openvk.android.legacy.R;

public class IncomingMessageLayout extends LinearLayout {
    public IncomingMessageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.message_in, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        LinearLayout msg_block = (LinearLayout) view.findViewById(R.id.msg_wrap);
        layoutParams = (LinearLayout.LayoutParams) msg_block.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        msg_block.setLayoutParams(layoutParams);
    }

    public void setAvatar(Bitmap bitmap) {
        ImageView avatar = findViewById(R.id.msg_sender_photo);
        avatar.setImageBitmap(bitmap);
    }
}
