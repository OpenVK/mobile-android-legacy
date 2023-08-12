package uk.openvk.android.legacy.ui.view.layouts;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

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
  * Location:             vk.3.0.4.apk/com/vkontakte/android/ui/XImageSpan.class
  * Java compiler version: 6 (50.0)
  * JD-Core Version:       1.1.3
  */

public class XImageSpan extends ImageSpan {
  public XImageSpan(Drawable paramDrawable, int paramInt) {
    super(paramDrawable, paramInt);
  }
  
  public int getSize(Paint paramPaint, CharSequence paramCharSequence, int paramInt1, int paramInt2, Paint.FontMetricsInt paramFontMetricsInt) {
    paramInt1 = super.getSize(paramPaint, paramCharSequence, paramInt1, paramInt2, paramFontMetricsInt);
    if (paramFontMetricsInt != null) {
      paramFontMetricsInt.ascent = (int)paramPaint.ascent();
      paramFontMetricsInt.descent = (int)paramPaint.descent();
      paramFontMetricsInt.bottom = (int)paramPaint.descent();
    } 
    return paramInt1;
  }
}