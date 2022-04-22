package uk.openvk.android.legacy.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.TimerTask;

import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.layouts.ConversationPanel;
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
    public MessagesListAdapter messagesListAdapter;
    public UpdateUITask updateUITask;
    public OvkAPIWrapper openVK_API;
    public String lastSendedMsg;

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

        initKeyboardListener();

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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                resizeTranslucentLayout();
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
        send_btn.setEnabled(false);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openVK_API.sendMethod("Messages.send", "peer_id=" + peer_id + "&message=" + URLEncoder.encode(((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString(), "UTF-8"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                lastSendedMsg = ((EditText) conversationPanel.findViewById(R.id.message_edit)).getText().toString();
                messagesListArray.add(new MessagesListItem(false, false, (int)(System.currentTimeMillis() / 1000), lastSendedMsg, ConversationActivity.this));
                messagesListAdapter = new MessagesListAdapter(ConversationActivity.this, messagesListArray);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }
    }

    private void initKeyboardListener() {
        final int MIN_KEYBOARD_HEIGHT_PX = 150;
        final View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int prevKeyboardHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    int navigation_height = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                    final int visibleViewHeight = decorView.getHeight();
                    int visibleKeyboardHeight = 0;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)  {
                        visibleKeyboardHeight = visibleViewHeight - windowVisibleDisplayFrame.bottom;
                    } else {
                        visibleKeyboardHeight = visibleViewHeight - windowVisibleDisplayFrame.bottom;
                    }
                    View statusbarView = findViewById(R.id.statusbarView);
                    LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) statusbarView.getLayoutParams();
                    if (visibleKeyboardHeight > 0) {
                        LinearLayout conv_ll = findViewById(R.id.msgs_layout);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) conv_ll.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, visibleKeyboardHeight);
                        conv_ll.setLayoutParams(layoutParams);
                    } else {
                        LinearLayout conv_ll = findViewById(R.id.msgs_layout);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) conv_ll.getLayoutParams();
                        layoutParams.setMargins(0, 0, 0, 0);
                        conv_ll.setLayoutParams(layoutParams);
                    }
                    prevKeyboardHeight = visibleKeyboardHeight;
                }
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
                        } else if (send_request.equals("/method/Messages.send")) {
                            try {
                                if (json_response.has("error_code")) {
                                        for(int message_index = 0; message_index < messagesListArray.size(); message_index++) {
                                            if(messagesListArray.get(message_index).text.equals(lastSendedMsg)) {
                                                MessagesListItem messagesListItem = messagesListArray.get(message_index);
                                                messagesListItem.isError = true;
                                                messagesListArray.set(message_index, messagesListItem);
                                                messagesListAdapter = new MessagesListAdapter(ConversationActivity.this, messagesListArray);
                                                ((ListView) findViewById(R.id.conversation_msgs_listview)).setAdapter(messagesListAdapter);
                                            }
                                        }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
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
                    messagesListArray.add(new MessagesListItem(isIncoming, false, json_response.getJSONObject("response").getJSONArray("items").getJSONObject(message_index).getInt("date"),
                            json_response.getJSONObject("response").getJSONArray("items").getJSONObject(message_index).getString("text"), ConversationActivity.this));
                }
            }
            messagesListAdapter = new MessagesListAdapter(ConversationActivity.this, messagesListArray);
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
