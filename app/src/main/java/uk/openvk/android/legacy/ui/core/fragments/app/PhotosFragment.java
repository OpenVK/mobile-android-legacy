package uk.openvk.android.legacy.ui.core.fragments.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.PhotoAlbum;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.legacy.ui.list.adapters.GroupsListAdapter;
import uk.openvk.android.legacy.ui.list.adapters.PhotoAlbumsListAdapter;
import uk.openvk.android.legacy.ui.list.decorations.VerticalSpace;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.view.InfinityRecyclerView;

public class PhotosFragment extends Fragment {

    private View view;
    private InfinityRecyclerView albumsListView;
    private String instance;
    private PhotoAlbumsListAdapter albumsAdapter;
    public boolean infinity_scroll;
    private ArrayList<PhotoAlbum> albums;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photos, container, false);
        albumsListView = view.findViewById(R.id.albums_listview);
        VerticalSpace dividerItemDecoration = new VerticalSpace(
                (int)(8 * getResources().getDisplayMetrics().scaledDensity));
        albumsListView.addItemDecoration(dividerItemDecoration);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public long getCount() {
        try {
            return albumsAdapter.getItemCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void createAdapter(final Context ctx, ArrayList<PhotoAlbum> albumsList, String type) {
        this.albums = albumsList;
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if (albumsAdapter == null) {
            albumsAdapter = new PhotoAlbumsListAdapter(ctx, albumsList);
            if(app.isTablet && app.swdp >= 760) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.albums_listview)).setLayoutManager(glm);
            } else if(app.isTablet && app.swdp >= 600) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.albums_listview)).setLayoutManager(glm);
            } else {
                LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.albums_listview)).setLayoutManager(llm);
            }
            albumsListView.setAdapter(albumsAdapter);
        } else {
            albumsAdapter.notifyDataSetChanged();
        }
        Log.d(OvkApplication.APP_TAG, String.format("Photo Albums count: %s | Real count: %s",
                albums.size(), albumsListView.getChildCount()));
    }

    public void refresh() {
        if(albumsAdapter != null) {
            albumsAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        albumsListView.setLoading(!infinity_scroll);
        albumsListView.setOnRecyclerScrollListener(new OnRecyclerScrollListener() {
            @Override
            public void onRecyclerScroll(RecyclerView recyclerView, int x, int y) {
                if(ctx instanceof AppActivity) {
                    OpenVKAPI ovk_api = ((AppActivity) ctx).ovk_api;
                }
            }
        });
    }
}
