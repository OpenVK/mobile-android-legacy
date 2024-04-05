/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.OnNestedScrollListener;
import uk.openvk.android.legacy.databases.NewsfeedCacheDB;
import uk.openvk.android.legacy.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.base.InfinityNestedScrollView;
import uk.openvk.android.legacy.ui.views.OvkRefreshableHeaderLayout;

public class NewsfeedFragment extends ActiveFragment {
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray newsfeed;
    public String send_request;
    public SharedPreferences global_prefs;
    private NewsfeedAdapter newsfeedAdapter;
    private RecyclerView newsfeedView;
    private ListView newsfeedListView;
    private LinearLayoutManager llm;
    private ArrayList<WallPost> wallPosts;
    public boolean loading_more_posts = false;
    private int pastComplVisiblesItems;
    private Parcelable recyclerViewState;
    private View view;
    private String instance;
    private Menu fragment_menu;
    private Account account;

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
        //loadFromCache(getActivity());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.newsfeed, menu);
        fragment_menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu != null) {
            if (menu.size() > 0) {
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

    public void loadFromCache(Context ctx) {
        ArrayList<WallPost> posts = NewsfeedCacheDB.getPostsList(ctx);
        if(posts != null && posts.size() > 0)
            createAdapter(ctx, posts, false);
    }

    public void createAdapter(Context ctx, ArrayList<WallPost> wallPosts, boolean cache) {
        this.wallPosts = wallPosts;
        newsfeedView = view.findViewById(R.id.news_listview);
        newsfeedView.setHasFixedSize(true);
        if(newsfeedAdapter == null) {
            newsfeedAdapter = new NewsfeedAdapter(ctx, this.wallPosts, false);
            llm = new WrappedLinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            newsfeedView.setLayoutManager(llm);
            newsfeedView.setAdapter(newsfeedAdapter);
        } else {
            newsfeedAdapter.setArray(wallPosts);
            newsfeedAdapter.notifyDataSetChanged();
        }
        if(cache) {
            NewsfeedCacheDB.putPosts(ctx, this.wallPosts, true);
        }
        CustomSwipeRefreshLayout p2r_news_view = view.findViewById(R.id.refreshable_layout);
        p2r_news_view.setCustomHeadview(new OvkRefreshableHeaderLayout(getContext()));
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
        adjustLayout(getContext().getResources().getConfiguration().orientation);
    }

    public void updateItem(WallPost item, int position) {
        if(newsfeedAdapter != null) {
            newsfeedView = view.findViewById(R.id.news_listview);
            wallPosts.set(position, item);
            newsfeedAdapter.notifyItemChanged(position);
        }
    }

    public void updateAllItems() {
        if(newsfeedAdapter != null) {
            newsfeedView = (RecyclerView) view.findViewById(R.id.news_listview);
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void loadAvatars() {
        if(newsfeedAdapter != null) {
            newsfeedView = (RecyclerView) view.findViewById(R.id.news_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    WallPost item = wallPosts.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/newsfeed_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.author_id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    wallPosts.set(i, item);
                } catch (OutOfMemoryError err) {
                    err.printStackTrace();
                }
            }
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    private void loadPhotos() {
        newsfeedView = view.findViewById(R.id.news_listview);
        try {
            newsfeedAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean load_photos,
                                      final boolean infinity_scroll) {
        loading_more_posts = false;
        if(load_photos) {
            loadPhotos();
        }
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
                            ((AppActivity) ctx).loadMoreNews();
                        }
                    }
                }
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
            if(newsfeedView != null) {
                if (((OvkApplication) getContext().getApplicationContext()).isTablet) {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.
                                LayoutParams((int) (600 * (getResources().getDisplayMetrics().density)),
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        newsfeedView.setLayoutParams(layoutParams);
                    } else {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.
                                LayoutParams((int) (500 * (getResources().getDisplayMetrics().density)),
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        newsfeedView.setLayoutParams(layoutParams);
                    }
                } else {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.
                                LayoutParams((int) (480 * (getResources().getDisplayMetrics().density)),
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        newsfeedView.setLayoutParams(layoutParams);
                    } else {
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.
                                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        newsfeedView.setLayoutParams(layoutParams);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshAdapter() {
        if(newsfeedAdapter != null) {
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void loadAPIData(Context ctx, OpenVKAPI ovk_api, Spinner ab_spinner,
                            int isGlobalFeed, boolean notScroll) {
        ((CustomSwipeRefreshLayout) view.findViewById(R.id.refreshable_layout)).refreshComplete();
        if(ab_spinner.getSelectedItemPosition() == isGlobalFeed) {
            createAdapter(ctx, ovk_api.newsfeed.getWallPosts(), true);
            if(ovk_api.newsfeed.getWallPosts().size() > 0) {
                return;
            }
            loading_more_posts = true;
            setScrollingPositions(ctx, false, true);
            if(!notScroll) {
                newsfeedView.scrollToPosition(0);
            }
            adjustLayout(getResources().getConfiguration().orientation);
        }
    }

    public void loadAccount(final OpenVKAPI ovk_api) {
        this.account = ovk_api.account;
        refreshOptionsMenu();
    }

    public void refreshOptionsMenu() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
}