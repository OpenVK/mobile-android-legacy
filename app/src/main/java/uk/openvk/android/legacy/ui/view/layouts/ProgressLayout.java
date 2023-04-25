package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import uk.openvk.android.legacy.R;

/**
 * Created by Dmitry on 28.09.2022.
 */
public class ProgressLayout extends LinearLayout {
    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_progress, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void enableDarkTheme() {
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_light));
    }
}
