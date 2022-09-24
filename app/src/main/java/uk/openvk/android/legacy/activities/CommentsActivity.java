package uk.openvk.android.legacy.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.TimerTask;

import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.layouts.CommentPanel;
import uk.openvk.android.legacy.layouts.ConversationPanel;
import uk.openvk.android.legacy.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.list_adapters.FriendsListAdapter;
import uk.openvk.android.legacy.list_adapters.MessagesListAdapter;
import uk.openvk.android.legacy.list_items.CommentsListItem;
import uk.openvk.android.legacy.list_items.FriendsListItem;
import uk.openvk.android.legacy.list_items.MessagesListItem;
import uk.openvk.android.legacy.list_items.NewsListItem;

public class CommentsActivity extends Activity {
    public String auth_token;
    public int post_id;
    public int owner_id;
    public int visibleKeyboardHeight;
    public boolean hasNavBar;
    public JSONObject json_response;
    public ArrayList<CommentsListItem> commentsListArray;
    public CommentsListAdapter commentsListAdapter;
    public UpdateUITask updateUITask;
    public OvkAPIWrapper openVK_API;
    public String state;
    public static Handler handler;
    public static final int UPDATE_UI = 0;
    public static final int GET_PICTURE = 1;
    public String action;
    public int comment_id;
    public String from;
    public Bitmap photo_bmp;
    public String send_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                owner_id = extras.getInt("owner_id");
                post_id = extras.getInt("post_id");
            }
        } else {
            auth_token = (String) savedInstanceState.getSerializable("auth_token");
        }

        setContentView(R.layout.comments_layout);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case UPDATE_UI:
                        state = msg.getData().getString("State");
                        send_request = msg.getData().getString("API_method");
                        try {
                            json_response = new JSONObject(msg.getData().getString("JSON_response"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nClass name: " + ProfileIntentActivity.class.getSimpleName() + "\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                        break;
                    case GET_PICTURE:
                        state = msg.getData().getString("State");
                        from = msg.getData().getString("From");
                        photo_bmp = (Bitmap) msg.getData().getParcelable("Picture");
                        comment_id = msg.getData().getInt("ID");
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nDownloaded picture!");
                        updateUITask.run();
                }
            }
        };

        commentsListArray = new ArrayList<CommentsListItem>();
        updateUITask = new UpdateUITask();

        initKeyboardListener();

        auth_token = getSharedPreferences("instance", 0).getString("auth_token", "");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
                getActionBar().setIcon(R.drawable.ic_ab_app);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.comments));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                resizeTranslucentLayout();
            }
        } else {
            final TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.comments));
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
        openVK_API = new OvkAPIWrapper(CommentsActivity.this, sharedPreferences.getString("server", ""), auth_token, json_response, sharedPreferences.getBoolean("useHTTPS", true));
        try {
            openVK_API.sendMethod("Wall.getComments", "post_id=" + post_id + "&owner_id=" + owner_id + "&extended=1");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final CommentPanel commentPanel = findViewById(R.id.comment_panel);
        final Button send_btn = commentPanel.findViewById(R.id.send_btn);
        send_btn.setEnabled(false);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openVK_API.sendMethod("Wall.createComment", "post_id=" + post_id + "&owner_id=" + owner_id + "&message=" + URLEncoder.encode(((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString(), "UTF-8"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String lastSendedComment = ((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString();
                commentsListArray.add(new CommentsListItem("You", lastSendedComment, null));
                commentsListAdapter = new CommentsListAdapter(CommentsActivity.this, commentsListArray);
                RecyclerView commentsList = findViewById(R.id.comments_list);
                commentsList.setAdapter(commentsListAdapter);
                ((EditText) commentPanel.findViewById(R.id.comment_edit)).setText("");
            }
        });
        ((EditText) commentPanel.findViewById(R.id.comment_edit)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString().length() > 0) {
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
                    final int navigation_height = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                    final int visibleViewHeight = decorView.getHeight();
                    int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasNavBar) {
                        visibleKeyboardHeight = visibleViewHeight - windowVisibleDisplayFrame.bottom - getResources().getDimensionPixelSize(navigation_height);
                    } else {
                        visibleKeyboardHeight = visibleViewHeight - windowVisibleDisplayFrame.bottom;
                    }
                    prevKeyboardHeight = visibleKeyboardHeight;
                }
            }
        });
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state.equals("getting_response")) {
                        try {
                            loadComments();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void loadComments() {
        commentsListArray.clear();
        Log.d("OpenVK Legacy", "Clearing friends list...");
        int commentsCount = 0; // zero rn
        try {
            commentsCount = json_response.getJSONObject("response").getJSONArray("items").length(); // we will use count of items this time bc we still don't have infinity loading or smth like that
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(commentsCount > 0)
        {
            for (int i = 0; i < commentsCount; i++) {
                try {
                    JSONObject json_item = (JSONObject) json_response.getJSONObject("response").getJSONArray("items").get(i);
                    commentsListArray.add(new CommentsListItem("Author ID: " + json_item.getInt("from_id"), json_item.getString("text"), null));
                    CommentsListItem item = commentsListArray.get(i);
                    if (json_item.getInt("from_id") < 0 && json_response.getJSONObject("response").isNull("groups") == false) {
                        for (int groups_index = 0; groups_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).length(); groups_index++) {
                            if (-json_item.getInt("from_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                    getJSONObject(groups_index).getInt("id")) {
                                item.author = ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                        getJSONObject(groups_index).getString("name");
                                // later: loadNewsAvatars(news_item_index, json_response.getJSONObject("response").getJSONArray("groups").getJSONObject(groups_index).getString("photo_50"), "group");
                                commentsListArray.set(i, item);
                            }
                        }
                    } else if(json_response.getJSONObject("response").isNull("profiles") == false) {
                        for (int users_index = 0; users_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).length(); users_index++) {
                            if (json_item.getInt("from_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                    getJSONObject(users_index).getInt("id")) {
                                item.author = ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getString("first_name") + " " + ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getString("last_name");
                                // later: loadNewsAvatars(news_item_index, json_response.getJSONObject("response").getJSONArray("profiles").getJSONObject(users_index).getString("photo_50"), "user");
                                commentsListArray.set(i, item);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            commentsListAdapter = new CommentsListAdapter(this, commentsListArray);
            RecyclerView comments_rv = findViewById(R.id.comments_list);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            comments_rv.setLayoutManager(llm);
            comments_rv.setAdapter(commentsListAdapter);
        }
    }
}
