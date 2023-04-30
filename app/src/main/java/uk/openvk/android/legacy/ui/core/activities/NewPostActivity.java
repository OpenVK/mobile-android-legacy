package uk.openvk.android.legacy.ui.core.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class NewPostActivity extends Activity {
    public String server;
    public String state;
    public String auth_token;
    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public JSONObject json_response;
    public String connectionErrorString;
    public boolean connection_status;
    public String send_request;
    public Boolean inputStream_isClosed;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;
    public long owner_id;
    public OvkAPIWrapper ovk_api;
    private Wall wall;
    public Handler handler;
    private long account_id;
    private String account_first_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.new_status));
        }
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                owner_id = extras.getLong("owner_id");
                account_id = extras.getLong("account_id");
                account_first_name = extras.getString("account_first_name");
                installLayouts();
                wall = new Wall();
                global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
                global_prefs_editor = global_prefs.edit();
                instance_prefs_editor = instance_prefs.edit();
                inputStream_isClosed = false;
                server = getApplicationContext().getSharedPreferences("instance", 0)
                        .getString("server", "");
                auth_token = getApplicationContext().getSharedPreferences("instance", 0).getString("auth_token", "");
                if (owner_id == 0) {
                    finish();
                }

                handler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle data = message.getData();
                        if(!BuildConfig.BUILD_TYPE.equals("release"))
                            Log.d(OvkApplication.APP_TAG, String.format("Handling API message: %s", message.what));
                        receiveState(message.what, data);
                    }
                };
                ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                ovk_api.setServer(instance_prefs.getString("server", ""));
                ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
                response_sb = new StringBuilder();
            }
        }
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

    private void installLayouts() {
        TextView where = findViewById(R.id.newpost_location_address);
        where.setText(String.format("%s %s", getResources().getString(R.string.wall), account_first_name));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
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
            actionBar.addAction(new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return R.drawable.ic_ab_done;
                }

                @Override
                public void performAction(View view) {
                    EditText statusEditText = findViewById(R.id.status_text_edit);
                    if (statusEditText.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            connectionDialog = new ProgressDialog(NewPostActivity.this);
                            connectionDialog.setMessage(getString(R.string.loading));
                            connectionDialog.setCancelable(false);
                            connectionDialog.show();
                            wall.post(ovk_api, owner_id, statusEditText.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            actionBar.setTitle(getResources().getString(R.string.new_status));
        }

    }

    private void receiveState(int message, Bundle data) {
        try {
            if(message == HandlerMessages.WALL_POST) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                connectionDialog.cancel();
                finish();
            } else if(message == HandlerMessages.ACCESS_DENIED){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posting_access_denied), Toast.LENGTH_LONG).show();
                connectionDialog.cancel();
            } else {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                connectionDialog.cancel();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newpost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if(item.getItemId() == R.id.sendpost) {
            EditText statusEditText = findViewById(R.id.status_text_edit);
            if(statusEditText.getText().toString().length() == 0) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
            } else if(!connection_status) {
                try {
                    connectionDialog = new ProgressDialog(this);
                    connectionDialog.setMessage(getString(R.string.loading));
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    wall.post(ovk_api, owner_id, statusEditText.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

}