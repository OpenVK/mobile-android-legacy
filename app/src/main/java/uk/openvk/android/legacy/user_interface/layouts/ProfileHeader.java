package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.text.CenteredImageSpan;

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

    public void setLastSeen(int sex, long date, int ls_platform) {
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
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, getResources().getString(R.string.date_ago_now)));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_ago_now)));
                }
            } else if((dt_midnight.getTime() - dt_sec) < 86400000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, new SimpleDateFormat("HH:mm").format(dt)));
                }
            } else if((dt_midnight.getTime() - dt_sec) < (86400000 * 2)) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else if((dt_midnight.getTime() - dt_sec) < 31536000000L) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM yyyy").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            }
        }
        ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(VISIBLE);
        if(ls_platform == 4) {
            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_android_app_indicator));
        } else if(ls_platform == 2) {
            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_ios_app_indicator));
        } else if(ls_platform == 1) {
            ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(getResources().getDrawable(R.drawable.ic_api_mobile_indicator));
        } else {
            ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(GONE);
        }
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setVerified(boolean verified, Context ctx) {
       if(verified) {
           SpannableStringBuilder sb = new SpannableStringBuilder(name);
           ImageSpan imageSpan;
           imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon);
           ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 * ctx.getResources().getDisplayMetrics().density));
           sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
           ((TextView) findViewById(R.id.profile_name)).setText(sb);
       }
    }
}
