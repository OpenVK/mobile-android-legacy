package uk.openvk.android.legacy.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.view.View;

import uk.openvk.android.legacy.ui.core.listeners.OnRecyclerScrollListener;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class InfinityRecyclerView extends RecyclerView {

    private OnScrollListener listener;
    public boolean isLoading = false;

    public InfinityRecyclerView(Context context) {
        super(context);
    }


    public InfinityRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InfinityRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnRecyclerScrollListener(final OnRecyclerScrollListener listener) {
        try {
            if (this.listener != null) {
                removeOnScrollListener(this.listener);
            }
        } catch (Exception ignored) {

        }
        this.listener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() == getAdapter().getItemCount() - 2) {
                        listener.onRecyclerScroll(recyclerView, dx, dy);
                        isLoading = true;
                    }
                }
            }
        };
        addOnScrollListener(this.listener);
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        isLoading = false;
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        isLoading = false;
    }
}
