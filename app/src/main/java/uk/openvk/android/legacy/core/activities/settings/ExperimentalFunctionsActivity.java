package uk.openvk.android.legacy.core.activities.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.TranslucentPreferenceActivity;

public class ExperimentalFunctionsActivity extends TranslucentPreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_experimental);
        setContentView(R.layout.layout_custom_preferences);

        listenPreferences();
    }

    private void listenPreferences() {
        final SharedPreferences experimental_pref = getSharedPreferences("experimental", 0);
        try {
            CheckBoxPreference ffmpeg_player = (CheckBoxPreference) findPreference("ffmpeg_player");
            if(ffmpeg_player != null) {
                ffmpeg_player.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        final SharedPreferences.Editor editor = experimental_pref.edit();
                        editor.putBoolean("ffmpeg_player", (boolean)o);
                        editor.commit();
                        return (boolean)o;
                    }
                });
            }

            CheckBoxPreference tSysUI_v14 = (CheckBoxPreference) findPreference("translucent_systemui_v14");
            if(ffmpeg_player != null) {
                tSysUI_v14.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        final SharedPreferences.Editor editor = experimental_pref.edit();
                        editor.putBoolean("translucent_systemui_v14", (boolean)o);
                        editor.commit();
                        return (boolean)o;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
