package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Authorization;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.view.layouts.EditTextAction;
import uk.openvk.android.legacy.ui.view.layouts.XLinearLayout;
import uk.openvk.android.legacy.ui.list.adapters.InstancesListAdapter;
import uk.openvk.android.legacy.ui.list.items.InstancesListItem;
import uk.openvk.android.legacy.ui.core.listeners.OnKeyboardStateListener;
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

@SuppressWarnings("ALL")
public class AuthActivity extends Activity {

    private OvkApplication app;
    private Global global = new Global();
    private OvkAlertDialog alertDialog;
    private OvkAlertDialog connectionDialog;
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
        setContentView(R.layout.activity_auth);
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
        ((EditText) findViewById(R.id.auth_pass)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(keyEvent != null && KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    authorize();
                }
                return false;
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
        ovk_api.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
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
        final EditTextAction instance_edit = (EditTextAction) findViewById(R.id.instance_name);
        if (instance.startsWith("http://")) {
            instance_edit.setText(instance.substring(7));
            instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        } else if (instance.startsWith("https://")) {
            instance_edit.setText(instance.substring(8));
            instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        }
        if (instance.contains("vkontakte.ru") || instance.contains("vk.com") || instance.contains("vk.ru")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                instance_edit.setText(getResources().getText(R.string.default_instance));
            } else {
                instance_edit.setText(getResources().getText(R.string.default_instance_no_https));
            }
            if (!global_prefs.getBoolean("hideOvkWarnForBeginners", false)) {
                AlertDialog.Builder dialog_builder = new AlertDialog.Builder(AuthActivity.this);
                dialog_builder.setTitle(R.string.ovk_warning_title);
                View warn_view = getLayoutInflater().inflate(R.layout.dialog_warn_message, null, false);
                dialog_builder.setView(warn_view);
                dialog_builder.setNeutralButton(R.string.ok, null);
                AlertDialog warn_dialog = dialog_builder.create();
                warn_dialog.show();
                ((TextView) warn_view.findViewById(R.id.warn_message_text)).setText(
                        Html.fromHtml(getResources().getString(R.string.ovk_warning)));
                ((TextView) warn_view.findViewById(R.id.warn_message_text)).
                        setMovementMethod(LinkMovementMethod.getInstance());
                ((CheckBox) warn_view.findViewById(R.id.do_not_show_messages))
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
                        global_prefs_editor.putBoolean("hideOvkWarnForBeginners", b);
                        global_prefs_editor.commit();
                    }
                });
            } else {
                Toast.makeText(this, "А мы вас предупреждали, что сюда не следует тыкать в ваш ВК!",
                        Toast.LENGTH_LONG).show();
            }

        } else if(username.length() > 0 && password.length() > 0) {
            ovk_api.requireHTTPS(global_prefs.getBoolean("useHTTPS", true));
            ovk_api.setServer(instance);
            ovk_api.authorize(username, password);
            connectionDialog = new OvkAlertDialog(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            connectionDialog.build(builder, "", getString(R.string.loading), null, "progressDlg");
            connectionDialog.show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.authdata_required),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void authorize(String code) {
        String instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        String username = ((EditText) findViewById(R.id.auth_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
        ovk_api.authorize(username, password, code);
        connectionDialog = new OvkAlertDialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        connectionDialog.build(builder, "", getString(R.string.loading), null, "progressDlg");
        connectionDialog.show();
    }

    private void showInstancesDialog() {
        alertDialog = new OvkAlertDialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
        ArrayList<InstancesListItem> instances_list = new ArrayList<>();
        for(int instances_index = 0; instances_index < getResources().getStringArray(
                R.array.official_instances_list).length; instances_index++) {
            String instance = getResources().getStringArray(R.array.official_instances_list)[instances_index];
            boolean secured;
            String regexp = Pattern.quote("|");
            secured = instance.split(regexp)[1].equals("HTTPS");
            instances_list.add(new InstancesListItem(instance.split(regexp)[0], true, secured));
        }
        for(int instances_index = 0; instances_index < getResources().getStringArray(
                R.array.instances_list).length; instances_index++) {
            String instance = getResources().getStringArray(R.array.instances_list)[instances_index];
            boolean secured;
            String regexp = Pattern.quote("|");
            secured = instance.split(regexp)[1].equals("HTTPS");
            instances_list.add(new InstancesListItem(instance.split(regexp)[0], false, secured));
        }
        InstancesListAdapter instancesAdapter = new InstancesListAdapter(
                AuthActivity.this, instances_list);
        builder.setSingleChoiceItems(instancesAdapter, -1, null);
        builder.setNegativeButton(R.string.close, null);
        alertDialog.build(builder, getResources().getString(R.string.instances_list_title), "", null, "listDlg");
        alertDialog.show();
    }

    public void clickInstancesItem(int position) {
        EditTextAction instance_edit = (EditTextAction) findViewById(R.id.instance_name);
        String regexp = Pattern.quote("|");
        String server;
        if(position >= getResources().getStringArray(R.array.official_instances_list).length) {
            server = getResources().getStringArray(R.array.instances_list)[position - 3].split(regexp)[0];
            instance_edit.setText(server);
        } else {
            server = getResources().getStringArray(R.array.official_instances_list)[position].split(regexp)[0];
            instance_edit.setText(server);
        }
        if(alertDialog != null) {
            alertDialog.cancel();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("StringFormatInvalid")
    public void receiveState(int message, String response) {
        alertDialog = new OvkAlertDialog(this);
        try {
            if (message == HandlerMessages.INVALID_USERNAME_OR_PASSWORD) {
                connectionDialog.close();
                OvkAlertDialog wrong_userdata_dlg;
                wrong_userdata_dlg = new OvkAlertDialog(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                builder.setMessage(R.string.auth_error);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                wrong_userdata_dlg.build(builder, getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error), null);
                if (!AuthActivity.this.isFinishing()) wrong_userdata_dlg.show();
            } else if (message == HandlerMessages.TWOFACTOR_CODE_REQUIRED) {
                connectionDialog.close();
                OvkAlertDialog twofactor_dlg;
                twofactor_dlg = new OvkAlertDialog(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                View twofactor_view = getLayoutInflater().inflate(R.layout
                        .dialog_twofactor_auth, null, false);
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
                twofactor_dlg.build(builder, "", getResources().getString(R.string.auth), twofactor_view);
                two_factor_code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if(keyEvent != null && KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()
                                && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            twoFactorLogin(two_factor_code.getText().toString());
                        }
                        return false;
                    }
                });
                twofactor_dlg.setCancelable(false);
                if (!AuthActivity.this.isFinishing()) twofactor_dlg.show();
            } else if (message == HandlerMessages.AUTHORIZED) {
                connectionDialog.close();
                String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
                SharedPreferences.Editor instance_editor = instance_prefs.edit();
                Authorization auth = new Authorization(response);
                instance_editor.putString("access_token", auth.getAccessToken());
                instance_editor.putString("server", ((EditTextAction) findViewById(R.id.instance_name)).getText());
                instance_editor.putString("account_password_hash", Global.GetSHA256Hash(password));
                instance_editor.commit();
                if (connectionDialog != null) connectionDialog.cancel();
                Context context = getApplicationContext();
                Intent intent = new Intent(context, AppActivity.class);
                startActivity(intent);
                finish();
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error_network), null);
                alertDialog.show();
            } else if (message == HandlerMessages.INTERNAL_ERROR) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.INVALID_JSON_RESPONSE) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.CONNECTION_TIMEOUT) {
                connectionDialog.cancel();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.BROKEN_SSL_CONNECTION) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.NOT_OPENVK_INSTANCE) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title), getResources().getString(R.string.auth_error_not_openvk_instance), null);
                alertDialog.show();
            } else if (message == HandlerMessages.INSTANCE_UNAVAILABLE) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_instance_unavaliable), null);
                alertDialog.show();
            } else if (message == HandlerMessages.UNKNOWN_ERROR) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error_unknown_error), null);
                alertDialog.show();
            }
        } catch (Exception e) {
            connectionDialog.close();
            alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                    getResources().getString(R.string.auth_error_title),
                    getResources().getString(R.string.auth_error, getReason(message)), null);
            alertDialog.show();
            e.printStackTrace();
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
        } else {
            description = "No reason";
        }
        return description;
    }

    public void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

}
