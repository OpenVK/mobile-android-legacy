package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;

public class ProfileCounterLayout extends LinearLayout {
    public String action;

    public ProfileCounterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_counter, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void setCounter(int count, String label, String action) {
        this.action = action;
        ((TextView) findViewById(R.id.profile_counter_value)).setText(String.valueOf(count));
        ((TextView) findViewById(R.id.profile_counter_title)).setText(label);
    }

    public void setOnCounterClickListener() {
        ((LinearLayout) findViewById(R.id.counter)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getContext().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) getContext()).openIntentfromCounters(action);
                } else if(getContext().getClass().getSimpleName().equals("ProfileIntentActivity")) {
                    ((ProfileIntentActivity) getContext()).openIntentfromCounters(action);
                }
            }
        });
    }
}
