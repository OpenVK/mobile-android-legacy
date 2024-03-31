/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.core.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.TranslucentActivity;

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
        forceBrowserForExternalLinks();
        loadNote();
    }

    private void forceBrowserForExternalLinks() {
        final String instance = ((OvkApplication) getApplicationContext()).getCurrentInstance();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = view.getOriginalUrl();
                    if (url != null) {
                        if(url.startsWith("/")) {
                            url = String.format("http://%s%s", instance, url);
                        }
                        view.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null) {
                        if(url.startsWith("/")) {
                            url = String.format("http://%s%s", instance, url);
                        }
                        view.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
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
