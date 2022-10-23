package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Authorization;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.EditTextAction;
import uk.openvk.android.legacy.user_interface.layouts.XLinearLayout;
import uk.openvk.android.legacy.user_interface.list_adapters.InstancesListAdapter;
import uk.openvk.android.legacy.user_interface.list_items.InstancesListItem;
import uk.openvk.android.legacy.user_interface.listeners.OnKeyboardStateListener;

public class AuthActivity extends Activity {

    private OvkApplication app;
    private Global global = new Global();
    private AlertDialog alertDialog;
    private ProgressDialog connectionDialog;
    private OvkAPIWrapper ovk_api;
    private Error error;
    public Handler handler;
    private Account account;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private JSONParser jsonParser = new JSONParser();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);
        app = ((OvkApplication) getApplicationContext());
        XLinearLayout auth_layout = ((XLinearLayout) findViewById(R.id.auth_layout));
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(!app.isTablet) {
            auth_layout.setOnKeyboardStateListener(new OnKeyboardStateListener() {
                @Override
                public void onKeyboardStateChanged(boolean state) {
                    ImageView auth_logo = (ImageView) findViewById(R.id.auth_logo);
                    TextView register_btn = (TextView) findViewById(R.id.reg_btn);
                    if (state) {
                        auth_logo.setVisibility(View.GONE);
                    } else {
                        auth_logo.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        final EditTextAction instance_edit = (EditTextAction) findViewById(R.id.instance_name);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            instance_edit.setText(getResources().getText(R.string.default_instance));
        } else {
            instance_edit.setText(getResources().getText(R.string.default_instance_no_https));
        }
        instance_edit.setActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstancesDialog();
            }
        });
        Button settings_btn = (Button) findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, MainSettingsActivity.class);
                startActivity(intent);
            }
        });
        Button auth_btn = (Button) findViewById(R.id.auth_btn);
        auth_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authorize();
            }
        });
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                receiveState(message.what, data.getString("response"));
            }
        };
        (findViewById(R.id.reg_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(global_prefs.getBoolean("useHTTPS", true)) {
                    openWebAddress(String.format("https://%s/reg", instance_edit.getText().toString()));
                } else {
                    openWebAddress(String.format("http://%s/reg", instance_edit.getText().toString()));
                }
            }
        });
        (findViewById(R.id.forgot_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(global_prefs.getBoolean("useHTTPS", true)) {
                    openWebAddress(String.format("https://%s/restore", instance_edit.getText().toString()));
                } else {
                    openWebAddress(String.format("http://%s/restore", instance_edit.getText().toString()));
                }
            }
        });
    }

    private void authorize() {
        String instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        String username = ((EditText) findViewById(R.id.auth_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
        ovk_api.requireHTTPS(global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setServer(instance);
        ovk_api.authorize(username, password);
        connectionDialog = new ProgressDialog(this);
        connectionDialog.setMessage(getString(R.string.loading));
        connectionDialog.setCancelable(false);
        connectionDialog.show();
    }

    private void authorize(String code) {
        String instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        String username = ((EditText) findViewById(R.id.auth_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
        ovk_api.authorize(username, password, code);
        connectionDialog = new ProgressDialog(this);
        connectionDialog.setMessage(getString(R.string.loading));
        connectionDialog.setCancelable(false);
        connectionDialog.show();
    }

    private void showInstancesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
        ArrayList<InstancesListItem> instances_list = new ArrayList<>();
        for(int instances_index = 0; instances_index < getResources().getStringArray(R.array.official_instances_list).length; instances_index++) {
            instances_list.add(new InstancesListItem(getResources().getStringArray(R.array.official_instances_list)[instances_index], true, true));
        }
        for(int instances_index = 0; instances_index < getResources().getStringArray(R.array.instances_list).length; instances_index++) {
            instances_list.add(new InstancesListItem(getResources().getStringArray(R.array.instances_list)[instances_index], false, true));
        }
        InstancesListAdapter instancesAdapter = new InstancesListAdapter(AuthActivity.this, instances_list);
        builder.setTitle(getResources().getString(R.string.instances_list_title));
        builder.setSingleChoiceItems(instancesAdapter, -1, null);
        builder.setNegativeButton(R.string.close, null);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void clickInstancesItem(int position) {
        EditTextAction instance_edit = (EditTextAction) findViewById(R.id.instance_name);
        if(position >= getResources().getStringArray(R.array.official_instances_list).length) {
            instance_edit.setText(getResources().getStringArray(R.array.instances_list)[position - 3]);
        } else {
            instance_edit.setText(getResources().getStringArray(R.array.official_instances_list)[position]);
        }
        if(alertDialog != null) {
            alertDialog.cancel();
        }
    }

    public void receiveState(int message, String response) {
        if (message == HandlerMessages.INVALID_USERNAME_OR_PASSWORD) {
            connectionDialog.cancel();
            AlertDialog wrong_userdata_dlg;
            AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
            builder.setTitle(R.string.auth_error_title);
            builder.setMessage(R.string.auth_error);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            wrong_userdata_dlg = builder.create();
            if (!AuthActivity.this.isFinishing()) wrong_userdata_dlg.show();
        } else if (message == HandlerMessages.TWOFACTOR_CODE_REQUIRED) {
            connectionDialog.cancel();
            AlertDialog twofactor_dlg;
            AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
            View twofactor_view = getLayoutInflater().inflate(R.layout.twofactor_auth, null, false);
            builder.setTitle(R.string.auth);
            builder.setView(twofactor_view);
            final EditText two_factor_code = (EditText) twofactor_view.findViewById(R.id.two_factor_code);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    twoFactorLogin(two_factor_code.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            twofactor_dlg = builder.create();
            twofactor_dlg.setCancelable(false);
            if (!AuthActivity.this.isFinishing()) twofactor_dlg.show();
        } else if (message == HandlerMessages.AUTHORIZED) {
            connectionDialog.cancel();
            String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
            SharedPreferences.Editor instance_editor = instance_prefs.edit();
            Authorization auth = new Authorization(response);
            instance_editor.putString("access_token", auth.getAccessToken());
            instance_editor.putString("server", ((EditTextAction) findViewById(R.id.instance_name)).getText());
            instance_editor.putString("account_password", password);
            instance_editor.commit();
            if(connectionDialog != null) connectionDialog.cancel();
            Context context = getApplicationContext();
            Intent intent = new Intent(context, AppActivity.class);
            startActivity(intent);
            finish();
        } else if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(R.string.auth_error_network)
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.INTERNAL_ERROR) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error, getReason(message)))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.INVALID_JSON_RESPONSE) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error, getReason(message)))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.CONNECTION_TIMEOUT) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error_network, getReason(message)))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.BROKEN_SSL_CONNECTION) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error_ssl))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.NOT_OPENVK_INSTANCE) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error_not_openvk_instance))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        } else if(message == HandlerMessages.UNKNOWN_ERROR) {
            connectionDialog.cancel();
            alertDialog = new AlertDialog.Builder(this).setTitle(R.string.auth_error_title).setMessage(getResources().getString(R.string.auth_error_unknown_error))
                    .setNeutralButton(R.string.ok, null).create();
            alertDialog.show();
        }
    }

    private void twoFactorLogin(String two_factor_code) {
        try {
            authorize(two_factor_code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getReason(int message) {
        String description = null;
        if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(0));
        } else if(message == HandlerMessages.INVALID_JSON_RESPONSE) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(1));
        } else if(message == HandlerMessages.CONNECTION_TIMEOUT) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(2));
        } else if(message == HandlerMessages.INTERNAL_ERROR) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(3));
        }
        return description;
    }

    public void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        startActivity(i);
    }
}
