package uk.openvk.android.legacy.ui.core.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import uk.openvk.android.legacy.R;

/**
 * File created by Dmitry on 14.02.2023.
 */

public class VideoPlayerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
    }
}
