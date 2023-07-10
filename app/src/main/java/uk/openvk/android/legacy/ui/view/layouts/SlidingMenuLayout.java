package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
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

    private String instance;

    public SlidingMenuLayout(final Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((ListView) findViewById(R.id.menu_view)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
        (findViewById(R.id.arrow)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleAccountMenu();
            }
        });
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
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_black_v2));
        }
    }

    public SlidingMenuLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        ((ListView) findViewById(R.id.menu_view)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
        (findViewById(R.id.profile_menu_ll)).setOnClickListener(new OnClickListener() {
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
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(
                    String.format("%s/%s/photos_cache/profile_avatars/avatar_%s",
                            getContext().getCacheDir(), instance, account.user.id), options);
            if (bitmap != null) avatar.setImageBitmap(bitmap);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
    }

    private void toogleAccountMenu() {
        final RecyclerView account_menu_view = findViewById(R.id.account_menu_view);
        this.showAccountMenu = !this.showAccountMenu;
        View arrow = findViewById(R.id.arrow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            float[] fArr = new float[2];
            fArr[0] = this.showAccountMenu ? 0 : -180;
            fArr[1] = this.showAccountMenu ? -180 : 0;
            ObjectAnimator.ofFloat(arrow, "rotation", fArr).setDuration(300L).start();
        } else {
            RotateAnimation anim = new RotateAnimation(this.showAccountMenu ? 0 : -180,
                    this.showAccountMenu ? -180 : 0, 1, 0.5f, 1, 0.5f);
            anim.setFillAfter(true);
            anim.setDuration(300L);
            arrow.startAnimation(anim);
        }
        if(account_menu_view.getVisibility() == VISIBLE) {
            account_menu_view.setVisibility(GONE);
        } else {
            account_menu_view.setVisibility(VISIBLE);
        }
    }
    
}
