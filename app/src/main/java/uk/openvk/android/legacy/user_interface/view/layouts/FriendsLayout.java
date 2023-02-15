package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.user_interface.core.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.user_interface.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.user_interface.list.adapters.FriendsRequestsAdapter;

public class FriendsLayout extends LinearLayout {
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

    public FriendsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.friends_layout, null);

        this.addView(view);
        loading_more_friends = false;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        friendsListView = (ListView) findViewById(R.id.friends_listview);
    }

    public void createAdapter(Context ctx, ArrayList<Friend> friends, String where) {
        if(where.equals("friends")) {
            this.friends = friends;
            if(friendsAdapter == null) {
                friendsAdapter = new FriendsListAdapter(ctx, friends);
                friendsListView.setAdapter(friendsAdapter);
            } else {
                friendsAdapter.notifyDataSetChanged();
            }
        } else {
            this.requests = friends;
            if(requestsAdapter == null) {
                requestsAdapter = new FriendsRequestsAdapter(ctx, this, requests);
            } else {
                requestsAdapter.notifyDataSetChanged();
            }
            LinearLayoutManager llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) findViewById(R.id.requests_view)).setLayoutManager(llm);
            ((RecyclerView) findViewById(R.id.requests_view)).setAdapter(requestsAdapter);
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
            friendsListView = (ListView) findViewById(R.id.friends_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Friend item = friends.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/friend_avatars/avatar_%d", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    friends.set(i, item);
                } catch (OutOfMemoryError ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
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
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/friend_avatars/avatar_%d", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e("OpenVK", String.format("%s/photos_cache/friend_avatars/avatar_%d", getContext().getCacheDir(), item.id));
                    }
                    requests.set(i, item);
                } catch (OutOfMemoryError ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
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
}
