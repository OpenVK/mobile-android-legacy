package uk.openvk.android.legacy.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.layouts.ProfileLayout;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DebugMenuActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_debug);
        setContentView(R.layout.custom_preferences_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(R.string.debug_menu);
            final ImageButton back_btn = findViewById(R.id.backButton);
            final ImageButton ovk_btn = findViewById(R.id.ovkButton);
            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            ovk_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            titlebar_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }

        Preference logToFile = (Preference) findPreference("logToFile");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            logToFile.setEnabled(false);
            logToFile.setSummary(getResources().getString(R.string.debug_incompatibillity_error));
        }
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
                            try {
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
                            } catch(Exception e) {
                                Log.e("OpenVK Legacy", "Could not save log to file: " + e.getMessage());
                                e.printStackTrace();
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
                        } else {
                            try {
                                File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "OpenVK");
                                if (!directory.exists()) {
                                    directory.mkdirs();
                                }

                                directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OpenVK", "App Logs");
                                if (!directory.exists()) {
                                    directory.mkdirs();
                                }
                                Date dt = new Date(System.currentTimeMillis());
                                File file = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(dt) + ".log");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if(getApplicationContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                        if (!file.exists()) {
                                            file.createNewFile();
                                        }
                                    } else {
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
                                        return true;
                                    }
                                } else {
                                    if (!file.exists()) {
                                        file.createNewFile();
                                    }
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

        Preference terminate_process = findPreference("terminate");
        terminate_process.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return false;
            }
        });

    }

    private void resizeTranslucentLayout() {
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusbarView = findViewById(R.id.statusbarView);
            LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) statusbarView.getLayoutParams();
            int statusbar_height = getResources().getIdentifier("status_bar_height", "dimen", "android");
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{android.R.attr.actionBarSize});
            int actionbar_height = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            if (statusbar_height > 0) {
                ll_layoutParams.height = getResources().getDimensionPixelSize(statusbar_height) + actionbar_height;
            }
            statusbarView.setLayoutParams(ll_layoutParams);
        } catch (Exception ex) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusbarView = findViewById(R.id.statusbarView);
            statusbarView.setVisibility(View.GONE);
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }
    }
}
