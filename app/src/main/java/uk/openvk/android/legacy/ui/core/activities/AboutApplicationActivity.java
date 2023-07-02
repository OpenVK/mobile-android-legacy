package uk.openvk.android.legacy.ui.core.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;
import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.ui.core.methods.CustomLinkMovementMethod;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class AboutApplicationActivity extends TranslucentActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private int logo_longclicks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_application);
        instance_prefs = getSharedPreferences("instance", 0);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.menu_about));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setHomeAction(new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return 0;
                }

                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            actionBar.setTitle(getResources().getString(R.string.menu_about));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
        }

        setView();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setView() {
        ImageView logo = findViewById(R.id.logo);
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(logo_longclicks == 2) {
                    Intent intent = new Intent(getApplicationContext(), DebugMenuActivity.class);
                    startActivity(intent);
                    logo_longclicks = 0;
                } else {
                    logo_longclicks++;
                }
                return false;
            }
        });

        ScrollView scrollView = findViewById(R.id.about_scrollview);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        TextView app_title = findViewById(R.id.app_title);
        TextView app_version_label = findViewById(R.id.app_version_text);
        TextView app_author_label = findViewById(R.id.app_author_text);
        TextView app_design_label = findViewById(R.id.app_design_text);
        TextView app_devteam_label = findViewById(R.id.app_development_text);
        TextView app_links = findViewById(R.id.app_links_text);
        TextView app_license_label = findViewById(R.id.app_license_text);
        TextView app_disclaimer_label = findViewById(R.id.app_disclaimer_text);
        OvkApplication app = ((OvkApplication) getApplicationContext());

        app_title.setText(getResources().getString(R.string.full_app_name));
        app_version_label.setText(getResources().getString(R.string.app_version_text,
                BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT));
        if(instance_prefs.getString("server", "").equals("openvk.su") ||
                instance_prefs.getString("server", "").equals("openvk.uk") ||
                instance_prefs.getString("server", "").equals("openvk.co")) {
            app_author_label.setText(Html.fromHtml(
                    getResources().getString(R.string.app_author_value, "openvk://profile")));
            app_devteam_label.setText(Html.fromHtml(
                    getResources().getString(R.string.app_devteam,
                            "openvk://profile", "openvk://profile")));
            app_links.setText(Html.fromHtml(
                    getResources().getString(R.string.app_links_text,
                    "openvk://group", String.format("http://%s", instance_prefs.getString("server", "")))));
            app_disclaimer_label.setText(Html.fromHtml(getResources().getString(
                    R.string.app_disclaimer,
                    String.format("http://%s", instance_prefs.getString("server", "")))));
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                app_author_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_author_value,
                                "https://openvk.uk/")));
                app_devteam_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_devteam,
                                "https://openvk.uk/", "https://openvk.uk/")));
                app_links.setText(Html.fromHtml(
                        getResources().getString(R.string.app_links_text,
                                "https://openvk.uk", "https://openvk.uk")));
                app_disclaimer_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_disclaimer,
                        "https://openvk.uk")));
            } else {
                app_author_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_author_value,
                                "http://openvk.co/")));
                app_devteam_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_devteam,
                                "http://openvk.co/", "http://openvk.co/")));
                app_links.setText(Html.fromHtml(
                        getResources().getString(R.string.app_links_text,
                                "http://openvk.co", "http://openvk.co")));
                app_disclaimer_label.setText(Html.fromHtml(
                        getResources().getString(R.string.app_disclaimer,
                        "http://openvk.co")));
            }
        }
        app_design_label.setText(Html.fromHtml(getResources().getString(R.string.app_design_value)));
        app_license_label.setText(Html.fromHtml(getResources().getString(R.string.app_license_text)));

        app_links.setMovementMethod(LinkMovementMethod.getInstance());
        app_author_label.setMovementMethod(LinkMovementMethod.getInstance());
        app_design_label.setMovementMethod(LinkMovementMethod.getInstance());
        app_devteam_label.setMovementMethod(LinkMovementMethod.getInstance());
        CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod(this);
        linkMovementMethod.setOnLinkClickListener(
                new CustomLinkMovementMethod.OnTextViewClickMovementListener() {
                    @Override
                    public void onLinkClicked(String linkText, CustomLinkMovementMethod.LinkType linkType) {
                        openWebViewDialog(linkText);
                    }

                    @Override
                    public void onLongClick(String text) {

                    }
        });
        app_license_label.setMovementMethod(linkMovementMethod);
        app_disclaimer_label.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openWebViewDialog(String url) {
        View webviewLayout = getLayoutInflater().inflate(R.layout.layout_web, null, false);
        WebView page = webviewLayout.findViewById(R.id.webview);
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        int mobile = ConnectivityManager.TYPE_MOBILE;
        int wifi = ConnectivityManager.TYPE_WIFI;

        boolean network_avaliable = (
                connectivityManager.getNetworkInfo(mobile).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(wifi).getState() == NetworkInfo.State.CONNECTED);
        page.loadUrl(url);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(webviewLayout);
        OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.app_license_title), "", page);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Global.fixWindowPadding(findViewById(R.id.about_layout), getTheme());
    }
}
