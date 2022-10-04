package uk.openvk.android.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import uk.openvk.android.legacy.activities.AuthActivity;
import uk.openvk.android.legacy.activities.MainSettingsActivity;

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
            DisplayMetrics dismetrics = ctx.getResources().getDisplayMetrics();
            float dpWidth = dismetrics.widthPixels / dismetrics.density;
            if(dpWidth >= 600) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}