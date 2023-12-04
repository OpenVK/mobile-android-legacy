package uk.openvk.android.legacy.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import uk.openvk.android.legacy.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

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
 **/

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
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        }
    }

    public void setProfileName(String name) {
        this.name = name;
        ((TextView) findViewById(R.id.profile_name)).setText(name);
        ((TextView) findViewById(R.id.profile_name)).setSelected(true);
    }

    public void setStatus(String status) {
        ((TextView) findViewById(R.id.profile_activity)).setText(status);
    }

    @SuppressLint("SimpleDateFormat")
    public void setLastSeen(int sex, long date, int ls_platform) {
        if(online) {
            ((TextView) findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.online));
        } else if(date > 0) {
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            dt_midnight.setHours(0);
            dt_midnight.setMinutes(0);
            dt_midnight.setSeconds(0);
            long dt_sec = (TimeUnit.SECONDS.toMillis(date));
            Date dt = new Date(dt_sec);
            if((dt_midnight.getTime() - dt_sec) < 60000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_f, getResources().getString(R.string.date_ago_now)));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_ago_now)));
                }
            } else if((dt_midnight.getTime() - dt_sec) < 86400000) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_f, new SimpleDateFormat("HH:mm").format(dt)));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_m, new SimpleDateFormat("HH:mm").format(dt)));
                }
            } else if((dt_midnight.getTime() - dt_sec) < (86400000 * 2)) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_f, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_m, String.format("%s %s",
                            getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else if((dt_midnight.getTime() - dt_sec) < 31536000000L) {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            } else {
                if(sex == 1) {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_f, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM yyyy").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                } else {
                    ((TextView) findViewById(R.id.profile_last_seen)).setText(
                            getResources().getString(R.string.last_seen_profile_m, String.format("%s %s %s",
                            new SimpleDateFormat("d MMMM").format(dt), getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt))));
                }
            }
        } else {
            ((TextView) findViewById(R.id.profile_last_seen)).setText("");
        }
        try {
            ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(VISIBLE);
            if (ls_platform == 4) {
                ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_api_android_app_indicator));
            } else if (ls_platform == 2) {
                ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_api_ios_app_indicator));
            } else if (ls_platform == 1) {
                ((ImageView) findViewById(R.id.profile_api_indicator)).setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_api_mobile_indicator));
            } else {
                ((ImageView) findViewById(R.id.profile_api_indicator)).setVisibility(GONE);
            }
        } catch (OutOfMemoryError ignored) {
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

    public void setAvatarPlaceholder(String picture) {
        if(picture.equals("common_user")) {
            ((ImageView) findViewById(R.id.profile_photo)).setImageDrawable(
                    getResources().getDrawable(R.drawable.profile_user_placeholder));
        }
    }

    public void hideExpandArrow() {
        findViewById(R.id.profile_expand).setVisibility(INVISIBLE);
    }

    public void createProfilePhotoViewer(
            final long user_id, final String original_url) {
        ((ImageView) findViewById(R.id.profile_photo)).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext().getApplicationContext(),
                                PhotoViewerActivity.class);
                        intent.putExtra("where", "comments");
                        try {
                            intent.putExtra("local_photo_addr",
                                    String.format("%s/profile_photos/avatar_o%s",
                                            getContext().getCacheDir(),
                                            user_id));
                            intent.putExtra("original_link", original_url);
                            intent.putExtra("author_id", user_id);
                            intent.putExtra("photo_id", user_id);
                            getContext().startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
        });
    }
}
