package uk.openvk.android.legacy.core.fragments.friends;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.intents.FriendsIntentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.listeners.InfinityRecyclerViewScrollListener;
import uk.openvk.android.legacy.core.listeners.OnSizeChangedListener;
import uk.openvk.android.legacy.ui.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.ui.list.adapters.FriendsRequestsAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

public class FriendListFragment extends ActiveFragment {

    InfinityRecyclerView listView;
    ArrayList<Friend> friends;
    RecyclerView.Adapter adapter;
    private long userId;
    private int previousListCount;
    private InfinityRecyclerViewScrollListener infinityScrollListener;
    private int position = -1;
    private boolean dataLoading = true;
    private OpenVKAPI ovk_api;

    public static FriendListFragment createInstance(int position) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putInt("pos", position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            position = getArguments().getInt("pos");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        listView = new InfinityRecyclerView(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        listView.setLayoutParams(params);

        if(position == 1)
            listView.setBackgroundColor(Color.parseColor("#e3e4e6"));



        return listView;
    }

    public void createAdapter(Context ctx, long userId, OpenVKAPI ovk_api, ArrayList<Friend> friends) {
        this.userId = userId;

        this.ovk_api = ovk_api;

        if(this.friends != null && this.friends.size() == 0 && adapter != null) {
            adapter.notifyDataSetChanged();
            return;
        }

        if(this.friends == null)
            this.friends = new ArrayList<>();
        else if(this.friends.size() > 0) {
            Friend lazyEntity = this.friends.get(this.friends.size() - 1);
            this.friends.remove(lazyEntity);
        }

        this.friends.addAll(friends);

        if(position == 0 && friends.size() > 0)
            this.friends.add(new Friend());

        if (adapter == null) {
            adapter = position == 0 ?
                    new FriendsListAdapter(ctx, this, this.friends) :
                    new FriendsRequestsAdapter(ctx, this, this.friends);

            adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
            setInfinityScrollListener(ovk_api, listView.getLayoutManager());
            listView.setAdapter(adapter);

            /*
             * This code is a workaround for creating a list of items for a nested RecyclerView
             * when LinearLayoutManager does not automatically lay out the items.
             *
             * The bug is reproducible on Android JB and earlier versions.
             *
             * See more: https://stackoverflow.com/a/57675484/24295422
             */
            if(listView.getChildCount() == 0) {
                listView.swapAdapter(adapter, false);
                listView.smoothScrollToPosition(0);
            }

        } else {
            if(adapter instanceof FriendsListAdapter)
                ((FriendsListAdapter) adapter).setArray(this.friends);
            else if(adapter instanceof FriendsRequestsAdapter)
                ((FriendsRequestsAdapter) adapter).setArray(this.friends);

            adapter.notifyDataSetChanged();
            if(listView.getChildCount() < this.friends.size())
                listView.smoothScrollToPosition(previousListCount - 1);
        }
        dataLoading = false;
        previousListCount += friends.size();
    }

    private void setInfinityScrollListener(final OpenVKAPI ovk_api, RecyclerView.LayoutManager lm) {
        infinityScrollListener = new InfinityRecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (previousListCount > 0 && getObjectsSize() > 0) {
                    dataLoading = true;
                    Global.loadMoreFriends(userId, ovk_api);
                }
            }
        };
        listView.addOnScrollListener(infinityScrollListener);
    }

    private void adjustLayoutSize(final Context ctx, int orientation) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        RecyclerView.LayoutManager lm;
        RecyclerView.LayoutManager rlm;

        if(app.isTablet && app.swdp >= 760 && (orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedGridLayoutManager(ctx, 3);
            ((WrappedGridLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            listView.setLayoutManager(lm);
            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedGridLayoutManager(ctx, 3);
                ((WrappedGridLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                listView.setLayoutManager(rlm);
            }
        } else if(app.isTablet && app.swdp >= 600) {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedGridLayoutManager(ctx, 2);
            ((WrappedGridLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            listView.setLayoutManager(lm);

            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedGridLayoutManager(ctx, 2);
                ((WrappedGridLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                listView.setLayoutManager(rlm);
            }
        } else {
            // Linking WGLM to ListView for Friends tab
            lm = new WrappedLinearLayoutManager(ctx);
            ((WrappedLinearLayoutManager) lm).setOrientation(LinearLayoutManager.VERTICAL);
            listView.setLayoutManager(lm);

            if(getActivity() instanceof AppActivity) {
                // Linking WGLM to ListView for Requests tab
                rlm = new WrappedLinearLayoutManager(ctx);
                ((WrappedLinearLayoutManager) rlm).setOrientation(LinearLayoutManager.VERTICAL);
                listView.setLayoutManager(rlm);
            }
        }
    }

    @Override
    public int getObjectsSize() {
        if(dataLoading)
            return -1;
        else
            return adapter != null ? adapter.getItemCount() : 0;
    }
}
