/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.legacy.ui.list.adapters.VideosListAdapter;
import uk.openvk.android.legacy.ui.list.decorations.VerticalSpace;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

public class VideosFragment extends ActiveFragment {

    private View view;
    private InfinityRecyclerView videosListView;
    private String instance;
    private VideosListAdapter videosAdapter;
    public boolean infinity_scroll;
    private ArrayList<Video> videos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_videos, container, false);
        videosListView = view.findViewById(R.id.videos_listview);
        VerticalSpace dividerItemDecoration = new VerticalSpace(
                (int)(8 * getResources().getDisplayMetrics().scaledDensity));
        videosListView.addItemDecoration(dividerItemDecoration);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public long getCount() {
        try {
            return videosAdapter.getItemCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void createAdapter(final Context ctx, ArrayList<Video> videos) {
        this.videos = videos;
        if (videosAdapter == null) {
            videosAdapter = new VideosListAdapter(ctx, videos);
            adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
            videosListView.setAdapter(videosAdapter);
        } else {
            videosAdapter.notifyDataSetChanged();
        }
        Log.d(OvkApplication.APP_TAG, String.format("Videos count: %s | Real count: %s",
                videos.size(), videosListView.getChildCount()));
    }

    private void adjustLayoutSize(Context ctx, int orientation) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if(app.isTablet && app.swdp >= 760
                && (orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
            glm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.videos_listview)).setLayoutManager(glm);
        } else if(app.isTablet && app.swdp >= 600) {
            LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
            glm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.videos_listview)).setLayoutManager(glm);
        } else {
            LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.videos_listview)).setLayoutManager(llm);
        }
    }

    public void refresh() {
        if(videosAdapter != null) {
            videosAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        videosListView.setLoading(!infinity_scroll);
        videosListView.setOnRecyclerScrollListener(new OnRecyclerScrollListener() {
            @Override
            public void onRecyclerScroll(RecyclerView recyclerView, int x, int y) {
                if(ctx instanceof AppActivity) {
                    OpenVKAPI ovk_api = ((AppActivity) ctx).ovk_api;
                }
            }
        });
    }

    public void refreshListAdapter() {
        videosAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutSize(getContext(), newConfig.orientation);
    }

    @Override
    public int getObjectsSize() {
        return videos != null ? videos.size() : 0;
    }
}
