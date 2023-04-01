package uk.openvk.android.legacy.ui.core.activities;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

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
                getActionBar().setTitle(getResources().getString(R.string.sett_network));
            } catch (Exception ex) {
                Log.e("OpenVK", "Cannot display home button.");
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setTitle(R.string.sett_network);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.AbstractAction(0) {
                @Override
                public void performAction(View view) {
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

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setListeners() {
        ((Preference) findPreference("proxySettings")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openProxySettingsDialog();
                return false;
            }
        });
        if(global_prefs.contains("proxy_address")) {
            if(global_prefs.getString("proxy_address", "").length() > 0) {
                ((Preference) findPreference("proxySettings")).setSummary(global_prefs.getString("proxy_address", ""));
            }
        }
        ((Preference) findPreference("useProxy")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(NetworkSettingsActivity.this, R.string.sett_app_restart_required, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private void openProxySettingsDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        proxy_settings_view = getLayoutInflater().inflate(R.layout.proxy_settings_layout, null, false);
        builder.setView(proxy_settings_view);
        final EditText proxy_address = ((EditText) proxy_settings_view.findViewById(R.id.proxy_address));
        final EditText proxy_port = ((EditText) proxy_settings_view.findViewById(R.id.proxy_port));
        final Spinner proxy_type_spinner = proxy_settings_view.findViewById(R.id.proxy_type);

        ArrayAdapter proxy_type_adapter = ArrayAdapter.createFromResource(this, R.array.proxy_type, android.R.layout.simple_spinner_item);
        final String[] proxy_types = getResources().getStringArray(R.array.proxy_type);
        proxy_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        proxy_type_spinner.setAdapter(proxy_type_adapter);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = global_prefs.edit();
                if(proxy_types[proxy_type_spinner.getSelectedItemPosition()].equals("HTTP")) {
                    editor.putString("proxy_type", "http");
                }
                if(proxy_port.getText().length() > 0) {
                    editor.putString("proxy_address", String.format("%s:%s", proxy_address.getText().toString(), proxy_port.getText().toString()));
                } else {
                    editor.putString("proxy_address", String.format("%s:8080", proxy_address.getText().toString()));
                }
                editor.commit();
                if(global_prefs.contains("proxy_address")) {
                    if(global_prefs.getString("proxy_address", "").length() > 0) {
                        ((Preference) findPreference("proxySettings")).setSummary(global_prefs.getString("proxy_address", ""));
                    }
                }
                Toast.makeText(NetworkSettingsActivity.this, R.string.sett_app_restart_required, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        final OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.sett_proxy_connection), "", proxy_settings_view);
        proxy_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(proxy_address.getText().length() > 0 && !proxy_address.getText().toString().contains(":") &&
                        !proxy_address.getText().toString().contains("/") &&
                        !proxy_address.getText().toString().contains("@")) {
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
        if(global_prefs.contains("proxy_address")) {
            if (global_prefs.getString("proxy_address", "").length() > 0) {
                String[] address_split = global_prefs.getString("proxy_address", "").split(":");
                proxy_address.setText(address_split[0]);
                proxy_port.setText(address_split[1]);
            }
        }
        if (proxy_address.getText().length() > 0 && !proxy_address.getText().toString().contains(":") &&
                !proxy_address.getText().toString().contains("/") &&
                !proxy_address.getText().toString().contains("@")) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setDialogStyle(proxy_settings_view, "proxy_settings");
        }
    }

    private void setDialogStyle(View view, String dialog_name) {
        try {
            if (dialog_name.equals("proxy_settings")) {
                //((TextView) view.findViewById(R.id.proxy_address_label)).setTextColor(Color.WHITE);
                //((TextView) view.findViewById(R.id.proxy_type_label)).setTextColor(Color.WHITE);
            }
        } catch (Exception ex) {

        }
    }
}
