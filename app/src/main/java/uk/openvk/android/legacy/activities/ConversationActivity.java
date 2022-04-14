package uk.openvk.android.legacy.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.layouts.ConversationPanel;
import uk.openvk.android.legacy.list_adapters.ConversationsListAdapter;
import uk.openvk.android.legacy.list_adapters.MessagesListAdapter;
import uk.openvk.android.legacy.list_items.MessagesListItem;

public class ConversationActivity extends Activity {

    public String auth_token;
    public int peer_id;
    public static Handler handler;
    public static final int UPDATE_UI = 0;
    public static final int GET_PICTURE = 1;
    public static final int SENDED_MESSAGE = 2;
    public String state;
    public String send_request;
    public String from;
    public Bitmap photo_bmp;
    public String conv_title;
    public int message_id;
    public int peer_online;
    public JSONObject json_response;
    public ArrayList<MessagesListItem> messagesListArray;
    public UpdateUITask updateUITask;
    public OvkAPIWrapper openVK_API;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        setContentView(R.layout.conversation_msgs_layout);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                peer_id = extras.getInt("peer_id");
                conv_title = extras.getString("conv_title");
                peer_online = extras.getInt("online");
            }
        } else {
            peer_id = savedInstanceState.getInt("peer_id");
            conv_title = (String) savedInstanceState.getSerializable("conv_title");
            peer_online = savedInstanceState.getInt("online");
        }

        auth_token = getSharedPreferences("instance", 0).getString("auth_token", "");

        messagesListArray = new ArrayList<MessagesListItem>();
        updateUITask = new UpdateUITask();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            getActionBar().setTitle(conv_title);
            if(peer_online > 0) {
                getActionBar().setSubtitle(R.string.online);
            }
        } else {
            final TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(conv_title);
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

        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case UPDATE_UI:
                        state = msg.getData().getString("State");
                        send_request = msg.getData().getString("API_method");
                        if(state != "no_connection" && state != "timeout") {
                            try {
                                json_response = new JSONObject(msg.getData().getString("JSON_response"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                        break;
                    case GET_PICTURE:
                        state = msg.getData().getString("State");
                        from = msg.getData().getString("from");
                        photo_bmp = (Bitmap) msg.getData().getParcelable("Picture");
                        message_id = msg.getData().getInt("ID");
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nDownloaded picture!");
                        updateUITask.run();
                }
            }
        };

        openVK_API = new OvkAPIWrapper(ConversationActivity.this, sharedPreferences.getString("server", ""), auth_token, json_response, sharedPreferences.getBoolean("useHTTPS", true));
        try {
            openVK_API.sendMethod("Messages.getHistory", "peer_id=" + peer_id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final ConversationPanel conversationPanel = findViewById(R.id.conversation_panel);
        final Button send_btn = conversationPanel.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openVK_API.sendMethod("Messages.send", "peer_id=" + peer_id + "&message=" + ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                messagesListArray.add(new MessagesListItem(false, false, 0, ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString()));
                MessagesListAdapter messagesListAdapter = new MessagesListAdapter(ConversationActivity.this, messagesListArray);
                ListView messagesListView = findViewById(R.id.conversation_msgs_listview);
                messagesListView.setAdapter(messagesListAdapter);
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

    public void hideSelectedItemBackground(int position) {
        ListView messages_listview = findViewById(R.id.conversation_msgs_listview);
        messages_listview.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state.equals("getting_response")) {
                        if(send_request.equals("/method/Messages.getHistory")) {
                            loadChatHistory();
                        }
                    }
                }
            });
        }
    }

    public void loadChatHistory() {
        messagesListArray.clear();
        int messages_item_count = 0;
        try {
            messages_item_count = json_response.getJSONObject("response").getJSONArray("items").length();
            boolean isIncoming = false;
            if(messages_item_count > 0) {
                for (int message_index = messages_item_count -1; message_index > -1; message_index--) {
                    if(json_response.getJSONObject("response").getJSONArray("items").getJSONObject(message_index).getInt("out") == 0) {
                        isIncoming = true;
                    } else {
                        isIncoming = false;
                    }
                    messagesListArray.add(new MessagesListItem(isIncoming, false, json_response.getJSONObject("response").getJSONArray("items").getJSONObject(message_index).getInt("date"), json_response.getJSONObject("response").getJSONArray("items").getJSONObject(message_index).getString("text")));
                }
            }
            MessagesListAdapter messagesListAdapter = new MessagesListAdapter(ConversationActivity.this, messagesListArray);
            ListView messagesListView = findViewById(R.id.conversation_msgs_listview);
            messagesListView.setAdapter(messagesListAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
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
        return true;
    }
}
