package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FixedSizeFrameLayout extends FrameLayout {
  private int h;
  
  private int w;
  
  public FixedSizeFrameLayout(Context paramContext) {
    super(paramContext);
  }
  
  public FixedSizeFrameLayout(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  public FixedSizeFrameLayout(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
  }
  
  public void onMeasure(int paramInt1, int paramInt2) {
    super.onMeasure(this.w | 0x40000000, this.h | 0x40000000);
  }
  
  public void setSize(int paramInt1, int paramInt2) {
    this.w = paramInt1;
    this.h = paramInt2;
    requestLayout();
  }
}


/* Location:              C:\Users\Dmitry\vk.3.0.4.jar!\com\vkontakte\androi\\ui\FixedSizeFrameLayout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */