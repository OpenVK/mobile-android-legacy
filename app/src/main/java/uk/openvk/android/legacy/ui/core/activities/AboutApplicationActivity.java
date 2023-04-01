package uk.openvk.android.legacy.ui.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/**
 * Created by Dmitry on 13.10.2022.
 */

public class AboutApplicationActivity extends Activity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private int logo_longclicks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_application_layout);
        instance_prefs = getSharedPreferences("instance", 0);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.menu_about));
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
        OvkApplication app = ((OvkApplication) getApplicationContext());

        app_title.setText(getResources().getString(R.string.full_app_name));
        app_version_label.setText(getResources().getString(R.string.app_version_text, BuildConfig.VERSION_NAME));
        if(instance_prefs.getString("server", "").equals("openvk.su") || instance_prefs.getString("server", "").equals("openvk.uk") || instance_prefs.getString("server", "").equals("openvk.co")) {
            app_author_label.setText(Html.fromHtml(getResources().getString(R.string.app_author_value, "openvk://profile")));
            app_devteam_label.setText(Html.fromHtml(getResources().getString(R.string.app_devteam, "openvk://profile", "openvk://profile")));
            app_links.setText(Html.fromHtml(getResources().getString(R.string.app_links_text, "openvk://group", String.format("http://%s", instance_prefs.getString("server", "")))));
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                app_author_label.setText(Html.fromHtml(getResources().getString(R.string.app_author_value, "https://openvk.uk/")));
                app_devteam_label.setText(Html.fromHtml(getResources().getString(R.string.app_devteam, "https://openvk.uk/", "https://openvk.uk/")));
                app_links.setText(Html.fromHtml(getResources().getString(R.string.app_links_text, "https://openvk.uk", "https://openvk.uk")));
            } else {
                app_author_label.setText(Html.fromHtml(getResources().getString(R.string.app_author_value, "http://openvk.co/")));
                app_devteam_label.setText(Html.fromHtml(getResources().getString(R.string.app_devteam, "http://openvk.co/", "http://openvk.co/")));
                app_links.setText(Html.fromHtml(getResources().getString(R.string.app_links_text, "http://openvk.co", "http://openvk.co")));
            }
        }
        app_design_label.setText(Html.fromHtml(getResources().getString(R.string.app_design_value)));
        app_license_label.setText(Html.fromHtml(getResources().getString(R.string.app_license_text)));

        app_links.setMovementMethod(LinkMovementMethod.getInstance());
        app_author_label.setMovementMethod(LinkMovementMethod.getInstance());
        app_design_label.setMovementMethod(LinkMovementMethod.getInstance());
        app_devteam_label.setMovementMethod(LinkMovementMethod.getInstance());
        app_license_label.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
