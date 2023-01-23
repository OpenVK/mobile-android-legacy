package uk.openvk.android.legacy.user_interface.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;

public class AdvancedSettingsActivity extends PreferenceActivity {
    private DownloadManager dlManager;
    private View quality_choose_view;
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_preferences_layout);
        addPreferencesFromResource(R.xml.advanced_preferences);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.sett_advanced));
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibility(true);
            actionBarImitation.setTitle(getResources().getString(R.string.sett_advanced));
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
        final Preference image_quality = findPreference("imageCacheQuality");
        image_quality.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openQualityChooseDialog();
                return false;
            }
        });

        final String[] quality_array = getResources().getStringArray(R.array.sett_cache_quality_array);
        if(global_prefs.getString("photos_quality", "").equals("medium")) {
            image_quality.setSummary(quality_array[0]);
        } else if(global_prefs.getString("photos_quality", "").equals("high")) {
            image_quality.setSummary(quality_array[1]);
        } else if(global_prefs.getString("photos_quality", "").equals("original")) {
            image_quality.setSummary(quality_array[2]);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void openQualityChooseDialog() {
        Global global = new Global();
        final long heap_size = global.getHeapSize();
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        quality_choose_view = getLayoutInflater().inflate(R.layout.imgcache_quality_layout, null, false);
        builder.setTitle(getResources().getString(R.string.sett_cache_quality_alt));
        builder.setView(quality_choose_view);
        builder.setNegativeButton(R.string.cancel, null);
        final SeekBar quality_seek = ((SeekBar) quality_choose_view.findViewById(R.id.quality_seek));
        final Preference image_quality = findPreference("imageCacheQuality");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = global_prefs.edit();
                if(quality_seek.getProgress() == 0) {
                    editor.putString("photos_quality", "medium");
                } else if(quality_seek.getProgress() == 1) {
                    editor.putString("photos_quality", "high");
                } else if(quality_seek.getProgress() == 2) {
                    editor.putString("photos_quality", "original");
                }
                editor.commit();
                final String[] quality_array = getResources().getStringArray(R.array.sett_cache_quality_array);
                if(global_prefs.getString("photos_quality", "").equals("medium")) {
                    image_quality.setSummary(quality_array[0]);
                } else if(global_prefs.getString("photos_quality", "").equals("high")) {
                    image_quality.setSummary(quality_array[1]);
                } else if(global_prefs.getString("photos_quality", "").equals("original")) {
                    image_quality.setSummary(quality_array[2]);
                }
            }
        });
        final TextView quality_value = ((TextView) quality_choose_view.findViewById(R.id.quality_label));
        final TextView quality_comm = ((TextView) quality_choose_view.findViewById(R.id.comment_label));
        final String[] quality_array = getResources().getStringArray(R.array.sett_cache_quality_array);
        quality_seek.setMax(2);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            quality_value.setTextColor(getResources().getColor(android.R.color.white));
        }

        quality_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                quality_value.setText(quality_array[i]);
                if(i == 0) {
                    quality_comm.setText(R.string.sett_cache_quality_lowres);
                    quality_comm.setTextColor(getResources().getColor(R.color.holo_blue_dark));
                    quality_comm.setVisibility(View.VISIBLE);
                } else if(i == 1 && heap_size <= 67108864L) {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        quality_comm.setTextColor(Color.parseColor("#ff0000"));
                    } else {
                        quality_comm.setTextColor(getResources().getColor(R.color.holo_red_light));
                    }
                    quality_comm.setText(R.string.sett_cache_quality_oomrisk);
                    quality_comm.setVisibility(View.VISIBLE);
                } else if(i == 2 && heap_size <= 134217728L) {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        quality_comm.setTextColor(Color.parseColor("#ff0000"));
                    } else {
                        quality_comm.setTextColor(getResources().getColor(R.color.holo_red_light));
                    }
                    quality_comm.setText(R.string.sett_cache_quality_oomrisk);
                    quality_comm.setVisibility(View.VISIBLE);
                } else if(i == 1) {
                    quality_comm.setText(R.string.sett_cache_quality_highres);
                    quality_comm.setTextColor(getResources().getColor(R.color.holo_blue_dark));
                    quality_comm.setVisibility(View.VISIBLE);
                } else if(i == 2) {
                    quality_comm.setText(R.string.sett_cache_quality_highestres);
                    quality_comm.setTextColor(getResources().getColor(R.color.holo_blue_dark));
                    quality_comm.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final AlertDialog dialog = builder.create();

        if(global_prefs.getString("photos_quality", "").equals("medium")) {
            quality_seek.setProgress(2);
            quality_seek.setProgress(0);
        } else if(global_prefs.getString("photos_quality", "").equals("high")) {
            quality_seek.setProgress(1);
        } else if(global_prefs.getString("photos_quality", "").equals("original")) {
            quality_seek.setProgress(2);
        }
        dialog.show();
    }
}
