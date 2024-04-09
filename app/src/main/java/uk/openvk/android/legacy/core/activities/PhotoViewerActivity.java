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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.ui.list.adapters.PhotosListAdapter;
import uk.openvk.android.legacy.ui.views.ProgressLayout;
import uk.openvk.android.legacy.ui.views.base.ZoomableImageView;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

public class PhotoViewerActivity extends NetworkActivity {
    private String access_token;
    private int owner_id;
    private int post_id;
    private Bitmap bitmap;
    private Menu activity_menu;
    private BitmapFactory.Options bfOptions;
    private ActionBar actionBar;
    private PopupWindow popupMenu;
    private String instance;
    private boolean isFullScreenMode;
    private DisplayImageOptions displayimageOptions;
    private ImageLoaderConfiguration imageLoaderConfig;
    private ImageLoader imageLoader;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = ((OvkApplication) getApplicationContext()).getCurrentInstance();

        if(getIntent().getExtras() == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(R.layout.activity_photo_viewer);
        actionBar = findViewById(R.id.actionbar);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.photo));
                    getActionBar().setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.bg_actionbar_black_transparent));
                    getActionBar().hide();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setTitle(R.string.photo);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setBackgroundDrawable(getResources().
                    getDrawable(R.drawable.bg_actionbar_black_transparent));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.AbstractAction(0) {
                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            createActionPopupMenu(activity_menu);
        }

        findViewById(R.id.picture_view).setVisibility(View.GONE);
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((ProgressLayout) findViewById(R.id.progress_layout)).enableDarkTheme(true, 1);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Log.e(OvkApplication.APP_TAG, "PhotoViewerActivity: Bundle is empty!");
                finish();
            } else {
                access_token = instance_prefs.getString("access_token", "");
                try {
                    if (extras.containsKey("original_link") && extras.getString("original_link").length() > 0) {
                        ovk_api.dlman.setForceCaching(global_prefs.getBoolean("forcedCaching", true));
                        ovk_api.dlman.downloadOnePhotoToCache(extras.getString("original_link"),
                                String.format("original_photo_a%s_%s", extras.getLong("author_id"),
                                        extras.getLong("photo_id")), "original_photos");
                    } else {
                        Log.e(OvkApplication.APP_TAG, "PhotoViewerActivity: Empty original link");
                        finish();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    finish();
                }
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
            Log.e(OvkApplication.APP_TAG, "PhotoViewerActivity: Empty token");
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void createActionPopupMenu(final Menu menu) {
        @SuppressLint("InflateParams")
        final View menu_container =
                getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
        final ActionBar actionBar = findViewById(R.id.actionbar);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }

    public void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.ACCESS_DENIED_MARSHMALLOW) {
            Global.allowPermissionDialog(this, false);
        } else if(message == HandlerMessages.ORIGINAL_PHOTO) {
            bfOptions = new BitmapFactory.Options();
            bfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                Bundle extras = getIntent().getExtras();
                assert extras != null;
                bitmap = BitmapFactory.decodeFile(
                        String.format("%s/%s/photos_cache/original_photos/original_photo_a%s_%s",
                                getCacheDir().getAbsolutePath(), instance, extras.getLong("author_id"),
                                extras.getLong("photo_id")), bfOptions);
                int max_size = 2880;
                if(getResources().getDisplayMetrics().widthPixels <= 720) {
                    max_size = 1536;
                } else if(getResources().getDisplayMetrics().widthPixels <= 480) {
                    max_size = 960;
                }
                float aspect_ratio = (float)bitmap.getWidth() / (float)max_size;
                if(bitmap.getWidth() > max_size || bitmap.getHeight() > max_size) {
                    Bitmap photo_scaled;
                    int w_scaled = (int)(bitmap.getHeight() / aspect_ratio);
                    if(bitmap.getWidth() > bitmap.getHeight()) { // Landscape
                        photo_scaled = Bitmap.createScaledBitmap(
                                bitmap,
                                max_size,
                                w_scaled,
                                false
                        );
                    } else {
                        photo_scaled = Bitmap.createScaledBitmap(
                                bitmap,
                                max_size,
                                max_size,
                                false
                        );
                    }
                    ((ZoomableImageView) findViewById(R.id.picture_view)).setImageBitmap(photo_scaled);
                } else {
                    ((ZoomableImageView) findViewById(R.id.picture_view)).setImageBitmap(bitmap);
                }

                ((ZoomableImageView) findViewById(R.id.picture_view)).enablePinchToZoom();

                findViewById(R.id.picture_view).setVisibility(View.VISIBLE);
                findViewById(R.id.progress_layout).setVisibility(View.GONE);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if(getActionBar() != null) {
                        getActionBar().show();
                    }
                } else {
                    actionBar.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.picture_view).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            if(getActionBar().isShowing()) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableFullScreenMode();
                                    }
                                }, 400);
                                getActionBar().hide();
                            } else {
                                enableFullScreenMode();
                                handler.postDelayed(new Runnable() {
                                    @SuppressLint("NewApi")
                                    @Override
                                    public void run() {
                                        getActionBar().show();
                                    }
                                }, 200);
                            }
                        } else {
                            if(actionBar.getVisibility() == View.VISIBLE) {
                                actionBar.setVisibility(View.GONE);
                            } else {
                                actionBar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            } catch (OutOfMemoryError err) {
                finish();
            }
        } else if(message == 40000) {
            ((ZoomableImageView) findViewById(R.id.picture_view)).rescale();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        if(item.getItemId() == R.id.save) {
            savePhoto();
        } else if(item.getItemId() == R.id.copy_link) {
            Bundle data = getIntent().getExtras();
            if(data != null) {
                if (data.containsKey("original_link")) {
                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setText(data.getString("original_link"));
                        }
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Photo URL",
                                data.getString("original_link"));
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                }
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void savePhoto() {
        Global global = new Global();
        final Bundle data = getIntent().getExtras();
        if(getIntent().getExtras() == null)
            return;
        String cache_path = String.format("%s/%s/photos_cache/original_photos/original_photo_a%s_%s",
                getCacheDir().getAbsolutePath(), instance, getIntent().getExtras().getLong("author_id"),
                getIntent().getExtras().getLong("photo_id"));
        File file = new File(cache_path);
        String[] path_array = cache_path.split("/");
        String dest = String.format("%s/OpenVK/Photos/%s", Environment.getExternalStorageDirectory()
                .getAbsolutePath(), path_array[path_array.length - 1]);
        String mime = bfOptions.outMimeType;
        if(bitmap != null) {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            if (mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/gif")) {
                try {
                    switch (mime) {
                        case "image/jpeg":
                            dest = dest + ".jpg";
                            break;
                        case "image/png":
                            dest = dest + ".png";
                            break;
                        case "image/gif":
                            dest = dest + ".gif";
                            break;
                    }

                    File directory = new File(
                            Environment.getExternalStorageDirectory().getAbsolutePath(),
                            "OpenVK"
                    );
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    directory = new File(
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/OpenVK",
                            "Photos"
                    );
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    sourceChannel = new FileInputStream(file).getChannel();
                    destChannel = new FileOutputStream(dest).getChannel();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getApplicationContext().checkSelfPermission(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                        } else {
                            Global.allowPermissionDialog(this, false);
                        }
                    } else {
                        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    }
                    Toast.makeText(getApplicationContext(),
                            R.string.photo_save_ok, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Global.allowPermissionDialog(this, false);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                    }
                } finally {
                    try {
                        if(sourceChannel != null && destChannel != null) {
                            sourceChannel.close();
                            destChannel.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_viewer, menu);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            createActionPopupMenu(menu);
        }
        activity_menu = menu;
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    protected void setTranslucentStatusBar() {
        resetTranslucentStatusBar();
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            super.setLegacyTranslucentStatusBar(1, Color.parseColor("#D8000000"));
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.parseColor("#D8000000"));
            getWindow().setStatusBarColor(Color.parseColor("#D8000000"));
        } else
            super.setLegacyTranslucentStatusBar(1, Color.parseColor("#A5000000"));
    }

    protected void enableFullScreenMode() {
        if (!isFullScreenMode) {
            resetTranslucentStatusBar();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setTranslucentStatusBar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
        isFullScreenMode = !isFullScreenMode;
    }

    @Override
    protected void resetTranslucentStatusBar() {
        super.resetTranslucentStatusBar();
    }
}
