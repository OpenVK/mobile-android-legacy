package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class ProfileWallSelector extends LinearLayout {
    public ProfileWallSelector(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_wall_selector, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        ((ImageView) findViewById(R.id.profile_wall_post_btn)).setVisibility(GONE);
    }

    public void selectTab() {
        return;
    }

    public void showNewPostIcon() {
        ((ProgressBar) findViewById(R.id.profile_wall_progress)).setVisibility(GONE);
        ((ImageView) findViewById(R.id.profile_wall_post_btn)).setVisibility(VISIBLE);
    }

    public void setUserName(String username) {
        ((TextView) findViewById(R.id.profile_wall_owner_posts)).setText(getResources().getString(R.string.wall_owners_posts, username));
        // not implemented yet!
        ((TextView) findViewById(R.id.profile_wall_owner_posts)).setVisibility(GONE);
    }

    public void setToGroup() {
        ((TextView) findViewById(R.id.profile_wall_owner_posts)).setVisibility(GONE);
    }
}
