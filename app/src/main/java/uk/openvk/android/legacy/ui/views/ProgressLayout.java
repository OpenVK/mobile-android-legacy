/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import uk.openvk.android.legacy.R;

public class ProgressLayout extends LinearLayout {
    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_progress, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void enableDarkTheme(boolean value) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        enableDarkTheme(value, 1);
    }

    public void enableDarkTheme(boolean value, int variant) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (value) {
                if (variant == 0) {
                    setBackgroundColor(getResources().getColor(R.color.color_black_v2));
                    progressBar.setIndeterminateDrawable(
                            getResources().getDrawable(R.drawable.progress_light)
                    );
                } else {
                    setBackgroundColor(getResources().getColor(R.color.window_bg_black));
                    progressBar.setIndeterminateDrawable(
                            getResources().getDrawable(R.drawable.progress_light)
                    );
                }
            } else {
                setBackgroundColor(Color.parseColor("#e3e4e6"));
                progressBar.setIndeterminateDrawable(
                        getResources().getDrawable(R.drawable.progress_dark));
            }
        }
    }
}
