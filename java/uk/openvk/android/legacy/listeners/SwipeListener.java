package uk.openvk.android.legacy.listeners;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import uk.openvk.android.legacy.activities.AppActivity;

public class SwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    public Context ctx;

    public SwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        ctx = context;
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                float angle;
                angle = (float) Math.toDegrees(Math.atan2(e1.getY() - e2.getY(), e2.getX() - e1.getX()));
                Log.d("Swipe", "Angle: " + angle);
                if(((AppActivity) ctx).menu_is_closed == false) {
                    ((AppActivity) ctx).openSlidingMenu();
                }
            } catch(Exception ex) {

            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float angle;
                angle = (float) Math.toDegrees(Math.atan2(e1.getY() - e2.getY(), e2.getX() - e1.getX()));
                Log.d("Swipe", "Angle: " + angle);

                if ((angle >= 125 && angle <= 180) || (angle >= -170)) {
                    //right to left swipe
                    if(((AppActivity) ctx).getAnimationState() == true) {
                        ((AppActivity) ctx).openSlidingMenu();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                } else if (angle >= -10 && angle <= 45) {
                    //right to left swipe
                    if(((AppActivity) ctx).getAnimationState() == true ) {
                        ((AppActivity) ctx).openSlidingMenu();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            } catch(Exception ex) {

            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}