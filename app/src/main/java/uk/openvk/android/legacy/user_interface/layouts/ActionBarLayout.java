package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.list_adapters.ActionBarSpinnerAdapter;
import uk.openvk.android.legacy.user_interface.list_items.SimpleListItem;

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
        layoutParams.height = LayoutParams.WRAP_CONTENT;
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
}
