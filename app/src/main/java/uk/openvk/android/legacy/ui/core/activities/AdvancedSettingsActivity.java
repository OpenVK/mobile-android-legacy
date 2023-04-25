package uk.openvk.android.legacy.ui.core.activities;

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
import android.widget.Toast;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

public class AdvancedSettingsActivity extends PreferenceActivity {
    private DownloadManager dlManager;
    private View quality_choose_view;
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_custom_preferences);
        addPreferencesFromResource(R.xml.advanced_preferences);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.sett_advanced));
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setTitle(getResources().getString(R.string.sett_advanced));
            actionBar.setHomeAction(new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return R.drawable.ic_ab_app;
                }

                @Override
                public void performAction(View view) {
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
        String cache_size_in_megabytes = String.format("%.2f %s", (double)cache_size / 1024, getResources().getString(R.string.fsize_mb));
        clear_image_cache.setSummary(cache_size_in_megabytes);
        if(cache_size == 0) {
            clear_image_cache.setEnabled(false);
        }
        clear_image_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dlManager.clearCache(null);
                long cache_size = dlManager.getCacheSize();
                String cache_size_in_megabytes = String.format("%.2f %s", (double)cache_size / 1024, getResources().getString(R.string.fsize_mb));
                clear_image_cache.setSummary(cache_size_in_megabytes);
                if(cache_size == 0) {
                    clear_image_cache.setEnabled(false);
                }
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.img_cache_cleared), Toast.LENGTH_LONG).show();
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
        quality_choose_view = getLayoutInflater().inflate(R.layout.dialog_imgcache_quality, null, false);
        builder.setTitle(getResources().getString(R.string.sett_cache_quality_alt));
        builder.setView(quality_choose_view);
        builder.setNegativeButton(R.string.cancel, null);
        final OvkAlertDialog dialog = new OvkAlertDialog(this);
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
                dialog.dismiss();
            }
        });
        final TextView quality_value = ((TextView) quality_choose_view.findViewById(R.id.quality_label));
        final TextView quality_comm = ((TextView) quality_choose_view.findViewById(R.id.comment_label));
        final String[] quality_array = getResources().getStringArray(R.array.sett_cache_quality_array);
        quality_seek.setMax(2);

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


        dialog.build(builder, getResources().getString(R.string.sett_cache_quality_alt), "", quality_choose_view);
        dialog.show();

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
