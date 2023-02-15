package uk.openvk.android.legacy.user_interface.view.layouts;

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
