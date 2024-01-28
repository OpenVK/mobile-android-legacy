package uk.openvk.android.legacy.core.fragments.pages;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import java.util.ArrayList;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.ConversationActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.core.fragments.base.ActiviableFragment;
import uk.openvk.android.legacy.core.listeners.OnScrollListener;
import uk.openvk.android.legacy.databases.WallCacheDB;
import uk.openvk.android.legacy.ui.views.OvkRefreshableHeaderLayout;
import uk.openvk.android.legacy.ui.views.base.InfinityScrollView;
import uk.openvk.android.legacy.ui.views.AboutProfileLayout;
import uk.openvk.android.legacy.ui.views.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.views.ProfileHeader;
import uk.openvk.android.legacy.ui.views.ProfileWallSelector;
import uk.openvk.android.legacy.ui.views.WallErrorLayout;
import uk.openvk.android.legacy.ui.views.WallLayout;

import static android.view.View.GONE;

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

public class ProfilePageFragment extends ActiviableFragment {
    private View view;
    private boolean showExtended;
    public boolean loading_more_posts;
    private String instance;
    private SharedPreferences global_prefs;
    public WallLayout wallLayout;
    public boolean loadedFromCache;
    private Menu fragment_menu;
    private User user;
    private android.support.v7.widget.PopupMenu popup_menu;
    private OpenVKAPI ovk_api;
    private boolean isActivated;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_page, container, false);
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
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            view.findViewById(R.id.about_profile_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            CustomSwipeRefreshLayout p2r_view = view.findViewById(R.id.refreshable_layout);
            p2r_view.setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            view.findViewById(R.id.send_direct_msg)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_gray));
            view.findViewById(R.id.add_to_friends)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_gray));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            view.findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            view.findViewById(R.id.about_profile_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            CustomSwipeRefreshLayout p2r_view = view.findViewById(R.id.refreshable_layout);
            p2r_view.setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            view.findViewById(R.id.send_direct_msg)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_black));
            view.findViewById(R.id.add_to_friends)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_black));
        }
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        wallLayout = (view.findViewById(R.id.wall_layout));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile, menu);
        fragment_menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu != null && menu.size() > 0) {
            if (getActivity() instanceof NetworkFragmentActivity) {
                NetworkFragmentActivity activity = ((NetworkFragmentActivity) getActivity());
                if(user != null) {
                    if (user.id == activity.ovk_api.account.id || user.deactivated != null) {
                        menu.findItem(R.id.remove_friend).setVisible(false);
                    } else {
                        if (user.friends_status == 0 || user.friends_status == 2)
                            menu.findItem(R.id.remove_friend).setTitle(
                                    getResources().getString(R.string.profile_add_friend)
                            );
                    }
                } else {
                    menu.clear();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_friend:
                if(ovk_api.account.id != user.id)
                    if(user.friends_status == 3 || user.friends_status == 1)
                        Global.deleteFromFriends(ovk_api, user.id);
                    else
                        Global.addToFriends(ovk_api, user.id);
                return false;
            case R.id.copy_link:
                Global.copyToClipboard(getContext(), String.format("http://%s/id%s", instance, user.id));
                return false;
            case R.id.open_in_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(String.format("http://%s/id%s", instance, user.id)));
                startActivity(intent);
                return false;
            default:
                break;
        }

        return false;
    }

    public void updateLayout(OpenVKAPI ovk_api, final WindowManager wm) {
        this.ovk_api = ovk_api;
        this.user = ovk_api.user;
        isActivated = !(getActivity() instanceof AppActivity) ||
                ((AppActivity) getActivity()).selectedFragment instanceof ProfilePageFragment;
        if (isActivated) refreshOptionsMenu();
        ProfileHeader header = view.findViewById(R.id.profile_header);
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
                "openvk://ovk/friends" + user.id);
        ((ProfileCounterLayout) view.findViewById(R.id.friends_counter)).setOnCounterClickListener();
        ((ProfileCounterLayout) view.findViewById(R.id.mutual_counter)).setCounter(0,
                Global.getPluralQuantityString(getContext().getApplicationContext(),
                        R.plurals.profile_mutual_friends, 0), "");
        ((ProfileCounterLayout) view.findViewById(R.id.mutual_counter)).setOnCounterClickListener();
        (view.findViewById(R.id.wall_error_layout)).setVisibility(GONE);
        if (user.deactivated == null) {
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
                    View aboutProfile = ProfilePageFragment.this.view.findViewById(R.id.about_profile_layout);
                    if (aboutProfile.getVisibility() == GONE) {
                        aboutProfile.setVisibility(View.VISIBLE);
                    } else {
                        aboutProfile.setVisibility(GONE);
                    }
                }
            });
            ((ProfileWallSelector) view.findViewById(R.id.wall_selector)).setUserName(user.first_name);
        } else {
            view.findViewById(R.id.profile_counters).setVisibility(GONE);
            header.findViewById(R.id.profile_activity).setVisibility(GONE);
            (view.findViewById(R.id.deactivated_info)).setVisibility(View.VISIBLE);
            view.findViewById(R.id.send_direct_msg).setVisibility(GONE);
        }
        adjustLayoutSize(getContext().getResources().getConfiguration().orientation);
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
                    "openvk://ovk/friends" + user.id);
        }
    }

    public void hideHeaderButtons(Context ctx, WindowManager wm) {
        float smallestWidth = Global.getSmalledWidth(wm);
        (view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
        (view.findViewById(R.id.add_to_friends)).setVisibility(GONE);
    }

    public void hideTabSelector() {
        ((ProfileWallSelector) view.findViewById(R.id.wall_selector)).setVisibility(GONE);
    }

    public ProfileHeader getHeader() {
        return view.findViewById(R.id.profile_header);
    }

    public ProfileWallSelector getWallSelector() {
        return view.findViewById(R.id.wall_selector);
    }

    public void refreshWallAdapter() {
        ((WallLayout) view.findViewById(R.id.wall_layout)).refreshAdapter();
    }

    public void setScrollingPositions(final Context ctx, final boolean load_photos) {
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
                                Global.loadMoreWallPosts(ovk_api, ovk_api.user.id);
                            } else if(ctx instanceof NetworkActivity) {
                                ovk_api = ((NetworkActivity) ctx).ovk_api;
                                Global.loadMoreWallPosts(ovk_api, ovk_api.user.id);
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
                                Global.loadMoreWallPosts(ovk_api, ovk_api.user.id);
                            } else if(ctx instanceof NetworkActivity) {
                                ovk_api = ((NetworkActivity) ctx).ovk_api;
                                Global.loadMoreWallPosts(ovk_api, ovk_api.user.id);
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadAPIData(Context ctx, final OpenVKAPI ovk_api, WindowManager wm) {
        CustomSwipeRefreshLayout p2r_view = view.findViewById(R.id.refreshable_layout);
        p2r_view.refreshComplete();
        OvkRefreshableHeaderLayout rhl = new OvkRefreshableHeaderLayout(getContext());
        rhl.enableDarkTheme();
        try {
            p2r_view.setCustomHeadview(rhl);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        p2r_view.setTriggerDistance(80);
        p2r_view.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ArrayList<Long> ids = new ArrayList<>();
                ids.add(ovk_api.user.id);
                ovk_api.users.get(ovk_api.wrapper, ids);
            }
        });
        getWallSelector().setUserName(ovk_api.account.first_name);
        updateLayout(ovk_api, wm);
        setDMButtonListener(ctx, ovk_api.user.id, wm);
        setAddToFriendsButtonListener(ctx, ovk_api.user.id, ovk_api.user);
        if(ovk_api.user.id == ovk_api.account.id) {
            hideHeaderButtons(ctx, wm);
        }
        if(ovk_api.user.deactivated == null) {
            ovk_api.user.downloadAvatar(ovk_api.dlman, global_prefs.getString("photos_quality", ""));
            loadWallFromCache(ctx, ovk_api, ovk_api.user.id);
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
    }

    public void loadWall(final Context ctx, final OpenVKAPI ovk_api) {
        if(ovk_api.wall.getWallItems().size() > 0) {
            wallLayout.createAdapter(ctx, ovk_api.wall.getWallItems());
            loading_more_posts = true;
            setScrollingPositions(
                    ctx, false
            );
            WallCacheDB.putPosts(ctx, ovk_api.wall.getWallItems(), ovk_api.user.id, true);
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

    public void loadWallFromCache(final Context ctx, final OpenVKAPI ovk_api, long owner_id) {
        ArrayList<WallPost> posts = WallCacheDB.getPostsList(ctx, owner_id);
        if(posts != null && !loadedFromCache) {
            if (posts.size() > 0) {
                loadedFromCache = true;
                wallLayout.createAdapter(ctx, posts);
                loading_more_posts = true;
                setScrollingPositions(ctx, false);
            } else {
                ovk_api.wall.get(ovk_api.wrapper, owner_id, 25);
            }
            ProfileWallSelector selector = view.findViewById(R.id.wall_selector);
            selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Global.openNewPostActivity(ctx, ovk_api);
                }
            });
            selector.showNewPostIcon();
        } else {
            ovk_api.wall.get(ovk_api.wrapper, owner_id, 25);
        }
    }

    public void refreshOptionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().invalidateOptionsMenu();
        } else {
            ActionBar actionBar = getActivity().findViewById(R.id.actionbar);
            actionBar.removeAllActions();
            if(popup_menu == null) {
                popup_menu = new android.support.v7.widget.PopupMenu(getContext(), null);
            } else {
                popup_menu.getMenu().clear();
            }
            popup_menu.inflate(R.menu.profile);
            if(popup_menu != null && popup_menu.getMenu().size() > 0) {
                if (getActivity() instanceof NetworkFragmentActivity) {
                    NetworkFragmentActivity activity = ((NetworkFragmentActivity) getActivity());
                    if(user != null) {
                        if (user.id == activity.ovk_api.account.id || user.deactivated != null) {
                            popup_menu.getMenu().findItem(R.id.remove_friend).setVisible(false);
                        } else {
                            if (user.friends_status == 0 || user.friends_status == 2)
                                popup_menu.getMenu().findItem(R.id.remove_friend).setTitle(
                                        getResources().getString(R.string.profile_add_friend)
                                );
                        }
                    }
                }
            }

            dev.tinelix.retro_ab.ActionBar.PopupMenuAction action =
                    new dev.tinelix.retro_ab.ActionBar.PopupMenuAction(
                            getContext(), "", popup_menu.getMenu(),
                            R.drawable.ic_overflow_holo_dark, new PopupMenu.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(dev.tinelix.retro_pm.MenuItem item) {
                                    onOptionsItemSelected(popup_menu.getMenu().findItem(item.getItemId()));
                                }
                        });
            actionBar.addAction(action);
        }
    }

    public void adjustLayoutSize(int orientation) {
        int dp = (int) getResources().getDisplayMetrics().scaledDensity;
        if(((OvkApplication) getContext().getApplicationContext()).isTablet) {
            View placeholder = view.findViewById(R.id.tablet_profile_placeholder);
            InfinityScrollView.LayoutParams lp = ((InfinityScrollView.LayoutParams)
                    placeholder.getLayoutParams());
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                lp.leftMargin = 160 * dp;
                lp.rightMargin = 160 * dp;
            } else {
                lp.leftMargin = 15 * dp;
                lp.rightMargin = 15 * dp;
            }
            placeholder.setLayoutParams(lp);
        } else {
            wallLayout.adjustLayoutSize(orientation);
        }
    }

    @Override
    public void onDestroy() {
        if(wallLayout.isActivatedAP)
            wallLayout.closeAudioPlayer();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
