package uk.openvk.android.legacy.ui.core.fragments.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.adapters.GroupsListAdapter;

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

public class GroupsFragment extends Fragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private ListView groupsListView;
    private ArrayList<Group> groups;
    private GroupsListAdapter groupsAdapter;
    private boolean loading_more_groups = false;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_groups, container, false);
        groupsListView = view.findViewById(R.id.groups_listview);
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Group> groups) {
        this.groups = groups;
        if (groupsAdapter == null) {
            groupsAdapter = new GroupsListAdapter(ctx, groups);
            groupsListView.setAdapter(groupsAdapter);
        } else {
            groupsAdapter.notifyDataSetChanged();
        }
    }

    public int getCount() {
        try {
            return groupsAdapter.getCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void loadAvatars() {
        try {
            if(groupsAdapter != null) {
                groupsListView = (ListView) view.findViewById(R.id.groups_listview);
                for (int i = 0; i < getCount(); i++) {
                    try {
                        Group item = groups.get(i);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/group_avatars/avatar_%s", getContext().getCacheDir(), item.id), options);
                        if (bitmap != null) {
                            item.avatar = bitmap;
                        }
                        groups.set(i, item);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (groupsAdapter == null) {
                    groupsAdapter = new GroupsListAdapter(getContext(), groups);
                    groupsListView.setAdapter(groupsAdapter);
                } else {
                    groupsAdapter.notifyDataSetChanged();
                }
            }
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        loading_more_groups = false;
        groupsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(infinity_scroll) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        if(!loading_more_groups) {
                            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                                loading_more_groups = true;
                                ((AppActivity) ctx).loadMoreGroups();
                            }
                        }
                    }
                }
            }
        });
    }
}
