package uk.openvk.android.legacy.user_interface.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ProgressLayout;
import uk.openvk.android.legacy.user_interface.layouts.ZoomableImageView;

public class PhotoViewerActivity extends Activity {
    private String access_token;
    private SharedPreferences instance_prefs;
    private int owner_id;
    private int post_id;
    private Bitmap bitmap;
    private Menu activity_menu;
    public Handler handler;
    private BitmapFactory.Options bfOptions;
    private DownloadManager downloadManager;
    private ActionBarImitation actionBarImitation;
    private PopupWindow popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.photo_viewer_layout);
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.photo));
                    getActionBar().hide();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
                    }
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
            actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibility(true);
            actionBarImitation.enableTransparentTheme(true);
            actionBarImitation.setTitle(getResources().getString(R.string.photo));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            android.support.v7.widget.PopupMenu p  = new android.support.v7.widget.PopupMenu(this, null);
            activity_menu = p.getMenu();
            getMenuInflater().inflate(R.menu.photo_viewer, activity_menu);
            createActionPopupMenu(activity_menu);
            actionBarImitation.setVisibility(View.GONE);
        }

        ((ZoomableImageView) findViewById(R.id.picture_view)).setVisibility(View.GONE);
        ((ProgressLayout) findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        ((ProgressLayout) findViewById(R.id.progress_layout)).enableDarkTheme();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
            } else {
                access_token = instance_prefs.getString("access_token", "");
                try {
                    if (extras.getString("original_link").length() > 0) {
                        downloadManager = new DownloadManager(this, true);
                        downloadManager.downloadOnePhotoToCache(extras.getString("original_link"), String.format("original_photo_%d", extras.getLong("photo_id")), "original_photos");
                    } else {
                        finish();
                    }
                } catch (Exception ex) {
                    finish();
                }
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }
    }

    private void createActionPopupMenu(final Menu menu) {
        final View menu_container = (View) getLayoutInflater().inflate(R.layout.popup_menu, null);
        popupMenu = new PopupWindow(menu_container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupMenu.setOutsideTouchable(true);
        popupMenu.setFocusable(true);
        final ListView menu_list = (ListView) popupMenu.getContentView().findViewById(R.id.popup_menulist);
        actionBarImitation.createOverflowMenu(true, menu, new View.OnClickListener() {
            @SuppressLint("RtlHardcoded")
            @Override
            public void onClick(View v) {
                if(popupMenu.isShowing()) {
                    popupMenu.dismiss();
                } else {
                    menu_list.setAdapter(actionBarImitation.overflow_adapter);
                    popupMenu.showAtLocation(actionBarImitation.findViewById(R.id.action_btn2_actionbar2), Gravity.TOP | Gravity.RIGHT, 0, 100);
                }
            }
        });
        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemSelected(0, menu.getItem(position));
                popupMenu.dismiss();
            }
        });
        ((LinearLayout) popupMenu.getContentView().findViewById(R.id.overlay_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.dismiss();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.ACCESS_DENIED_MARSHMALLOW) {
            allowPermissionDialog();
        } else if(message == HandlerMessages.ORIGINAL_PHOTO) {
            bfOptions = new BitmapFactory.Options();
            bfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                Bundle extras = getIntent().getExtras();
                bitmap = BitmapFactory.decodeFile(String.format("%s/original_photos/original_photo_%d", getCacheDir().getAbsolutePath(), extras.getLong("photo_id")), bfOptions);
                ((ZoomableImageView) findViewById(R.id.picture_view)).setImageBitmap(bitmap);
                ((ZoomableImageView) findViewById(R.id.picture_view)).enablePinchToZoom();
                ((ZoomableImageView) findViewById(R.id.picture_view)).setVisibility(View.VISIBLE);
                ((ProgressLayout) findViewById(R.id.progress_layout)).setVisibility(View.GONE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().show();
                } else {
                    actionBarImitation.setVisibility(View.VISIBLE);
                }
                ((ZoomableImageView) findViewById(R.id.picture_view)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WindowManager.LayoutParams attrs = getWindow().getAttributes();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            if(getActionBar().isShowing()) {
                                getActionBar().hide();
                                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            } else {
                                getActionBar().show();
                                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            }
                        } else {
                            if(actionBarImitation.getVisibility() == View.VISIBLE) {
                                actionBarImitation.setVisibility(View.GONE);
                            } else {
                                actionBarImitation.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            } catch (OutOfMemoryError err) {
                finish();
            }
        }
    }

    private void allowPermissionDialog() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            if(data.containsKey("original_link")) {
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(data.getString("original_link"));
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Photo URL", data.getString("original_link"));
                    clipboard.setPrimaryClip(clip);
                }
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void savePhoto() {
        Global global = new Global();
        final Bundle data = getIntent().getExtras();
        String cache_path = String.format("%s/original_photos/original_photo_%d", getCacheDir().getAbsolutePath(), getIntent().getExtras().getLong("photo_id"));
        File file = new File(cache_path);
        String[] path_array = cache_path.split("/");
        String dest = String.format("%s/OpenVK/Photos/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), path_array[path_array.length - 1]);
        String mime = bfOptions.outMimeType;
        if(bitmap != null) {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            if (mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/gif")) {
                try {
                    if(mime.equals("image/jpeg")) {
                        dest = dest + ".jpg";
                    } else if(mime.equals("image/png")) {
                        dest = dest + ".png";
                    } else if(mime.equals("image/gif")) {
                        dest = dest + ".gif";
                    }

                    File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "OpenVK");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OpenVK", "Photos");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    sourceChannel = new FileInputStream(file).getChannel();
                    destChannel = new FileOutputStream(dest).getChannel();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                        } else {
                            allowPermissionDialog();
                        }
                    } else {
                        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    }
                    Toast.makeText(getApplicationContext(), R.string.photo_save_ok, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        allowPermissionDialog();
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
}
