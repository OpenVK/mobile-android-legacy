package uk.openvk.android.legacy.core.activities.settings;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.TranslucentPreferenceActivity;

public class ExperimentalFunctionsActivity extends TranslucentPreferenceActivity {
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_experimental);
        setContentView(R.layout.layout_custom_preferences);
        global_prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
            actionBar.setTitle(R.string.experimental_functions);
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
        listenPreferences();
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

    private void listenPreferences() {
        final SharedPreferences experimental_pref = getSharedPreferences("experimental", 0);
        try {
            CheckBoxPreference ffmpeg_player =
                    (CheckBoxPreference) findPreference("video_ffmpeg_player");
            if(ffmpeg_player != null) {
                ffmpeg_player.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        final SharedPreferences.Editor editor = experimental_pref.edit();
                        editor.putBoolean("video_ffmpeg_player", ((CheckBoxPreference) preference).isChecked());
                        editor.commit();
                        return false;
                    }
                });
                ffmpeg_player.setChecked(experimental_pref.getBoolean("video_ffmpeg_player", false));
            }

            CheckBoxPreference tSysUI_v14 =
                    (CheckBoxPreference) findPreference("core_translucent_systemui_v14");
            if(tSysUI_v14 != null) {
                tSysUI_v14.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        final SharedPreferences.Editor editor = experimental_pref.edit();
                        editor.putBoolean("core_translucent_systemui_v14", ((CheckBoxPreference) preference).isChecked());
                        editor.commit();
                        return false;
                    }
                });
                tSysUI_v14.setChecked(experimental_pref.getBoolean("core_translucent_systemui_v14", false));
            }

            CheckBoxPreference xmas_mood =
                    (CheckBoxPreference) findPreference("core_xmas_mood");
            if(xmas_mood != null) {
                xmas_mood.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        final SharedPreferences.Editor editor = experimental_pref.edit();
                        editor.putBoolean("core_xmas_mood", ((CheckBoxPreference) preference).isChecked());
                        editor.commit();
                        return false;
                    }
                });
                xmas_mood.setChecked(experimental_pref.getBoolean("core_xmas_mood", false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
