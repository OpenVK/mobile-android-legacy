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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.TextView;

import dev.tinelix.twemojicon.EmojiconEditText;
import dev.tinelix.twemojicon.EmojiconGridFragment;
import dev.tinelix.twemojicon.EmojiconsFragment;
import dev.tinelix.twemojicon.emoji.Emojicon;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.client.entities.Message;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.receivers.LongPollReceiver;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.core.enumerations.UiMessages;
import uk.openvk.android.legacy.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.legacy.ui.views.ConversationPanel;
import uk.openvk.android.legacy.ui.list.adapters.MessagesHistoryAdapter;
import uk.openvk.android.legacy.ui.views.base.XLinearLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

public class ConversationActivity extends NetworkFragmentActivity implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnKeyboardStateListener {

    public Conversation conversation;
    private RecyclerView messagesList;
    private MessagesHistoryAdapter conversation_adapter;
    public String state;
    public String from;
    public long peer_id;
    public ActionBar actionBar;
    public ArrayList<uk.openvk.android.client.entities.Message> history;
    private uk.openvk.android.client.entities.Message last_sended_message;
    private LongPollReceiver lpReceiver;
    private String last_lp_message;
    private int keyboard_height;
    private int minKbHeight;
    private Menu activity_menu;
    private ImageView ab_profile_photo;
    private int msgCursorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_msgs);
        conversation = new Conversation();
        messagesList = findViewById(R.id.conversation_msgs_listview);

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

        installLayouts();
        setConversationView();
        registerBroadcastReceiver();
        setEmojiconFragment(false);

        ovk_api.messages.getConversationById(ovk_api.wrapper, conversation.peer_id);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
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
                        if(height - visible >= minKbHeight) {
                            keyboard_height = height - visible;
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
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
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
    }

    private void registerBroadcastReceiver() {
        lpReceiver = new LongPollReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Bundle data = intent.getExtras();
                receiveState(HandlerMessages.LONGPOLL, data);
            }
        };
        registerReceiver(lpReceiver, new IntentFilter(
                "uk.openvk.android.legacy.LONGPOLL_RECEIVE"));
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
                    0, R.id.attach_photo_item, 0, getResources().getString(R.string.attach_photo)
            ).setIcon(R.drawable.ic_attach_menu_photo);

            attachMenu.add(
                    0, R.id.attach_photo_item, 0, getResources().getString(R.string.audio)
            ).setIcon(R.drawable.ic_attach_menu_audio);

            attachMenu.add(
                    0, R.id.attach_photo_item, 0, getResources().getString(R.string.video)
            ).setIcon(R.drawable.ic_attach_menu_video);

            attachMenu.add(
                    0, R.id.attach_photo_item, 0, getResources().getString(R.string.attach_note_to_post)
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
            activity_menu = menu;
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
                            getCacheDir(), global_prefs.getString("current_instance", ""),
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
            i.setData(Uri.parse("openvk://ovk/id" + peer_id));
        else if(conversation.peer_type.equals("group"))
            i.setData(Uri.parse("openvk://ovk/club" + -peer_id));

        if(conversation.peer_type.equals("user") || conversation.peer_type.equals("group"))
            startActivity(i);
    }

    private void setConversationView() {
        final ConversationPanel conversationPanel = findViewById(R.id.conversation_panel);
        conversationPanel.findViewById(R.id.emoji_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.emojicons).getVisibility() == View.GONE) {
                    View view = ConversationActivity.this.getCurrentFocus();
                    if (view != null) {
                        if(!((OvkApplication) getApplicationContext()).isTablet) {
                            if (keyboard_height >= minKbHeight) {
                                findViewById(R.id.emojicons).getLayoutParams().height = keyboard_height;
                            } else {
                                findViewById(R.id.emojicons).getLayoutParams().height = minKbHeight;
                            }
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
                            findViewById(R.id.emojicons).getLayoutParams().height = minKbHeight;
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
                            conversation.sendMessage(ovk_api.wrapper, msg_text);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        last_sended_message = new uk.openvk.android.client.entities.Message(
                                0, false,
                                (int) (System.currentTimeMillis() / 1000),
                                msg_text
                        );

                        last_sended_message.sending = true;
                        last_sended_message.isError = false;

                        if (history == null) {
                            history = new ArrayList<>();
                        }

                        history.add(last_sended_message);

                        createAdapter();

                        ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit)).setText("");
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                            messagesList.smoothScrollToPosition(history.size() - 1);

                    } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TAB
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        (conversationPanel.findViewById(R.id.message_edit)).clearFocus();
                        messagesList.requestFocus();
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
                    conversation.sendMessage(ovk_api.wrapper, msg_text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                last_sended_message = new uk.openvk.android.client.entities.Message(
                        0, false, (int)(System.currentTimeMillis() / 1000), msg_text
                );
                last_sended_message.sending = true;
                last_sended_message.isError = false;

                if(history == null) {
                    history = new ArrayList<>();
                }
                history.add(last_sended_message);

                createAdapter();

                ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit)).setText("");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                messagesList.smoothScrollToPosition(history.size() -1);
            }
        });
        ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit))
                .addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit)).getText()
                        .toString().length() > 0) {
                    send_btn.setEnabled(true);
                } else {
                    send_btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit))
                        .getLineCount() > 4) {
                    ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit))
                            .setLines(4);
                } else {
                    ((EmojiconEditText) conversationPanel.findViewById(R.id.message_edit)).setLines(
                            ((EditText) conversationPanel.findViewById(R.id.message_edit))
                                    .getLineCount());
                }
            }
        });
    }

    private void createAdapter() {

        if(history == null)
            history = new ArrayList<>();

        if(conversation_adapter == null) {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setReverseLayout(true);
            messagesList.setLayoutManager(llm);
            conversation_adapter = new MessagesHistoryAdapter(
                    ConversationActivity.this, conversation, history
            );
            messagesList.setAdapter(conversation_adapter);
        } else {
            conversation_adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
            conversation = ovk_api.messages.searchConversation(conversation.peer_id);
            if(conversation != null)
                conversation.getHistory(ovk_api.wrapper, conversation.peer_id);
        } else if(message == HandlerMessages.MESSAGES_GET_HISTORY) {
            createAdapter();
        } else if (message == HandlerMessages.CHAT_DISABLED) {
            last_sended_message.sending = false;
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        } else if (message == HandlerMessages.MESSAGES_DELETE) {
            history.remove(msgCursorId);
            conversation_adapter.notifyDataSetChanged();
        } else if(message == HandlerMessages.MESSAGES_SEND) {
            last_sended_message.sending = false;
            last_sended_message.getSendedId(data.getString("response"));
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        } else if(message == HandlerMessages.LONGPOLL) {
            if(!((OvkApplication) getApplicationContext()).notifMan.isRepeat(last_lp_message,
                    data.getString("response"))) {
                conversation.getHistory(ovk_api.wrapper, conversation.peer_id);
            }
            last_lp_message = data.getString("response");
        } else if(message == UiMessages.RIGHT_AVATAR_IN_ACTIONBAR) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(
                        String.format("%s/%s/photos_cache/conversations_avatars/avatar_%s",
                                getCacheDir(), global_prefs.getString("current_instance", ""), peer_id), options);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (activity_menu != null) {
                        ab_profile_photo = activity_menu.getItem(0).getActionView().findViewById(R.id.profile_photo);
                        ab_profile_photo.setImageBitmap(bitmap);
                    }
                }
            } catch (OutOfMemoryError | Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(lpReceiver);
        super.onDestroy();
    }

    public void getMsgContextMenu(final int item_pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> functions = new ArrayList<>();
        builder.setTitle(R.string.message);
        if(!history.get(item_pos).isIncoming) {
            functions.add(getResources().getString(R.string.copy_text));
            functions.add(getResources().getString(R.string.delete));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                    functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                        long id) {
                    if (functions.get(position).equals(getResources().getString(R.string.delete))) {
                        showDeleteConfirmDialog(item_pos);
                    } else if(functions.get(position).equals(getResources().getString(R.string.copy_text))) {
                        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard =
                                    (android.text.ClipboardManager)
                                            getSystemService(CLIPBOARD_SERVICE);
                            clipboard.setText(history.get(item_pos).text);
                        } else {
                            android.content.ClipboardManager clipboard =
                                    (android.content.ClipboardManager)
                                            getSystemService(CLIPBOARD_SERVICE);
                            android.content.ClipData clip =
                                    android.content.ClipData.newPlainText("Message text",
                                            history.get(item_pos).text);
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    dialog.dismiss();
                }
            });
        } else {
            functions.add(getResources().getString(R.string.copy_text));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                        long id) {
                    if(functions.get(position).equals(getResources().getString(R.string.copy_text))) {
                        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                                    getSystemService(CLIPBOARD_SERVICE);
                            clipboard.setText(history.get(item_pos).text);
                        } else {
                            android.content.ClipboardManager clipboard =
                                    (android.content.ClipboardManager) getSystemService(
                                            CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.
                                    newPlainText("Message text", history.get(item_pos).text);
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    dialog.dismiss();
                }
            });
        }
    }

    private void showDeleteConfirmDialog(final int position) {
        msgCursorId = position;
        uk.openvk.android.client.entities.Message msg = history.get(position);
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
                ovk_api.messages.delete(ovk_api.wrapper, history.get(position).id);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.confirm),
                getResources().getString(R.string.delete_msgs_confirm,
                        String.format("\"%s\"", text)), null);
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
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
}
