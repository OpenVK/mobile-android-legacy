package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiviableFragment;
import uk.openvk.android.legacy.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.legacy.ui.list.adapters.GroupsListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class GroupsFragment extends ActiviableFragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private InfinityRecyclerView groupsListView;
    private ArrayList<Group> groups;
    private GroupsListAdapter groupsAdapter;
    private boolean loading_more_groups = false;
    private View view;
    private String instance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        groupsListView = view.findViewById(R.id.groups_listview);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Group> groups) {
        this.groups = groups;
        if (groupsAdapter == null) {
            groupsAdapter = new GroupsListAdapter(ctx, groups);
            groupsListView.setAdapter(groupsAdapter);
            adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
        } else {
            groupsAdapter.notifyDataSetChanged();
        }
    }

    private void adjustLayoutSize(Context ctx, int orientation) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if(app.isTablet && app.swdp >= 760 && (orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
            glm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.groups_listview)).setLayoutManager(glm);
        } else if(app.isTablet && app.swdp >= 600) {
            LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
            glm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.groups_listview)).setLayoutManager(glm);
        } else {
            LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.groups_listview)).setLayoutManager(llm);
        }
    }

    public int getCount() {
        try {
            return groupsAdapter.getItemCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void loadAvatars() {
        try {
            if(groupsAdapter != null) {
                groupsListView = view.findViewById(R.id.groups_listview);
                for (int i = 0; i < getCount(); i++) {
                    try {
                        Group item = groups.get(i);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(
                                String.format("%s/%s/photos_cache/group_avatars/avatar_%s",
                                        getContext().getCacheDir(), instance, item.id), options);
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
        groupsListView.setLoading(!infinity_scroll);
        groupsListView.setOnRecyclerScrollListener(new OnRecyclerScrollListener() {
            @Override
            public void onRecyclerScroll(RecyclerView recyclerView, int x, int y) {
                if(ctx instanceof AppActivity) {
                    OpenVKAPI ovk_api = ((AppActivity) ctx).ovk_api;
                    Global.loadMoreGroups(ovk_api);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutSize(getContext(), newConfig.orientation);
    }
}
