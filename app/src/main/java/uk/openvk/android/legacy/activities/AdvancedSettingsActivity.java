package uk.openvk.android.legacy.activities;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.layouts.ActionBarImitation;

public class AdvancedSettingsActivity extends PreferenceActivity {
    private DownloadManager dlManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_preferences_layout);
        addPreferencesFromResource(R.xml.preferences_adv);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibillity(true);
            actionBarImitation.setTitle(getResources().getString(R.string.menu_settings));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        dlManager = new DownloadManager(this, false);
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

    private void setListeners() {
        final Preference clear_image_cache = findPreference("clearImageCache");
        long cache_size = dlManager.getCacheSize();
        String cache_size_in_megabytes = String.format("%s %s", cache_size / 1024, getResources().getString(R.string.fsize_kb));
        clear_image_cache.setSummary(cache_size_in_megabytes);
        clear_image_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dlManager.clearCache();
                long cache_size = dlManager.getCacheSize();
                String cache_size_in_megabytes = String.format("%s %s", cache_size / 1024, getResources().getString(R.string.fsize_kb));
                clear_image_cache.setSummary(cache_size_in_megabytes);
                return false;
            }
        });
    }
}
