/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.views.base;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

 /* Reverse-engineered from VK 3.0.4
  * Location:             vk.3.0.4.apk/com/vkontakte/android/ui/XImageSpan.class
  * Java compiler version: 6 (50.0)
  * JD-Core Version:       1.1.3
  */

public class XImageSpan extends ImageSpan {
  public XImageSpan(Drawable paramDrawable, int paramInt) {
    super(paramDrawable, paramInt);
  }
  
  public int getSize(Paint paramPaint, CharSequence paramCharSequence, int param1, int param2, Paint.FontMetricsInt paramFontMetricsInt) {
    param1 = super.getSize(paramPaint, paramCharSequence, param1, param2, paramFontMetricsInt);
    if (paramFontMetricsInt != null) {
      paramFontMetricsInt.ascent = (int)paramPaint.ascent();
      paramFontMetricsInt.descent = (int)paramPaint.descent();
      paramFontMetricsInt.bottom = (int)paramPaint.descent();
    } 
    return param1;
  }
}