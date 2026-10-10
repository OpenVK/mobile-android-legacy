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
import android.support.v4.app.FragmentActivity;
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
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.InfinityRecyclerViewScrollListener;
import uk.openvk.android.legacy.core.listeners.OnSizeChangedListener;
import uk.openvk.android.legacy.databases.GroupsCacheDB;
import uk.openvk.android.legacy.databases.NewsfeedCacheDB;
import uk.openvk.android.legacy.databases.UsersCacheDB;
import uk.openvk.android.legacy.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.ActionBarLayout;
import uk.openvk.android.legacy.ui.views.OvkRefreshableHeaderLayout;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

public class NewsfeedFragment extends ActiveFragment {
    public String state;
    public JSONArray newsfeed;
    public SharedPreferences global_prefs;
    private NewsfeedAdapter newsfeedAdapter;
    private InfinityRecyclerView newsfeedView;
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
        NewsfeedCacheDB.initDatabases(getContext());
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
            if(loadFromCache() && getActivity() instanceof AppActivity) {
                ((AppActivity) getActivity()).showContent(2);
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
                    AppActivity activity = ((AppActivity) getActivity());
                    if (activity.getCustomActionBarLayout().getNewsfeedSelection() == 0) {
                        activity.refreshPage("subscriptions_newsfeed");
                    } else {
                        activity.refreshPage("global_newsfeed");
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
                    account = ((NetworkFragmentActivity) getActivity()).getOpenVKAPI().account;

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
                        ((NetworkFragmentActivity) getActivity()).getOpenVKAPI()
                );
                return false;
            default:
                break;
        }

        return false;
    }

    public boolean loadFromCache() {
        ArrayList<WallPost> posts = NewsfeedCacheDB.getPostsList();
        if(posts != null && posts.size() > 0) {
            createAdapter(posts, false, false);
            return true;
        } else
            Log.e("OpenVK","Empty posts");

        return false;
    }

    public void createAdapter(ArrayList<WallPost> wallPosts, boolean cache, boolean clear) {

        NetworkFragmentActivity activity;

        GroupsCacheDB.initDatabase(getContext());
        UsersCacheDB.initDatabase(getContext());
        NewsfeedCacheDB.initDatabases(getContext());

        if(getActivity() instanceof NetworkFragmentActivity)
            activity = (NetworkFragmentActivity) getActivity();
        else
            return;

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
            newsfeedAdapter = new NewsfeedAdapter(activity, this.wallPosts, false);
            llm = new WrappedLinearLayoutManager(activity);
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

            newsfeedView.setOnSizeChangedListener(
                    new OnSizeChangedListener() {
                        @Override
                        public void onSizeChanged(View view) {
                            adjustLayout(getResources().getConfiguration().orientation);
                        }
                    }
            );
            newsfeedView.addOnScrollListener(listener);
            Log.d(OvkApplication.APP_TAG, "NewsfeedFragment: create adapter 0");
            newsfeedView.setAdapter(newsfeedAdapter);
            Log.d(OvkApplication.APP_TAG, "NewsfeedFragment: create adapter 1");
        } else {
            newsfeedAdapter.setArray(this.wallPosts);
            newsfeedAdapter.notifyDataSetChanged();
        }

        if(cache) {
            NewsfeedCacheDB.putPosts(this.wallPosts, clear);
        }

        adjustLayout(((OvkApplication)(getContext().getApplicationContext())).config.orientation);

        final LinearLayoutManager layoutManager = ((LinearLayoutManager) newsfeedView.getLayoutManager());

        CustomSwipeRefreshLayout p2r_news_view = view.findViewById(R.id.refreshable_layout);

        p2r_news_view.setScroolUpHandler(new CustomSwipeRefreshLayout.ScrollUpHandler() {
            // ^ typo detected in SRL library: github.com/xyxyLiu/SwipeRefreshLayout
            @Override
            public boolean canScrollUp(View view) {

                int paddingStart;
                if(layoutManager != null) {
                    View firstChild = layoutManager.getChildAt(0);
                    paddingStart = firstChild.getTop();

                    return view == newsfeedView &&
                            (layoutManager.findFirstVisibleItemPosition() != 0 ||
                                paddingStart != 0
                            );
                }
                return false;
            }
        });
    }

    public int getCount() {
        try {
            return newsfeedView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    public void addOrDeleteLike(int position, String value) {
        wallPosts.get(position).counters.isLiked = value.equals("add");
        newsfeedAdapter.notifyDataSetChanged();
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

    @Override
    public boolean onReceivedAPIResponse(int msgCode, Bundle data) {
        super.onReceivedAPIResponse(msgCode, data);

        if(getView() == null)
            return true;

        switch (msgCode) {
            case HandlerMessages.ACCOUNT_PROFILE_INFO:
                refreshOptionsMenu();
                break;

            case HandlerMessages.NEWSFEED_GET:
            case HandlerMessages.NEWSFEED_GET_GLOBAL:
                wallPosts = mOpenVK.newsfeed.getWallPosts();

                ((CustomSwipeRefreshLayout) view.findViewById(R.id.refreshable_layout)).refreshComplete();

                if (wallPosts != null && wallPosts.size() > 0) {
                    int lastEntity = wallPosts.size() - 1;
                    if (wallPosts.get(lastEntity).getEntityType() == LazyEntity.SLEEPING_ENTITY) {
                        wallPosts.remove(lastEntity);
                    }
                }

                createAdapter(mOpenVK.newsfeed.getWallPosts(), true, true);
                adjustLayout(((OvkApplication) (getContext().getApplicationContext())).config.orientation);

                loading_more_posts = true;
                newsfeedView.scrollToPosition(0);
                break;
            case HandlerMessages.NEWSFEED_GET_MORE:
            case HandlerMessages.NEWSFEED_GET_MORE_GLOBAL:
                if (wallPosts != null && wallPosts.size() > 0) {
                    int lastEntity = wallPosts.size() - 1;
                    if (wallPosts.get(lastEntity).getEntityType() == LazyEntity.SLEEPING_ENTITY) {
                        wallPosts.remove(lastEntity);
                    }
                }
                createAdapter(mOpenVK.newsfeed.getWallPosts(), false, false);
                break;
            case HandlerMessages.LIKES_ADD:
                addOrDeleteLike(mOpenVK.likes.position, "add");
                break;
            case HandlerMessages.LIKES_DELETE:
                addOrDeleteLike(mOpenVK.likes.position, "delete");
                break;
            case HandlerMessages.NEWSFEED_ATTACHMENTS:
            case HandlerMessages.WALL_ATTACHMENTS:
            case HandlerMessages.NEWSFEED_AVATARS:
            case HandlerMessages.WALL_AVATARS:
                if(data.containsKey("parent_id"))
                    refreshAdapterItem(
                            newsfeedAdapter.findItemPos(data.getLong("parent_id"))
                    );
                else
                    refreshAdapter();
                break;
        }

        return true;
    }

    private void refreshAdapterItem(int position) {
        if(position < 0)
            return;

        newsfeedAdapter.notifyItemChanged(position);
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
                                    Global.openNewPostActivity(getContext(), activity.getOpenVKAPI());
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