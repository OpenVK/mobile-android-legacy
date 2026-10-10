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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.twemojicon.EmojiconEditText;
import dev.tinelix.twemojicon.EmojiconGridFragment;
import dev.tinelix.twemojicon.EmojiconsFragment;
import dev.tinelix.twemojicon.emoji.Emojicon;
import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.Message;
import uk.openvk.android.client.entities.Note;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.enumerations.UiMessages;
import uk.openvk.android.legacy.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.legacy.receivers.LongPollReceiver;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.list.adapters.MessagesHistoryAdapter;
import uk.openvk.android.legacy.ui.list.adapters.UploadableAttachmentsAdapter;
import uk.openvk.android.legacy.ui.views.ConversationPanel;
import uk.openvk.android.legacy.ui.views.base.XLinearLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

import uk.openvk.android.legacy.ui.list.items.UploadableAttachment;
import uk.openvk.android.legacy.utils.RealPathUtil;

public class ConversationActivity extends NetworkFragmentActivity implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnKeyboardStateListener {

    public Conversation conversation;
    private RecyclerView mMessagesList;
    private RecyclerView mAttachmentsList;
    private MessagesHistoryAdapter mHistoryAdapter;
    private uk.openvk.android.client.entities.Message mLastSendedMsg;
    private LongPollReceiver mLpReceiver;
    private String mLastLongPollMsg;
    private int mKeyboardHeight;
    private int mMinKbHeight;
    private Menu mActivityMenu;
    private ImageView ab_profile_photo;
    private int mMsgCursorId;
    private int mMsgSelected;
    private ArrayList<UploadableAttachment> mAttachments;
    private UploadableAttachmentsAdapter mAttachmentsAdapter;

