package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import uk.openvk.android.legacy.Global;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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

 /* Reverse-engineered from VK 3.0.4
  * Location:              vk.3.0.4.apk/com/vkontakte/android/ui/FlowLayout.class
  * Java compiler version: 6 (50.0)
  * JD-Core Version:       1.1.3
  */

public class FlowLayout extends ViewGroup {
  private Vector<Integer> lineHeights = new Vector<Integer>();
  
  private List<LayoutParams> lparams;
  
  private int measuredHeight = 0;
  
  public int pwidth = Global.scale(5.0F);
  
  static {
    boolean bool;
    if (!FlowLayout.class.desiredAssertionStatus()) {
      bool = true;
    } else {
      bool = false;
    }
      boolean assertionsDisabled = bool;
  }
  
  public FlowLayout(Context paramContext) {
    super(paramContext);
  }
  
  public FlowLayout(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  protected boolean checkLayoutParams(ViewGroup.LayoutParams paramLayoutParams) {
    return (paramLayoutParams instanceof LayoutParams);
  }
  
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(Global.scale(2.0F), Global.scale(2.0F));
  }
  
  public int getFullHeight() {
    int i = 0;
    Iterator<Integer> iterator = this.lineHeights.iterator();
    while (true) {
      if (!iterator.hasNext())
        return i; 
      i += ((Integer)iterator.next()).intValue();
    } 
  }
  
  public void layoutWithParams(List<LayoutParams> paramList, int paramInt1, int paramInt2) {
      // not implemented, bytecode can't convert to Java code
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    // not implemented, bytecode can't convert to Java code
  }
  
  protected void onMeasure(int paramInt1, int paramInt2) {

  }
  
  public void resetParams() {
    this.lparams = null;
  }
  
  public static class LayoutParams extends ViewGroup.LayoutParams {
    public boolean breakAfter;
    
    public boolean center;
    
    public boolean floating;
    
    public int height;
    
    public int horizontal_spacing;
    
    public int vertical_spacing;
    
    public int width;
    
    public LayoutParams() {
      super(0, 0);
    }
    
    public LayoutParams(int param1Int1, int param1Int2) {
      super(0, 0);
      this.horizontal_spacing = param1Int1;
      this.vertical_spacing = param1Int2;
    }
  }
}