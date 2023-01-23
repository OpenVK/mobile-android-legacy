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

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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

    public static String GetSHA256Hash(String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.DEFAULT);
    }

}