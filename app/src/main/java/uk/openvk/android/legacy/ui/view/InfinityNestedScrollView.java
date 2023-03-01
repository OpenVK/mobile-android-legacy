package uk.openvk.android.legacy.ui.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.widget.ScrollView;

import uk.openvk.android.legacy.ui.core.listeners.OnNestedScrollListener;
import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;

/**
 * File created by Dmitry on 13.02.2023.
 */

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
