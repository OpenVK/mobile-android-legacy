package uk.openvk.android.legacy.ui.views.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

import uk.openvk.android.legacy.OvkApplication;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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
  * Location:              vk.3.0.4.apk/com/vkontakte/android/ui/XEditText.class
  * Java compiler version: 6 (50.0)
  * JD-Core Version:       1.1.3
  */

@SuppressLint("AppCompatCustomView")
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
    Log.i(OvkApplication.APP_TAG, "keyEvent " + paramKeyEvent.getKeyCode() + " | " + paramKeyEvent.getMetaState());
    return super.onKeyDown(paramInt, paramKeyEvent);
  }
}