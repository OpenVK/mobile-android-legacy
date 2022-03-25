package uk.openvk.android.legacy;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

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

                if (angle >= 125 && angle <= 180 && ((AppActivity) ctx).getSlidingMenuState() == false) {
                    //right to left swipe
                    if(((AppActivity) ctx).getAnimationState() == true) {
                        ((AppActivity) ctx).openSlidingMenu();
                    }
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }

                if (angle >= -10 && angle <= 45 && ((AppActivity) ctx).getSlidingMenuState() == true) {
                    //right to left swipe
                    if(((AppActivity) ctx).getAnimationState() == true ) {
                        ((AppActivity) ctx).openSlidingMenu();
                    }
                    return super.onScroll(e1, e2, distanceX, distanceY);
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

                if (angle >= 135 && angle < 180) {
                    //right to left swipe
                    ((AppActivity) ctx).openSlidingMenu();
                    return super.onFling(e1, e2, velocityX, velocityY);
                }

                if (angle >= -135 && angle < -180) {
                    //right to left swipe
                    ((AppActivity) ctx).openSlidingMenu();
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            } catch(Exception ex) {

            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}