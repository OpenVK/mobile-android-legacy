package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import uk.openvk.android.legacy.R;

public class ProfileHeader extends RelativeLayout {
    private boolean online;
    private String name;

    public ProfileHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.profile_header, null);

        this.addView(view);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void setProfileName(String name) {
        this.name = name;
        ((TextView) findViewById(R.id.profile_name)).setText(name);
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.profile_activity)).setText(status);
    }

    public void setLastSeen(int date) {
        if(online) {
            ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.online));
        } else {
            ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, new SimpleDateFormat("HH:mm").format(date)));
        }
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setVerified(boolean verified, Context ctx) {
       if(verified) {
           SpannableStringBuilder sb = new SpannableStringBuilder(name);
           ImageSpan imageSpan = new ImageSpan(ctx.getApplicationContext(), R.drawable.ic_verified, DynamicDrawableSpan.ALIGN_BASELINE);
           sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           ((TextView) findViewById(R.id.profile_name)).setText(sb);
       }
    }
}
