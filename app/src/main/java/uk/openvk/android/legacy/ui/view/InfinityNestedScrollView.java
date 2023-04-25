package uk.openvk.android.legacy.ui.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.widget.ScrollView;

import uk.openvk.android.legacy.ui.core.listeners.OnNestedScrollListener;
import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;

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

public class InfinityNestedScrollView extends NestedScrollView {

    private OnNestedScrollListener onScrollListener;

    public InfinityNestedScrollView(Context context) {
        super(context);
    }

    public InfinityNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrollListener(OnNestedScrollListener scrollListener) {
        this.onScrollListener = scrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(onScrollListener != null) {
            onScrollListener.onScroll(this, l, t, oldl, oldt);
        }
    }
}
