package uk.openvk.android.legacy.user_interface.core.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Messages;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.longpoll_api.receivers.LongPollReceiver;
import uk.openvk.android.legacy.user_interface.OvkAlertDialog;
import uk.openvk.android.legacy.user_interface.view.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.view.layouts.ConversationPanel;
import uk.openvk.android.legacy.user_interface.list.adapters.MessagesListAdapter;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;

public class ConversationActivity extends Activity {

    private OvkAPIWrapper ovk_api;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private Conversation conversation;
    private ListView messagesList;
    private MessagesListAdapter conversation_adapter;
    public String state;
    public String from;
    public String conv_title;
    public int peer_online;
    public long peer_id;
    private int cursor_id;
    public ActionBarImitation actionBarImitation;
    private ArrayList<uk.openvk.android.legacy.api.models.Message> history;
    private Messages messages;
    private uk.openvk.android.legacy.api.models.Message last_sended_message;
    private LongPollReceiver lpReceiver;
    private String last_lp_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        setContentView(R.layout.conversation_msgs_layout);
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        conversation = new Conversation();
        messagesList = (ListView) findViewById(R.id.conversation_msgs_listview);
        installLayouts();
        setConversationView();
        messages = new Messages();
        registerBroadcastReceiver();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/conversations_avatars/avatar_%s", getCacheDir(), peer_id), options);
            conversation.avatar = bitmap;
        } catch (OutOfMemoryError error) {

        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                peer_id = extras.getLong("peer_id");
                conv_title = extras.getString("conv_title");
                peer_online = extras.getInt("online");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(conv_title);
                    if(peer_online == 1) {
                        getActionBar().setSubtitle(R.string.online);
                    } else {
                        getActionBar().setSubtitle(R.string.offline);
                    }
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayUseLogoEnabled(false);
                } else {
                    actionBarImitation.setHomeButtonVisibility(true);
                    actionBarImitation.setTitle(conv_title);
                    if(peer_online == 1) {
                        actionBarImitation.setSubtitle(getResources().getString(R.string.online));
                    } else {
                        actionBarImitation.setSubtitle(getResources().getString(R.string.offline));
                    }
                    actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });
                }
                ovk_api.setServer(instance_prefs.getString("server", ""));
                ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
                conversation.getHistory(ovk_api, peer_id);
            }
        } else {
            peer_id = savedInstanceState.getInt("peer_id");
            conv_title = (String) savedInstanceState.getSerializable("conv_title");
            peer_online = savedInstanceState.getInt("online");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(conv_title);
                if(peer_online == 1) {
                    getActionBar().setSubtitle(R.string.online);
                } else {
                    getActionBar().setSubtitle(R.string.offline);
                }
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayUseLogoEnabled(false);
            } else {
                actionBarImitation.setHomeButtonVisibility(true);
                actionBarImitation.setTitle(conv_title);
                actionBarImitation.setSubtitle(conv_title);
                actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }
            ovk_api.setServer(instance_prefs.getString("server", ""));
            ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
            conversation.getHistory(ovk_api, peer_id);

        }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
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

    private void setConversationView() {
        final ConversationPanel conversationPanel = (ConversationPanel) findViewById(R.id.conversation_panel);
        ((EditText) conversationPanel.findViewById(R.id.message_edit)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if(getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
                    final String msg_text = ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString();
                    if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        try {
                            conversation.sendMessage(ovk_api, msg_text);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        last_sended_message = new uk.openvk.android.legacy.api.models.Message(0, false, false, (int) (System.currentTimeMillis() / 1000), msg_text, ConversationActivity.this);
                        last_sended_message.sending = true;
                        last_sended_message.isError = false;
                        if (history == null) {
                            history = new ArrayList<uk.openvk.android.legacy.api.models.Message>();
                        }
                        history.add(last_sended_message);
                        if (conversation_adapter == null) {
                            conversation_adapter = new MessagesListAdapter(ConversationActivity.this, history, peer_id);
                            messagesList.setAdapter(conversation_adapter);
                        } else {
                            conversation_adapter.notifyDataSetChanged();
                        }
                        ((EditText) conversationPanel.findViewById(R.id.message_edit)).setText("");
                        messagesList.smoothScrollToPosition(history.size() - 1);
                    } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TAB
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        ((EditText) conversationPanel.findViewById(R.id.message_edit)).clearFocus();
                        messagesList.requestFocus();
                    }
                }
                return true;
            }
        });
        final Button send_btn = (Button) conversationPanel.findViewById(R.id.send_btn);
        send_btn.setEnabled(false);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg_text = ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString();
                try {
                    conversation.sendMessage(ovk_api, msg_text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                last_sended_message = new uk.openvk.android.legacy.api.models.Message(0, false, false, (int)(System.currentTimeMillis() / 1000), msg_text, ConversationActivity.this);
                last_sended_message.sending = true;
                last_sended_message.isError = false;
                if(history == null) {
                    history = new ArrayList<uk.openvk.android.legacy.api.models.Message>();
                }
                history.add(last_sended_message);
                if(conversation_adapter == null) {
                    conversation_adapter = new MessagesListAdapter(ConversationActivity.this, history, peer_id);
                    messagesList.setAdapter(conversation_adapter);
                } else {
                    conversation_adapter.notifyDataSetChanged();
                }
                ((EditText) conversationPanel.findViewById(R.id.message_edit)).setText("");
                messagesList.smoothScrollToPosition(history.size() -1);
            }
        });
        ((EditText) conversationPanel.findViewById(R.id.message_edit)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString().length() > 0) {
                    send_btn.setEnabled(true);
                } else {
                    send_btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(((EditText) conversationPanel.findViewById(R.id.message_edit)).getLineCount() > 4) {
                    ((EditText) conversationPanel.findViewById(R.id.message_edit)).setLines(4);
                } else {
                    ((EditText) conversationPanel.findViewById(R.id.message_edit)).setLines(((EditText) conversationPanel.findViewById(R.id.message_edit)).getLineCount());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void installLayouts() {
        actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
    }

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.MESSAGES_GET_HISTORY) {
            history = conversation.parseHistory(this, data.getString("response"));
            conversation_adapter = new MessagesListAdapter(this, history, peer_id);
            messagesList.setAdapter(conversation_adapter);
        } else if (message == HandlerMessages.CHAT_DISABLED) {
            last_sended_message.sending = false;
            last_sended_message.isError = true;
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        } else if (message == HandlerMessages.MESSAGES_DELETE) {
            history.remove(cursor_id);
            conversation_adapter.notifyDataSetChanged();
        } else if(message == HandlerMessages.MESSAGES_SEND) {
            last_sended_message.sending = false;
            last_sended_message.getSendedId(data.getString("response"));
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        } else if(message == HandlerMessages.LONGPOLL) {
            if(!((OvkApplication) getApplicationContext()).notifMan.isRepeat(last_lp_message, data.getString("response"))) {
                conversation.getHistory(ovk_api, peer_id);
            }
            last_lp_message = data.getString("response");
        }
    }

    public void hideSelectedItemBackground(int position) {
        messagesList.setBackgroundColor(getResources().getColor(R.color.transparent));
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
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, functions);
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
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(history.get(item_pos).text);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Message text", history.get(item_pos).text);
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    dialog.dismiss();
                }
            });
        } else {
            functions.add(getResources().getString(R.string.copy_text));
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                        long id) {
                    if(functions.get(position).equals(getResources().getString(R.string.copy_text))) {
                        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(history.get(item_pos).text);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Message text", history.get(item_pos).text);
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    dialog.dismiss();
                }
            });
        }
    }

    private void showDeleteConfirmDialog(final int position) {
        cursor_id = position;
        uk.openvk.android.legacy.api.models.Message msg = history.get(position);
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
                messages.delete(ovk_api, history.get(position).id);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        OvkAlertDialog dialog = new OvkAlertDialog(this);
        dialog.build(builder, getResources().getString(R.string.confirm), getResources().getString(R.string.delete_msgs_confirm, String.format("\"%s\"", text)), null);
        dialog.show();
    }
}
