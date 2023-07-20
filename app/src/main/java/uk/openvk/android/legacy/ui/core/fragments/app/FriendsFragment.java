package uk.openvk.android.legacy.ui.core.fragments.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.legacy.ui.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.ui.list.adapters.FriendsRequestsAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.view.InfinityRecyclerView;
import uk.openvk.android.legacy.ui.view.layouts.TabSelector;

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

public class FriendsFragment extends Fragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private InfinityRecyclerView friendsListView;
    private ArrayList<Friend> friends;
    private ArrayList<Friend> requests;
    private FriendsListAdapter friendsAdapter;
    private FriendsRequestsAdapter requestsAdapter;
    public int requests_cursor_index;
    public boolean loading_more_friends;
    private View view;
    private Context activity_ctx;
    private String instance;

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
        if(activity_ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(2);
            setupTabHost(friends_tabhost, "friends_2");
        } else {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(1);
            setupTabHost(friends_tabhost, "friends");
        }
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(0, getResources().getString(R.string.friends));
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(1, getResources().getString(R.string.friend_requests));
        ((TabSelector) view.findViewById(R.id.selector)).setup(friends_tabhost, new
                View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Friend> friends, String where) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if(view != null) {
            if (where.equals("friends")) {
                this.friends = friends;
                if (friendsAdapter == null) {
                    friendsAdapter = new FriendsListAdapter(ctx, this, friends);
                    if(app.isTablet && app.swdp >= 760) {
                        LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
                        glm.setOrientation(LinearLayoutManager.VERTICAL);
                        ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(glm);
                    } else if(app.isTablet && app.swdp >= 600) {
                        LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
                        glm.setOrientation(LinearLayoutManager.VERTICAL);
                        ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(glm);
                    } else {
                        LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        ((RecyclerView) view.findViewById(R.id.friends_listview)).setLayoutManager(llm);
                    }
                    friendsListView.setAdapter(friendsAdapter);
                } else {
                    friendsAdapter.notifyDataSetChanged();
                }
            } else {
                this.requests = friends;
                if (requestsAdapter == null) {
                    requestsAdapter = new FriendsRequestsAdapter(ctx, this, requests);
                } else {
                    requestsAdapter.notifyDataSetChanged();
                }
                if(app.isTablet && app.swdp >= 760) {
                    LinearLayoutManager glm = new GridLayoutManager(ctx, 3);
                    glm.setOrientation(LinearLayoutManager.VERTICAL);
                    ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(glm);
                } else if(app.isTablet && app.swdp >= 600) {
                    LinearLayoutManager glm = new GridLayoutManager(ctx, 2);
                    glm.setOrientation(LinearLayoutManager.VERTICAL);
                    ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(glm);
                } else {
                    LinearLayoutManager llm = new LinearLayoutManager(ctx);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(llm);
                }
                ((RecyclerView) view.findViewById(R.id.requests_view)).setAdapter(requestsAdapter);
            }
        }
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
        if(friendsAdapter != null) {
            friendsAdapter.notifyDataSetChanged();
        }
        if(requestsAdapter != null) {
            requestsAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        friendsListView.setLoading(!infinity_scroll);
        friendsListView.setOnRecyclerScrollListener(new OnRecyclerScrollListener() {
            @Override
            public void onRecyclerScroll(RecyclerView recyclerView, int x, int y) {
                if(ctx instanceof AppActivity) {
                    ((AppActivity) ctx).loadMoreFriends();
                } else if(ctx instanceof FriendsIntentActivity) {
                    ((FriendsIntentActivity) ctx).loadMoreFriends();
                }
            }
        });
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
    }

    public void setActivityContext(Context ctx) {
        activity_ctx = ctx;
    }

    public void hideSelectedItemBackground(int position) {
        (view.findViewById(R.id.friends_listview)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
    }
}