    public String state;
    public String from;
    public ActionBar actionBar;
    public ArrayList<uk.openvk.android.client.entities.Message> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation_msgs);
        conversation = new Conversation();
        mMessagesList = findViewById(R.id.conversation_msgs_listview);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                conversation.peer_id = extras.getLong("peer_id");
                conversation.peer_type = extras.getString("peer_type");
                conversation.title = extras.getString("conv_title");
                conversation.online = extras.getInt("online");
                conversation.members_count = extras.getLong("members_count");
            }
        } else {
            conversation.peer_id = savedInstanceState.getInt("peer_id");
            conversation.peer_type = savedInstanceState.getString("peer_type");
            conversation.title = (String) savedInstanceState.getSerializable("conv_title");
            conversation.online = savedInstanceState.getInt("online");
            conversation.members_count = savedInstanceState.getLong("members_count");
        }

        if(conversation.peer_type == null) {
            if(conversation.peer_id >= Conversation.PEER_ID_USER_UPPER_START)
                conversation.peer_type = "chat";
            else if(conversation.peer_id > 0)
                conversation.peer_type = "user";
            else
                conversation.peer_type = "group";
        }

        installLayouts();
        setConversationView();
        setEmojiconFragment(false);

        mOpenVK.messages.getConversationById(mOpenVK.wrapper, conversation.peer_id);
        mOpenVK.photos.getOwnerUploadServer(
                mOpenVK.wrapper, ((OvkApplication) getApplicationContext()).getCurrentUserId()
        );

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mMinKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            mMinKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
        }

        ((XLinearLayout) findViewById(R.id.conversation_view)).setOnKeyboardStateListener(this);

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = getWindow().getDecorView().getHeight();
                        Rect r = new Rect();
                        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                        int visible = r.bottom - r.top;
                        if(height - visible >= mMinKbHeight) {
                            mKeyboardHeight = height - visible;
                        }
                    }
                }
        );
    }

    private void installLayouts() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setTitle(conversation.title);
            if(conversation.peer_type.equals("chat"))
                getActionBar().setSubtitle(
                        Global.getPluralQuantityString(
                                this, R.plurals.chat_members, conversation.members_count
                        )
                );
            else
                getActionBar().setSubtitle(conversation.online == 1 ? R.string.online : R.string.offline);

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);

            if(mGlobalPrefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(mGlobalPrefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setTitle(conversation.title);
            actionBar.setSubtitle(conversation.online == 1 ? R.string.online : R.string.offline);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.AbstractAction(0) {
                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            switch (mGlobalPrefs.getString("uiTheme", "blue")) {
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
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = loadConversationAvatar();
                actionBar.setRightLogo(new BitmapDrawable(getResources(), bitmap));
            } catch (OutOfMemoryError oom) {
                oom.printStackTrace();
            }
        }
        actionBar = findViewById(R.id.actionbar);

        createAttachmentsAdapter();
    }

    @SuppressLint({"AppCompatCustomView", "DrawAllocation"})
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SubMenu attachMenu = menu.addSubMenu(
                    0, R.id.attach_to_msg, 0, getResources().getString(R.string.attach)
            );
            attachMenu.setIcon(R.drawable.ic_ab_attach);

            attachMenu.add(
                    0, R.id.attach_photo_item, 0,
                    getResources().getString(R.string.attach_photo_to_post)
            ).setIcon(R.drawable.ic_attach_menu_photo);

            attachMenu.add(
                    0, R.id.attach_audio_item, 0, getResources().getString(R.string.audio)
            ).setIcon(R.drawable.ic_attach_menu_audio);

            attachMenu.add(
                    0, R.id.attach_video_item, 0, getResources().getString(R.string.video)
            ).setIcon(R.drawable.ic_attach_menu_video);

            attachMenu.add(
                    0, R.id.attach_note_item, 0, getResources().getString(R.string.attach_note_to_post)
            ).setIcon(R.drawable.ic_attach_menu_document);

            // Add VK3-like chat photo to right to ActionBar (pre-ViewImageLoader method)
            MenuItem profile_photo = menu.add(0, R.id.profile_photo, 0, R.string.profile);
            profile_photo.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            final ImageView ab_profile_photo = new ImageView(this) {
                @SuppressWarnings("SuspiciousNameCombination")
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    int height = View.MeasureSpec.getSize(heightMeasureSpec);
                    setMeasuredDimension(height, height);
                }
            };

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                ab_profile_photo.setBackground(null);

            Bitmap bitmap = loadConversationAvatar();
            if(bitmap != null)
                ab_profile_photo.setImageBitmap(bitmap);
            else if(conversation.peer_type.equals("chat") || conversation.peer_type.equals("group"))
                ab_profile_photo.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_multi));
            else
                ab_profile_photo.setImageDrawable(getResources().getDrawable(R.drawable.user_placeholder_chat));

            ab_profile_photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ab_profile_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPeerIntent();
                }
            });
            profile_photo.setActionView(ab_profile_photo);
            mActivityMenu = menu;
            handler.sendEmptyMessageDelayed(UiMessages.RIGHT_AVATAR_IN_ACTIONBAR, 20);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem attachMenuItem = menu.findItem(R.id.attach_to_msg);
            attachMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void createAttachmentsAdapter() {
        mAttachmentsList = findViewById(R.id.msg_attachments);
        mAttachments = new ArrayList<>();
        mAttachmentsAdapter = new UploadableAttachmentsAdapter(this, mAttachments);
        mAttachmentsList.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)
        );
        mAttachmentsList.setAdapter(mAttachmentsAdapter);
    }

    private Bitmap loadConversationAvatar() {
        Bitmap bitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.photo_loading
        );
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(
                    String.format(
                            "%s/%s/photos_cache/conversations_avatars/avatar_%s",
                            getCacheDir(), mGlobalPrefs.getString("current_instance", ""),
                            conversation.peer_id
                    ), options
            );
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    break;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void openPeerIntent() {
        if(conversation == null || conversation.peer_type == null)
            return;

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage("uk.openvk.android.legacy");

        if(conversation.peer_type.equals("user"))
            i.setData(Uri.parse("openvk://ovk/id" + conversation.peer_id));
        else if(conversation.peer_type.equals("group"))
            i.setData(Uri.parse("openvk://ovk/club" + -conversation.peer_id));

        if(conversation.peer_type.equals("user") || conversation.peer_type.equals("group"))
            startActivity(i);
    }

    private void setConversationView() {
        final ConversationPanel conversationPanel = findViewById(R.id.conversation_panel);
        final EmojiconEditText editText = conversationPanel.findViewById(R.id.message_edit);

        conversationPanel.findViewById(R.id.emoji_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.emojicons).getVisibility() == View.GONE) {
                    View view = ConversationActivity.this.getCurrentFocus();
                    if (view != null) {
                        if(!((OvkApplication) getApplicationContext()).isTablet) {

                            findViewById(R.id.emojicons).getLayoutParams().height =
                                    mKeyboardHeight >= mMinKbHeight ? mKeyboardHeight : mMinKbHeight;

                            Log.d(OvkApplication.APP_TAG, String.format("KB height: %s",
                                    findViewById(R.id.emojicons).getLayoutParams().height));
                            InputMethodManager imm =
                                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                            view.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.emojicons).setVisibility(View.VISIBLE);
                                }
                            }, 200);
                        } else {
                            findViewById(R.id.emojicons).setVisibility(View.VISIBLE);
                        }
                    } else {
                        if(!((OvkApplication) getApplicationContext()).isTablet) {
                            findViewById(R.id.emojicons).getLayoutParams().height = mMinKbHeight;
                        }
                        findViewById(R.id.emojicons).setVisibility(View.VISIBLE);
                    }
                } else {
                    findViewById(R.id.emojicons).setVisibility(View.GONE);
                }
            }
        });

        ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit))
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if(getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
                    final String msg_text = ((EditText) conversationPanel.findViewById(R.id.message_edit))
                            .getText().toString();
                    if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        try {
                            if(mAttachments.size() > 0)
                                conversation.sendMessage(mOpenVK.wrapper, msg_text, createAttachmentsList());
                            else
                                conversation.sendMessage(mOpenVK.wrapper, msg_text);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        mLastSendedMsg = new uk.openvk.android.client.entities.Message(
                                0, false,
                                (int) (System.currentTimeMillis() / 1000),
                                msg_text
                        );

                        mLastSendedMsg.sending = true;
                        mLastSendedMsg.isError = false;

                        mHistoryAdapter.addMessage(mLastSendedMsg);

                        editText.setText("");

                        mMessagesList.smoothScrollToPosition(0);

                    } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TAB
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        editText.clearFocus();
                        mMessagesList.requestFocus();
                    }
                }
                return true;
            }
        });

        final Button send_btn = conversationPanel.findViewById(R.id.send_btn);
        send_btn.setEnabled(false);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg_text = ((EmojiconEditText) conversationPanel
                        .findViewById(R.id.message_edit)).getText().toString();
                try {
                    conversation.sendMessage(mOpenVK.wrapper, msg_text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                createLastSendedMessage(msg_text);

                ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit)).setText("");
                mMessagesList.smoothScrollToPosition(0);
            }
        });

        ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit))
                .addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                send_btn.setEnabled(editText.getText().toString().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editText.setLines(
                        editText.getLineCount() > 4 ?
                                4 : editText.getLineCount()
                );
            }
        });
    }

    private void createLastSendedMessage(String msgText) {
        mLastSendedMsg = new uk.openvk.android.client.entities.Message(
                0, false, System.currentTimeMillis(),
                msgText
        );
        mLastSendedMsg.sending = true;
        mLastSendedMsg.isError = false;

        if(mHistoryAdapter != null)
            mHistoryAdapter.addMessage(mLastSendedMsg);


    }

    private String createAttachmentsList() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < mAttachments.size(); i++) {
            UploadableAttachment attach = mAttachments.get(i);
            sb.append(attach.id);
            if(i < mAttachments.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private void createAdapter() {

        if(history == null)
            history = new ArrayList<>();

        if(mHistoryAdapter == null) {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setReverseLayout(true);
            mMessagesList.setLayoutManager(llm);
            mHistoryAdapter = new MessagesHistoryAdapter(
                    ConversationActivity.this, conversation, history
            );
            mMessagesList.setAdapter(mHistoryAdapter);
        } else {
            mHistoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.attach_photo_item:
                startPickerActivity(UploadableAttachment.Result.RESULT_ATTACH_LOCAL_PHOTO);
                break;
            case R.id.attach_video_item:
                startPickerActivity(UploadableAttachment.Result.RESULT_ATTACH_VIDEO);
                break;
            case R.id.attach_audio_item:
                startPickerActivity(UploadableAttachment.Result.RESULT_ATTACH_AUDIO);
                break;
            case R.id.attach_note_item:
                startPickerActivity(UploadableAttachment.Result.RESULT_ATTACH_NOTE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPickerActivity(int resultCode) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        String url = null;

        switch (resultCode) {
            case UploadableAttachment.Result.RESULT_ATTACH_LOCAL_PHOTO:
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                break;
            case UploadableAttachment.Result.RESULT_ATTACH_VIDEO:
                intent = new Intent(Intent.ACTION_VIEW);
                url = "openvk://ovk/videos" + mOpenVK.account.id;
                intent.putExtra("action", "video_picker");
                break;
            case UploadableAttachment.Result.RESULT_ATTACH_AUDIO:
                intent = new Intent(Intent.ACTION_VIEW);
                url = "openvk://ovk/audios" + mOpenVK.account.id;
                intent.putExtra("action", "audio_picker");
                break;
            case UploadableAttachment.Result.RESULT_ATTACH_NOTE:
                intent = new Intent(Intent.ACTION_VIEW);
                url = "openvk://ovk/notes" + mOpenVK.account.id;
                intent.putExtra("action", "notes_picker");
                break;
        }

        if(url != null || intent.getType() != null) {
            if(url != null) {
                intent.setData(Uri.parse(url));
                intent.setPackage("uk.openvk.android.legacy");
            }
            startActivityForResult(intent, resultCode);
        }
    }

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
                return;
            }
        }
        if(message == HandlerMessages.MESSAGES_GET_CONVERSATIONS_BY_ID) {
            conversation = mOpenVK.messages.searchConversation(conversation.peer_id);
            if(conversation != null)
                conversation.getHistory(mOpenVK.wrapper, conversation.peer_id);

        } else if(message == HandlerMessages.MESSAGES_GET_HISTORY) {

            createAdapter();

        } else if (message == HandlerMessages.CHAT_DISABLED) {

            mLastSendedMsg.sending = false;
            history.set(history.size() - 1, mLastSendedMsg);
            mHistoryAdapter.notifyDataSetChanged();

        } else if (message == HandlerMessages.MESSAGES_DELETE) {

            history.remove(mMsgCursorId);
            mHistoryAdapter.notifyDataSetChanged();

        } else if(message == HandlerMessages.MESSAGES_SEND) {

            mLastSendedMsg.sending = false;
            mLastSendedMsg.getSendedId(data.getString("response"));
            history.set(history.size() - 1, mLastSendedMsg);
            mHistoryAdapter.notifyDataSetChanged();

        } else if(message == HandlerMessages.LONGPOLL) {

            if(!((OvkApplication) getApplicationContext()).
                    notifMan.isRepeat(mLastLongPollMsg, data.getString("response"))) {
                conversation.getHistory(mOpenVK.wrapper, conversation.peer_id);
            }
            mLastLongPollMsg = data.getString("response");

        } else if(message == UiMessages.RIGHT_AVATAR_IN_ACTIONBAR) {

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(
                        String.format(
                                "%s/%s/photos_cache/conversations_avatars/avatar_%s",
                                getCacheDir(),
                                mGlobalPrefs.getString("current_instance", ""),
                                conversation.peer_id
                        ), options);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (mActivityMenu != null) {
                        ab_profile_photo = mActivityMenu.getItem(0).getActionView()
                                                        .findViewById(R.id.profile_photo);
                        ab_profile_photo.setImageBitmap(bitmap);
                    }
                }
            } catch (OutOfMemoryError | Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public void getMsgContextMenu(final int item_pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> functions = new ArrayList<>();
        builder.setTitle(R.string.message);
        functions.add(getResources().getString(R.string.copy_text));

        if(!mHistoryAdapter.getMessage(item_pos).isIncoming) {
            functions.add(getResources().getString(R.string.delete));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                functions);
        builder.setSingleChoiceItems(adapter, -1, null);
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                String functionStr = functions.get(position);
                if (functionStr.equals(getResources().getString(R.string.delete))) {
                    showDeleteConfirmDialog(item_pos);
                } else if(functionStr.equals(getResources().getString(R.string.copy_text))) {
                    copyMessageTextToClipboard(mHistoryAdapter.getMessage(position));
                }
                dialog.dismiss();
            }
        });
    }

    private void copyMessageTextToClipboard(Message message) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(message.text);
        } else {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager)
                            getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText(
                            "OpenVK Message Text",
                            message.text
                    );
            clipboard.setPrimaryClip(clip);
        }
    }

    private void showDeleteConfirmDialog(final int position) {
        mMsgCursorId = position;
        uk.openvk.android.client.entities.Message msg = mHistoryAdapter.getMessage(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text;
        if(msg.text.length() <= 200) {
            text = msg.text.replace("\n", " ");
        } else {
            text = msg.text.replace("\n", " ").substring(0, 200) + "...";
        }
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOpenVK.messages.delete(mOpenVK.wrapper, history.get(position).id);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.confirm),
                getResources().getString(R.string.delete_msgs_confirm,
                        String.format("\"%s\"", text)), null);
        dialog.show();
    }

    private String uriToFilename(Uri uri) {
        return RealPathUtil.getRealPathFromURI(this, uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        UploadableAttachment attach = new UploadableAttachment();

        if(data == null)
            return;

        Bundle extras = data.getExtras();

        if(extras == null || data.getData() == null)
            return;

        switch (requestCode) {
            case UploadableAttachment.Result.RESULT_ATTACH_LOCAL_PHOTO:
                if (mOpenVK.photos.ownerPhotoUploadServer == null ||
                        mOpenVK.photos.ownerPhotoUploadServer.length() == 0) {
                    Toast.makeText(
                            this, R.string.err_text, Toast.LENGTH_LONG
                    ).show();
                    return;
                } else if (data.getData() == null) {
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
            case UploadableAttachment.Result.RESULT_ATTACH_VIDEO:
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
                break;
            case UploadableAttachment.Result.RESULT_ATTACH_AUDIO:
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
                break;
            case UploadableAttachment.Result.RESULT_ATTACH_NOTE:
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
                break;
        }

        mAttachments.add(attach);
        mAttachmentsAdapter.notifyDataSetChanged();
        mAttachmentsList.setVisibility(View.VISIBLE);
    }

    private void uploadFile(String path) {
        File file = new File(path);

        if(file.exists()) {
            mAttachmentsList.setVisibility(View.VISIBLE);

            UploadableAttachment upload_file = new UploadableAttachment(path, file);
            upload_file.length = file.length();
            mAttachments.add(upload_file);
            mAttachmentsAdapter.notifyDataSetChanged();

            mOpenVK.ulman.uploadFile(mOpenVK.photos.ownerPhotoUploadServer, file, path);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                if (mActivityMenu != null && mActivityMenu.size() >= 1) {
                    mActivityMenu.getItem(0).setEnabled(false);
                }

        } else {
            Log.e(OvkApplication.APP_TAG, String.format("'%s' not found!", path));
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mMinKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            mMinKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
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
        EmojiconsFragment.input((EditText) findViewById(R.id.conversation_panel)
                .findViewById(R.id.message_edit), emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace((EditText) findViewById(R.id.conversation_panel)
                .findViewById(R.id.message_edit));
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
    }

    public void loadMsgHistory(ArrayList<Message> msgHistory) {
        history = msgHistory;
    }

    public void startActionMode(final int position, final boolean selected) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionMode.Callback actionModeCb = new ActionMode.Callback() {

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    if(selected)
                        mMsgSelected++;
                    else if(mMsgSelected > 0)
                        mMsgSelected--;

                    mode.getMenuInflater().inflate(R.menu.chat_action_mode, menu);
                    mode.setTitle(
                            getResources().getString(R.string.selected_n, mMsgSelected)
                    );

                    if(mMsgSelected != 0)
                        setTranslucentStatusBar(0, R.color.holo_action_mode_statusbar_color);
                    else
                        resetTranslucentStatusBar();

                    if(mMsgSelected > 1)
                        menu.findItem(R.id.copy).setVisible(false);

                    return mMsgSelected != 0;
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.copy:
                            copyMessageTextToClipboard(mHistoryAdapter.getMessage(position));
                            break;
                        case R.id.delete:
                            showDeleteConfirmDialog(position);
                            break;
                    }
                    mode.finish();
                    mMsgSelected = 0;
                    return true;
                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    resetTranslucentStatusBar();
                }
            };

            startActionMode(actionModeCb);
        }
    }
}
