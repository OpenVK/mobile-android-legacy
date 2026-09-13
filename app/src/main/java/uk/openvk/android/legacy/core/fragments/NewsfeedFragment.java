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

package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.InfinityRecyclerViewScrollListener;
import uk.openvk.android.legacy.databases.NewsfeedCacheDB;
import uk.openvk.android.legacy.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.OvkRefreshableHeaderLayout;

public class NewsfeedFragment extends ActiveFragment {
    public String state;
    public JSONArray newsfeed;
    public SharedPreferences global_prefs;
    private NewsfeedAdapter newsfeedAdapter;
    private RecyclerView newsfeedView;
    private LinearLayoutManager llm;
    private ArrayList<WallPost> wallPosts;
    public boolean loading_more_posts = false;
    private View view;
    private String instance;
    private Menu fragment_menu;
    private Account account;
    public boolean autoLoad;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        onPrepareOptionsMenu(fragment_menu);
        view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
        adjustLayout(getContext().getResources().getConfiguration().orientation);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        if(autoLoad) {
            if(loadFromCache(getActivity()) && getActivity() instanceof AppActivity) {
                ((AppActivity) getActivity()).errorLayout.setVisibility(View.GONE);
                ((AppActivity) getActivity()).progressLayout.setVisibility(View.GONE);
            }
        }

        OvkRefreshableHeaderLayout refreshHeader = new OvkRefreshableHeaderLayout(getContext());
        refreshHeader.setBackgroundColor(Color.parseColor("#e3e4e6"));

