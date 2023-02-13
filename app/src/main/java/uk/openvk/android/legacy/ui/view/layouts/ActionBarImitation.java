package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;

public class ActionBarImitation extends LinearLayout {                  // for pre-Honeycomb (pre-3.0) devices
    private boolean homeButtonisVisible;
    public ArrayAdapter overflow_adapter;
    private String title;

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

    public void setHomeButtonVisibility(boolean value) {
        homeButtonisVisible = value;
        if(value) {
            ((LinearLayout) findViewById(R.id.titlebar)).setVisibility(GONE);
            ((LinearLayout) findViewById(R.id.titlebar2)).setVisibility(VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.titlebar)).setVisibility(VISIBLE);
            ((LinearLayout) findViewById(R.id.titlebar2)).setVisibility(GONE);
        }
    }

    public void createOverflowMenu(boolean value, final Menu menu, final OnClickListener onClickListener) {
        if(value) {
            ArrayList<String> item_titles = new ArrayList<>();
            ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow_holo_dark));
            try {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    item_titles.add(item.getTitle().toString());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            overflow_adapter = new ArrayAdapter(getContext(), R.layout.popup_item, android.R.id.text1, item_titles);
            ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v);
                }
            });
            ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(VISIBLE);
            ((ImageButton) findViewById(R.id.action_btn2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow_holo_dark));
            ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v);
                }
            });
            ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
        } else {
            ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(GONE);
            ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(GONE);
        }
    }

    public void setTitle(String title) {
        this.title = title;
        ((ActionBarLayout) findViewById(R.id.custom_layout)).setAppTitle(title);
        if(((LinearLayout) findViewById(R.id.titlebar)).getVisibility() == GONE) {
            TextView title_view = (TextView) findViewById(R.id.titlebar_title2);
            title_view.setText(title);
        }
    }

    public void setSubtitle(String title) {
        if(((LinearLayout) findViewById(R.id.titlebar)).getVisibility() == GONE) {
            TextView title_view = (TextView) findViewById(R.id.titlebar_subtitle2);
            title_view.setText(title);
            if(title.length() > 0)
                title_view.setVisibility(VISIBLE);
        }
    }

    public void setActionButton(String icon, int position, OnClickListener onClickListener) {
        if(icon.equals("new_post")) {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setVisibility(VISIBLE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_write));
                ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
            }
        } else if(icon.equals("done")) {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setVisibility(VISIBLE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_done));
                ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
            }
        } else if(icon.equals("download")) {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_download_holo_dark));
                ((ImageButton) findViewById(R.id.action_btn)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_download_holo_dark));
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setVisibility(VISIBLE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_download_holo_dark));
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.action_btn2)).setImageDrawable(getResources().getDrawable(R.drawable.ic_download_holo_dark));
                ((ImageButton) findViewById(R.id.action_btn2)).setOnClickListener(onClickListener);
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(VISIBLE);
            }
        } else {
            if(position == 0) {
                ((ImageButton) findViewById(R.id.action_btn)).setVisibility(GONE);
                ((ImageButton) findViewById(R.id.action_btn_actionbar2)).setVisibility(GONE);
            } else {
                ((ImageButton) findViewById(R.id.action_btn2)).setVisibility(GONE);
                ((ImageButton) findViewById(R.id.action_btn2_actionbar2)).setVisibility(GONE);
            }
        }
    }

    public void setOnBackClickListener(OnClickListener onClickListener) {
        (findViewById(R.id.ovkButton)).setOnClickListener(onClickListener);
        (findViewById(R.id.backButton)).setOnClickListener(onClickListener);
    }

    public void enableDarkTheme(boolean value) {
        if(value) {
            ((LinearLayout) findViewById(R.id.titlebar)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            ((LinearLayout) findViewById(R.id.titlebar2)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
        } else {
            ((LinearLayout) findViewById(R.id.titlebar)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            ((LinearLayout) findViewById(R.id.titlebar2)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
        }
    }

    @Deprecated
    public void enableCustomView(boolean value) {
        if(value) {
            ((ActionBarLayout) findViewById(R.id.custom_layout)).setVisibility(VISIBLE);
            ((ActionBarLayout) findViewById(R.id.custom_layout2)).setVisibility(VISIBLE);
            ((LinearLayout) findViewById(R.id.title_layout2)).setVisibility(GONE);
        } else {
            ((ActionBarLayout) findViewById(R.id.custom_layout)).setVisibility(GONE);
            ((ActionBarLayout) findViewById(R.id.custom_layout2)).setVisibility(GONE);
            ((LinearLayout) findViewById(R.id.title_layout2)).setVisibility(VISIBLE);
        }
    }

    public ActionBarLayout getLayout() {
        if(homeButtonisVisible) {
            return ((ActionBarLayout) findViewById(R.id.custom_layout2));
        } else {
            return ((ActionBarLayout) findViewById(R.id.custom_layout));
        }
    }

    public void enableTransparentTheme(boolean value) {
        if(value) {
            ((LinearLayout) findViewById(R.id.titlebar)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black_transparent));
            ((LinearLayout) findViewById(R.id.titlebar2)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black_transparent));
        } else {
            ((LinearLayout) findViewById(R.id.titlebar)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            ((LinearLayout) findViewById(R.id.titlebar2)).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
        }
    }

    public String getTitle() {
        return title;
    }
}
