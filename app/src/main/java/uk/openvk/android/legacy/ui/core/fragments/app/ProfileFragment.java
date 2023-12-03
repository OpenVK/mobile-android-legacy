package uk.openvk.android.legacy.ui.core.fragments.app;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.ConversationActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;
import uk.openvk.android.legacy.ui.view.InfinityScrollView;
import uk.openvk.android.legacy.ui.view.layouts.AboutProfileLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileHeader;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.WallErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;

import static android.view.View.GONE;

/*  Copyleft © 2022, 2023 OpenVK Team
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

public class ProfileFragment extends Fragment {
    private View view;
    private boolean showExtended;
    public boolean loading_more_posts;
    private String instance;
    private SharedPreferences global_prefs;
    private WallLayout wallLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ProfileWallSelector selector = view.findViewById(R.id.wall_selector);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        (selector.findViewById(R.id.profile_wall_post_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenVKAPI ovk_api = null;
                if(getActivity() instanceof AppActivity) {
                    ovk_api = ((AppActivity) getActivity()).ovk_api;
                } else {
                    return;
                }
                Global.openNewPostActivity(getActivity(), ovk_api);
            }
        });
        ((WallLayout) view.findViewById(R.id.wall_layout)).adjustLayoutSize(getResources()
                .getConfiguration().orientation);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            view.findViewById(R.id.about_profile_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            view.findViewById(R.id.send_direct_msg)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_gray));
            view.findViewById(R.id.add_to_friends)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_gray));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            view.findViewById(R.id.about_profile_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            view.findViewById(R.id.send_direct_msg)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_black));
            view.findViewById(R.id.add_to_friends)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_black));
        }
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        wallLayout = (view.findViewById(R.id.wall_layout));
        return view;
    }

    public void updateLayout(User user, final WindowManager wm) {
        ProfileHeader header = (ProfileHeader) view.findViewById(R.id.profile_header);
        header.setProfileName(String.format("%s %s  ", user.first_name, user.last_name));
        header.setOnline(user.online);
        header.setStatus(user.status);
        header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
        header.setVerified(user.verified, getContext());
        ((ProfileCounterLayout) view.findViewById(R.id.photos_counter)).setCounter(0,
                Global.getPluralQuantityString(getContext().getApplicationContext(),
                        R.plurals.profile_photos, 0), "");
        ((ProfileCounterLayout) view.findViewById(R.id.photos_counter)).setOnCounterClickListener();
        ((ProfileCounterLayout) view.findViewById(R.id.friends_counter)).setCounter(0,
                Global.getPluralQuantityString(getContext().getApplicationContext(),
                        R.plurals.profile_friends, 0),
                "openvk://friends/id" + user.id);
        ((ProfileCounterLayout) view.findViewById(R.id.friends_counter)).setOnCounterClickListener();
        ((ProfileCounterLayout) view.findViewById(R.id.mutual_counter)).setCounter(0,
                Global.getPluralQuantityString(getContext().getApplicationContext(),
                        R.plurals.profile_mutual_friends, 0), "");
        ((ProfileCounterLayout) view.findViewById(R.id.mutual_counter)).setOnCounterClickListener();
        ((LinearLayout) view.findViewById(R.id.wall_error_layout)).setVisibility(GONE);
        if(user.deactivated == null) {
            ((AboutProfileLayout) view.findViewById(R.id.about_profile_layout)).setBirthdate("");
            ((AboutProfileLayout) view.findViewById(R.id.about_profile_layout)).setStatus(user.status);
            ((AboutProfileLayout) view.findViewById(R.id.about_profile_layout)).setInterests(
                    user.interests, user.music, user.movies, user.tv, user.books);
            ((AboutProfileLayout) view.findViewById(R.id.about_profile_layout)).setContacts(user.city);
            header.findViewById(R.id.profile_head_highlight).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleExtendedInfo();
                    DisplayMetrics metrics = new DisplayMetrics();
                    Display display = wm.getDefaultDisplay();
                    display.getMetrics(metrics);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                            ((OvkApplication)getContext().getApplicationContext()).isTablet &&
                            getContext().getResources().getConfiguration().smallestScreenWidthDp >= 800) {
                        View aboutProfile = ProfileFragment.this.view.findViewById(R.id.about_profile_ll);
                        if (aboutProfile.getVisibility() == GONE) {
                            aboutProfile.setVisibility(View.VISIBLE);
                        } else {
                            aboutProfile.setVisibility(GONE);
                        }
                    } else {
                        View aboutProfile = ProfileFragment.this.view.findViewById(R.id.about_profile_layout);
                        if (aboutProfile.getVisibility() == GONE) {
                            aboutProfile.setVisibility(View.VISIBLE);
                        } else {
                            aboutProfile.setVisibility(GONE);
                        }
                    }
                }
            });
            ((ProfileWallSelector) view.findViewById(R.id.wall_selector)).setUserName(user.first_name);
        } else {
            view.findViewById(R.id.profile_counters).setVisibility(GONE);
            (view.findViewById(R.id.deactivated_info)).setVisibility(View.VISIBLE);
        }
    }

    public void toggleExtendedInfo() {
        this.showExtended = !this.showExtended;
        View arrow = getHeader().findViewById(R.id.profile_expand);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            float[] fArr = new float[2];
            fArr[0] = this.showExtended ? 0 : -180;
            fArr[1] = this.showExtended ? -180 : 0;
            ObjectAnimator.ofFloat(arrow, "rotation", fArr).setDuration(300L).start();
        } else {
            RotateAnimation anim = new RotateAnimation(this.showExtended ? 0 : -180,
                    this.showExtended ? -180 : 0, 1, 0.5f, 1, 0.5f);
            anim.setFillAfter(true);
            anim.setDuration(300L);
            arrow.startAnimation(anim);
        }
    }

    public void setDMButtonListener(final Context ctx, final long peer_id, WindowManager wm) {
        float smallestWidth = Global.getSmalledWidth(wm);
        (view.findViewById(R.id.send_direct_msg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctx instanceof NetworkFragmentActivity) {
                    OpenVKAPI ovk_api = ((NetworkFragmentActivity) ctx).ovk_api;
                    getConversationById(peer_id, ovk_api);
                }
            }
        });
    }

    public void openNewPostActivity(User user, OpenVKAPI ovk_api) {
        try {
            Intent intent = new Intent(getContext().getApplicationContext(), NewPostActivity.class);
            intent.putExtra("owner_id", user.id);
            intent.putExtra("account_id", ovk_api.account.id);
            intent.putExtra("account_first_name", user.first_name);
            startActivity(intent);
        } catch (Exception ignored) {

        }
    }

    public void getConversationById(long peer_id, OpenVKAPI ovk_api) {
        Intent intent = new Intent(getContext().getApplicationContext(), ConversationActivity.class);
        try {
            intent.putExtra("peer_id", peer_id);
            intent.putExtra("conv_title",
                    String.format("%s %s", ovk_api.user.first_name, ovk_api.user.last_name));
            intent.putExtra("online", ovk_api.user.online ? 0 : 1);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setAddToFriendsButtonListener(final Context ctx, final long user_id, final User user) {
        TextView friend_status = ((TextView) view.findViewById(R.id.friend_status));
        int dp = (int) getResources().getDisplayMetrics().scaledDensity;
        ImageButton add_to_friends_btn = ((ImageButton) view.findViewById(R.id.add_to_friends));
        ((ViewGroup.MarginLayoutParams) add_to_friends_btn.getLayoutParams()).leftMargin = 8 * dp;
        ((ViewGroup.MarginLayoutParams) add_to_friends_btn.getLayoutParams()).rightMargin = 0;
        if(user.friends_status == 0) {
            friend_status.setVisibility(GONE);
            LinearLayout.LayoutParams layoutParams =
                    ((LinearLayout.LayoutParams) view.findViewById(R.id.send_direct_msg)
                            .getLayoutParams());
            view.findViewById(R.id.send_direct_msg).setLayoutParams(layoutParams);
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
        } else if(user.friends_status == 1) {
            friend_status.setText(getResources().getString(R.string.friend_status_req_sent, user.first_name));
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
        } else if(user.friends_status == 2) {
            if(user.sex == 1) {
                friend_status.setText(getResources().getString(R.string.friend_status_req_recv_f, user.first_name));
            } else {
                friend_status.setText(getResources().getString(R.string.friend_status_req_recv_m, user.first_name));
            }
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
        } else if(user.friends_status == 3){
            friend_status.setText(getResources().getString(R.string.friend_status_friend, user.first_name));
            add_to_friends_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
        }
        add_to_friends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenVKAPI ovk_api = null;
                if (ctx instanceof AppActivity) {
                    ovk_api = ((AppActivity) ctx).ovk_api;
                } else if (ctx instanceof ProfileIntentActivity) {
                    ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
                } else {
                    return;
                }
                if (user.friends_status == 0 || user.friends_status == 2) {
                    Global.addToFriends(ovk_api, user_id);
                } else {
                    Global.deleteFromFriends(ovk_api, user_id);
                }
            }
        });
    }

    public void loadAvatar(User user, String quality) {
        try {
            if (getContext() != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(
                        String.format("%s/%s/photos_cache/profile_avatars/avatar_%s",
                                getContext().getCacheDir(), instance, user.id), options);
                switch (quality) {
                    case "medium":
                        if (bitmap != null) {
                            user.avatar = bitmap;
                        } else if (user.avatar_msize_url.length() > 0) {
                            user.avatar = null;
                        } else {
                            user.avatar = null;
                        }
                        break;
                    case "high":
                        if (bitmap != null) {
                            user.avatar = bitmap;
                        } else if (user.avatar_hsize_url.length() > 0) {
                            user.avatar = null;
                        } else {
                            user.avatar = null;
                        }
                        break;
                    default:
                        if (bitmap != null) {
                            user.avatar = bitmap;
                        } else if (user.avatar_osize_url.length() > 0) {
                            user.avatar = null;
                        } else {
                            user.avatar = null;
                        }
                        break;
                }
                if (user.avatar != null)
                    ((ImageView) view.findViewById(R.id.profile_photo)).setImageBitmap(user.avatar);
                getHeader().createProfilePhotoViewer(user.id, user.avatar_url);
            }
        } catch(OutOfMemoryError ex){
            ex.printStackTrace();
        }
    }

    public void setCounter(User user, String where, int count) {
        if(where.equals("friends")) {
            ((ProfileCounterLayout) view.findViewById(R.id.friends_counter)).setCounter(count,
                    Global.getPluralQuantityString(getContext().getApplicationContext(),
                            R.plurals.profile_friends, count),
                    "openvk://friends/id" + user.id);
        }
    }

    public void hideHeaderButtons(Context ctx, WindowManager wm) {
        float smallestWidth = Global.getSmalledWidth(wm);
        if(!((OvkApplication)getContext().getApplicationContext()).isTablet) {
            ((Button) view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
        } else if(((OvkApplication)getContext().getApplicationContext()).isTablet &&
                smallestWidth < 800) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ((ImageButton) view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
            } else {
                ((Button) view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
            }
        } else {
            ((ImageButton) view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
        }
        ((ImageButton) view.findViewById(R.id.add_to_friends)).setVisibility(GONE);
    }

    public void hideTabSelector() {
        ((ProfileWallSelector) view.findViewById(R.id.wall_selector)).setVisibility(GONE);
    }

    public ProfileHeader getHeader() {
        return view.findViewById(R.id.profile_header);
    }

    public void refreshWallAdapter() {
        ((WallLayout) view.findViewById(R.id.wall_layout)).refreshAdapter();
    }

    public void setScrollingPositions(final Context ctx, final boolean load_photos,
                                      final boolean infinity_scroll, final long owner_id) {
        loading_more_posts = false;
        if(load_photos) {
            ((WallLayout) view.findViewById(R.id.wall_layout)).loadPhotos();
        }
        final InfinityScrollView scrollView = view.findViewById(R.id.scrollView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    OpenVKAPI ovk_api = null;
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if(ctx instanceof NetworkFragmentActivity){
                                ovk_api = ((NetworkFragmentActivity) ctx).ovk_api;
                                Global.loadMoreWallPosts(ovk_api, owner_id);
                            } else if(ctx instanceof NetworkActivity) {
                                ovk_api = ((NetworkActivity) ctx).ovk_api;
                                Global.loadMoreWallPosts(ovk_api, owner_id);
                            }
                        }
                    }
                }
            });
        } else {
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    OpenVKAPI ovk_api = null;
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if(ctx instanceof NetworkFragmentActivity){
                                ovk_api = ((NetworkFragmentActivity) ctx).ovk_api;
                                Global.loadMoreWallPosts(ovk_api, owner_id);
                            }
                        }
                    }
                }
            });
        }
    }

    public ProfileWallSelector getWallSelector() {
        return view.findViewById(R.id.wall_selector);
    }

    public void loadAPIData(Context ctx, OpenVKAPI ovk_api, WindowManager wm) {
        getWallSelector().setUserName(ovk_api.account.first_name);
        updateLayout(ovk_api.user, wm);
        setDMButtonListener(ctx, ovk_api.user.id, wm);
        setAddToFriendsButtonListener(ctx, ovk_api.user.id, ovk_api.user);
        if(ovk_api.user.id == ovk_api.account.id) {
            hideHeaderButtons(ctx, wm);
        }
        if(ovk_api.user.deactivated == null) {
            ovk_api.user.downloadAvatar(ovk_api.dlman, global_prefs.getString("photos_quality", ""));
            ovk_api.wall.get(ovk_api.wrapper, ovk_api.user.id, 25);
            ovk_api.friends.get(ovk_api.wrapper, ovk_api.user.id, 10, "profile_counter");

        } else {
            hideTabSelector();
            getHeader().hideExpandArrow();
            if(ovk_api.user.deactivated.equals("banned")) {
                if(ovk_api.user.ban_reason.length() > 0) {
                    ((TextView) view.findViewById(R.id.deactivated_info)).setText(
                            String.format("%s\r\n%s: %s",
                                    getResources().getString(R.string.profile_inactive_banned),
                                    getResources().getString(R.string.reason), ovk_api.user.ban_reason
                            )
                    );
                } else {
                    ((TextView) view.findViewById(R.id.deactivated_info)).setText(
                            getResources().getString(R.string.profile_inactive_banned)
                    );
                }
            }
        }
        ovk_api.user.downloadAvatar(ovk_api.dlman, global_prefs.getString("photos_quality", ""));
        ovk_api.wall.get(ovk_api.wrapper, ovk_api.user.id, 25);
        ovk_api.friends.get(ovk_api.wrapper, ovk_api.user.id, 25, "profile_counter");
    }

    public void loadWall(final Context ctx, final OpenVKAPI ovk_api) {
        if(ovk_api.wall.getWallItems().size() > 0) {
            wallLayout.createAdapter(ctx, ovk_api.wall.getWallItems());
            loading_more_posts = true;
            setScrollingPositions(
                    ctx, false, true, ovk_api.account.id
            );
        } else {
            WallErrorLayout wall_error = view.findViewById(R.id.wall_error_layout);
            wall_error.setErrorText(getResources().getString(R.string.no_news));
            wall_error.setVisibility(View.VISIBLE);
        }
        ProfileWallSelector selector = view.findViewById(R.id.wall_selector);
        selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Global.openNewPostActivity(ctx, ovk_api);
            }
        });
        selector.showNewPostIcon();
    }
}
