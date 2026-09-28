package uk.openvk.android.legacy.core.fragments.friends;

import android.content.Context;
import android.content.res.Configuration;
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
import uk.openvk.android.legacy.ui.list.adapters.FriendsListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;

public class FriendListFragment extends ActiveFragment {

    RecyclerView listView;
    ArrayList<Friend> friends;
    FriendsListAdapter adapter;
    private long userId;
    private int previousListCount;
    private InfinityRecyclerViewScrollListener infinityScrollListener;
    private int position = -1;

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

        listView = new RecyclerView(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        listView.setLayoutParams(params);

        return listView;
    }

    public void createAdapter(Context ctx, long userId, ArrayList<Friend> friends) {
        this.userId = userId;

        if(friends.size() == 0) {
            adapter.notifyDataSetChanged();
            return;
        }

        if(this.friends == null)
            this.friends = new ArrayList<>();

        this.friends.addAll(friends);

        this.friends.add(new Friend());

        if (adapter == null) {
            adapter = new FriendsListAdapter(ctx, this, this.friends);
            adjustLayoutSize(ctx, getResources().getConfiguration().orientation);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            listView.setLayoutManager(layoutManager);
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
            adapter.setArray(this.friends);
            adapter.notifyDataSetChanged();
        }

        previousListCount = friends.size();
    }

    public void hideSelectedItemBackground(int position) {
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

        infinityScrollListener = new InfinityRecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(previousListCount > 0 && getObjectsSize() > 0) {
                    OpenVKAPI ovk_api = null;
                    if (ctx instanceof AppActivity)
                        ovk_api = ((AppActivity) ctx).ovk_api;
                    else if (ctx instanceof FriendsIntentActivity)
                        ovk_api = ((FriendsIntentActivity) ctx).ovk_api;
                    else
                        return;

                    Global.loadMoreFriends(userId, ovk_api);
                }
            }
        };

        listView.addOnScrollListener(infinityScrollListener);
    }

    @Override
    public int getObjectsSize() {
        return adapter != null ? adapter.getItemCount() : -1;
    }
}
