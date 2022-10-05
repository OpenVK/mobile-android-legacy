package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.CommentPanel;
import uk.openvk.android.legacy.layouts.CommentsListLayout;
import uk.openvk.android.legacy.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.list_adapters.MessagesListAdapter;

import static uk.openvk.android.legacy.R.id.send_btn;


public class CommentsActivity extends Activity {
    private OvkAPIWrapper ovk_api;
    private Wall wall;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;
    private int owner_id;
    private int post_id;
    private ArrayList<Comment> comments;
    private CommentsListLayout commentsLayout;
    private CommentPanel commentPanel;
    private CommentsListAdapter commentsAdapter;
    private String author_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        commentsLayout = findViewById(R.id.comments_layout);
        commentPanel = commentsLayout.findViewById(R.id.comment_panel);
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };

        setCommentsView();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                owner_id = extras.getInt("owner_id");
                post_id = extras.getInt("post_id");
                author_name = extras.getString("author_name");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        getActionBar().setHomeButtonEnabled(true);
                    }
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
                    actionBarImitation.setHomeButtonVisibillity(true);
                    actionBarImitation.setTitle(getResources().getString(R.string.comments));
                    actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onBackPressed();
                        }
                    });
                }
                wall = new Wall();
                ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                ovk_api.setServer(instance_prefs.getString("server", ""));
                ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
                wall.getComments(ovk_api, owner_id, post_id);
            }
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setHomeButtonEnabled(true);
                }
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
                actionBarImitation.setHomeButtonVisibillity(true);
                actionBarImitation.setTitle(getResources().getString(R.string.menu_settings));
                actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }
            wall = new Wall();
            ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
            ovk_api.setServer(instance_prefs.getString("server", ""));
            ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
            handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle data = message.getData();
                    Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                    receiveState(message.what, data);
                }
            };
            wall.getComments(ovk_api, owner_id, post_id);
        }
    }

    private void setCommentsView() {
        final CommentPanel commentPanel = (CommentPanel) findViewById(R.id.comment_panel);
        final Button send_btn = ((Button) commentPanel.findViewById(R.id.send_btn));
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg_text = ((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString();
                try {
                    wall.createComment(ovk_api, owner_id, post_id, msg_text);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Comment comment = new Comment(author_name, (int)(System.currentTimeMillis() / 1000), msg_text);
                comments.add(comment);
                commentsLayout.createAdapter(CommentsActivity.this, comments);
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void receiveState(int message, Bundle data) {
        if (message == HandlerMessages.WALL_ALL_COMMENTS) {
            comments = wall.parseComments(data.getString("response"));
            commentsLayout.createAdapter(this, comments);
        }
    }
}
