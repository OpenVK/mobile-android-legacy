package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import uk.openvk.android.legacy.R;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

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
