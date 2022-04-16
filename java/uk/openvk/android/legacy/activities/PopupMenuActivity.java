package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.os.Bundle;

import uk.openvk.android.legacy.R;

public class PopupMenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_menu);
    }
}
