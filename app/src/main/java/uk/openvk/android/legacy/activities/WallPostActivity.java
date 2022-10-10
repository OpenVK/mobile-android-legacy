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

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.CommentPanel;
import uk.openvk.android.legacy.layouts.WallPostLayout;
import uk.openvk.android.legacy.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.list_items.NewsItemCountersInfo;
import uk.openvk.android.legacy.list_items.NewsfeedItem;


public class WallPostActivity extends Activity {
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;
    private int owner_id;
    private int post_id;
    private ArrayList<Comment> comments;
    private WallPostLayout wallPostLayout;
    private CommentPanel commentPanel;
    private CommentsListAdapter commentsAdapter;
    private String author_name;
    private int author_id;
    private int post_author_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        wallPostLayout = findViewById(R.id.comments_layout);
        commentPanel = wallPostLayout.findViewById(R.id.comment_panel);
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
                NewsfeedItem post = new NewsfeedItem();
                post.owner_id = extras.getInt("owner_id");
                post.post_id = extras.getInt("post_id");
                post.name = extras.getString("post_author_name");
                post.info = extras.getString("post_info");
                post.text = extras.getString("post_text");
                owner_id = extras.getInt("owner_id");
                post_id = extras.getInt("post_id");
                String where = extras.getString("where");
                author_name = extras.getString("author_name");
                post_author_id = extras.getInt("post_author_id");
                author_id = extras.getInt("author_id");
                wallPostLayout.setPost(post);
                wallPostLayout.loadWallAvatar(post_author_id);
                wallPostLayout.loadWallPhoto(post_id, where);
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
                downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
                wall.getComments(ovk_api, post_author_id, post.post_id);
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
                actionBarImitation.setTitle(getResources().getString(R.string.new_status));
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
                Comment comment = new Comment(author_id, author_name, (int)(System.currentTimeMillis() / 1000), msg_text);
                comments.add(comment);
                wallPostLayout.createAdapter(WallPostActivity.this, comments);
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
            comments = wall.parseComments(this, downloadManager, data.getString("response"));
            wallPostLayout.createAdapter(this, comments);
        } else if (message == HandlerMessages.COMMENT_AVATARS) {
            wallPostLayout.loadAvatars();
        }
    }
}
