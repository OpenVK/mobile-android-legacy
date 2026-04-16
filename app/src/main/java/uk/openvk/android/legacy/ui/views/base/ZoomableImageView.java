/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.openvk.android.legacy.OvkApplication;

@SuppressLint("AppCompatCustomView")
public class ZoomableImageView extends ImageView {
    private float userScale;
    private PhotoViewAttacher photoAttacher;
    private Matrix suppMatrix;

    public ZoomableImageView(Context context) {
        super(context);
    }

    public ZoomableImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void enablePinchToZoom(Window window){
        if(photoAttacher != null) {
            photoAttacher.update();
        } else {
            photoAttacher = new PhotoViewAttacher(this);
        }
        photoAttacher.setMaximumScale(8);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    //adjustZoom();
                }
            });
        }

        photoAttacher.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {
            @Override
            public void onScaleChange(float v, float v1, float v2) {
                userScale = v;
            }
        });
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        photoAttacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                assert l != null;
                l.onClick(getRootView());
                userScale = photoAttacher.getScale();
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

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if(photoAttacher != null) {
            if (((OvkApplication) getContext().getApplicationContext()).isTablet ||
                    (bm.getWidth() < 1536 && bm.getHeight() < 1024)) {
                photoAttacher.setMaximumScale(6);
            } else {
                photoAttacher.setMaximumScale(8);
            }
        }
    }

    public void rescale() {
        photoAttacher.setScale(userScale, false);
    }

    public PhotoViewAttacher getAttacher() {
        return photoAttacher;
    }

    public float getUserScale() {
        return userScale;
    }
}
