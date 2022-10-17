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
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            dt_midnight.setHours(0);
            dt_midnight.setMinutes(0);
            dt_midnight.setSeconds(0);
            long dt_sec = (TimeUnit.SECONDS.toMillis(date));
            Date dt = new Date(dt_sec);
            if((dt_midnight.getTime() - dt_sec) < 60000) {
                ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_ago_now)));
            } else if((dt_midnight.getTime() - dt_sec) < 86400000) {
                ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, new SimpleDateFormat("HH:mm").format(dt)));
            } else if((dt_midnight.getTime() - dt_sec) < (86400000 * 2)) {
                ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s",
                        getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
            } else if((dt_midnight.getTime() - dt_sec) < 31536000000L) {
                ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                        new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
            } else {
                ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                        new SimpleDateFormat("d MMMM yyyy").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
            }
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
