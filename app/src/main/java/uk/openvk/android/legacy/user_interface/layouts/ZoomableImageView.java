package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ZoomableImageView extends ImageView {
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
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        photoAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                l.onClick(getRootView());
            }

            @Override
            public void onOutsidePhotoTap() {

            }
        });
    }
}
