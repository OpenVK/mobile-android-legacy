package uk.openvk.android.legacy.ui.core.listeners;

import uk.openvk.android.legacy.ui.view.InfinityScrollView;

/**
 * File created by Dmitry on 13.02.2023.
 */

public interface OnScrollListener {
    void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y);
}
