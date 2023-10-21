package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import java.util.zip.Inflater;

import uk.openvk.android.legacy.R;

import static uk.openvk.android.legacy.BuildConfig.DEBUG;

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
 */

public class OvkRefreshableHeaderLayout extends LinearLayout implements CustomSwipeRefreshLayout.CustomSwipeRefreshHeadLayout {

    private Context ctx;
    private TextView p2r_tv;
    private ImageView p2r_arrow;
    private ProgressBar p2r_progress;
    private static final SparseArray<String> STATE_MAP = new SparseArray<>();
    {
        STATE_MAP.put(0, "STATE_NORMAL");
        STATE_MAP.put(1, "STATE_READY");
        STATE_MAP.put(2, "STATE_REFRESHING");
        STATE_MAP.put(3, "STATE_COMPLETE");
    }

    public OvkRefreshableHeaderLayout(Context ctx) {
        super(ctx);
        this.ctx = ctx;
        setup();
    }

    private void setup() {
        ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.pull_to_refresh, null);
        header.setBackgroundColor(Color.WHITE);
        p2r_tv = header.findViewById(R.id.p2r_text);
        p2r_arrow = header.findViewById(R.id.p2r_arrow);
        p2r_arrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_pull_arrow));
        p2r_progress = header.findViewById(R.id.p2r_progressbar);
        addView(header, lp);
    }

    @Override
    public void onStateChange(CustomSwipeRefreshLayout.State state, CustomSwipeRefreshLayout.State lastState) {
        if (DEBUG)
            Log.d("csrh", "onStateChange state = " + state + ", lastState = " + lastState);
        int stateCode = state.getRefreshState();
        float percent = state.getPercent();
        int lastStateCode = lastState.getRefreshState();
        switch (stateCode) {
            case CustomSwipeRefreshLayout.State.STATE_NORMAL:
                if (stateCode != lastStateCode) {
                    p2r_arrow.setVisibility(View.VISIBLE);
                    p2r_progress.setVisibility(View.GONE);
                    p2r_tv.setText(R.string.pull_to_refresh);
                }
            case CustomSwipeRefreshLayout.State.STATE_READY:
                if(percent > 1.08) {
                    p2r_arrow.setVisibility(View.VISIBLE);
                    p2r_progress.setVisibility(View.GONE);
                    setImageRotation(180);
                    p2r_tv.setText(R.string.release_to_refresh);
                } else {
                    p2r_arrow.clearAnimation();
                    setImageRotation(0);
                    p2r_progress.setVisibility(View.GONE);
                    p2r_tv.setText(R.string.pull_to_refresh);
                }
                break;
            case CustomSwipeRefreshLayout.State.STATE_REFRESHING:
                if (stateCode != lastStateCode) {
                    p2r_arrow.clearAnimation();
                    p2r_arrow.setVisibility(View.GONE);
                    p2r_progress.setVisibility(View.VISIBLE);
                    p2r_tv.setText(R.string.refreshing);
                }
        }
    }

    private void setImageRotation(float rotation) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            p2r_arrow.setRotation(rotation);
        } else {
            if (p2r_arrow.getTag() == null){
                p2r_arrow.setTag(0f);
            }
            p2r_arrow.clearAnimation();
            Float lastDegree = (Float)p2r_arrow.getTag();
            RotateAnimation rotate = new RotateAnimation(lastDegree, rotation,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            p2r_arrow.setTag(rotation);
            rotate.setFillAfter(true);
            p2r_arrow.startAnimation(rotate);
        }
    }
}
