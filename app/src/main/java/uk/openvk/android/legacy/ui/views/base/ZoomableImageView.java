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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

@SuppressLint("AppCompatCustomView")
public class ZoomableImageView extends ImageView {
    private float userScale;
    private PhotoViewAttacher photoAttacher;

    public ZoomableImageView(Context context) {
        super(context);
    }

    public ZoomableImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void enablePinchToZoom(){
        if(photoAttacher != null) {
            photoAttacher.update();
        } else {
            photoAttacher = new PhotoViewAttacher(this);
        }
        userScale = photoAttacher.getScale();
        photoAttacher.setMaximumScale(8);
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        photoAttacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                userScale = photoAttacher.getScale();
                assert l != null;
                l.onClick(getRootView());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(photoAttacher.getScale() == photoAttacher.getMinimumScale()) {
                    photoAttacher.setScale(photoAttacher.getMediumScale(), true);
                } else if(photoAttacher.getScale() == photoAttacher.getMediumScale()) {
                    photoAttacher.setScale(photoAttacher.getMaximumScale(), true);
                } else {
                    photoAttacher.setScale(photoAttacher.getMinimumScale(), true);
                }
                userScale = photoAttacher.getScale();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
    }

    public void rescale() {
        photoAttacher.setScale(userScale, false);
    }
}
