package uk.openvk.android.legacy.ui.core.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Ovk;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentFragmentActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.MainSettingsFragment;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class MainSettingsActivity extends TranslucentFragmentActivity {
    private boolean isQuiting;
    private OvkApplication app;
    private Global global = new Global();
    public OvkAPIWrapper ovk_api;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkAlertDialog about_instance_dlg;
    public Handler handler;
    private View about_instance_view;
    private Ovk ovk;
    private int danger_zone_multiple_tap;
    private String account_name;
    private MainSettingsFragment mainSettingsFragment;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        setContentView(R.layout.activity_intent);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                account_name = "";
            } else {
                account_name = extras.getString("account_name");
            }
        } else {
            account_name = (String) savedInstanceState.getSerializable("account_name");
        }
        app = ((OvkApplication) getApplicationContext());
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(BuildConfig.DEBUG) Log.d(OvkApplication.APP_TAG,
                        String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true),
                global_prefs.getBoolean("legacyHttpClient", false), handler);
        ovk_api.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk = new Ovk();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setTitle(getResources().getString(R.string.menu_settings));
                if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                    getActionBar().setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.bg_actionbar_gray));
                } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                    getActionBar().setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.bg_actionbar_black));
                }
            } catch (Exception ex) {
                Log.e(OvkApplication.APP_TAG, "Cannot display home button.");
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
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
            actionBar.setTitle(getResources().getString(R.string.menu_settings));
        }
        installFragments();
    }

    private void installFragments() {
        mainSettingsFragment = new MainSettingsFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, mainSettingsFragment, "settings");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.show(mainSettingsFragment);
        ft.commit();
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.OVK_VERSION) {
                ovk.parseVersion(data.getString("response"));
                mainSettingsFragment.setInstanceVersion(ovk);
            } else if(message == HandlerMessages.OVK_ABOUTINSTANCE) {
                ovk.parseAboutInstance(data.getString("response"));
                mainSettingsFragment.setAboutInstanceData(ovk);
            } else {
                mainSettingsFragment.setConnectionType(message, false);
            }
        } catch (Exception ex) {
            mainSettingsFragment.setConnectionType(message, false);
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
}
