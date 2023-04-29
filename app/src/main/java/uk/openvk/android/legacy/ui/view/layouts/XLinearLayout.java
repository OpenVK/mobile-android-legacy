package uk.openvk.android.legacy.ui.view.layouts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.listeners.OnKeyboardStateListener;

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
  * Location:              vk.3.0.4.jar!\com\vkontakte\android\ui\XLinearLayout.class
  * Java compiler version: 6 (50.0)
  * JD-Core Version:       1.1.3
  */

public class XLinearLayout extends LinearLayout implements OnKeyboardStateListener {

  private OnKeyboardStateListener listener;
  private int prevh = -1;
  private int prevw = -1;
  public XLinearLayout(Context paramContext) {
    super(paramContext);
      LayoutInflater inflater = (LayoutInflater) paramContext
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.layout_blank, this, false);
  }
  
  public XLinearLayout(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
      LayoutInflater inflater = (LayoutInflater) paramContext
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.layout_blank, this, false);
  }

  public void setOnKeyboardStateListener(final OnKeyboardStateListener listener) {
      this.listener = listener;
      final int MIN_KEYBOARD_HEIGHT_PX = 150;
      final View decorView = ((Activity)getContext()).getWindow().getDecorView();
      decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          private final Rect windowVisibleDisplayFrame = new Rect();
          private int lastVisibleDecorViewHeight;

          @Override
          public void onGlobalLayout() {
              decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
              final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

              if (lastVisibleDecorViewHeight != 0) {
                  if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                      onKeyboardStateChanged(true);
                  } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                      onKeyboardStateChanged(false);
                  }
              }
              lastVisibleDecorViewHeight = visibleDecorViewHeight;
          }
      });
  }

    @Override
    public void onKeyboardStateChanged(boolean param1Boolean) {
        this.listener.onKeyboardStateChanged(param1Boolean);
    }
}