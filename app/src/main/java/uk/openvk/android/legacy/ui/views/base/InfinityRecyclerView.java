/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.ui.views.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import uk.openvk.android.legacy.core.listeners.InfinityRecyclerViewScrollListener;
import uk.openvk.android.legacy.core.listeners.OnEndlessScrollListener;

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

    public void setOnRecyclerScrollListener(final InfinityRecyclerViewScrollListener listener) {
        try {
            if (this.listener != null) {
                removeOnScrollListener(this.listener);
            }
            addOnScrollListener(listener);
            this.listener = listener;
        } catch (Exception ignored) {

        }
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
