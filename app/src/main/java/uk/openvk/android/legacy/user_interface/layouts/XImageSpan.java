package uk.openvk.android.legacy.user_interface.layouts;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

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


/* Location:              C:\Users\Dmitry\vk.3.0.4.jar!\com\vkontakte\androi\\ui\XImageSpan.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */