package uk.openvk.android.legacy.user_interface.view.layouts;

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

/**
 * Created by Dmitry on 30.09.2022.
 */
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
    }

    public void setToGroup() {
        ((TextView) findViewById(R.id.profile_wall_owner_posts)).setVisibility(GONE);
    }
}
