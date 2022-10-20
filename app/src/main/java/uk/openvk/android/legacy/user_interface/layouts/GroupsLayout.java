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
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.user_interface.list_adapters.GroupsListAdapter;

public class GroupsLayout extends LinearLayout {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private ListView groupsListView;
    private ArrayList<Group> groups;
    private GroupsListAdapter groupsAdapter;

    public GroupsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.groups_layout, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        groupsListView = (ListView) findViewById(R.id.groups_listview);
    }

    public void createAdapter(Context ctx, ArrayList<Group> groups) {
        this.groups = groups;
        groupsAdapter = new GroupsListAdapter(ctx, groups);
        groupsListView.setAdapter(groupsAdapter);
    }

    public int getCount() {
        try {
            return groupsAdapter.getCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void loadAvatars() {
        if(groupsAdapter != null) {
            groupsListView = (ListView) findViewById(R.id.groups_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Group item = groups.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/group_avatars/avatar_%d", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    groups.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            groupsAdapter = new GroupsListAdapter(getContext(), groups);
            groupsListView.setAdapter(groupsAdapter);
        }
    }
}
