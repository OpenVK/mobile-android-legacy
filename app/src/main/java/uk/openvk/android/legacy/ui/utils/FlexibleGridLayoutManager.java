package uk.openvk.android.legacy.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class FlexibleGridLayoutManager extends GridLayoutManager {
    // Dummy column count just to supply some value to the super constructor
    private static final int FAKE_COUNT = 1;

    @Nullable
    private ColumnCountProvider columnCountProvider;

    public interface ColumnCountProvider {
        int getColumnCount(int recyclerViewWidth);
    }

    public static class DefaultColumnCountProvider implements ColumnCountProvider {
        @NonNull
        private final Context context;

        public DefaultColumnCountProvider(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public int getColumnCount(int recyclerViewWidth) {
            return columnsForWidth(context, recyclerViewWidth);
        }

        public static int columnsForWidth(Context context, int widthPx) {
            int widthDp = dpFromPx(context, widthPx);
            if (widthDp >= 900) {
                return 5;
            } else if (widthDp >= 720) {
                return 4;
            } else if (widthDp >= 600) {
                return 3;
            } else if (widthDp >= 480) {
                return 2;
            } else if (widthDp >= 320) {
                return 2;
            } else {
                return 2;
            }
        }

        public static int dpFromPx(Context context, float px) {
            return (int)(px / context.getResources().getDisplayMetrics().density + 0.5f);
        }
    }

    public FlexibleGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FlexibleGridLayoutManager(Context context) {
        super(context, FAKE_COUNT);
    }

    public FlexibleGridLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, FAKE_COUNT, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler,
                                 RecyclerView.State state) {
        updateSpanCount(getWidth());
        super.onLayoutChildren(recycler, state);
    }

    private void updateSpanCount(int width) {
        if (columnCountProvider != null) {
            int spanCount = columnCountProvider.getColumnCount(width);
            setSpanCount(spanCount > 0 ? spanCount : 1);
        }
    }

    public void setColumnCountProvider(@Nullable ColumnCountProvider provider) {
        this.columnCountProvider = provider;
    }
}
