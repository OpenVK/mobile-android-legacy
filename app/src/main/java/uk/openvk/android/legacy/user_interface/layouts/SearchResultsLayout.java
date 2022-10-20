package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.user_interface.list_adapters.GroupsSearchResultAdapter;
import uk.openvk.android.legacy.user_interface.list_adapters.UsersSearchResultAdapter;

public class SearchResultsLayout extends LinearLayout {
    private ListView people_listview;
    private ListView groups_listview;
    private ArrayList<User> users;
    private UsersSearchResultAdapter usersSearchResultAdapter;
    private GroupsSearchResultAdapter groupsSearchResultAdapter;
    private ArrayList<Group> groups;

    public SearchResultsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.search_results, null);

        this.addView(view);
    }

    public void createGroupsAdapter(Context ctx, ArrayList<Group> groups) {
        this.groups = groups;
        groupsSearchResultAdapter = new GroupsSearchResultAdapter(ctx, groups);
        groups_listview = (ListView) findViewById(R.id.community_listview);
        groups_listview.setAdapter(groupsSearchResultAdapter);
    }

    public void createUsersAdapter(Context ctx, ArrayList<User> users) {
        this.users = users;
        usersSearchResultAdapter = new UsersSearchResultAdapter(ctx, users);
        people_listview = (ListView) findViewById(R.id.people_listview);
        people_listview.setAdapter(usersSearchResultAdapter);
    }
}
