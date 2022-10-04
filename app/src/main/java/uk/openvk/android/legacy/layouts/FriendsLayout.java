package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.list_adapters.FriendsListAdapter;
import uk.openvk.android.legacy.list_adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

public class FriendsLayout extends LinearLayout {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private ListView friendsListView;
    private ArrayList<Friend> friends;
    private FriendsListAdapter friendsAdapter;

    public FriendsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.friends_layout, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        friendsListView = (ListView) findViewById(R.id.friends_listview);
    }

    public void createAdapter(Context ctx, ArrayList<Friend> friends) {
        this.friends = friends;
        friendsAdapter = new FriendsListAdapter(ctx, friends);
        friendsListView.setAdapter(friendsAdapter);
    }

    public int getCount() {
        try {
            return friendsAdapter.getCount();
        } catch(Exception ex) {
            return 0;
        }
    }
}
