package uk.openvk.android.legacy.user_interface.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.counters.AccountCounters;
import uk.openvk.android.legacy.user_interface.list.adapters.ActionBarSpinnerAdapter;
import uk.openvk.android.legacy.user_interface.list.items.SimpleListItem;

/**
 * Created by Dmitry on 07.11.2022.
 */

public class ActionBarLayout extends LinearLayout {
    private ArrayList<SimpleListItem> spinnerActionBarArray;
    private ActionBarSpinnerAdapter spinnerAdapter;

    public ActionBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.custom_actionbar_layout, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            layoutParams.height = LayoutParams.WRAP_CONTENT;
        } else {
            layoutParams.height = LayoutParams.MATCH_PARENT;
        }
        view.setLayoutParams(layoutParams);
    }

    public ActionBarLayout(Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.custom_actionbar_layout, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        createSpinnerAdapter(context);
    }

    public void createSpinnerAdapter(Context ctx) {
        if(spinnerActionBarArray != null) {
            spinnerActionBarArray.clear();
            for (int spinner_action_bar_index = 0; spinner_action_bar_index < getResources().getStringArray(R.array.newsfeed_actionbar_items).length; spinner_action_bar_index++) {
                spinnerActionBarArray.add(new SimpleListItem(getResources().getStringArray(R.array.newsfeed_actionbar_items)[spinner_action_bar_index]));
            }
            spinnerAdapter.notifyDataSetChanged();
        } else {
            spinnerActionBarArray = new ArrayList<SimpleListItem>();
            for (int spinner_action_bar_index = 0; spinner_action_bar_index < getResources().getStringArray(R.array.newsfeed_actionbar_items).length; spinner_action_bar_index++) {
                spinnerActionBarArray.add(new SimpleListItem(getResources().getStringArray(R.array.newsfeed_actionbar_items)[spinner_action_bar_index]));
            }
            spinnerAdapter = new ActionBarSpinnerAdapter(ctx, spinnerActionBarArray, Color.BLACK, Color.WHITE, "newsfeed_actionbar");
            ((Spinner) findViewById(R.id.spinner)).setAdapter(spinnerAdapter);
        }
    }

    public void adjustLayout() {
        spinnerAdapter.notifyDataSetChanged();
    }

    public void selectItem(int i) {
        ((Spinner) findViewById(R.id.spinner)).setSelection(0);
    }

    public void setMode(String mode) {
        if(mode.equals("spinner")) {
            findViewById(R.id.spinner_ab).setVisibility(VISIBLE);
            findViewById(R.id.title_ab).setVisibility(GONE);
        } else {
            findViewById(R.id.title_ab).setVisibility(VISIBLE);
            findViewById(R.id.spinner_ab).setVisibility(GONE);
        }
    }

    @SuppressLint("DefaultLocale")
    public void setNotificationCount(AccountCounters counters) {
        long total_count = (counters.friends_requests + counters.new_messages);
        if(total_count > 999) {
            ((TextView) findViewById(R.id.notif_badge)).setText(String.format("%.1fK", ((double)total_count / (double)1000)));
        } else if(total_count > 9999) {
            ((TextView) findViewById(R.id.notif_badge)).setText(String.format("%sK+", (int)((double)total_count / (double)1000)));
        } else {
            ((TextView) findViewById(R.id.notif_badge)).setText(String.format("%s", total_count));
        }
        if(total_count > 0)
        ((TextView) findViewById(R.id.notif_badge)).setVisibility(VISIBLE);
    }

    public void setOnHomeButtonClickListener(OnClickListener listener) {
        findViewById(R.id.home_button).setOnClickListener(listener);
    }

    public void setAppTitle(String title) {
        ((TextView) findViewById(R.id.ab_title)).setText(title);
    }

    public int getNewsfeedSelection() {
        return ((Spinner) findViewById(R.id.spinner)).getSelectedItemPosition();
    }
}
