package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

public class ActionBarImitation extends LinearLayout {                  // for pre-Honeycomb (pre-3.0) devices
    public ActionBarImitation(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.actionbar_imitation, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public void setHomeButtonVisibillity(boolean value) {
        if(value) {
            ((LinearLayout) findViewById(R.id.titlebar)).setVisibility(GONE);
            ((LinearLayout) findViewById(R.id.titlebar2)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.titlebar)).setVisibility(VISIBLE);
            ((LinearLayout) findViewById(R.id.titlebar2)).setVisibility(GONE);
        }
    }

    public void setTitle(String title) {
        TextView title_view = (TextView) findViewById(R.id.titlebar_title);
        if(((LinearLayout) findViewById(R.id.titlebar)).getVisibility() == GONE) {
            title_view = (TextView) findViewById(R.id.titlebar_title2);
        }
        title_view.setText(title);
    }

    public void setSubtitle(String title) {
        TextView title_view = (TextView) findViewById(R.id.titlebar_subtitle);
        if(((LinearLayout) findViewById(R.id.titlebar)).getVisibility() == GONE) {
            title_view = (TextView) findViewById(R.id.titlebar_subtitle2);
        }
        title_view.setText(title);
    }

    public void setActionButton(String icon, int position, OnClickListener onClickListener) {
        if(icon.equals("new_post")) {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(VISIBLE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
            }
        } else if(icon.equals("check_mark")) {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(VISIBLE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
            }
        } else {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(GONE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(GONE);
            }
        }
    }


    public void setOnMenuClickListener(OnClickListener onClickListener) {
        ((ImageButton) findViewById(R.id.menuButton)).setOnClickListener(onClickListener);
    }

    public void setOnBackClickListener(OnClickListener onClickListener) {
        ((ImageButton) findViewById(R.id.ovkButton)).setOnClickListener(onClickListener);
    }
}
