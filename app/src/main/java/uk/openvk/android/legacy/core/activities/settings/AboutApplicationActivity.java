/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.core.activities.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.TranslucentPreferenceActivity;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.preferences.ButtonGridPreference;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AboutApplicationActivity extends TranslucentPreferenceActivity {

    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_custom_preferences);
        addPreferencesFromResource(R.xml.preferences_about_app);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setTitle(R.string.debug_menu);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.AbstractAction(0) {
                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            switch (global_prefs.getString("uiTheme", "blue")) {
                case "Gray":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
                case "Black":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
                    break;
                default:
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
            }
        }
        setListeners();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setListeners() {
        ButtonGridPreference gridPreference = (ButtonGridPreference) findPreference("tileWidget");

        PreferenceCategory authorsCategory = (PreferenceCategory) findPreference("appAuthors");
        PreferenceCategory translatorsCategory = (PreferenceCategory) findPreference("appTranslators");

        String[] authors = getResources().getStringArray(R.array.app_dev_team);
        String[] positions = getResources().getStringArray(R.array.app_dev_positions);

        for (String author : authors) {
            String authorArray[] = author.split("\\|");
            String authorName = authorArray[0];
            String authorPositions[] = authorArray[1].split(",");
            StringBuilder authorPositionsStr = new StringBuilder();

            Preference authorPref = new Preference(getApplicationContext());
            authorPref.setLayoutResource(R.layout.pref_widget);
            authorPref.setTitle(authorName);

            for (int i2 = 0; i2 < authorPositions.length; i2++) {
                int positionIndex = Integer.parseInt(authorPositions[i2]);
                if (i2 == authorPositions.length - 1) {
                    if(i2 > 0)
                        authorPositionsStr.append(positions[positionIndex].toLowerCase());
                    else
                        authorPositionsStr.append(positions[positionIndex]);
                } else if(i2 == 0) {
                    authorPositionsStr.append(positions[positionIndex]).append(", ");
                } else {
                    authorPositionsStr.append(positions[positionIndex].toLowerCase()).append(", ");
                }
            }

            if(authorArray.length == 3) {
                authorPositionsStr.append(" / ").append(authorArray[2]);
            }

            authorPref.setSummary(authorPositionsStr.toString());

            authorsCategory.addPreference(authorPref);
        }

        String[] translators = getResources().getStringArray(R.array.app_translators);
        String languages[] = {
                "English", "Русский", "Украïнська"
        };

        for (String translator : translators) {
            String translatorArray[] = translator.split("\\|");
            String translatorName = translatorArray[0];
            String translatorPositions[] = translatorArray[1].split(",");
            StringBuilder languagesStr = new StringBuilder();

            Preference translatorPref = new Preference(getApplicationContext());
            translatorPref.setLayoutResource(R.layout.pref_widget);
            translatorPref.setTitle(translatorName);
            translatorsCategory.addPreference(translatorPref);

            for (int i2 = 0; i2 < translatorPositions.length; i2++) {
                int positionIndex = Integer.parseInt(translatorPositions[i2]);
                if (i2 == translatorPositions.length - 1) {
                    languagesStr.append(languages[positionIndex]);
                } else {
                    languagesStr.append(languages[positionIndex]).append(", ");
                }
            }

            translatorPref.setSummary(languagesStr.toString());
        }

        Preference licensePref = findPreference("appLicense");
        licensePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openWebViewDialog("file:///android_res/raw/agpl_3.html");
                return false;
            }
        });
    }

    private void openWebViewDialog(String link) {
        View webviewLayout = getLayoutInflater().inflate(R.layout.dialog_web, null, false);
        WebView page = webviewLayout.findViewById(R.id.webview);
        page.getSettings().setSupportZoom(true);
        page.loadUrl("file:///android_res/raw/agpl_3.html");
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
}
