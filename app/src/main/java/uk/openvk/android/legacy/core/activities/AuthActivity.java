/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.core.activities;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.client.entities.Authorization;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.legacy.core.activities.settings.MainSettingsActivity;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.core.activities.base.NetworkAuthActivity;
import uk.openvk.android.legacy.ui.views.EditTextAction;
import uk.openvk.android.legacy.ui.views.base.XLinearLayout;
import uk.openvk.android.legacy.ui.list.adapters.InstancesListAdapter;
import uk.openvk.android.legacy.ui.list.items.InstancesListItem;
import uk.openvk.android.legacy.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

@SuppressWarnings("ALL")
public class AuthActivity extends NetworkAuthActivity {

    private OvkApplication app;
    private Global global = new Global();
    private OvkAlertDialog alertDialog;
    private OvkAlertDialog connectionDialog;
    private Error error;
    private Account account;
    private JSONParser jsonParser = new JSONParser();
    private int twofactor_fail = -1;
    private Authorization auth;
    private ArrayList<InstancesListItem> instances_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        app = ((OvkApplication) getApplicationContext());
        XLinearLayout auth_layout = ((XLinearLayout) findViewById(R.id.auth_layout));
        loadInstances();
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

        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            findViewById(R.id.auth_layout)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_auth_gray));
            ((ImageView) findViewById(R.id.auth_logo))
                    .setImageDrawable(getResources().getDrawable(R.drawable.login_logo_gray));
            findViewById(R.id.reg_btn)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            findViewById(R.id.auth_layout)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_auth_black));
            ((ImageView) findViewById(R.id.auth_logo))
                    .setImageDrawable(getResources().getDrawable(R.drawable.login_logo_black));
            findViewById(R.id.reg_btn)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
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
                intent.putExtra("start_from", "AuthActivity");
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

    private void loadInstances() {
        instances_list = new ArrayList<>();
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
            checkHttpsEnabled(instance.startsWith("https"));
            if(!global_prefs.getBoolean("useHTTPS", false) && !global_prefs.getBoolean("useProxy", false)) {
                return;
            }
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
                Toast.makeText(this, getResources().getString(R.string.vk_not_supported),
                        Toast.LENGTH_LONG).show();
            }

        } else if(username.length() > 0 && password.length() > 0) {
            for (int i = 0; i < instances_list.size(); i++) {
                if(instances_list.get(i).server.equals(instance) && instances_list.get(i).secured) {
                    checkHttpsEnabled(instances_list.get(i).secured);
                    if(!global_prefs.getBoolean("useHTTPS", false) && !global_prefs.getBoolean("useProxy", false)) {
                        return;
                    }
                }
            }
            ovk_api.wrapper.requireHTTPS(global_prefs.getBoolean("useHTTPS", true));
            ovk_api.wrapper.setServer(instance);
            ovk_api.wrapper.authorize(username, password);
            connectionDialog = new OvkAlertDialog(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            connectionDialog.build(builder, "", getString(R.string.loading), null, "progressDlg");
            connectionDialog.show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.authdata_required),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void checkHttpsEnabled(boolean isHttp) {
        if(isHttp && !global_prefs.getBoolean("useHTTPS", false)
                && !global_prefs.getBoolean("useProxy", false)) {
            final OvkAlertDialog http_disabled_dlg;
            http_disabled_dlg = new OvkAlertDialog(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
            builder.setMessage(R.string.auth_error_https_disabled);
            builder.setNegativeButton(android.R.string.no, null);
            http_disabled_dlg.build(builder, getResources().getString(R.string.ovk_warning_title),
                    getResources().getString(R.string.auth_error_https_disabled), null);
            http_disabled_dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            http_disabled_dlg.dismiss();
                            global_prefs.edit().putBoolean("useHTTPS", true).commit();
                            authorize();
                        }
                    });
            http_disabled_dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            http_disabled_dlg.dismiss();
                        }
                    });
            if (!AuthActivity.this.isFinishing()) http_disabled_dlg.show();
        }
    }

    private void authorize(String code) {
        String instance = ((EditTextAction) findViewById(R.id.instance_name)).getText();
        String username = ((EditText) findViewById(R.id.auth_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
        ovk_api.wrapper.authorize(username, password, code);
        connectionDialog = new OvkAlertDialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        connectionDialog.build(builder, "", getString(R.string.loading), null, "progressDlg");
        connectionDialog.show();
    }

    private void showInstancesDialog() {
        alertDialog = new OvkAlertDialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
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
        int official_instances_count = getResources().getStringArray(R.array.official_instances_list).length;
        if(position >= official_instances_count) {
            server = getResources().getStringArray(R.array.instances_list)[position - official_instances_count].split(regexp)[0];
            instance_edit.setText(server);
        } else {
            server = getResources().getStringArray(R.array.official_instances_list)[position].split(regexp)[0];
            instance_edit.setText(server);
        }
        if(alertDialog != null) {
            alertDialog.cancel();
        }
    }

    @Override
    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null) {
                return;
            }
            boolean isCurrentActivity = activityName.equals(
                    String.format("%s_%s", getLocalClassName(), getSessionId())
            );
            if(!isCurrentActivity) {
                Log.d(OvkApplication.APP_TAG, String.format("%s != %s", activityName, String.format("%s_%s", getLocalClassName(), getSessionId())));
                return;
            }
        }
        alertDialog = new OvkAlertDialog(this);
        try {
            String response = data.getString("response");
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
                twofactor_fail++;
                connectionDialog.close();
                OvkAlertDialog twofactor_dlg;
                twofactor_dlg = new OvkAlertDialog(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                View twofactor_view = getLayoutInflater().inflate(R.layout
                        .dialog_twofactor_auth, null, false);
                builder.setTitle(R.string.auth);
                builder.setView(twofactor_view);
                final EditText two_factor_code = (EditText) twofactor_view.findViewById(R.id.two_factor_code);
                twofactor_dlg.build(builder, getResources().getString(R.string.auth), "", twofactor_view);
                twofactor_dlg.setButton(DialogInterface.BUTTON_POSITIVE,
                        getResources().getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                twoFactorLogin(two_factor_code.getText().toString());
                            }
                        });
                twofactor_dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                twofactor_fail = -1;
                            }
                        });
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
                if(twofactor_fail > 0) {
                    twofactor_view.findViewById(R.id.twofactor_error).setVisibility(View.VISIBLE);
                } else {
                    twofactor_view.findViewById(R.id.twofactor_error).setVisibility(View.GONE);
                }
                twofactor_dlg.setCancelable(false);
                if (!AuthActivity.this.isFinishing()) twofactor_dlg.show();
            } else if (message == HandlerMessages.AUTHORIZED) {
                auth = new Authorization(response);
                if(connectionDialog.isShowing()) {
                    connectionDialog.setProgressText(getResources().getString(R.string.creating_account));
                }
                account = new Account(this);
                ovk_api.wrapper.setAccessToken(auth.getAccessToken());
                account.getProfileInfo(ovk_api.wrapper);
            } else if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                String server = ((EditTextAction) findViewById(R.id.instance_name)).getText();
                String username = ((EditText) findViewById(R.id.auth_login)).getText().toString();
                String password = ((EditText) findViewById(R.id.auth_pass)).getText().toString();
                Log.d(OvkApplication.APP_TAG, "Creating OpenVK Account...");
                account = new Account(response, this, ovk_api.wrapper);
                instance_prefs = getSharedPreferences(
                        String.format("instance_a%s_%s", account.id, server),
                        0);
                SharedPreferences.Editor global_editor = global_prefs.edit();
                global_editor.putString("current_instance", server);
                global_editor.putLong("current_uid", account.id);
                global_editor.commit();
                SharedPreferences.Editor instance_editor = instance_prefs.edit();
                instance_editor.putLong("uid", account.id);
                instance_editor.putString("account_name",
                        String.format("id%s, %s", account.id, server)
                );
                instance_editor.putString("email", username);
                instance_editor.putString("access_token", auth.getAccessToken());
                instance_editor.putString("server", server);
                instance_editor.putString("account_password_hash", Global.GetSHA256Hash(password));
                instance_editor.commit();
                createAndroidAccount(
                        String.format("%s %s", account.first_name, account.last_name),
                        account.id, server, auth
                );
                connectionDialog.close();
                connectionDialog.cancel();
                if(!getIntent().hasExtra("accountAuthenticatorResponse") &&
                        !getIntent().hasExtra("authFromAppActivity")) {
                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, AppActivity.class);
                    startActivity(intent);
                }
                finish();
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error_network), null);
                alertDialog.show();
            } else if (message == HandlerMessages.INTERNAL_ERROR) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.INVALID_JSON_RESPONSE) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.CONNECTION_TIMEOUT) {
                connectionDialog.cancel();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.BROKEN_SSL_CONNECTION) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.BANNED_ACCOUNT) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error_banned, getReason(message)), null);
                alertDialog.show();
            } else if (message == HandlerMessages.NOT_OPENVK_INSTANCE) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error_not_openvk_instance), null);
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
            if(connectionDialog != null) {
                connectionDialog.close();
                alertDialog.build(new AlertDialog.Builder(this).setNeutralButton(R.string.ok, null),
                        getResources().getString(R.string.auth_error_title),
                        getResources().getString(R.string.auth_error, getReason(message)), null);
                alertDialog.show();
            }
            e.printStackTrace();
        }
    }

    private void createAndroidAccount(String username, long id, String server, Authorization auth) {
        // Add OpenVK account to operating system
        android.accounts.Account account = new android.accounts.Account(
                String.format("id%s, %s", id, server),
                Authorization.ACCOUNT_TYPE);
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        accountManager.addAccountExplicitly(account, auth.getAccessToken(), null);
        if(getIntent().hasExtra("accountAuthenticatorResponse")) {
            getIntent().getParcelableExtra("accountAuthenticatorResponse");
            Bundle res = new Bundle();
            res.putString("authAccount", String.format("id%s, %s", id, server));
            res.putString("accountType", Authorization.ACCOUNT_TYPE);
            setAccountAuthenticatorResult(res);
        }
        boolean success = accountManager.addAccountExplicitly(account,
                auth.getAccessToken(), null);
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
            description = getResources().getString(R.string.reason,
                    Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(0));
        } else if(message == HandlerMessages.INVALID_JSON_RESPONSE) {
            description = getResources().getString(R.string.reason,
                    Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(1));
        } else if(message == HandlerMessages.CONNECTION_TIMEOUT) {
            description = getResources().getString(R.string.reason,
                    Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(2));
        } else if(message == HandlerMessages.INTERNAL_ERROR) {
            description = getResources().getString(R.string.reason,
                    Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(3));
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
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

}
