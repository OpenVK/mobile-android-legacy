/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.views;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.ui.list.adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuObject;

public class SlidingMenuLayout extends LinearLayout {

    private final RecyclerView menuListView;
    private int accountMenuTargetHeight;
    private String instance;
    private ArrayList<SlidingMenuObject> menuItems;
    public boolean showAccountMenu = true;
    private int friendsCount = -1;

    public SlidingMenuLayout(final Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);

        RecyclerView account_menu_view = findViewById(R.id.account_menu_view);
        account_menu_view.measure(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        );
        account_menu_view.setHasFixedSize(true);

        accountMenuTargetHeight = account_menu_view.getMeasuredHeight();
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        menuListView = findViewById(R.id.menu_view);
        menuListView.setHasFixedSize(true);
        findViewById(R.id.menu_view).setBackgroundColor(
                getResources().getColor(R.color.transparent)
        );

        (findViewById(R.id.arrow)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleAccountMenu(!isVisibleAccountMenu());
            }
        });

        findViewById(R.id.profile_menu_ll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof AppActivity) {
                    ((AppActivity) context).openAccountProfile();
                }
            }
        });

        TextView profile_name = findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));

        TextView version_name = findViewById(R.id.version_label);
        version_name.setText(
                getResources().getString(
                        R.string.app_version_s, BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT
                )
        );

        if(!OvkApplication.isDebug) {
            version_name.setVisibility(GONE);
        }

        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_black_v2));
        }
        setSearchListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext().getApplicationContext(), QuickSearchActivity.class);
                getContext().startActivity(intent);
            }
        });
    }

    public SlidingMenuLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_sliding_menu, this, false);
        this.addView(view);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();

        RecyclerView account_menu_view = findViewById(R.id.account_menu_view);
        account_menu_view.setHasFixedSize(true);
        menuListView = findViewById(R.id.menu_view);
        menuListView.setHasFixedSize(true);

        findViewById(R.id.menu_view).setBackgroundColor(getResources().getColor(R.color.transparent));

        findViewById(R.id.profile_menu_ll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof AppActivity) {
                    ((AppActivity) context).openAccountProfile();
                }
            }
        });

        (findViewById(R.id.arrow)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleAccountMenu(!isVisibleAccountMenu());
            }
        });

        TextView profile_name = findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));
        TextView version_name = findViewById(R.id.version_label);
        version_name.setText(getResources().getString(R.string.app_version_s,
                BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT));

        if(!OvkApplication.isDebug) {
            version_name.setVisibility(GONE);
        }

        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.setBackgroundColor(getResources().getColor(R.color.color_black_v2));
        }
        setSearchListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext().getApplicationContext(), QuickSearchActivity.class);
                getContext().startActivity(intent);
            }
        });
    }

    public void setSearchListener(OnClickListener onClickListener) {
        SlidingMenuSearch search = findViewById(R.id.sliding_menu_search);
        search.setOnClickListener(onClickListener);
    }

    public void setProfileName(String name) {
        TextView profile_name = findViewById(R.id.profile_name);
        profile_name.setText(name);
    }

    public void setAccountProfileListener(final Context ctx) {
        findViewById(R.id.profile_menu_ll).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx instanceof AppActivity) {
                    ((AppActivity) ctx).openAccountProfile();
                }
            }
        });
    }

    public void loadAccountAvatar(OpenVKAPI ovk_api, String quality, boolean download) {
        ImageView avatar = findViewById(R.id.avatar);
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap bitmap = BitmapFactory.decodeFile(
                    String.format("%s/%s/photos_cache/profile_avatars/avatar_%s",
                            getContext().getCacheDir(), instance, ovk_api.account.user.id), options);

            if (bitmap != null) avatar.setImageBitmap(bitmap);

            if(ovk_api.account.user != null && ovk_api.account.user.id > 0 && download)
                ovk_api.account.user.downloadAvatar(ovk_api.dlman, quality);

        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
    }

    public void toogleAccountMenu(boolean open) {
        final RecyclerView account_menu_view = findViewById(R.id.account_menu_view);
            accountMenuTargetHeight = (int) (account_menu_view.getAdapter().getItemCount() *
                    ((47 * (getResources().getDisplayMetrics().scaledDensity))));

            final View arrow = findViewById(R.id.arrow);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                float[] fArr = new float[2];
                fArr[0] = open ? 0 : -180;
                fArr[1] = open ? -180 : 0;
                ObjectAnimator.ofFloat(arrow, "rotation", fArr).setDuration(300L).start();
            } else {
                RotateAnimation anim = new RotateAnimation(
                        open ? 0 : -180, open ? -180 : 0,
                        1, 0.5f, 1, 0.5f
                );
                anim.setFillAfter(true);
                anim.setDuration(300L);
                arrow.startAnimation(anim);
            }

        if(!open) {
            Log.d(OvkApplication.APP_TAG, "Account menu state: Close");
            ValueAnimator animator = ValueAnimator.ofInt(accountMenuTargetHeight, 1);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = account_menu_view.getLayoutParams();
                    layoutParams.height = value;
                    account_menu_view.setLayoutParams(layoutParams);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    arrow.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    arrow.setEnabled(true);
                    account_menu_view.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(300);
            animator.start();
        } else {
            Log.d(OvkApplication.APP_TAG, "Account menu state: Open");
            ValueAnimator animator = ValueAnimator.ofInt(1, accountMenuTargetHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = account_menu_view.getLayoutParams();
                    layoutParams.height = value;
                    account_menu_view.setLayoutParams(layoutParams);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    arrow.setEnabled(false);
                    account_menu_view.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    arrow.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(300);
            animator.start();
        }
    }

    public boolean isVisibleAccountMenu() {
        return findViewById(R.id.account_menu_view).getVisibility() == VISIBLE;
    }

    public void createFriendsList(ArrayList<Friend> friends) {
        friendsCount = friends.size();
        SlidingMenuAdapter adapter = (SlidingMenuAdapter) menuListView.getAdapter();

        if(adapter != null) {
            SlidingMenuObject category = new SlidingMenuObject(
                    SlidingMenuObject.TYPE_CATEGORY,
                    getResources().getString(R.string.friends).toUpperCase()
            );
            menuItems.add(category);

            for(int i = 0; i < friends.size(); i++) {
                Friend friend = friends.get(i);
                String name = friend.last_name != null ?
                        String.format("%s %s", friend.first_name, friend.last_name) :
                        friend.first_name;

                SlidingMenuObject friendObj = new SlidingMenuObject(
                        SlidingMenuObject.TYPE_PUBLIC_PAGE,
                        name, friend
                );
                menuItems.add(friendObj);
            }

            adapter.notifyDataSetChanged();
        }
    }

    public void createGroupsList(ArrayList<Group> groups) {
        if(groups.size() == 0) {
            updateAdapter();
            return;
        }

        SlidingMenuAdapter adapter = (SlidingMenuAdapter) menuListView.getAdapter();

        if(adapter == null)
            return;

        SlidingMenuObject item = menuItems.get(menuItems.size() - 1);

        if((item.embed != null && item.embed instanceof Friend) || friendsCount == 0) {
            SlidingMenuObject category = new SlidingMenuObject(
                    SlidingMenuObject.TYPE_CATEGORY,
                    getResources().getString(R.string.groups).toUpperCase()
            );
            menuItems.add(category);

            for(int i = 0; i < groups.size(); i++) {
                Group group = groups.get(i);
                String name = group.name;

                SlidingMenuObject groupObj = new SlidingMenuObject(
                        SlidingMenuObject.TYPE_PUBLIC_PAGE,
                        name, group
                );
                menuItems.add(groupObj);
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void setMenuItems(ArrayList<SlidingMenuObject> menuItems) {
        this.menuItems = menuItems;
    }

    public void updateAdapter() {
        RecyclerView.Adapter adapter = menuListView.getAdapter();
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }
}
