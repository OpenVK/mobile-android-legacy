package uk.openvk.android.legacy.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import dev.tinelix.twemojicon.EmojiconEditText;
import dev.tinelix.twemojicon.EmojiconGridFragment;
import dev.tinelix.twemojicon.EmojiconsFragment;
import dev.tinelix.twemojicon.emoji.Emojicon;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.models.Wall;
import uk.openvk.android.legacy.api.entities.Poll;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.Comment;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.legacy.databases.NewsfeedCacheDB;
import uk.openvk.android.legacy.ui.views.CommentPanel;
import uk.openvk.android.legacy.ui.views.PostViewLayout;
import uk.openvk.android.legacy.ui.list.adapters.CommentsListAdapter;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.ui.views.base.XLinearLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class WallPostActivity extends NetworkFragmentActivity
        implements EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnKeyboardStateListener {
    public Wall wall;
    public Handler handler;
    public ArrayList<Comment> comments;
    private PostViewLayout postViewLayout;
    private CommentPanel commentPanel;
    private CommentsListAdapter commentsAdapter;
    private String account_name;
    private long account_id;
    private long post_author_id;
    private WallPost post;
    private String author_mention = "";
    private int keyboard_height;
    private String where;
    private ArrayList<Attachment> attachments;
    private int minKbHeight = 450;
    private String instance;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_post);
        instance = instance_prefs.getString("server", "");
        ((XLinearLayout) findViewById(R.id.comments_view)).setOnKeyboardStateListener(this);
        setEmojiconFragment(false);
        postViewLayout = findViewById(R.id.comments_layout);
        postViewLayout.adjustLayoutSize(getResources().getConfiguration().orientation);
        commentPanel = findViewById(R.id.comment_panel);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if(((EmojiconEditText)commentPanel.findViewById(R.id.comment_edit))
                .getText().toString().length() == 0) {
            (commentPanel.findViewById(R.id.send_btn)).setEnabled(false);
        }
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message message) {
                final Bundle data = message.getData();
                if(BuildConfig.DEBUG) Log.d(
                        OvkApplication.APP_TAG, String.format("Handling API message: %s", message.what));
            }
        };
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
        }

        loadPost();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(getResources().getString(R.string.wall_view));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            actionBar.setHomeAction(new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return 0;
                }

                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            actionBar.setTitle(getResources().getString(R.string.wall_view));
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
        }
    }

    private void loadPost() {
        String args;
        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                args = Global.getUrlArguments(path);
                if(args.length() > 0) {
                    setCommentsView();
                    ArrayList<WallPost> posts = NewsfeedCacheDB.getPostsList(this);
                    String[] ids = args.substring(4).split("_");
                    if(posts != null) {
                        for (int i = 0; i < posts.size(); i++) {
                            WallPost post = posts.get(i);
                            if(post.owner_id == Long.parseLong(ids[0])
                                    && post.post_id == Long.parseLong(ids[1])) {
                                postViewLayout.setPost(post, this);
                                postViewLayout.setPhotoListener(this);
                                postViewLayout.loadWallAvatar(post.author_id, where);
                                postViewLayout.loadWallPhoto(post, where);
                                this.post = post;
                                wall = new Wall();
                                ovk_api.wall.getComments(ovk_api.wrapper, post.owner_id, post.post_id);
                                getWindow().getDecorView().getViewTreeObserver()
                                        .addOnGlobalLayoutListener(
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
                        }
                    } else {
                        ovk_api.wall.getByID(
                                ovk_api.wrapper, Long.parseLong(ids[0]), Long.parseLong(ids[1])
                        );
                    }
                } else {
                    finish();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setCommentsView() {
        final CommentPanel commentPanel = findViewById(R.id.comment_panel);
        final Button send_btn = (commentPanel.findViewById(R.id.send_btn));
        ((ImageButton) commentPanel.findViewById(R.id.emoji_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.emojicons).getVisibility() == View.GONE) {
                    View view = WallPostActivity.this.getCurrentFocus();
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
                                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
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
        ((EditText) commentPanel.findViewById(R.id.comment_edit)).setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                try {
                    if(getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
                        if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                            final String msg_text = ((EmojiconEditText) commentPanel
                                    .findViewById(R.id.comment_edit)).getText().toString();
                            try {
                                ovk_api.wall.createComment(
                                        ovk_api.wrapper, post.owner_id,
                                        post.post_id, msg_text
                                );
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            Comment comment = new Comment(0, account_id, account_name,
                                    (int) (System.currentTimeMillis() / 1000), msg_text, null);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeFile(
                                    String.format("%s/%s/photos_cache/account_avatar/avatar_%s",
                                            getCacheDir(), instance, account_id), options);
                            comment.avatar = bitmap;
                            if (comments == null) {
                                comments = new ArrayList<Comment>();
                            }
                            comments.add(comment);
                            postViewLayout.createAdapter(WallPostActivity.this, comments);
                            ((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).setText("");
                        } else if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_TAB
                                && event.getAction() == KeyEvent.ACTION_DOWN) {
                            (commentPanel.findViewById(R.id.comment_edit)).clearFocus();
                            postViewLayout.requestFocus();
                        }
                    }
                } catch (OutOfMemoryError ignored) {

                }

                return true;
            }
        });
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final String msg_text = ((EmojiconEditText) commentPanel.
                            findViewById(R.id.comment_edit)).getText().toString();
                    try {
                        ovk_api.wall.createComment(ovk_api.wrapper, post.owner_id, post.post_id, msg_text);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Comment comment = new Comment(0, account_id, account_name, (int)
                            (System.currentTimeMillis() / 1000), msg_text, null);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/account_avatar/avatar_%s",
                                    getCacheDir(), instance, account_id), options);
                    comment.avatar = bitmap;
                    if (comments == null) {
                        comments = new ArrayList<Comment>();
                    }
                    comments.add(comment);
                    postViewLayout.createAdapter(WallPostActivity.this, comments);
                    ((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).setText("");
                } catch (OutOfMemoryError ignored) {

                }
            }
        });
        ((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit))
                .addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).getText().toString().length() > 0) {
                    send_btn.setEnabled(true);
                } else {
                    send_btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).getLineCount() > 4) {
                    ((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).setLines(4);
                } else {
                    ((EmojiconEditText) commentPanel.findViewById(R.id.comment_edit)).setLines(
                            ((EmojiconEditText)
                            commentPanel.findViewById(R.id.comment_edit)).getLineCount());
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
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            minKbHeight = (int) (520 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            minKbHeight = (int) (360 * getResources().getDisplayMetrics().scaledDensity);
        }
        super.onConfigurationChanged(newConfig);
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
        if (message == HandlerMessages.WALL_ALL_COMMENTS) {
            postViewLayout.createAdapter(this, comments);
        } else if (message == HandlerMessages.COMMENT_PHOTOS) {
            postViewLayout.loadPhotos();
        } else if (message == HandlerMessages.COMMENT_AVATARS) {
            postViewLayout.loadAvatars();
        }
    }

    public void viewPhotoAttachment() {
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        intent.putExtra("where", "wall");
        try {
            if(attachments.get(0).type.equals("photo")) {
                Photo photo = (Photo) attachments.get(0);
                intent.putExtra("original_link", photo.original_url);
                intent.putExtra("author_id", post.author_id);
                intent.putExtra("photo_id", photo.id);
                if(where.equals("newsfeed")) {
                    intent.putExtra("local_photo_addr",
                            String.format(
                                    "%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s",
                                    getCacheDir(),
                                    post.owner_id, post.post_id));
                } else {
                    intent.putExtra("local_photo_addr",
                            String.format(
                                    "%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s",
                                    getCacheDir(),
                                    post.owner_id, post.post_id));
                    intent.putExtra("photo_id", getIntent().getExtras().getLong("photo_id"));
                }
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
            intent.putExtra("account_name", account_name);
            intent.putExtra("account_id", account_id);
            intent.putExtra("post_author_id", post.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", post.repost.newsfeed_item.name);
            intent.putExtra("post_json", post.repost.newsfeed_item.getJSONString());
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


    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input((EditText) findViewById(R.id.comment_panel).findViewById(R.id.
                comment_edit), emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace((EditText) findViewById(R.id.comment_panel)
                .findViewById(R.id.comment_edit));
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
}
