/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.twemojicon.EmojiconGridFragment;
import dev.tinelix.twemojicon.EmojiconsFragment;
import dev.tinelix.twemojicon.emoji.Emojicon;
import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Note;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.client.models.PhotoUploadParams;
import uk.openvk.android.client.models.Wall;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.legacy.ui.list.adapters.UploadableAttachmentsAdapter;
import uk.openvk.android.legacy.ui.list.items.UploadableAttachment;
import uk.openvk.android.legacy.ui.views.base.XLinearLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;
import uk.openvk.android.legacy.utils.RealPathUtil;

import static uk.openvk.android.legacy.ui.list.items.UploadableAttachment.Result.RESULT_ATTACH_AUDIO;
import static uk.openvk.android.legacy.ui.list.items.UploadableAttachment.Result.RESULT_ATTACH_LOCAL_PHOTO;
import static uk.openvk.android.legacy.ui.list.items.UploadableAttachment.Result.RESULT_ATTACH_NOTE;
import static uk.openvk.android.legacy.ui.list.items.UploadableAttachment.Result.RESULT_ATTACH_VIDEO;

public class NewPostActivity extends NetworkFragmentActivity implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnKeyboardStateListener {

    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public boolean connection_status;
    public Boolean inputStream_isClosed;
    public long owner_id;
    public Handler handler;
    private long account_id;
    private String account_first_name;
    private ArrayList<UploadableAttachment> attachments;
    private UploadableAttachmentsAdapter attachmentsAdapter;
    private UploadableAttachment attach;
    private Menu activity_menu;

