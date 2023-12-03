package uk.openvk.android.legacy.ui.view.layouts;

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
            ((TextView) findViewById(R.id.tab_rl2).findViewById(R.id.tab_text2)).setText(title);
        } else {
            ((TextView) findViewById(R.id.tab_rl3).findViewById(R.id.tab_text3)).setText(title);
        }
    }

    public void setup(final TabHost host, final OnClickListener onClickListener) {
        findViewById(R.id.tab_rl).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 0;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked2).setVisibility(GONE);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked3).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl));
            }
        });
        findViewById(R.id.tab_rl2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 1;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked2).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(GONE);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked3).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl2));
            }
        });
        findViewById(R.id.tab_rl3).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionPos = 2;
                host.setCurrentTab(selectionPos);
                findViewById(R.id.tab_rl3).findViewById(R.id.tab_checked3).setVisibility(VISIBLE);
                findViewById(R.id.tab_rl).findViewById(R.id.tab_checked).setVisibility(GONE);
                findViewById(R.id.tab_rl2).findViewById(R.id.tab_checked2).setVisibility(GONE);
                onClickListener.onClick(findViewById(R.id.tab_rl3));
            }
        });
    }

    public int getCursorPos() {
        return selectionPos;
    }
}
