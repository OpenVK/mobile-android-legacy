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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.intents.FriendsIntentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.InfinityRecyclerViewScrollListener;
import uk.openvk.android.legacy.ui.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.ui.list.adapters.FriendsRequestsAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.TabSelector;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

public class FriendsFragment extends ActiveFragment {
    public String state;
    private RecyclerView friendsListView;
    private ArrayList<Friend> friends;
    private ArrayList<Friend> requests;
    private FriendsListAdapter friendsAdapter;
    private FriendsRequestsAdapter requestsAdapter;
    public int requests_cursor_index;
    private View view;
    private Context activity_ctx;
    private String instance;
    private int previousListCount;
    private InfinityRecyclerViewScrollListener infinityScrollListener = null;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsListView = view.findViewById(R.id.friends_listview);
        TabHost friends_tabhost = view.findViewById(R.id.friends_tabhost);
        if(activity_ctx == null) {
            activity_ctx = getActivity();
        }
        if(activity_ctx instanceof AppActivity) {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(2);
            setupTabHost(friends_tabhost, "friends_2");
        } else {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(1);
            setupTabHost(friends_tabhost, "friends");
        }
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(
                0, getResources().getString(R.string.friends)
        );
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(
                1, getResources().getString(R.string.friend_requests)
        );

        ((TabSelector) view.findViewById(R.id.selector)).setup(friends_tabhost, new
                View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, long userId, ArrayList<Friend> friends, String where) {

        this.userId = userId;

        if(friends.size() == 0) {
            friendsAdapter.notifyDataSetChanged();
            return;
        }

        if(view != null) {
            if (where.equals("friends")) {
                if(this.friends == null)
                    this.friends = new ArrayList<>();

                this.friends.addAll(friends);

                this.friends.add(new Friend());

                if (friendsAdapter == null) {
                    friendsAdapter = new FriendsListAdapter(ctx, this, this.friends);
                    adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
                    friendsListView.setAdapter(friendsAdapter);
                } else {
                    friendsAdapter.setArray(this.friends);
                    friendsAdapter.notifyDataSetChanged();
                }

                previousListCount = friends.size();
            } else {
                if(this.requests == null)
                    this.requests = friends;
                else
                    this.requests.addAll(friends);

                if (requestsAdapter == null) {
                    requestsAdapter = new FriendsRequestsAdapter(ctx, this, requests);
                    adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
                    ((RecyclerView) view.findViewById(R.id.requests_view)).setAdapter(requestsAdapter);
                } else {
                    requestsAdapter.setArray(this.requests);
                    requestsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void adjustLayoutSize(final Context ctx, int orientation) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        RecyclerView.LayoutManager lm;
        RecyclerView.LayoutManager rlm;

        if(app.isTablet && app.swdp >= 760 && (orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedGridLayoutManager(ctx, 3);
            ((WrappedGridLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(lm);
            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedGridLayoutManager(ctx, 3);
                ((WrappedGridLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(rlm);
            }
        } else if(app.isTablet && app.swdp >= 600) {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedGridLayoutManager(ctx, 2);
            ((WrappedGridLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(lm);

            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedGridLayoutManager(ctx, 2);
                ((WrappedGridLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(rlm);
            }
        } else {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedLinearLayoutManager(ctx);
            ((WrappedLinearLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(lm);

            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedLinearLayoutManager(ctx);
                ((WrappedLinearLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(rlm);
            }
        }

        infinityScrollListener = new InfinityRecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(previousListCount > 0 && getCount() > 0) {
                    OpenVKAPI ovk_api = null;
                    if (ctx instanceof AppActivity) {
                        ovk_api = ((AppActivity) ctx).ovk_api;
                    } else if (ctx instanceof FriendsIntentActivity) {
                        ovk_api = ((FriendsIntentActivity) ctx).ovk_api;
                    } else {
                        return;
                    }

                    Global.loadMoreFriends(userId, ovk_api);
                }
            }
        };

        friendsListView.addOnScrollListener(infinityScrollListener);
    }

    public int getCount() {
        try {
            return friendsAdapter.getItemCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void loadAvatars() {
        if(friendsAdapter != null) {
            friendsListView = view.findViewById(R.id.friends_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Friend item = friends.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/friend_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    friends.set(i, item);
                } catch (OutOfMemoryError | Exception ex) {
                    ex.printStackTrace();
                }
            }
            friendsAdapter.notifyDataSetChanged();
        }

        if(requests != null) {
            for (int i = 0; i < requests.size(); i++) {
                try {
                    Friend item = requests.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile
                            (String.format("%s/%s/photos_cache/friend_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance,  item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e(OvkApplication.APP_TAG,
                                String.format("%s/%s/photos_cache/friend_avatars/avatar_%d",
                                        getContext().getCacheDir(), instance, item.id));
                    }
                    requests.set(i, item);
                } catch (OutOfMemoryError | Exception ex) {
                    ex.printStackTrace();
                }
            }
            requestsAdapter.notifyDataSetChanged();
        }
    }

    public void refresh() {
        if(friendsAdapter != null)
            friendsAdapter.notifyDataSetChanged();

        if(requestsAdapter != null)
            requestsAdapter.notifyDataSetChanged();
    }

    private void setupTabHost(TabHost tabhost, String where) {
        tabhost.setup();
        if (where.equals("friends")) {
            TabHost.TabSpec tabSpec = tabhost.newTabSpec("main");
            tabSpec.setContent(R.id.tab1);
            tabSpec.setIndicator(getResources().getString(R.string.friends));
            tabhost.addTab(tabSpec);
        } else if (where.equals("friends_2")) {
            TabHost.TabSpec tabSpec = tabhost.newTabSpec("main");
            tabSpec.setContent(R.id.tab1);
            tabSpec.setIndicator(getResources().getString(R.string.friends));
            tabhost.addTab(tabSpec);
            tabSpec = tabhost.newTabSpec("requests");
            tabSpec.setContent(R.id.tab2);
            tabSpec.setIndicator(getResources().getString(R.string.friend_requests));
            tabhost.addTab(tabSpec);
        }
        tabhost.setCurrentTab(0);
    }

    public void setActivityContext(Context ctx) {
        activity_ctx = ctx;
    }

    public void hideSelectedItemBackground(int position) {
        (view.findViewById(R.id.friends_listview)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
    }

    public void updateTabsCounters(int counter, int count) {
        TabSelector selector = view.findViewById(R.id.selector);
        if(counter == 0) {
            if(count > 0) {
                selector.setTabTitle(0,
                        Global.getPluralQuantityString(
                                getContext(),
                                R.plurals.friends_tab_all, count
                        )
                );
            } else {
                selector.setTabTitle(0, getResources().getString(R.string.friends));
            }
        } else {
            if(count >= 0) {
                selector.setTabTitle(1,
                        String.format(
                                "%s (%s)",
                                getResources().getString(R.string.friend_requests), count
                        )
                );
            } else {
                selector.setTabTitle(1, getResources().getString(R.string.friend_requests));
            }
        }
    }

    public void loadAPIData(Context ctx, long userId, OpenVKAPI ovk_api) {
        if(this.friends != null && this.friends.size() > 0) {
            int lastEntity = this.friends.size() - 1;
            if(this.friends.get(lastEntity).getEntityType() == LazyEntity.SLEEPING_ENTITY)
                this.friends.remove(lastEntity);
        }
        createAdapter(ctx, userId, ovk_api.friends.getFriends(), "friends");

        if((this.requests == null || this.requests.size() == 0) &&
                ovk_api.user.id == ovk_api.account.id)
            ovk_api.friends.getRequests(ovk_api.wrapper);

        updateTabsCounters(0, ovk_api.friends.count);
        /* it's buggy
         * updateTabsCounters(1, ovk_api.account.counters.friends_requests);
         */
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutSize(getContext(), newConfig.orientation);
    }

    @Override
    public int getObjectsSize() {
        return friends != null ? friends.size() : 0;
    }
}
