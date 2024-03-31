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

package uk.openvk.android.legacy.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import uk.openvk.android.legacy.Global;

public class AlbumScrollView extends ScrollView {
    public int heightMSpec;
    public int widthMSpec;

    public AlbumScrollView(Context context) {
        super(context);
    }

    public AlbumScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override // android.widget.ScrollView, android.widget.FrameLayout, android.view.View
    public void onMeasure(int wms, int hms) {
        this.widthMSpec = wms;
        this.heightMSpec = hms;
        super.onMeasure(wms, hms);
    }

    @Override // android.widget.ScrollView, android.view.View
    protected float getTopFadingEdgeStrength() {
        return (float) 0.0;
    }

    @Override // android.widget.ScrollView, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        return isEnabled() && super.onTouchEvent(ev);
    }

    @Override // android.view.View
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(Math.min(Global.scale(20.0f), y));
    }
}
