package uk.openvk.android.legacy.ui.core.fragments.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.ui.list.adapters.FriendsRequestsAdapter;
import uk.openvk.android.legacy.ui.view.layouts.TabSelector;

public class FriendsFragment extends Fragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private ListView friendsListView;
    private ArrayList<Friend> friends;
    private ArrayList<Friend> requests;
    private FriendsListAdapter friendsAdapter;
    private FriendsRequestsAdapter requestsAdapter;
    public int requests_cursor_index;
    public boolean loading_more_friends;
    private View view;
    private Context activity_ctx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.friends_layout, container, false);
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
        ((TabSelector) view.findViewById(R.id.selector)).setup(friends_tabhost, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Friend> friends, String where) {
        if(view != null) {
            if (where.equals("friends")) {
                this.friends = friends;
                if (friendsAdapter == null) {
                    friendsAdapter = new FriendsListAdapter(ctx, this, friends);
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
                LinearLayoutManager llm = new LinearLayoutManager(ctx);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.requests_view)).setLayoutManager(llm);
                ((RecyclerView) view.findViewById(R.id.requests_view)).setAdapter(requestsAdapter);
            }
        }
    }

    public int getCount() {
        try {
            return friendsAdapter.getCount();
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
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/friend_avatars/avatar_%s", getContext().getCacheDir(), item.id), options);
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
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/friend_avatars/avatar_%s", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e("OpenVK", String.format("%s/photos_cache/friend_avatars/avatar_%d", getContext().getCacheDir(), item.id));
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
        loading_more_friends = false;
        friendsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(infinity_scroll) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        if(!loading_more_friends) {
                            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                                loading_more_friends = true;
                                ((AppActivity) ctx).loadMoreFriends();
                            } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                                loading_more_friends = true;
                                ((FriendsIntentActivity) ctx).loadMoreFriends();
                            }
                        }
                    }
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
        (view.findViewById(R.id.friends_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
    }
}
