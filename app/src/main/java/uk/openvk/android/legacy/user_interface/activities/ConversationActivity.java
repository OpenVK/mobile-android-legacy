package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.content.SharedPreferences;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ConversationPanel;
import uk.openvk.android.legacy.user_interface.list_adapters.MessagesListAdapter;

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
    public int peer_id;
    public ActionBarImitation actionBarImitation;
    private ArrayList<uk.openvk.android.legacy.api.models.Message> history;
    private uk.openvk.android.legacy.api.models.Message last_sended_message;

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
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                peer_id = extras.getInt("peer_id");
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
                    actionBarImitation.setHomeButtonVisibillity(true);
                    actionBarImitation.setTitle(conv_title);
                    if(peer_online == 1) {
                        actionBarImitation.setSubtitle(getResources().getString(R.string.online));
                    } else {
                        actionBarImitation.setSubtitle(getResources().getString(R.string.offline));
                    }
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
                actionBarImitation.setHomeButtonVisibillity(true);
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
                final String msg_text = ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    try {
                        conversation.sendMessage(ovk_api, msg_text);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    last_sended_message = new uk.openvk.android.legacy.api.models.Message(false, false, (int)(System.currentTimeMillis() / 1000), msg_text, ConversationActivity.this);
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
                }
                return false;
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
                last_sended_message = new uk.openvk.android.legacy.api.models.Message(false, false, (int)(System.currentTimeMillis() / 1000), msg_text, ConversationActivity.this);
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
            messagesList.setAdapter(conversation_adapter);
        } else if(message == HandlerMessages.MESSAGES_SEND) {
            last_sended_message.sending = false;
            history.set(history.size() - 1, last_sended_message);
            messagesList.setAdapter(conversation_adapter);
        }
    }

    public void hideSelectedItemBackground(int position) {
        messagesList.setBackgroundColor(getResources().getColor(R.color.transparent));
    }
}
