package uk.openvk.android.legacy.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.CommonAttachment;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class CommonAttachView extends FrameLayout {
    private final String instance;
    private Attachment attachment;
    private Bitmap thumbnail;

    public CommonAttachView(@NonNull Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_common, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
    }

    public CommonAttachView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_common, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
    }

    @SuppressLint("DefaultLocale")
    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
        if(attachment != null) {
            if(attachment.type.equals("note")) {
                CommonAttachment commonAttachment = ((CommonAttachment) attachment.getContent());
                ((TextView) findViewById(R.id.attach_title)).setText(commonAttachment.title);
                ((TextView) findViewById(R.id.attach_subtitle)).setText(
                        getResources().getString(R.string.attach_note));
                ((ImageView) findViewById(R.id.attach_icon)).setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_attach_note));
            }
        }
    }

    public void setIntent(final Intent intent) {
        if(intent != null) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(intent);
                }
            });
        }
    }

}
