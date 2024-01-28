package uk.openvk.android.legacy.core.fragments.pages;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.core.activities.GroupMembersActivity;
import uk.openvk.android.legacy.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiviableFragment;
import uk.openvk.android.legacy.core.listeners.OnScrollListener;
import uk.openvk.android.legacy.databases.WallCacheDB;
import uk.openvk.android.legacy.ui.views.AboutGroupLayout;
import uk.openvk.android.legacy.ui.views.GroupHeader;
import uk.openvk.android.legacy.ui.views.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.views.ProfileWallSelector;
import uk.openvk.android.legacy.ui.views.WallErrorLayout;
import uk.openvk.android.legacy.ui.views.WallLayout;
import uk.openvk.android.legacy.ui.views.base.InfinityScrollView;

public class GroupPageFragment extends ActiviableFragment {

    public View view;
    public boolean loading_more_posts;
    private String instance;
    private boolean showExtended;
    public WallLayout wallLayout;
    private boolean loadedFromCache;
    private Group group;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        OvkApplication app = ((OvkApplication) getContext().getApplicationContext());
        SharedPreferences instance_prefs = app.getAccountPreferences();
        instance = instance_prefs.getString("server", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group_page, container, false);
        wallLayout = view.findViewById(R.id.wall_layout);
        return view;
    }

    public void loadWall(final Context ctx, Group group, final OpenVKAPI ovk_api) {
        if(ovk_api.wall.getWallItems().size() > 0) {
            wallLayout.createAdapter(ctx, ovk_api.wall.getWallItems());
            loading_more_posts = true;
            setScrollingPositions(ctx, ovk_api,false, -group.id);
            WallCacheDB.putPosts(ctx, ovk_api.wall.getWallItems(), -group.id, true);
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

    public void loadWallFromCache(final Context ctx, final OpenVKAPI ovk_api, Group group) {
        long owner_id = -group.id;
        ArrayList<WallPost> posts = WallCacheDB.getPostsList(ctx, owner_id);
        if(posts != null && !loadedFromCache) {
            if (posts.size() > 0) {
                loadedFromCache = true;
                wallLayout.createAdapter(ctx, posts);
                loading_more_posts = true;
                setScrollingPositions(ctx, ovk_api, false, owner_id);
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

    public void setJoinButtonListener(final Group group, final OpenVKAPI ovk_api) {
        float smallestWidth = Global.getSmalledWidth(getActivity().getWindowManager());
        if(((OvkApplication)getContext().getApplicationContext()).isTablet && smallestWidth >= 800) {
            final Button join_btn = (view.findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setText(R.string.leave_group);
            } else {
                join_btn.setText(R.string.join_group);
            }
            join_btn.setVisibility(View.VISIBLE);
        } else if(((OvkApplication)getContext().getApplicationContext()).isTablet &&
                smallestWidth < 800) {
            final Button join_btn = (view.findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setText(R.string.leave_group);
            } else {
                join_btn.setText(R.string.join_group);
            }
            join_btn.setVisibility(View.VISIBLE);
        } else {
            final Button join_btn = (view.findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setText(R.string.leave_group);
            } else {
                join_btn.setText(R.string.join_group);
            }
            join_btn.setVisibility(View.VISIBLE);
        }
    }

    public void loadAPIData(final Group group) {
        this.group = group;
        GroupHeader header = getHeader();
        header.setProfileName(String.format("%s  ", group.name));
        header.setVerified(group.verified, getContext());
        ((ProfileCounterLayout) view.findViewById(R.id.members_counter)).setCounter(group.members_count,
                Global.getPluralQuantityString(getContext().getApplicationContext(),
                        R.plurals.profile_members, (int) group.members_count), "");
        ((ProfileCounterLayout) view.findViewById(R.id.members_counter)).setOnCounterClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), GroupMembersActivity.class);
                        i.putExtra("group_id", group.id);
                        startActivity(i);
                    }
                });
        ((AboutGroupLayout) view.findViewById(R.id.about_group_layout))
                .setGroupInfo(group.description, group.site);
        header.findViewById(R.id.profile_head_highlight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float smallestWidth = Global.getSmalledWidth(getActivity().getWindowManager());
                toggleExtendedInfo();
                View aboutGroup = GroupPageFragment.this.view.findViewById(R.id.about_group_layout);
                if (aboutGroup.getVisibility() == View.GONE) {
                    aboutGroup.setVisibility(View.VISIBLE);
                } else {
                    aboutGroup.setVisibility(View.GONE);
                }
            }
        });
        header.findViewById(R.id.profile_activity).getLayoutParams().height =
                (int) (10 * getResources().getDisplayMetrics().scaledDensity);
        getWallSelector().setToGroup();
        adjustLayoutSize(getResources().getConfiguration().orientation);
    }

    public GroupHeader getHeader() {
        return view.findViewById(R.id.group_header);
    }

    public ProfileWallSelector getWallSelector() {
        return view.findViewById(R.id.wall_selector);
    }

    public void refreshWallAdapter() {
        ((WallLayout) view.findViewById(R.id.wall_layout)).refreshAdapter();
    }

    public void setScrollingPositions(final Context ctx, final OpenVKAPI ovk_api,
                                      final boolean load_photos, final long owner_id) {
        loading_more_posts = false;
        if(load_photos) {
            ((WallLayout) view.findViewById(R.id.wall_layout)).loadPhotos();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final InfinityScrollView scrollView = view.findViewById(R.id.group_scrollview);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            Global.loadMoreWallPosts(ovk_api, owner_id);
                        }
                    }
                }
            });
        } else {
            final InfinityScrollView scrollView = view.findViewById(R.id.group_scrollview);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            Global.loadMoreWallPosts(ovk_api, owner_id);
                        }
                    }
                }
            });
        }
    }

    public void loadAvatar(Group group) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(
                String.format("%s/%s/photos_cache/group_avatars/avatar_%s",
                        getContext().getCacheDir(), instance, group.id), options);
        if (bitmap != null) {
            group.avatar = bitmap;
        } else if(group.avatar_msize_url.length() > 0 || group.avatar_hsize_url.length() > 0
                || group.avatar_osize_url.length() > 0) {
            group.avatar = null;
        } else {
            group.avatar = null;
        }
        if(group.avatar != null) {
            if(((OvkApplication) getContext().getApplicationContext()).isTablet) {
                ((ImageView) view.findViewById(R.id.group_photo)).setImageBitmap(group.avatar);
            } else {
                ((ImageView) view.findViewById(R.id.profile_photo)).setImageBitmap(group.avatar);
                getHeader().createGroupPhotoViewer(group.id, group.avatar_url);
            }
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

    public void refreshOptionsMenu() {
        if(group.is_member > 0) {
            view.findViewById(R.id.join_to_comm).setVisibility(View.GONE);
//            if(activity_menu != null) {
//                activity_menu.findItem(R.id.leave_group).setTitle(R.string.leave_group);
//            }
        } else {
            view.findViewById(R.id.join_to_comm).setVisibility(View.VISIBLE);
//            if(activity_menu != null) {
//                activity_menu.findItem(R.id.leave_group).setTitle(R.string.join_group);
//            }
        }
//        if(activity_menu != null) {
//            for (int i = 0; i < activity_menu.size(); i++) {
//                activity_menu.getItem(i).setVisible(true);
//            }
//        }
    }

    public void adjustLayoutSize(int orientation) {
        int dp = (int) getResources().getDisplayMetrics().scaledDensity;
        if(((OvkApplication) getContext().getApplicationContext()).isTablet) {
            View placeholder = view.findViewById(R.id.tablet_group_placeholder);
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
}
