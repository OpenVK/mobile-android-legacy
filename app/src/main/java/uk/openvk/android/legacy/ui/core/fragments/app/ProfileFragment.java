package uk.openvk.android.legacy.ui.core.fragments.app;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.Arrays;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.core.listeners.OnNestedScrollListener;
import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;
import uk.openvk.android.legacy.ui.view.InfinityNestedScrollView;
import uk.openvk.android.legacy.ui.view.InfinityScrollView;
import uk.openvk.android.legacy.ui.view.layouts.AboutProfileLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileHeader;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;

import static android.view.View.GONE;

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

public class ProfileFragment extends Fragment {
    private View view;
    private boolean showExtended;
    public boolean loading_more_posts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ProfileWallSelector selector = view.findViewById(R.id.wall_selector);
        (selector.findViewById(R.id.profile_wall_post_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) getActivity()).openNewPostActivity();
                }
            }
        });
        ((WallLayout) view.findViewById(R.id.wall_layout)).adjustLayoutSize(getResources()
                .getConfiguration().orientation);
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
                    if(((OvkApplication)getContext().getApplicationContext()).isTablet &&
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
        if(!((OvkApplication)getContext().getApplicationContext()).isTablet) {
            ((Button) view.findViewById(R.id.send_direct_msg)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).getConversationById(peer_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).getConversationById(peer_id);
                    }
                }
            });
        } else if(((OvkApplication)getContext().getApplicationContext()).isTablet && smallestWidth < 800) {
            ((Button) view.findViewById(R.id.send_direct_msg)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).getConversationById(peer_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).getConversationById(peer_id);
                    }
                }
            });
        } else {
            ((ImageButton) view.findViewById(R.id.send_direct_msg)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).getConversationById(peer_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).getConversationById(peer_id);
                    }
                }
            });
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
                if (user.friends_status == 0 || user.friends_status == 2) {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).addToFriends(user_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).addToFriends(user_id);
                    }
                } else {
                    if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).deleteFromFriends(user_id);
                    } else if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).deleteFromFriends(user_id);
                    }
                }
            }
        });
    }

    public void loadAvatar(User user, String quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(
                    String.format("%s/photos_cache/profile_avatars/avatar_%s",
                            getContext().getCacheDir(), user.id), options);
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
        } catch (OutOfMemoryError ex) {
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
            ((Button) view.findViewById(R.id.send_direct_msg)).setVisibility(GONE);
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
                                      final boolean infinity_scroll) {
        loading_more_posts = false;
        if(load_photos) {
            ((WallLayout) view.findViewById(R.id.wall_layout)).loadPhotos();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final InfinityNestedScrollView scrollView = view.findViewById(R.id.scrollView);
            scrollView.setOnScrollListener(new OnNestedScrollListener() {
                @Override
                public void onScroll(InfinityNestedScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if (ctx instanceof AppActivity) {
                                loading_more_posts = true;
                                ((AppActivity) ctx).loadMoreWallPosts();
                            } else if (ctx instanceof ProfileIntentActivity) {
                                ((ProfileIntentActivity) ctx).loadMoreWallPosts();
                            } else if (ctx instanceof GroupIntentActivity) {
                                ((GroupIntentActivity) ctx).loadMoreWallPosts();
                            }
                        }
                    }
                }
            });
        } else {
            final InfinityScrollView scrollView = view.findViewById(R.id.scrollView);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if (ctx instanceof AppActivity) {
                                loading_more_posts = true;
                                ((AppActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof ProfileIntentActivity) {
                                ((ProfileIntentActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof GroupIntentActivity) {
                                ((GroupIntentActivity) ctx).loadMoreWallPosts();
                            }
                        }
                    }
                }
            });
        }
    }
}
