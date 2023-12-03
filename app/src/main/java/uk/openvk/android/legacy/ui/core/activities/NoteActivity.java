package uk.openvk.android.legacy.ui.core.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;

/* OPENVK LEGACY LICENSE NOTIFICATION
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

public class NoteActivity extends TranslucentActivity {
    private WebView webView;
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.attach_note));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        }
        webView = findViewById(R.id.webview);
        loadNote();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNote() {
        Bundle data = getIntent().getExtras();
        if(data != null) {
            if (data.containsKey("content")) {
                // Generate (X)HTML note layout to web document
                String page =
                        "<!DOCTYPE html>" +
                        "<html>" +
                        "   <head>" +
                        "       <meta name=\"http-equiv\" content=\"Content-type: text/html; " +
                        "charset=UTF-8\" charset=\"UTF-8\">" +
                        "   </head>" +
                        "   <body bgcolor=\"#d5e8fe\" style=\"margin: 0\">" +
                        "       <div>" +
                        data.getString("content") +
                        "       </div>" +
                        "   </body>" +
                        "</html>";
                WebSettings settings = webView.getSettings();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    settings.setPluginState(WebSettings.PluginState.ON);
                }
                settings.setSupportZoom(true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    webView.loadDataWithBaseURL(null, page, "text/html; charset=UTF-8", "UTF-8", null);
                } else {
                    webView.loadData(page, "text/html; charset=UTF-8", "UTF-8");
                }
            }
            if(data.containsKey("title")) {
                ((TextView) findViewById(R.id.note_title)).setText(data.getString("title"));
            }
            if(data.containsKey("author")) {
                ((TextView) findViewById(R.id.note_author)).setText(data.getString("author"));
            }
        } else {
            finish();
        }
    }
}
