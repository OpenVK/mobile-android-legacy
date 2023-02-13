package uk.openvk.android.legacy.ui.view;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;

import com.rockerhieu.emojicon.EmojiconTextView;

public class TightTextView extends EmojiconTextView {
    public TightTextView(Context context) {
        super(context);
    }

    public TightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specModeW = MeasureSpec.getMode(widthMeasureSpec);
        if (specModeW != MeasureSpec.EXACTLY) {
            Layout layout = getLayout();
            if (layout != null) {
                int w = (int) Math.ceil(getMaxLineWidth(layout)) + getCompoundPaddingLeft() + getCompoundPaddingRight();
                if (w < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                            heightMeasureSpec);
                }
            }
        }
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }
}