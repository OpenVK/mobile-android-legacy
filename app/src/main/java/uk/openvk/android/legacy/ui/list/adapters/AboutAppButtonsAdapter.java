package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;

public class AboutAppButtonsAdapter extends BaseAdapter {
    private final SharedPreferences global_prefs;
    private final String instance;
    private final float xdpi;
    private Context ctx;
    ArrayList<Button> buttons;
    float dp;

    public AboutAppButtonsAdapter(final Context ctx) {
        this.ctx = ctx;
        this.global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.instance = global_prefs.getString("current_instance", "");
        dp = ctx.getResources().getDisplayMetrics().scaledDensity;
        xdpi = ctx.getResources().getDisplayMetrics().xdpi;
        buttons = new ArrayList<>();

        Button sourceCodeBtn = new Button(ctx);
        sourceCodeBtn.setText(ctx.getResources().getString(R.string.source_code));
        sourceCodeBtn.setBackgroundResource(R.drawable.btn_blue);
        sourceCodeBtn.setTextColor(ctx.getResources().getColor(R.color.white));
        sourceCodeBtn.setFocusable(false);
        sourceCodeBtn.setClickable(false);
        buttons.add(sourceCodeBtn);

        Button donateBtn = new Button(ctx);
        donateBtn.setText(ctx.getResources().getString(R.string.donate));
        donateBtn.setBackgroundResource(R.drawable.login_btn);
        donateBtn.setTextColor(ctx.getResources().getColor(R.color.auth_btn));
        donateBtn.setFocusable(false);
        donateBtn.setClickable(false);
        buttons.add(donateBtn);

        Button faqBtn = new Button(ctx);
        faqBtn.setText(ctx.getResources().getString(R.string.faq));
        faqBtn.setBackgroundResource(R.drawable.login_btn);
        faqBtn.setTextColor(ctx.getResources().getColor(R.color.auth_btn));
        faqBtn.setFocusable(false);
        faqBtn.setClickable(false);
        buttons.add(faqBtn);

        Button appsBtn = new Button(ctx);
        appsBtn.setText(ctx.getResources().getString(R.string.ovk_mobile_apps));
        appsBtn.setBackgroundResource(R.drawable.login_btn);
        appsBtn.setTextColor(ctx.getResources().getColor(R.color.auth_btn));
        appsBtn.setFocusable(false);
        appsBtn.setClickable(false);
        buttons.add(appsBtn);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Button getItem(int i) {
        return buttons.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = getItem(i);
        view.setLayoutParams(
                new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        i > 0 ? (int)(33 * dp) : (int)(32 * dp)
                )
        );

        if((dp * ctx.getResources().getDisplayMetrics().widthPixels) <= 760)
            ((Button) view).setTextSize(13);
        else
            ((Button) view).setTextSize(14);

        view.setPadding((int)(8.0 * dp), 0,  (int)(8.0 * dp), 0);
        return view;
    }
}
