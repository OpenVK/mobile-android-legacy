package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.list.adapters.GroupsSearchResultAdapter;
import uk.openvk.android.legacy.ui.list.adapters.UsersSearchResultAdapter;

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
