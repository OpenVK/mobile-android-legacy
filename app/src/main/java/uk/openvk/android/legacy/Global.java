package uk.openvk.android.legacy;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

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
}