package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class XEditText extends EditText {
  public XEditText(Context paramContext) {
    super(paramContext);
  }
  
  public XEditText(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
  }
  
  public XEditText(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
  }
  
  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
    Log.i("vk", "keyEvent " + paramKeyEvent.getKeyCode() + " | " + paramKeyEvent.getMetaState());
    return super.onKeyDown(paramInt, paramKeyEvent);
  }
}


/* Location:              C:\Users\Dmitry\vk.3.0.4.jar!\com\vkontakte\androi\\ui\XEditText.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */