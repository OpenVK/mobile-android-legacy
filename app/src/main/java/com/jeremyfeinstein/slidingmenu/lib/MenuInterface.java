/*
 * Copyright 2012-2014 Jeremy Feinstein
 * Copyright 2026 Dmitry Tretyakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jeremyfeinstein.slidingmenu.lib;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public interface MenuInterface {

	public abstract void scrollBehindTo(int x, int y, 
			CustomViewBehind cvb, float scrollScale);
	
	public abstract int getMenuLeft(CustomViewBehind cvb, View content);
	
	public abstract int getAbsLeftBound(CustomViewBehind cvb, View content);

	public abstract int getAbsRightBound(CustomViewBehind cvb, View content);

	public abstract boolean marginTouchAllowed(View content, int x, int threshold);
	
	public abstract boolean menuOpenTouchAllowed(View content, int currPage, int x);
	
	public abstract boolean menuTouchInQuickReturn(View content, int currPage, int x);
	
	public abstract boolean menuClosedSlideAllowed(int x);
	
	public abstract boolean menuOpenSlideAllowed(int x);
	
	public abstract void drawShadow(Canvas canvas, Drawable shadow, int width);
	
	public abstract void drawFade(Canvas canvas, int alpha, 
			CustomViewBehind cvb, View content);
	
	public abstract void drawSelector(View content, Canvas canvas, float percentOpen);
	
}
