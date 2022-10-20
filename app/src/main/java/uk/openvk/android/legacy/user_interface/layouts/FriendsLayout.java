package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.user_interface.list_adapters.FriendsListAdapter;

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

    public void loadAvatars() {
        if(friendsAdapter != null) {
            friendsListView = (ListView) findViewById(R.id.friends_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Friend item = friends.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/friend_avatars/avatar_%d", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    friends.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            friendsAdapter = new FriendsListAdapter(getContext(), friends);
            friendsListView.setAdapter(friendsAdapter);
        }
    }
}
