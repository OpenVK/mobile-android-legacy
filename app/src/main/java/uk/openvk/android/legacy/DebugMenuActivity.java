package uk.openvk.android.legacy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DebugMenuActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_debug);
        Preference logToFile = (Preference) findPreference("logToFile");
        logToFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                        Process process = Runtime.getRuntime().exec("logcat -d");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        StringBuilder log = new StringBuilder();
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line + "\r\n");
                        }
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), "OpenVK");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/OpenVK", "App Logs");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            Date dt = new Date(System.currentTimeMillis());
                            File file = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(dt) + ".log");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            Log.d("OpenVK Legacy", "Log file created!");
                            FileWriter writer = new FileWriter(file);
                            writer.append(log);
                            writer.flush();
                            writer.close();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.saved_logs_successfully, "Documents/OpenVK/App Logs"), Toast.LENGTH_LONG).show();
                        } else {
                            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "OpenVK");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OpenVK", "App Logs");
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }
                            try {
                                Date dt = new Date(System.currentTimeMillis());
                                File file = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(dt) + ".log");
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                Log.d("OpenVK Legacy", "Log file created!");
                                FileWriter writer = null;
                                writer = new FileWriter(file);
                                writer.append(log);
                                writer.flush();
                                writer.close();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.saved_logs_successfully, "OpenVK/App Logs"), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.e("OpenVK Legacy", "Could not save log to file: " + e.getMessage());
                                e.printStackTrace();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    AlertDialog dialog;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DebugMenuActivity.this);
                                    builder.setTitle(getResources().getString(R.string.allow_permisssion_in_storage_title));
                                    builder.setMessage(getResources().getString(R.string.allow_permisssion_in_storage));
                                    builder.setPositiveButton(getResources().getString(R.string.open_btn), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    });
                                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            return;
                                        }
                                    });
                                    dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
}
