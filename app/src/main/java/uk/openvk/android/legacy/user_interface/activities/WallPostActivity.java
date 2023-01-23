package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.models.RepostInfo;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.CommentPanel;
import uk.openvk.android.legacy.user_interface.layouts.PostViewLayout;
import uk.openvk.android.legacy.user_interface.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.api.models.WallPost;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;


public class WallPostActivity extends Activity {
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;
    private long owner_id;
    private long post_id;
    private ArrayList<Comment> comments;
    private PostViewLayout postViewLayout;
    private CommentPanel commentPanel;
    private CommentsListAdapter commentsAdapter;
    private String author_name;
    private int author_id;
    private long post_author_id;
    private WallPost post;
    private String author_mention = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wall_post_layout);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        postViewLayout = findViewById(R.id.comments_layout);
        postViewLayout.adjustLayoutSize(getResources().getConfiguration().orientation);
        commentPanel = findViewById(R.id.comment_panel);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if(((EditText)commentPanel.findViewById(R.id.comment_edit)).getText().toString().length() == 0) {
            ((Button) commentPanel.findViewById(R.id.send_btn)).setEnabled(false);
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
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
                author_name = extras.getString("author_name");
                author_id = extras.getInt("author_id");
                post = new WallPost();
                getPost(post, extras);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        getActionBar().setHomeButtonEnabled(true);
                    }
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.comments));
                } else {
                    final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
                    actionBarImitation.setHomeButtonVisibility(true);
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
                wall.getComments(ovk_api, owner_id, post.post_id);
            }
        } else {
            finish();
            return;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void getPost(WallPost post, Bundle extras) {
        post.owner_id = extras.getLong("owner_id");
        post.post_id = extras.getLong("post_id");
        post.name = extras.getString("post_author_name");
        post.info = extras.getString("post_info");
        post.text = extras.getString("post_text");
        post.counters = new PostCounters();
        post.counters.likes = extras.getInt("post_likes");
        owner_id = extras.getLong("owner_id");
        post_id = extras.getLong("post_id");
        String where = extras.getString("where");
        post_author_id = extras.getLong("post_author_id");
        post.attachments = new ArrayList<>();
        if(extras.getBoolean("is_repost")) {
            post.repost = new RepostInfo(extras.getString("repost_author_name"), 0, this);
            post.repost.newsfeed_item = new WallPost();
            post.repost.newsfeed_item.attachments = new ArrayList<>();
            post.repost.newsfeed_item.name = extras.getString("repost_author_name");
            post.repost.newsfeed_item.info = extras.getString("repost_info");
            post.repost.newsfeed_item.owner_id = extras.getInt("repost_owner_id");
            post.repost.newsfeed_item.post_id = extras.getInt("repost_id");
            post.repost.newsfeed_item.text = extras.getString("repost_text");
        }

        postViewLayout.setPost(post, this);
        postViewLayout.setPhotoListener(this);
        postViewLayout.loadWallAvatar(post_author_id, where);
        postViewLayout.loadWallPhoto(post, where);
    }

    private void setCommentsView() {
        final CommentPanel commentPanel = (CommentPanel) findViewById(R.id.comment_panel);
        final Button send_btn = ((Button) commentPanel.findViewById(R.id.send_btn));
        ((EditText) commentPanel.findViewById(R.id.comment_edit)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                try {
                    if(getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
                        if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                            final String msg_text = ((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString();
                            try {
                                wall.createComment(ovk_api, owner_id, post_id, msg_text);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            Comment comment = new Comment(0, author_id, author_name, (int) (System.currentTimeMillis() / 1000), msg_text);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/account_avatar/avatar_%s", getCacheDir(), author_id), options);
                            comment.avatar = bitmap;
                            if (comments == null) {
                                comments = new ArrayList<Comment>();
                            }
                            comments.add(comment);
                            postViewLayout.createAdapter(WallPostActivity.this, comments);
                            ((EditText) commentPanel.findViewById(R.id.comment_edit)).setText("");
                        } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TAB
                                && event.getAction() == KeyEvent.ACTION_DOWN) {
                            ((EditText) commentPanel.findViewById(R.id.comment_edit)).clearFocus();
                            postViewLayout.requestFocus();
                        }
                    }
                } catch (OutOfMemoryError error) {

                }

                return true;
            }
        });
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final String msg_text = ((EditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString();
                    try {
                        wall.createComment(ovk_api, owner_id, post_id, msg_text);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Comment comment = new Comment(0, author_id, author_name, (int) (System.currentTimeMillis() / 1000), msg_text);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/account_avatar/avatar_%s", getCacheDir(), author_id), options);
                    comment.avatar = bitmap;
                    if (comments == null) {
                        comments = new ArrayList<Comment>();
                    }
                    comments.add(comment);
                    postViewLayout.createAdapter(WallPostActivity.this, comments);
                    ((EditText) commentPanel.findViewById(R.id.comment_edit)).setText("");
                } catch (OutOfMemoryError error) {

                }
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
                if(((EditText) commentPanel.findViewById(R.id.comment_edit)).getLineCount() > 4) {
                    ((EditText) commentPanel.findViewById(R.id.comment_edit)).setLines(4);
                } else {
                    ((EditText) commentPanel.findViewById(R.id.comment_edit)).setLines(((EditText) commentPanel.findViewById(R.id.comment_edit)).getLineCount());
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
    public void onConfigurationChanged(Configuration newConfig) {
        postViewLayout.adjustLayoutSize(newConfig.orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void receiveState(int message, Bundle data) {
        if (message == HandlerMessages.WALL_ALL_COMMENTS) {
            comments = wall.parseComments(this, downloadManager, data.getString("response"));
            postViewLayout.createAdapter(this, comments);
        } else if (message == HandlerMessages.COMMENT_AVATARS) {
            postViewLayout.loadAvatars();
        }
    }

    public void viewPhotoAttachment() {
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        intent.putExtra("where", "wall");
        try {
            if(getIntent().getExtras().getBoolean("contains_photo")) {
                intent.putExtra("local_photo_addr", String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%dp%d", getCacheDir(),
                        owner_id, post_id));
                intent.putExtra("photo_id", getIntent().getExtras().getLong("photo_id"));
                startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openWallRepostComments() {
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        try {
            intent.putExtra("post_id", post.repost.newsfeed_item.post_id);
            intent.putExtra("owner_id", post.repost.newsfeed_item.owner_id);
            intent.putExtra("author_name", author_name);
            intent.putExtra("author_id", author_id);
            intent.putExtra("post_author_id", post.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", post.repost.newsfeed_item.name);
            intent.putExtra("post_info", post.repost.newsfeed_item.info);
            intent.putExtra("post_text", post.repost.newsfeed_item.text);
            intent.putExtra("post_likes", 0);
            boolean contains_poll = false;
            boolean contains_photo = false;
            boolean is_repost = false;
            if (post.repost.newsfeed_item.attachments.size() > 0) {
                for (int i = 0; i < post.repost.newsfeed_item.attachments.size(); i++) {
                    if (post.repost.newsfeed_item.attachments.get(i).type.equals("poll")) {
                        contains_poll = true;
                        PollAttachment poll = ((PollAttachment) post.repost.newsfeed_item.attachments.get(i).getContent());
                        intent.putExtra("poll_question", poll.question);
                        intent.putExtra("poll_anonymous", poll.anonymous);
                        //intent.putExtra("poll_answers", poll.answers);
                        intent.putExtra("poll_total_votes", poll.votes);
                        intent.putExtra("poll_user_votes", poll.user_votes);
                    } else if(post.repost.newsfeed_item.attachments.get(i).type.equals("photo")) {
                        contains_photo = true;
                        PhotoAttachment photo = ((PhotoAttachment) post.repost.newsfeed_item.attachments.get(i).getContent());
                        intent.putExtra("photo_id", photo.id);
                    }
                }
            }
            intent.putExtra("contains_poll", contains_poll);
            intent.putExtra("contains_photo", contains_photo);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addAuthorMention(int position) {
        Comment comment = comments.get(position);
        String comment_text = commentPanel.getText();
        String old_author_mention = author_mention;
        if(comment.author_id > 0) {
            author_mention = String.format("[id%s|%s], ", comment.author_id, comment.author);
        } else {
            author_mention = String.format("[club%s|%s], ", -comment.author_id, comment.author);
        }
        if(comment_text.startsWith(old_author_mention)) {
            commentPanel.setText(author_mention + comment_text.substring(old_author_mention.length()));
        } else {
            commentPanel.setText(author_mention + comment_text);
        }
    }
}
