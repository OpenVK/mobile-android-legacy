package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

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