    private int minKbHeight;
    private int keyboard_height;
    private boolean[] post_settings = new boolean[]{false, false};

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(((OvkApplication) getApplicationContext()).isTablet) {
            enableDialogMode();
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(getActionBar() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setHomeButtonEnabled(true);
                }
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setTitle(getResources().getString(R.string.new_status));
            }
        }

        setEmojiconFragment(false);
        if(((OvkApplication) getApplicationContext()).isTablet) {
            minKbHeight = (int) (320 * getResources().getDisplayMetrics().scaledDensity);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (200 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (160 * getResources().getDisplayMetrics().scaledDensity);
        }

        ((XLinearLayout) findViewById(R.id.newpost_root)).setOnKeyboardStateListener(this);

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

                inputStream_isClosed = false;
                if (owner_id == 0) {
                    finish();
                    return;
                } else {
                    ovk_api.photos.getOwnerUploadServer(ovk_api.wrapper, owner_id);
                }

                response_sb = new StringBuilder();
            }
        }

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = getWindow().getDecorView().getHeight();
                        Rect r = new Rect();
                        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                        int visible = r.bottom - r.top;
                        if(height - visible >= minKbHeight) {
                            keyboard_height = height - visible - 60;
                        }
                    }
                }
        );
        createAttachmentsAdapter();
        setUiListeners();
    }

    private void enableDialogMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = getWindow().getAttributes();

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.height = (int) (700 * getResources().getDisplayMetrics().scaledDensity);
            } else {
                params.height = (int) (720 * getResources().getDisplayMetrics().scaledDensity);
            }

            params.width = (int) (600 * getResources().getDisplayMetrics().scaledDensity);
            params.alpha = 1.0f;
            params.dimAmount = 0.5f;
            getWindow().setAttributes(params);
        }
    }

    private void createAttachmentsAdapter() {
        RecyclerView attachments_view = findViewById(R.id.newpost_attachments);
        attachments = new ArrayList<>();
        attachmentsAdapter = new UploadableAttachmentsAdapter(this, attachments);
        attachments_view.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)
        );
        attachments_view.setAdapter(attachmentsAdapter);
    }

    private void setUiListeners() {
        findViewById(R.id.newpost_btn_photo).setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("IntentReset")
                    @Override
                    public void onClick(View v) {
                        Intent intent =
                                new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, RESULT_ATTACH_LOCAL_PHOTO);
                    }
        });

        findViewById(R.id.newpost_btn_attach).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAttachMenuDialog();
                    }
        });

        findViewById(R.id.newpost_btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPostSettingsDialog();
            }
        });

        (findViewById(R.id.emoji_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.emojicons).getVisibility() == View.GONE) {
                    View view = NewPostActivity.this.getCurrentFocus();
                    if (view != null) {
                        if (keyboard_height >= minKbHeight) {
                            findViewById(R.id.emojicons).getLayoutParams().height = keyboard_height;
                        } else {
                            findViewById(R.id.emojicons).getLayoutParams().height = minKbHeight;
                        }
                        Log.d(OvkApplication.APP_TAG, String.format("KB height: %s",
                                findViewById(R.id.emojicons).getLayoutParams().height));
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.emojicons).setVisibility(View.VISIBLE);
                                if(!((OvkApplication) getApplicationContext()).isTablet) {
                                    findViewById(R.id.attach_buttons).setVisibility(View.GONE);
                                }
                            }
                        }, 200);

                    } else {

                        if(!((OvkApplication) getApplicationContext()).isTablet) {
                            findViewById(R.id.emojicons).getLayoutParams().height = minKbHeight;
                        }

                        findViewById(R.id.emojicons).setVisibility(View.VISIBLE);
                    }
                } else {
                    findViewById(R.id.emojicons).setVisibility(View.GONE);

                    if(!((OvkApplication) getApplicationContext()).isTablet) {
                        findViewById(R.id.attach_buttons).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void openPostSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> options = new ArrayList<>();
        builder.setTitle(R.string.post_options);
        options.add(getResources().getString(R.string.post_from_group));
        options.add(getResources().getString(R.string.post_from_group_signed));
        builder.setMultiChoiceItems(
                options.toArray(new String[options.size()]),
                post_settings,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        post_settings[i] = b;
                    }
        });
        builder.setNeutralButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.getListView().setOnHierarchyChangeListener(
                new ViewGroup.OnHierarchyChangeListener() {
                    @SuppressWarnings({"SuspiciousMethodCalls", "ConstantConditions"})
                    @Override
                    public void onChildViewAdded(View parent, View child) {
                        CharSequence text = ((CheckedTextView)child).getText();
                        int itemIndex = Collections.singletonList(options).indexOf(text);
                        if(itemIndex < 2 && owner_id > 0) {
                            child.setEnabled(false);
                            child.setOnClickListener(null);
                        }
                    }

                    @Override
                    public void onChildViewRemoved(View view, View view1) {
                    }
                });
        dialog.show();
    }

    private void showAttachMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> functions = new ArrayList<>();
        builder.setTitle(R.string.attach);
        functions.add(getResources().getString(R.string.video));
        functions.add(getResources().getString(R.string.audio));
        functions.add(getResources().getString(R.string.attach_note_to_post));
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.list_item_select_dialog, R.id.text,
                        functions
                );
        builder.setSingleChoiceItems(adapter, -1, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String functionName = functions.get(position);
                String url = null;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                int attachResult = 0;

                if(functionName.equals(getResources().getString(R.string.video))){
                    attachResult = RESULT_ATTACH_VIDEO;
                    url = "openvk://ovk/videos" + account_id;
                    intent.putExtra("action", "video_picker");
                } else if(functionName.equals(getResources().getString(R.string.audio))){
                    attachResult = RESULT_ATTACH_AUDIO;
                    url = "openvk://ovk/audios" + account_id;
                    intent.putExtra("action", "audio_picker");
                    intent.putExtra("user_first_name", ovk_api.account.first_name);
                } else if(functionName.equals(getResources().getString(R.string.attach_note_to_post))) {
                    attachResult = RESULT_ATTACH_NOTE;
                    url = "openvk://ovk/notes" + account_id;
                    intent.putExtra("action", "notes_picker");
                }

                if(url != null) {
                    intent.setData(Uri.parse(url));
                    intent.setPackage("uk.openvk.android.legacy");
                    startActivityForResult(intent, attachResult);
                    dialog.dismiss();
                }
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
                            (attachments == null || attachments.size() == 0)) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.post_fail_empty),
                                Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            connectionDialog = new ProgressDialog(NewPostActivity.this);
                            connectionDialog.setMessage(getString(R.string.loading));
                            connectionDialog.setCancelable(false);
                            connectionDialog.show();
                            if(attachments.size() > 0) {
                                ovk_api.wall.post(ovk_api.wrapper, owner_id,
                                        statusEditText.getText().toString(),
                                        post_settings[0], post_settings[1],
                                        createAttachmentsList()
                                );
                            } else {
                                ovk_api.wall.post(ovk_api.wrapper, owner_id,
                                        statusEditText.getText().toString(),
                                        post_settings[0], post_settings[1]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            actionBar.setTitle(getResources().getString(R.string.new_status));
            switch (global_prefs.getString("uiTheme", "blue")) {
                case "Gray":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
                case "Black":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
                    break;
                default:
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
            }
        } else {
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray)
                );
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black)
                );
            }
        }
    }

    private String createAttachmentsList() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < attachments.size(); i++) {
            UploadableAttachment attach = attachments.get(i);
            sb.append(attach.id);
            if(i < attachments.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("ConstantConditions")
    public void receiveState(int message, Bundle data) {
        try {
            if(data.containsKey("address")) {
                String activityName = data.getString("address");
                if(activityName == null) {
                    return;
                }
                boolean isCurrentActivity = activityName.equals(
                        String.format("%s_%s", getLocalClassName(), getSessionId())
                );
                if(!isCurrentActivity) {
                    return;
                }
            }
            if(message == HandlerMessages.WALL_POST) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG
                ).show();
                connectionDialog.cancel();
                finish();
            } else if(message == HandlerMessages.UPLOAD_PROGRESS) {
                String filename = data.getString("filename");
                int pos = attachmentsAdapter.searchByFileName(filename);
                attach = attachments.get(pos);
                attach.progress = data.getLong("position");
                attach.status = "uploading";
                attachments.set(pos, attach);
                attachmentsAdapter.notifyDataSetChanged();
            } else if(message == HandlerMessages.UPLOADED_SUCCESSFULLY) {
                String filename = data.getString("filename");
                PhotoUploadParams params = new PhotoUploadParams(data.getString("response"));
                int pos = attachmentsAdapter.searchByFileName(filename);
                attach = attachments.get(pos);
                attach.progress = attach.length;
                attach.status = "saving";
                attachments.set(pos, attach);
                attachmentsAdapter.notifyDataSetChanged();
                ovk_api.photos.saveWallPhoto(ovk_api.wrapper, params.photo, params.hash);
            } else if(message == HandlerMessages.UPLOAD_ERROR) {
                String filename = data.getString("filename");
                int pos = attachmentsAdapter.searchByFileName(filename);
                attach = attachments.get(pos);
                attach.status = "error";
                attachments.set(pos, attach);
                attachmentsAdapter.notifyDataSetChanged();
            } else if(message == HandlerMessages.PHOTOS_SAVE) {
                try {
                    int pos = attachmentsAdapter.searchByFileName(attach.filename);
                    Photo photo = ovk_api.photos.list.get(0);
                    attachments.get(pos).setContent(photo);
                    attach.id = String.format("photo%s_%s", photo.owner_id, photo.id);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        if (activity_menu != null && activity_menu.size() >= 1) {
                            activity_menu.getItem(0).setEnabled(true);
                        }
                    }
                    attach.status = "uploaded";
                    attachments.set(pos, attach);
                    attachmentsAdapter.notifyDataSetChanged();
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
                            int pos = attachmentsAdapter.searchByFileName(attach.filename);
                            if(attachments.get(pos).status.equals("uploading") ||
                                    attachments.get(pos).status.equals("uploaded")) {
                                attach = attachments.get(pos);
                                attach.status = "error";
                                attachments.set(pos, attach);
                                attachmentsAdapter.notifyDataSetChanged();
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
                    (attachments == null || attachments.size() == 0)) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.post_fail_empty), Toast.LENGTH_LONG).show();
            } else if(!connection_status) {
                try {
                    connectionDialog = new ProgressDialog(this);
                    connectionDialog.setMessage(getString(R.string.loading));
                    connectionDialog.setCancelable(false);
                    connectionDialog.show();
                    if(attachments == null || attachments.size() == 0) {
                        ovk_api.wall.post(ovk_api.wrapper, owner_id, post_content,
                                post_settings[0], post_settings[1]);
                    } else {
                        ovk_api.wall.post(ovk_api.wrapper, owner_id, post_content,
                                post_settings[0], post_settings[1], createAttachmentsList());
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
        UploadableAttachment attach = new UploadableAttachment();

        switch (requestCode) {
            case RESULT_ATTACH_LOCAL_PHOTO:
                if (ovk_api.photos.ownerPhotoUploadServer == null ||
                        ovk_api.photos.ownerPhotoUploadServer.length() == 0) {
                    Toast.makeText(this, R.string.err_text, Toast.LENGTH_LONG).show();
                    return;
                } else if (data == null || data.getData() == null) {
                    return;
                }

                Uri uri = data.getData();

                try {
                    String path = uriToFilename(uri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            uploadFile(path);
                        } else {
                            Global.allowPermissionDialog(this, true);
                        }
                    } else {
                        uploadFile(path);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                }
                break;
            case RESULT_ATTACH_NOTE:
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    if (extras.containsKey("attachment")) {
                        attach.type = "note";
                        attach.id = extras.getString("attachment");
                        Note note = new Note();
                        note.id = extras.getLong("note_id");
                        note.owner_id = extras.getLong("owner_id");
                        note.title = extras.getString("note_title");
                        attach.setContent(note);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_ATTACH_VIDEO:
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    if (extras.containsKey("attachment")) {
                        attach.type = "video";
                        attach.id = extras.getString("attachment");
                        Video video = new Video();
                        video.id = extras.getLong("video_id");
                        video.owner_id = extras.getLong("owner_id");
                        video.title = extras.getString("video_title");
                        attach.setContent(video);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RESULT_ATTACH_AUDIO:
                if (data != null && data.getExtras() != null) {
                    Bundle extras = data.getExtras();
                    if (extras.containsKey("attachment")) {
                        attach.type = "audio";
                        attach.id = extras.getString("attachment");
                        Audio audio = new Audio();
                        audio.id = extras.getLong("audio_id");
                        audio.owner_id = extras.getLong("audio_id");
                        audio.title = extras.getString("audio_title");
                        attach.setContent(audio);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }

        if(attach.type != null) {
            attachments.add(attach);
            attachmentsAdapter.notifyDataSetChanged();
            findViewById(R.id.msg_attachments).setVisibility(View.VISIBLE);
        }
    }

    private void uploadFile(String path) {
        File file = new File(path);
        if(file.exists()) {
            findViewById(R.id.newpost_attachments).setVisibility(View.VISIBLE);
            UploadableAttachment upload_file = new UploadableAttachment(path, file);
            upload_file.length = file.length();
            Log.d(OvkApplication.APP_TAG, "Filesize: " + upload_file.length + " bytes");
            attachments.add(upload_file);
            attachmentsAdapter.notifyDataSetChanged();
            ovk_api.ulman.uploadFile(ovk_api.photos.ownerPhotoUploadServer, file, path);
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

    private String uriToFilename(Uri uri) {
        return RealPathUtil.getRealPathFromURI(this, uri);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (200 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (160 * getResources().getDisplayMetrics().scaledDensity);
        }
    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input((EditText) findViewById(R.id.status_text_edit), emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace((EditText) findViewById(R.id.status_text_edit));
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.emojicons).getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            findViewById(R.id.emojicons).setVisibility(View.GONE);
        }
    }

    @Override
    public void onKeyboardStateChanged(boolean param1Boolean) {
        if(param1Boolean) findViewById(R.id.emojicons).setVisibility(View.GONE);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            if (param1Boolean)
                findViewById(R.id.attach_buttons).setVisibility(View.GONE);
            else
                findViewById(R.id.attach_buttons).setVisibility(View.VISIBLE);
        }
    }
}