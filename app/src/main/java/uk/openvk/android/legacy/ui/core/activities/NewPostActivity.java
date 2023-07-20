package uk.openvk.android.legacy.ui.core.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.models.PhotoUploadParams;
import uk.openvk.android.legacy.api.models.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.ui.list.adapters.UploadableFilesAdapter;
import uk.openvk.android.legacy.ui.list.items.UploadableFile;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;
import uk.openvk.android.legacy.utils.RealPathUtil;

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

public class NewPostActivity extends TranslucentActivity {
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
    public OpenVKAPI ovk_api;
    private Wall wall;
    public Handler handler;
    private long account_id;
    private String account_first_name;
    private ArrayList<UploadableFile> files;
    private UploadableFilesAdapter filesAdapter;
    private UploadableFile file;
    private Menu activity_menu;

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
            } else {
                owner_id = extras.getLong("owner_id");
                account_id = extras.getLong("account_id");
                account_first_name = extras.getString("account_first_name");
                installLayouts();
                global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
                ovk_api = new OpenVKAPI(this, global_prefs, instance_prefs);
                global_prefs_editor = global_prefs.edit();
                instance_prefs_editor = instance_prefs.edit();
                inputStream_isClosed = false;
                server = getApplicationContext().getSharedPreferences("instance", 0)
                        .getString("server", "");
                auth_token = getApplicationContext().getSharedPreferences("instance", 0)
                        .getString("auth_token", "");
                if (owner_id == 0) {
                    finish();
                }

                handler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        final Bundle data = message.getData();
                        if(!BuildConfig.BUILD_TYPE.equals("release"))
                            Log.d(OvkApplication.APP_TAG, String.format("Handling API message: %s",
                                    message.what));
                        if(message.what == HandlerMessages.PARSE_JSON){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ovk_api.wrapper.parseJSONData(data, NewPostActivity.this);
                                }
                            }).start();
                        } else {
                            receiveState(message.what, data);
                        }
                    }
                };
                response_sb = new StringBuilder();
                ovk_api.photos.getOwnerUploadServer(ovk_api.wrapper, owner_id);
            }
        }
        createAttachmentsAdapter();
        setUiListeners();
    }

    private void createAttachmentsAdapter() {
        RecyclerView attachments_view = findViewById(R.id.newpost_attachments);
        files = new ArrayList<>();
        filesAdapter = new UploadableFilesAdapter(this, files);
        attachments_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attachments_view.setAdapter(filesAdapter);
    }

    private void setUiListeners() {
        findViewById(R.id.newpost_btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        getResources().getString(R.string.add_photo_gallery)), 4);
            }
        });
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
        global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
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
                    if (statusEditText.getText().toString().length() == 0 &&
                            (files == null || files.size() == 0)) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            connectionDialog = new ProgressDialog(NewPostActivity.this);
                            connectionDialog.setMessage(getString(R.string.loading));
                            connectionDialog.setCancelable(false);
                            connectionDialog.show();
                            if(files.size() > 0) {
                                ovk_api.wall.post(ovk_api.wrapper, owner_id, statusEditText.getText().toString(),
                                        createAttachmentsList());
                            } else {
                                ovk_api.wall.post(ovk_api.wrapper, owner_id, statusEditText.getText().toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            actionBar.setTitle(getResources().getString(R.string.new_status));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
        } else {
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        }

    }

    private String createAttachmentsList() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < files.size(); i++) {
            UploadableFile file = files.get(i);
            if(file.mime.startsWith("image")) {
                Photo photo = file.getPhoto();
                sb.append(String.format("photo%s_%s", photo.owner_id, photo.id));
                if(i < files.size() - 1) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("ConstantConditions")
    private void receiveState(int message, Bundle data) {
        try {
            if(message == HandlerMessages.WALL_POST) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                connectionDialog.cancel();
                finish();
            } else if(message == HandlerMessages.UPLOAD_PROGRESS) {
                String filename = data.getString("filename");
                int pos = filesAdapter.searchByFileName(filename);
                UploadableFile file = files.get(pos);
                file.progress = data.getLong("position");
                file.status = "uploading";
                files.set(pos, file);
                filesAdapter.notifyDataSetChanged();
            } else if(message == HandlerMessages.UPLOADED_SUCCESSFULLY) {
                String filename = data.getString("filename");
                int pos = filesAdapter.searchByFileName(filename);
                file = files.get(pos);
                file.progress = file.length;
                file.status = "uploaded";
                files.set(pos, file);
                filesAdapter.notifyDataSetChanged();
                PhotoUploadParams params = new PhotoUploadParams(data.getString("response"));
                ovk_api.photos.saveWallPhoto(ovk_api.wrapper, params.photo, params.hash);
            } else if(message == HandlerMessages.UPLOAD_ERROR) {
                String filename = data.getString("filename");
                int pos = filesAdapter.searchByFileName(filename);
                file = files.get(pos);
                file.status = "error";
                files.set(pos, file);
                filesAdapter.notifyDataSetChanged();
            } else if(message == HandlerMessages.PHOTOS_SAVE) {
                try {
                    int pos = filesAdapter.searchByFileName(file.filename);
                    files.get(pos).setPhoto(ovk_api.photos.list.get(0));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        if (activity_menu != null && activity_menu.size() >= 1) {
                            activity_menu.getItem(0).setEnabled(true);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if(message == HandlerMessages.ACCESS_DENIED){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posting_access_denied), Toast.LENGTH_LONG).show();
                connectionDialog.cancel();
            } else if(message < 0){
                if(data.containsKey("method")) {
                    if (data.getString("method").equals("Wall.post")) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                        if (connectionDialog != null)
                            connectionDialog.cancel();
                    } else if(data.getString("method").startsWith("Photos.save")) {
                        try {
                            int pos = filesAdapter.searchByFileName(file.filename);
                            if(files.get(pos).status.equals("uploading") || files.get(pos).status.equals("uploaded")) {
                                UploadableFile file = files.get(pos);
                                file.status = "error";
                                files.set(pos, file);
                                filesAdapter.notifyDataSetChanged();
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                if (activity_menu != null && activity_menu.size() >= 1) {
                                    activity_menu.getItem(0).setEnabled(true);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
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
        activity_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if(item.getItemId() == R.id.sendpost) {
            EditText statusEditText = findViewById(R.id.status_text_edit);
            String post_content = statusEditText.getText().toString();
            if((post_content.length() == 0 || post_content.startsWith(" ")) &&
                    (files == null || files.size() == 0)) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
            } else if(!connection_status) {
                try {
                    connectionDialog = new ProgressDialog(this);
                    connectionDialog.setMessage(getString(R.string.loading));
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    if(files == null || files.size() == 0) {
                        ovk_api.wall.post(ovk_api.wrapper, owner_id, post_content);
                    } else {
                        ovk_api.wall.post(ovk_api.wrapper, owner_id, post_content, createAttachmentsList());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 4) {
            if (ovk_api.photos.ownerPhotoUploadServer == null ||
                    ovk_api.photos.ownerPhotoUploadServer.length() == 0) {
                Toast.makeText(this, R.string.err_text, Toast.LENGTH_LONG).show();
                return;
            } else if(data == null || data.getData() == null) {
                return;
            }
            Uri uri = data.getData();
            String path = uriToFilename(uri);
            File file = new File(path);
            if(file.exists()) {
                findViewById(R.id.newpost_attachments).setVisibility(View.VISIBLE);
                UploadableFile upload_file = new UploadableFile(uriToFilename(uri), file);
                upload_file.length = file.length();
                Log.d(OvkApplication.APP_TAG, "Filesize: " + upload_file.length + " bytes");
                files.add(upload_file);
                filesAdapter.notifyDataSetChanged();
                ovk_api.ulman.uploadFile(ovk_api.photos.ownerPhotoUploadServer, file, "");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if(activity_menu != null && activity_menu.size() >= 1) {
                        activity_menu.getItem(0).setEnabled(false);
                    }
                }
            } else {
                Log.e(OvkApplication.APP_TAG, String.format("'%s' not found!", path));
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String uriToFilename(Uri uri) {
        return RealPathUtil.getRealPathFromURI(this, uri);
    }

}