        CustomSwipeRefreshLayout p2r_news_view = view.findViewById(R.id.refreshable_layout);
        p2r_news_view.setCustomHeadview(refreshHeader);
        p2r_news_view.setTriggerDistance(80);
        p2r_news_view.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getActivity() instanceof AppActivity) {
                    if (((AppActivity) getActivity()).ab_layout.getNewsfeedSelection() == 0) {
                        ((AppActivity) getActivity()).refreshPage("subscriptions_newsfeed");
                    } else {
                        ((AppActivity) getActivity()).refreshPage("global_newsfeed");
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if(menu != null && menu.size() > 0)
            menu.clear();

        inflater.inflate(R.menu.newsfeed, menu);
        fragment_menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu != null) {
            if (menu.size() > 0) {
                if(getActivity() instanceof NetworkFragmentActivity)
                    account = ((NetworkFragmentActivity) getActivity()).ovk_api.account;

                if (account == null || account.id == 0) {
                    menu.findItem(R.id.newpost).setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newpost:
                Global.openNewPostActivity(
                        getContext(),
                        ((NetworkFragmentActivity) getActivity()).ovk_api
                );
                return false;
            default:
                break;
        }

        return false;
    }

    public boolean loadFromCache(Context ctx) {
        ArrayList<WallPost> posts = NewsfeedCacheDB.getPostsList(ctx);
        if(posts != null && posts.size() > 0) {
            createAdapter(ctx, posts, false, false);
            return true;
        } else
            Log.e("OpenVK","Empty posts");

        return false;
    }

    public void createAdapter(Context ctx, ArrayList<WallPost> wallPosts, boolean cache, boolean clear) {
        if(this.wallPosts == null || clear)
            this.wallPosts = wallPosts;
        else
            this.wallPosts.addAll(wallPosts);

        if(wallPosts.size() > 0)
            this.wallPosts.add(new WallPost());

        if(view == null)
            return;

        newsfeedView = view.findViewById(R.id.news_listview);
        newsfeedView.setHasFixedSize(true);
        if(newsfeedAdapter == null) {
            newsfeedAdapter = new NewsfeedAdapter(ctx, this.wallPosts, false);
            llm = new WrappedLinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            newsfeedView.setLayoutManager(llm);
            InfinityRecyclerViewScrollListener listener = new InfinityRecyclerViewScrollListener(llm) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if(getActivity() instanceof AppActivity) {
                        ((AppActivity) getActivity()).loadMoreNews();
                    }
                }
            };
            newsfeedView.addOnScrollListener(listener);
            newsfeedView.setAdapter(newsfeedAdapter);
        } else {
            newsfeedAdapter.setArray(this.wallPosts);
            newsfeedAdapter.notifyDataSetChanged();
        }

        if(cache)
            NewsfeedCacheDB.putPosts(ctx, this.wallPosts, clear);

        adjustLayout(((OvkApplication)(getContext().getApplicationContext())).config.orientation);

        final LinearLayoutManager layoutManager = ((LinearLayoutManager) newsfeedView.getLayoutManager());

        CustomSwipeRefreshLayout p2r_news_view = view.findViewById(R.id.refreshable_layout);

        p2r_news_view.setScroolUpHandler(new CustomSwipeRefreshLayout.ScrollUpHandler() {
            // ^ typo detected in SRL library: github.com/xyxyLiu/SwipeRefreshLayout
            @Override
            public boolean canScrollUp(View view) {

                int paddingStart = 0;
                if(layoutManager != null) {
                    View firstChild = layoutManager.getChildAt(0);
                    paddingStart = firstChild.getTop();

                    return view == newsfeedView &&
                            (layoutManager.findFirstVisibleItemPosition() != 0 ||
                            paddingStart != 0);
                }
                return false;
            }
        });
    }

    public void loadAvatars() {
            newsfeedAdapter.notifyDataSetChanged();
    }

    public void loadPhotos() {
        newsfeedView = view.findViewById(R.id.news_listview);
        try {
            newsfeedAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getCount() {
        try {
            return newsfeedView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    public void select(int position, String item, int value) {
        if(item.equals("likes")) {
            wallPosts.get(position).counters.isLiked = value == 1;
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void select(int position, String item, String value) {
        if(item.equals("likes")) {
            wallPosts.get(position).counters.isLiked = value.equals("add");
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void adjustLayout(int orientation) {
        super.adjustLayout(orientation);
        try {
            if(newsfeedView == null && view != null)
                newsfeedView = view.findViewById(R.id.news_listview);

            OvkApplication app = ((OvkApplication) getContext().getApplicationContext());
            if(newsfeedView != null) {
                if (app.isTablet) {
                    int menuWidth = 0;

                    if(getActivity() instanceof AppActivity)
                        menuWidth = (int) ((AppActivity) getActivity()).getSlidingMenuWidth();

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        int sidePadding = (int) ((300 - (menuWidth / 2)) * (getResources().getDisplayMetrics().density));
                        newsfeedView.setPadding(sidePadding, 0, sidePadding, 0);
                    } else {
                        int sidePadding = (int) ((140 - (menuWidth / 2))  * (getResources().getDisplayMetrics().density));
                        newsfeedView.setPadding(sidePadding, 0, sidePadding, 0);
                    }
                } else {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        int sidePadding =
                                (int) ((app.isWidescreen ? 200 : 60) * (getResources().getDisplayMetrics().density));
                        newsfeedView.setPadding(sidePadding, 0, sidePadding, 0);
                    } else {
                        newsfeedView.setPadding(0, 0, 0, 0);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayout(newConfig.orientation);
    }

    public void refreshAdapter() {
        if(newsfeedAdapter != null) {
            newsfeedAdapter.notifyDataSetChanged();
            adjustLayout(((OvkApplication)(getContext().getApplicationContext())).config.orientation);
        }
    }

    public void loadAPIData(Context ctx, OpenVKAPI ovk_api, Spinner ab_spinner,
                            int isGlobalFeed, boolean clear) {
        ((CustomSwipeRefreshLayout) view.findViewById(R.id.refreshable_layout)).refreshComplete();

        if(ab_spinner.getSelectedItemPosition() == isGlobalFeed) {
            if(wallPosts != null && wallPosts.size() > 0) {
                int lastEntity = wallPosts.size() - 1;
                if(wallPosts.get(lastEntity).getEntityType() == LazyEntity.SLEEPING_ENTITY) {
                    wallPosts.remove(lastEntity);
                }
            }

            createAdapter(ctx, ovk_api.newsfeed.getWallPosts(), true, clear);
            adjustLayout(((OvkApplication)(getContext().getApplicationContext())).config.orientation);
            if(ovk_api.newsfeed.getWallPosts().size() > 0)
                return;
            loading_more_posts = true;
            if(clear)
                newsfeedView.scrollToPosition(0);
        }
    }

    public void loadAccount(final OpenVKAPI ovk_api) {
        this.account = ovk_api.account;
        refreshOptionsMenu();
    }

    public void refreshOptionsMenu() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                clearOptionsMenu();
                getActivity().invalidateOptionsMenu();
            } else {
                dev.tinelix.retro_ab.ActionBar actionBar = getActivity().findViewById(R.id.actionbar);
                if (actionBar.getActionCount() > 0) {
                    actionBar.removeAllActions();
                }
                dev.tinelix.retro_ab.ActionBar.Action newpost =
                        new dev.tinelix.retro_ab.ActionBar.Action() {
                            @Override
                            public int getDrawable() {
                                return R.drawable.ic_ab_write;
                            }

                            @Override
                            public void performAction(View view) {
                                if (getActivity() instanceof NetworkFragmentActivity) {
                                    NetworkFragmentActivity activity = ((NetworkFragmentActivity) getActivity());
                                    Global.openNewPostActivity(getContext(), activity.ovk_api);
                                }
                            }
                        };
                actionBar.addAction(newpost);
            }
        } catch (Exception ignored) {

        }
    }

    public void clearOptionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(fragment_menu != null)
                fragment_menu.clear();
        } else {
            dev.tinelix.retro_ab.ActionBar actionBar = getActivity().findViewById(R.id.actionbar);
            if (actionBar.getActionCount() > 0) {
                actionBar.removeAllActions();
            }
        }
    }

    @Override
    public void onActivated() {
        super.onActivated();
        refreshOptionsMenu();
    }

    @Override
    public void onDeactivated() {
        super.onDeactivated();
        clearOptionsMenu();
        if(getActivity() instanceof AppActivity) {
            ((AppActivity) getActivity()).setActionBar("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOptionsMenu();
    }

    @Override
    public int getObjectsSize() {
        return wallPosts != null ? wallPosts.size() : 0;
    }

    public WallPost getPost(int index) {
        return wallPosts != null ? wallPosts.get(index) : null;
    }
}