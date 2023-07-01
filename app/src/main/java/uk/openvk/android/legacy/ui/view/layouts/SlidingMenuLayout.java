package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.api.entities.Account;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class SlidingMenuLayout extends LinearLayout {

    public SlidingMenuLayout(final Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((ListView) findViewById(R.id.menu_view)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
        ((ListView) findViewById(R.id.menu_view)).setCacheColorHint(
                getResources().getColor(R.color.transparent));
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
        TextView version_name = findViewById(R.id.version_label);
        version_name.setText(getResources().getString(R.string.app_version_s,
                BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT));
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            version_name.setVisibility(GONE);
        }

        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.transparent_statusbar_color_gray));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.transparent_statusbar_color_black));
        }
    }

    public SlidingMenuLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);
        ((ListView) findViewById(R.id.menu_view)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
        ((ListView) findViewById(R.id.menu_view)).setCacheColorHint(
                getResources().getColor(R.color.transparent));
        ((LinearLayout) findViewById(R.id.profile_menu_ll)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) context).openAccountProfile();
                }
            }
        });
        TextView profile_name = findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));
        TextView version_name = findViewById(R.id.version_label);
        version_name.setText(getResources().getString(R.string.app_version_s,
                BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT));
        if(BuildConfig.BUILD_TYPE.equals("release")) {
            version_name.setVisibility(GONE);
        }

        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_black_v2));
        }
    }

    public void setSearchListener(OnClickListener onClickListener) {
        SlidingMenuSearch search = findViewById(R.id.sliding_menu_search);
        search.setOnClickListener(onClickListener);
    }

    public void setProfileName(String name) {
        TextView profile_name = (TextView) findViewById(R.id.profile_name);
        profile_name.setText(name);
    }

    public void setAccountProfileListener(final Context ctx) {
        ((LinearLayout) findViewById(R.id.profile_menu_ll)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).openAccountProfile();
                }
            }
        });
    }

    public void loadAccountAvatar(Account account, String quality) {
        ImageView avatar = (ImageView) findViewById(R.id.avatar);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(
                String.format("%s/photos_cache/account_avatar/avatar_%s",
                        getContext().getCacheDir(), account.id), options);
        try {
            switch (quality) {
                case "medium":
                    if (bitmap != null) {
                        account.user.avatar = bitmap;
                    } else if (account.user.avatar_msize_url.length() > 0) {
                        account.user.avatar = null;
                    } else {
                        account.user.avatar = null;
                    }
                    break;
                case "high":
                    if (bitmap != null) {
                        account.user.avatar = bitmap;
                    } else if (account.user.avatar_hsize_url.length() > 0) {
                        account.user.avatar = null;
                    } else {
                        account.user.avatar = null;
                    }
                    break;
                default:
                    if (bitmap != null) {
                        account.user.avatar = bitmap;
                    } else if (account.user.avatar_osize_url.length() > 0) {
                        account.user.avatar = null;
                    } else {
                        account.user.avatar = null;
                    }
                    break;
            }
            if (account.user.avatar != null) ((ImageView) findViewById(R.id.avatar))
                    .setImageBitmap(account.user.avatar);
        } catch (Exception ignored) {
        }

    }
    
}
