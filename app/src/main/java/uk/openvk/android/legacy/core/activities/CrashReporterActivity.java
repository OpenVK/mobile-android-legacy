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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.BaseCrashReportDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;

public class CrashReporterActivity extends BaseCrashReportDialog
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private OvkAlertDialog dialog;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final int titleResourceId = ACRA.getConfig().resDialogTitle();
        if (titleResourceId != 0) {
            dialogBuilder.setTitle(titleResourceId);
        }
        view = getLayoutInflater().inflate(R.layout.activity_crash_report, null);
        ((TextView) view.findViewById(R.id.crash_description)).setText(
                Html.fromHtml(getResources().getString(R.string.crash_description)));
        ((TextView) view.findViewById(R.id.crash_description))
                .setMovementMethod(LinkMovementMethod.getInstance());

        try {
            writeLog();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ((EditText) view.findViewById(R.id.crash_report)).setKeyListener(null);
        dialogBuilder.setView(view);
        dialog = new OvkAlertDialog(this);
        dialog.build(dialogBuilder, getResources().getString(R.string.crash_title), null, view);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(R.string.show_crash_report),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showReport();
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.restart_app),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartApp();
                    }
                });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (CrashReporterActivity.this.view.findViewById(R.id.crash_report)
                                    .getVisibility() == View.GONE) {
                                ((Button) view).setText(getResources().getString(R.string.hide_crash_report));
                                showReport();
                            } else {
                                ((Button) view).setText(getResources().getString(R.string.show_crash_report));
                                hideReport();
                            }
                        }
                    });
                }
            });
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    private void writeLog() throws IOException {
        Process process = Runtime.getRuntime().exec("logcat -d");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        final StringBuilder log = new StringBuilder();
        String line;
        OvkApplication ovk = ((OvkApplication) getApplicationContext());
        SharedPreferences instance_prefs = getSharedPreferences("instance", 0);
        SharedPreferences global_prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String server;
        String usingHTTPS;
        String isTablet;
        if(instance_prefs.contains("server")) {
            server = instance_prefs.getString("server", "");
        } else {
            server = "N/A";
        }
        if(global_prefs.getBoolean("useHTTPS", false)) {
            usingHTTPS = "Yes";
        } else {
            usingHTTPS = "No";
        }
        if(ovk.isTablet) {
            isTablet = "Yes";
        } else {
            isTablet = "No";
        }
        String header = String.format(
                "OpenVK Legacy %s (%s)\r\n" +
                        "==============================================" +
                        "\r\nDEVICE" +
                        "\r\nDevice: %s %s (codename: %s)" +
                        "\r\nAndroid: %s (API %s)\r\n" +
                        "==============================================" +
                        "\r\nAPP SETTINGS" +
                        "\r\nInstance: %s" +
                        "\r\nHTTPS: %s" +
                        "\r\nTablet UI?: %s\r\n" +
                        "==============================================\r\n",
                ovk.version, BuildConfig.GITHUB_COMMIT, Build.BRAND, Build.MODEL, Build.DEVICE,
                Build.VERSION.RELEASE, Build.VERSION.SDK_INT, server, usingHTTPS, isTablet);
        int lines_count = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains("E ACRA") || line.contains("E AndroidRuntime")) {
                log.append(line).append("\r\n");
            }
        }
        if(log.length() == 0) {
            log.append("ERROR: Logcat not supported or not allowed. Please enable USB debugging.");
        }
        final String crash_report = String.format("%s%s", header, log.toString());
        ((EditText) view.findViewById(R.id.crash_report)).setText(crash_report);
        view.findViewById(R.id.crash_report).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(
                        getApplicationContext(), getResources().getString(R.string.text_copied),
                        Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if(clipboard != null) {
                        clipboard.setText(crash_report);
                    }
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip =
                            android.content.ClipData.newPlainText("OpenVK Legacy crash report", crash_report);
                    if(clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        //super.onClick(dialog, which);
        if(which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
        } else if(which == DialogInterface.BUTTON_NEUTRAL) {
            restartApp();
        } else if(which == DialogInterface.BUTTON_POSITIVE) {
            showReport();
        }
    }

    private void restartApp() {
        Intent activity = new Intent(getApplicationContext(), MainActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activity);
        System.exit(0);
    }

    private void showReport() {
        view.findViewById(R.id.crash_report).setVisibility(View.VISIBLE);
    }

    private void hideReport() {
        view.findViewById(R.id.crash_report).setVisibility(View.GONE);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        dialog.dismiss();
    }
}
