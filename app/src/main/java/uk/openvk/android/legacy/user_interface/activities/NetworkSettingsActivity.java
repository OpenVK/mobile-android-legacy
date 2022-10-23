package uk.openvk.android.legacy.user_interface.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;

/**
 * Created by Dmitry on 23.10.2022.
 */

public class NetworkSettingsActivity extends PreferenceActivity {
    private boolean isQuiting;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkApplication app;
    private View proxy_settings_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        addPreferencesFromResource(R.xml.network_preferences);
        setContentView(R.layout.custom_preferences_layout);
        app = ((OvkApplication) getApplicationContext());
        setListeners();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception ex) {
                Log.e("OpenVK", "Cannot display home button.");
            }
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibillity(true);
            actionBarImitation.setTitle(getResources().getString(R.string.sett_network));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
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

    private void setListeners() {
        /*((Preference) findPreference("proxySettings")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openProxySettingsDialog();
                return false;
            }
        });*/
    }

    private void openProxySettingsDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        proxy_settings_view = getLayoutInflater().inflate(R.layout.proxy_settings_layout, null, false);
        builder.setTitle(getResources().getString(R.string.sett_proxy_connection));
        builder.setView(proxy_settings_view);
        final EditText proxy_address = ((EditText) proxy_settings_view.findViewById(R.id.proxy_address));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        proxy_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(proxy_address.getText().length() > 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
        if (proxy_address.getText().length() > 0) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
}
