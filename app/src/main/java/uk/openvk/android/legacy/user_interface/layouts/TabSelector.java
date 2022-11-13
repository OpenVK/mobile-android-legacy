package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;

/**
 * Created by Dmitry on 13.11.2022.
 */

public class TabSelector extends LinearLayout {
    private int selectionPos;
    public TabSelector(Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.tab_selector, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public TabSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.tab_selector, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public void setLength(int length) {
        if(length == 1) {
            findViewById(R.id.tab_rl2).setVisibility(GONE);
            findViewById(R.id.tab_rl3).setVisibility(GONE);
        } else if(length == 2) {
            findViewById(R.id.tab_rl2).setVisibility(VISIBLE);
            findViewById(R.id.tab_rl3).setVisibility(GONE);
        } else if(length == 3) {
            findViewById(R.id.tab_rl2).setVisibility(VISIBLE);
            findViewById(R.id.tab_rl3).setVisibility(VISIBLE);
        }
    }

    public void setTabTitle(int position, String title) {
        if(position == 0) {
            ((TextView) findViewById(R.id.tab_rl).findViewById(R.id.tab_text)).setText(title);
        } else if(position == 1) {
            ((TextView) findViewById(R.id.tab_rl2).findViewById(R.id.tab_text)).setText(title);
        } else {
            ((TextView) findViewById(R.id.tab_rl3).findViewById(R.id.tab_text)).setText(title);
        }
    }

    public void setup(final TabHost host, final OnClickListener onClickListener) {
        findViewById(R.id.tab_rl).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 0;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked).setVisibility(GONE);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl));
            }
        });
        findViewById(R.id.tab_rl2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 1;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(GONE);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl2));
            }
        });
        findViewById(R.id.tab_rl3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 2;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(GONE);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl3));
            }
        });
    }

    public int getCursorPos() {
        return selectionPos;
    }
}
