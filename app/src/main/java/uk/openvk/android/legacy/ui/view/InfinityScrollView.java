package uk.openvk.android.legacy.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;

/**
 * File created by Dmitry on 13.02.2023.
 */

public class InfinityScrollView extends ScrollView {

    private OnScrollListener onScrollListener;

    public InfinityScrollView(Context context) {
        super(context);
    }

    public InfinityScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrollListener(OnScrollListener scrollListener) {
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
