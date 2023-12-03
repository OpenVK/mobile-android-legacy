package uk.openvk.android.legacy.ui.core.methods;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import uk.openvk.android.legacy.ui.core.activities.AboutApplicationActivity;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy */

public class CustomLinkMovementMethod extends LinkMovementMethod {
    private final String TAG = CustomLinkMovementMethod.class.getSimpleName();

    private OnTextViewClickMovementListener mListener;
    private final GestureDetector mGestureDetector;
    private TextView mWidget;
    private Spannable mBuffer;

    public CustomLinkMovementMethod(Context ctx) {
        mGestureDetector = new GestureDetector(ctx, new SimpleOnGestureListener());
    }

    public enum LinkType {

        /** Indicates that phone link was clicked */
        PHONE,

        /** Identifies that URL was clicked */
        WEB_URL,

        /** Identifies that Email Address was clicked */
        EMAIL_ADDRESS,

        /** Indicates that none of above mentioned were clicked */
        NONE
    }

    /**
     * Interface used to handle Long clicks on the {@link TextView} and taps
     * on the phone, web, mail links inside of {@link TextView}.
     */
    public interface OnTextViewClickMovementListener {

        /**
         * This method will be invoked when user press and hold
         * finger on the {@link TextView}
         *
         * @param linkText Text which contains link on which user presses.
         * @param linkType Type of the link can be one of {@link LinkType} enumeration
         */
        void onLinkClicked(final String linkText, final LinkType linkType);

        /**
         *
         * @param text Whole text of {@link TextView}
         */
        void onLongClick(final String text);
    }


    public CustomLinkMovementMethod(final OnTextViewClickMovementListener listener, final Context ctx) {
        mListener        = listener;
        mGestureDetector = new GestureDetector(ctx, new SimpleOnGestureListener());
    }

    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {

        mWidget = widget;
        mBuffer = buffer;
        mGestureDetector.onTouchEvent(event);

        return false;
    }

    /**
     * Detects various gestures and events.
     * Notify users when a particular motion event has occurred.
     */
    class SimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            // Notified when a tap occurs.
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Notified when a long press occurs.
            final String text = mBuffer.toString();

            if (mListener != null) {
                mListener.onLongClick(text);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // Notified when tap occurs.
            final String linkText = getLinkText(mWidget, mBuffer, event);

            LinkType linkType = LinkType.NONE;

            if (Patterns.PHONE.matcher(linkText).matches()) {
                linkType = LinkType.PHONE;
            }
            else if (Patterns.WEB_URL.matcher(linkText).matches()) {
                linkType = LinkType.WEB_URL;
            }
            else if (Patterns.EMAIL_ADDRESS.matcher(linkText).matches()) {
                linkType = LinkType.EMAIL_ADDRESS;
            }

            if (mListener != null) {
                mListener.onLinkClicked(linkText, linkType);
            }

            return false;
        }

        private String getLinkText(final TextView widget, final Spannable buffer, final MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                return buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString();
            }

            return "";
        }
    }

    public void setOnLinkClickListener(OnTextViewClickMovementListener listener) {
        mListener = listener;
    }
}
