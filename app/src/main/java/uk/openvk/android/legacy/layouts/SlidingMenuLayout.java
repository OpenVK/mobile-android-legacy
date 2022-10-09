package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;

public class SlidingMenuLayout extends LinearLayout {

    public SlidingMenuLayout(final Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.sliding_menu_layout, null);

        this.addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((ListView) findViewById(R.id.menu_view)).setBackgroundColor(getResources().getColor(R.color.transparent));
        ((ListView) findViewById(R.id.menu_view)).setCacheColorHint(getResources().getColor(R.color.transparent));
        ((LinearLayout) findViewById(R.id.profile_menu_ll)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) context).openAccountProfile();
                }
            }
        });
        TextView profile_name = (TextView) findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));
    }

    public SlidingMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.sliding_menu_layout, null);

        this.addView(view);
    }

    public void setSearchListener(OnClickListener onClickListener) {
        SlidingMenuSearch search = findViewById(R.id.sliding_menu_search);
        search.setOnClickListener(onClickListener);
    }

    public void setProfileName(String name) {
        TextView profile_name = (TextView) findViewById(R.id.profile_name);
        profile_name.setText(name);
    }
}
