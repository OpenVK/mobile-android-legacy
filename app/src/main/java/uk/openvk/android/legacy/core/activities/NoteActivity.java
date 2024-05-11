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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.core.methods.CustomLinkMovementMethod;

public class NoteActivity extends NetworkActivity {
    private WebView webView;
    private SharedPreferences global_prefs;
    private String page;
    private long note_id;
    private Menu menu;
    private boolean editor_mode;
    private long owner_id;


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
        ((TextView) findViewById(R.id.note_some_xhtml_features)).setText(
                Html.fromHtml(getResources().getString(R.string.some_xhtml_features_text))
        );
        webView = findViewById(R.id.webview);
        Bundle data = getIntent().getExtras();
        if(data != null) {
            if (data.containsKey("id")) {
                note_id = data.getLong("id");
            } else {
                finish();
            }

            if (data.containsKey("owner_id")) {
                owner_id = data.getLong("owner_id");
            } else {
                finish();
            }
            ovk_api.notes.getById(ovk_api.wrapper, owner_id, note_id);
            findViewById(R.id.note_viewer).setVisibility(View.GONE);
            findViewById(R.id.note_editor).setVisibility(View.GONE);
            findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        } else {
            finish();
        }
        forceBrowserForExternalLinks();
    }

    private void forceBrowserForExternalLinks() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webView.setWebViewClient(new WebViewClient() {
                @SuppressLint("NewApi")
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url;
                    if(request.getUrl() != null) {
                        url = request.getUrl().toString();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                    }
                    return true;
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if(url != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if(item.getItemId() == R.id.edit){
            switchToEditorMode(true);
        } else if(item.getItemId() == R.id.note_save) {
            switchToEditorMode(false);
            String new_title = ((EditText) findViewById(R.id.note_title_editor)).getText().toString();
            String new_content = ((EditText) findViewById(R.id.note_content_editor)).getText().toString();
            ovk_api.notes.edit(ovk_api.wrapper, note_id, new_title, new_content);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNote(String text) {
        Bundle data = getIntent().getExtras();
        String instance = ((OvkApplication) getApplication()).getCurrentInstance();
        if(data != null) {
            if (data.containsKey("content")) {
                // Generate (X)HTML note layout to web document
                page =
                        "<!DOCTYPE html>" +
                        "<html>" +
                        "   <head>" +
                        "       <meta name=\"http-equiv\" content=\"Content-type: text/html; " +
                        "charset=UTF-8\" charset=\"UTF-8\">" +
                        "   </head>" +
                        "   <body bgcolor=\"#d5e8fe\" style=\"margin: 0\">" +
                        "       <div>" +
                                text
                                    .replace("&amp;", "&")
                                    .replace("<a href=\"/",
                                            String.format("<a href=\"http://%s/", instance)
                                    ) +
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

                ((EditText) findViewById(R.id.note_content_editor)).setText(data.getString("content"));
            }
            if(data.containsKey("title")) {
                ((TextView) findViewById(R.id.note_title)).setText(data.getString("title"));
                ((EditText) findViewById(R.id.note_title_editor)).setText(data.getString("title"));
            }
            if(data.containsKey("author")) {
                ((TextView) findViewById(R.id.note_author)).setText(data.getString("author"));
                ((TextView) findViewById(R.id.note_author_2)).setText(data.getString("author"));
            }
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(editor_mode) {
            switchToEditorMode(false);
        } else {
            finish();
        }
    }

    public void switchToEditorMode(boolean value) {
        editor_mode = value;
        menu.findItem(R.id.edit).setVisible(!value);
        menu.findItem(R.id.note_save).setVisible(value);
        findViewById(R.id.note_viewer).setVisibility(value ? View.GONE : View.VISIBLE);
        findViewById(R.id.note_editor).setVisibility(value ? View.VISIBLE : View.GONE);
    }

    public void receiveState(int message, Bundle data) {
        try {
            if (data.containsKey("address")) {
                String activityName = data.getString("address");
                if (activityName == null) {
                    return;
                }
                boolean isCurrentActivity = activityName.equals(
                        String.format("%s_%s", getLocalClassName(), getSessionId())
                );
                if (!isCurrentActivity) {
                    return;
                }
            }
            if (message == HandlerMessages.NOTES_GET_BY_ID) {
                findViewById(R.id.progress_layout).setVisibility(View.GONE);
                loadNote(ovk_api.notes.list.get(0).content);
                switchToEditorMode(false);
                ((TextView) findViewById(R.id.note_title)).setText(ovk_api.notes.list.get(0).title);
                ((EditText) findViewById(R.id.note_title_editor)).setText(ovk_api.notes.list.get(0).title);
            } else if (message == HandlerMessages.NOTES_EDIT) {
                ovk_api.notes.getById(ovk_api.wrapper, owner_id, note_id);
                switchToEditorMode(editor_mode);
            } else if (message < 0) {
                Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
