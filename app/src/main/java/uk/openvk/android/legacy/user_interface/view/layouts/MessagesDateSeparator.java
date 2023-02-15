package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

public class MessagesDateSeparator extends LinearLayout {
    public MessagesDateSeparator(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.messages_date_separator, null);
        this.addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        layoutParams.setMargins((int)(8 * getResources().getDisplayMetrics().scaledDensity), (int)(4 * getResources().getDisplayMetrics().scaledDensity),
                (int)(4 * getResources().getDisplayMetrics().scaledDensity), (int)(4 * getResources().getDisplayMetrics().scaledDensity));
        ((TextView) view.findViewById(R.id.date_text)).setLayoutParams(layoutParams);
    }
}
