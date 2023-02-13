package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

public class GroupHeader extends RelativeLayout {
    private boolean online;
    private String name;

    public GroupHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_header, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((TextView) view.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.open_group));
        ((TextView) view.findViewById(R.id.profile_activity)).setText("");
    }

    public void setProfileName(String name) {
        this.name = name;
        ((TextView) findViewById(R.id.profile_name)).setText(name);
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.profile_activity)).setText(status);
    }

    public void setVerified(boolean verified, Context ctx) {
       if(verified) {
           SpannableStringBuilder sb = new SpannableStringBuilder(name);
           ImageSpan imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon);
           ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 * ctx.getResources().getDisplayMetrics().density));
           sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           ((TextView) findViewById(R.id.profile_name)).setText(sb);
       }
    }
}
