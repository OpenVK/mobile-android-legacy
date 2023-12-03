package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

/*  Copyleft © 2022, 2023 OpenVK Team
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

public class GroupHeader extends RelativeLayout {
    private boolean online;
    private String name;

    public GroupHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_header, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((TextView) view.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.open_group));
        ((TextView) view.findViewById(R.id.profile_activity)).setText("");
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        }
    }

    public void setProfileName(String name) {
        this.name = name;
        ((TextView) findViewById(R.id.profile_name)).setText(name);
        ((TextView) findViewById(R.id.profile_name)).setSelected(true);
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.profile_activity)).setText(status);
    }

    public void setVerified(boolean verified, Context ctx) {
       if(verified) {
           SpannableStringBuilder sb = new SpannableStringBuilder(name);
           ImageSpan imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon);
           ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 *
                   ctx.getResources().getDisplayMetrics().density));
           sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           ((TextView) findViewById(R.id.profile_name)).setText(sb);
       }
    }

    public void createGroupPhotoViewer(
            final long user_id, final String original_url) {
        ((ImageView) findViewById(R.id.profile_photo)).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext().getApplicationContext(),
                                PhotoViewerActivity.class);
                        intent.putExtra("where", "comments");
                        try {
                            intent.putExtra("local_photo_addr",
                                    String.format("%s/profile_photos/avatar_o%s",
                                            getContext().getCacheDir(),
                                            user_id));
                            intent.putExtra("original_link", original_url);
                            intent.putExtra("author_id", user_id);
                            intent.putExtra("photo_id", user_id);
                            getContext().startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
    }
}
