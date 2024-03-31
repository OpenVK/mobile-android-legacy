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

package uk.openvk.android.legacy.utils;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.AuthActivity;
import uk.openvk.android.legacy.core.activities.MainActivity;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.list.items.InstanceAccount;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context ctx;

    public AccountAuthenticator(Context ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse,
                             String s, String s2, String[] strings, Bundle bundle)
            throws NetworkErrorException {
        Intent i = new Intent(this.ctx, AuthActivity.class);
        i.putExtra("accountAuthenticatorResponse", accountAuthenticatorResponse);
        bundle.putParcelable("intent", i);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     Account account, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                              Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    public static void openChangeAccountDialog(final Context ctx, SharedPreferences global_prefs) {
        openChangeAccountDialog(ctx, global_prefs, false);
    }

    public static void loadAccounts(Context ctx, ArrayList<InstanceAccount> accountArray,
                                    AccountManager accountManager, SharedPreferences instance_prefs) {
        android.accounts.Account[] accounts = accountManager.getAccounts();
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        String package_name = ctx.getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = ctx.getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            ctx.getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if (server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        try {
                            accountArray.add(account);
                        } catch (ArrayIndexOutOfBoundsException ignored) {

                        }
                    }
                }
            }
            account_names = new String[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                if (accounts[i] != null &&
                        accounts[i].name.equals(instance_prefs.getString("account_name", "")) &&
                        accounts[i].type.equals("uk.openvk.android.legacy.account")) {
                    account_names[i] = accounts[i].name;
                    if (ctx.getApplicationContext() instanceof OvkApplication) {
                        OvkApplication app = ((OvkApplication) ctx.getApplicationContext());
                        app.androidAccount = accounts[i];
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void openChangeAccountDialog(final Context ctx, SharedPreferences global_prefs,
                                               boolean cancelable) {
        int valuePos = 0;
        final ArrayList<InstanceAccount> accountArray = new ArrayList<>();
        final int[] selectedPosition = {0};
        if(global_prefs == null) {
            global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        long current_uid = global_prefs.getLong("current_uid", 0);
        String current_instance = global_prefs.getString("current_instance", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        String package_name = ctx.getApplicationContext().getPackageName();
        @SuppressLint("SdCardPath") String profile_path =
                String.format("/data/data/%s/shared_prefs", package_name);
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        String file_extension;
        String account_names[] = new String[0];
        Context app_ctx = ctx.getApplicationContext();
        accountArray.clear();
        try {
            for (File prefs_file : prefs_files) {
                String filename = prefs_file.getName();
                if (prefs_file.getName().startsWith("instance")
                        && prefs_file.getName().endsWith(".xml")) {
                    SharedPreferences prefs =
                            ctx.getSharedPreferences(
                                    filename.substring(0, filename.length() - 4), 0);
                    String name = prefs.getString("account_name", "[Unknown account]");
                    long uid = prefs.getLong("uid", 0);
                    String server = prefs.getString("server", "");
                    if(server.length() > 0 && uid > 0 && name.length() > 0) {
                        InstanceAccount account = new InstanceAccount(name, uid, server);
                        accountArray.add(account);
                    }
                }
            }
            account_names = new String[accountArray.size()];
            for(int i = 0; i < accountArray.size(); i++) {
                account_names[i] = accountArray.get(i).name;
                if (accountArray.get(i).instance.equals(current_instance)) {
                    valuePos = i;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(accountArray.size() > 0) {
            builder.setSingleChoiceItems(account_names, valuePos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedPosition[0] = which;
                        }
                    }
            );
            OvkAlertDialog dialog = new OvkAlertDialog(ctx);
            dialog.build(builder, ctx.getResources().getString(R.string.sett_account), "", null, "listDlg");
            final SharedPreferences finalGlobal_prefs = global_prefs;
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    ctx.getResources().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = finalGlobal_prefs.edit();
                            editor.putString("current_instance",
                                    accountArray.get(selectedPosition[0]).instance);
                            editor.putLong("current_uid",
                                    accountArray.get(selectedPosition[0]).id);
                            editor.commit();
                            dialog.dismiss();
                            if (ctx instanceof Activity) {
                                ((Activity) ctx).finish();
                                Intent intent = new Intent(ctx, AppActivity.class);
                                ctx.startActivity(intent);
                                System.exit(0);
                            } else {
                                Toast.makeText(ctx, R.string.sett_app_restart_required,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                    ctx.getResources().getString(R.string.add),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ctx, AuthActivity.class);
                            intent.putExtra("authFromAppActivity", true);
                            ctx.startActivity(intent);
                        }
                    });
            dialog.setCancelable(cancelable);
            dialog.show();
        } else {
            Intent intent = new Intent(ctx, AuthActivity.class);
            System.exit(0);
            ctx.startActivity(intent);
        }
    }

    public static void openLogoutConfirmationDialog(final Context ctx, final SharedPreferences global_prefs) {
        OvkAlertDialog logout_dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor =
                        ((OvkApplication) ctx.getApplicationContext())
                                .getAccountPreferences().edit();
                editor.clear();
                editor.commit();
                global_prefs.edit().putString("current_instance", "").commit();
                if(ctx.getApplicationContext() instanceof OvkApplication) {
                    AccountManager am = AccountManager.get(ctx);
                    try {
                        am.removeAccount(((OvkApplication) ctx.getApplicationContext()).androidAccount, null, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                DownloadManager dlm = new DownloadManager(ctx,
                        SecureCredentialsStorage.generateClientInfo(
                                ctx,
                                new HashMap<String, Object>()
                        ),
                        new Handler(Looper.myLooper()));
                dlm.clearCache(ctx.getCacheDir());
                Intent activity = new Intent(ctx.getApplicationContext(), MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ctx.startActivity(activity);
                System.exit(0);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        logout_dlg = new OvkAlertDialog(ctx);
        logout_dlg.build(builder, "", ctx.getResources().getString(R.string.log_out_warning), null, "");
        logout_dlg.show();
    }
}
