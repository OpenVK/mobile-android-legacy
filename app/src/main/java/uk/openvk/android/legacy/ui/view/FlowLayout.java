package uk.openvk.android.legacy.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import uk.openvk.android.legacy.Global;

/* Original FlowLayout implementation from VK 3.x */

public class FlowLayout extends ViewGroup {
    static final boolean $assertionsDisabled;
    private Vector<Integer> lineHeights;
    private List<LayoutParams> lparams;
    private int measuredHeight;
    public int pwidth;

    static {
        $assertionsDisabled = !FlowLayout.class.desiredAssertionStatus();
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public boolean breakAfter;
        public boolean center;
        public boolean floating;
        public int height;
        public int horizontal_spacing;
        public int vertical_spacing;
        public int width;

        public LayoutParams(int horizontal_spacing, int vertical_spacing) {
            super(0, 0);
            this.horizontal_spacing = horizontal_spacing;
            this.vertical_spacing = vertical_spacing;
        }

        public LayoutParams() {
            super(0, 0);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp.width, lp.height);
        }
    }

    public FlowLayout(Context context) {
        super(context);
        this.lineHeights = new Vector<>();
        this.pwidth = Global.scale(5.0f);
        this.measuredHeight = 0;
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.lineHeights = new Vector<>();
        this.pwidth = Global.scale(5.0f);
        this.measuredHeight = 0;
    }

    public List<Rect> layoutWithParams(List<LayoutParams> params, int width, int height) {
        int child_width;
        this.lparams = params;
        ArrayList<Rect> result = new ArrayList<>();
        int count = params.size();
        int x_pos = getPaddingLeft();
        int y_pos = getPaddingTop();
        boolean breakNext = false;
        this.lineHeights.clear();
        int floatingH = 0;
        int line_height = 0;
        int max_width = 0;
        for (int i = 0; i < count; i++) {
            LayoutParams lp = params.get(i);
            int child_w2 = lp.width <= 0 ? width : lp.width;
            int child_height = lp.height;
            if (child_height < 0) {
                throw new IllegalArgumentException("Height should be constant");
            }
            if (breakNext || x_pos + child_w2 > this.pwidth + width) {
                x_pos = getPaddingLeft();
                y_pos += Math.max(line_height, floatingH);
                this.lineHeights.add(Math.max(line_height, floatingH));
                line_height = 0;
                floatingH = 0;
            }
            line_height = Math.max(line_height, lp.vertical_spacing + child_height);
            if (lp.floating) {
                y_pos += lp.vertical_spacing + child_height;
                floatingH += lp.vertical_spacing + child_height;
                max_width = Math.max(max_width, x_pos + child_w2);
            } else {
                floatingH = 0;
                x_pos += lp.horizontal_spacing + child_w2;
            }
            breakNext = lp.breakAfter;
            max_width = Math.max(max_width, x_pos - lp.horizontal_spacing);
        }
        if (line_height > 0) {
            this.lineHeights.add(line_height);
        }
        int xpos2 = getPaddingLeft();
        int ypos2 = getPaddingTop();
        int floatY = 0;
        boolean prevFloat = false;
        boolean breakNext2 = false;
        int line = 0;
        for (int i2 = 0; i2 < count; i2++) {
            LayoutParams lp2 = params.get(i2);
            if (lp2.width <= 0) {
                child_width = width;
            } else {
                child_width = lp2.width;
            }
            int childh2 = lp2.height;
            if (childh2 < 0) {
                throw new IllegalArgumentException("Height should be constant");
            }
            if (!lp2.floating && prevFloat) {
                ypos2 = floatY;
            }
            if (breakNext2 || xpos2 + child_width > this.pwidth + width) {
                xpos2 = getPaddingLeft();
                ypos2 += this.lineHeights.elementAt(line);
                line++;
            }
            if (lp2.center) {
                xpos2 = (getWidth() / 2) - (child_width / 2);
            }
            result.add(new Rect(xpos2, ypos2, xpos2 + child_width, ypos2 + childh2));
            if (lp2.floating) {
                if (!prevFloat) {
                    floatY = ypos2;
                    prevFloat = true;
                }
                ypos2 += lp2.vertical_spacing + childh2;
            } else {
                prevFloat = false;
                xpos2 += lp2.horizontal_spacing + child_width;
            }
            breakNext2 = lp2.breakAfter;
        }
        this.measuredHeight = getFullHeight();
        return result;
    }

    public int getFullHeight() {
        int h = 0;
        for (Integer lineHeight : this.lineHeights) {
            int i = lineHeight;
            h += i;
        }
        return h;
    }

    public void resetParams() {
        this.lparams = null;
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childHeightMeasureSpec;
        int childh;
        if (!$assertionsDisabled
                && View.MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            throw new AssertionError();
        }
        int width =
                (View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight();
        int height =
                (View.MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()) - getPaddingBottom();
        int count = getChildCount();
        int line_height = 0;
        int x_pos = getPaddingLeft();
        int y_pos = getPaddingTop();
        int max_width = 0;
        if (View.MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED);
        } else {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        this.lineHeights.clear();
        boolean breakNext = false;
        int floatingH = 0;
        int i = 0;
        while (true) {
            if (i >= Math.max(count, this.lparams != null ? this.lparams.size() : 0)) {
                break;
            }
            View child = getChildAt(i);
            if ((child != null && child.getVisibility() != GONE) || child == null) {
                @SuppressLint("DrawAllocation")
                LayoutParams lp = child != null ? new LayoutParams(child.getLayoutParams()) : this.lparams.get(i);
                if (child != null) {
                    child.measure(
                            lp.width <= 0 ?
                                    View.MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED) :
                                    View.MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                            childHeightMeasureSpec);
                }
                int childw = lp.width <= 0 ? child != null ? child.getMeasuredWidth() : width : lp.width;
                if (lp.height <= 0) {
                    childh = child != null ? child.getMeasuredHeight() : 0;
                } else {
                    childh = lp.height;
                }
                if (breakNext || x_pos + childw > this.pwidth + width) {
                    x_pos = getPaddingLeft();
                    y_pos += Math.max(line_height, floatingH);
                    this.lineHeights.add(Math.max(line_height, floatingH));
                    line_height = 0;
                    floatingH = 0;
                }
                line_height = Math.max(line_height, lp.vertical_spacing + childh);
                if (lp.floating) {
                    y_pos += lp.vertical_spacing + childh;
                    floatingH += lp.vertical_spacing + childh;
                    max_width = Math.max(max_width, x_pos + childw);
                } else {
                    floatingH = 0;
                    x_pos += lp.horizontal_spacing + childw;
                }
                breakNext = lp.breakAfter;
                max_width = Math.max(max_width, x_pos - lp.horizontal_spacing);
            }
            i++;
        }
        if (View.MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = Math.max(line_height, floatingH);
            for (Integer lineHeight : this.lineHeights) {
                int h = lineHeight;
                height += h;
            }
        } else if (View.MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED
                && y_pos + line_height < height) {
            height = line_height;
            for (Integer h2 : this.lineHeights) {
                height += h2;
            }
        }
        if (View.MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(max_width, height);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(Global.scale(2.0f), Global.scale(2.0f));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int width = r - l;
        int x_pos = getPaddingLeft();
        int y_pos = getPaddingTop();
        int line = 0;
        boolean breakNext = false;
        boolean prevFloat = false;
        int floatY = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                @SuppressLint("DrawAllocation")
                LayoutParams lp = new LayoutParams(child.getLayoutParams());
                int childw = lp.width <= 0 ? child.getMeasuredWidth() : lp.width;
                int childh = lp.height <= 0 ? child.getMeasuredHeight() : lp.height;
                if (!lp.floating && prevFloat) {
                    y_pos = floatY;
                }
                if (breakNext || x_pos + childw > this.pwidth + width) {
                    x_pos = getPaddingLeft();
                    y_pos += this.lineHeights.elementAt(line);
                    line++;
                }
                if (lp.center) {
                    x_pos = (getWidth() / 2) - (childw / 2);
                }
                child.layout(x_pos, y_pos, x_pos + childw, y_pos + childh);

                if (lp.floating) {
                    if (!prevFloat) {
                        floatY = y_pos;
                        prevFloat = true;
                    }
                    y_pos += lp.vertical_spacing + childh;
                } else {
                    prevFloat = false;
                    x_pos += lp.horizontal_spacing + childw;
                }
                breakNext = lp.breakAfter;
            }
        }
    }
}
