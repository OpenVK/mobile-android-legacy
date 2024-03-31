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

package uk.openvk.android.legacy.ui.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

public class CenteredImageSpan extends ImageSpan {
  private WeakReference<Drawable> mDrawableRef;

  public CenteredImageSpan(Context context, final int drawableRes) {
    super(context, drawableRes);
  }

  @Override
  public int getSize(Paint paint, CharSequence text,
                     int start, int end,
                     Paint.FontMetricsInt fm) {
    Drawable d = getCachedDrawable();
    Rect rect = d.getBounds();

    if (fm != null) {
      Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
      // keep it the same as paint's fm
      fm.ascent = pfm.ascent;
      fm.descent = pfm.descent;
      fm.top = pfm.top;
      fm.bottom = pfm.bottom;
    }

    return rect.right;
  }

  @Override
  public void draw(@NonNull Canvas canvas, CharSequence text,
                   int start, int end, float x,
                   int top, int y, int bottom, @NonNull Paint paint) {
    Drawable b = getCachedDrawable();
    canvas.save();

    int drawableHeight = b.getIntrinsicHeight();
    int fontAscent = paint.getFontMetricsInt().ascent;
    int fontDescent = paint.getFontMetricsInt().descent;
    int transY = bottom - b.getBounds().bottom +  // align bottom to bottom
        (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center

    canvas.translate(x, transY);
    b.draw(canvas);
    canvas.restore();
  }

  // Redefined locally because it is a private member from DynamicDrawableSpan
  private Drawable getCachedDrawable() {
    WeakReference<Drawable> wr = mDrawableRef;
    Drawable d = null;

    if (wr != null)
      d = wr.get();

    if (d == null) {
      d = getDrawable();
      mDrawableRef = new WeakReference<>(d);
    }

    return d;
  }
}