package uk.openvk.android.legacy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class Global {

    private Context ctx;

    public Global() {

    }

    public Global(Context ctx) {
        this.ctx = ctx;
    }

    public static int scale(float dip) {
        return Math.round(dip);
    }

    public boolean isTablet() {
        if(ctx != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                DisplayMetrics dismetrics = ctx.getResources().getDisplayMetrics();
                float dpWidth = dismetrics.widthPixels / dismetrics.density;
                if (dpWidth >= 600) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public long getHeapSize() {
        Runtime rt = Runtime.getRuntime();
        return rt.maxMemory();
    }

    public static float getSmalledWidth(WindowManager wm) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        int widthPixels = 0;
        int heightPixels = 0;
        if(Build.VERSION.SDK_INT >= 14) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                widthPixels = (Integer) mGetRawW.invoke(display);
                heightPixels = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                widthPixels = display.getWidth();
                heightPixels= display.getHeight();
            }
        } else {
            widthPixels = 0;
            heightPixels = 0;
        }
        float scaleFactor = metrics.density;
        float widthDp = 0;
        float heightDp = 0;
        if(scaleFactor > 0) {
            widthDp = (float) widthPixels / scaleFactor;
            heightDp = (float) heightPixels / scaleFactor;
        } else {
            widthDp = (float) widthPixels;
            heightDp = (float) heightPixels;
        }
        return Math.min(widthDp, heightDp);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String GetSHA256Hash(String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
            return Base64.encodeToString(digest, Base64.DEFAULT);
        } else {
            try {
                return bytesToHex(digest);
            } catch(Exception ex) {
                return "";
            }
        }
    }

    public static String formatLinksAsHtml(String subtitle) {
        return subtitle;
    }
}