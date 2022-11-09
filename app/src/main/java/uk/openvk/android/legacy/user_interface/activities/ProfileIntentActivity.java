package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ErrorLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProfileLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.user_interface.layouts.ProgressLayout;
import uk.openvk.android.legacy.user_interface.layouts.WallErrorLayout;
import uk.openvk.android.legacy.user_interface.layouts.WallLayout;
import uk.openvk.android.legacy.api.models.WallPost;

public class ProfileIntentActivity extends Activity {

    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private ProfileLayout profileLayout;
    private Users users;
    private Friends friends;
    private Account account;
    private Wall wall;
    private Likes likes;
    private String access_token;
    private User user;
    private String args;
    private int item_pos;
    private int poll_answer;
    private Menu activity_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        setContentView(R.layout.app_layout);
        installLayouts();
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        likes = new Likes();
        user = new User();
        friends = new Friends();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                access_token = instance_prefs.getString("access_token", "");
            } else {
                access_token = instance_prefs.getString("access_token", "");
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }

        final Uri uri = intent.getData();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };

        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                account = new Account(this);
                likes = new Likes();
                ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                ovk_api.setServer(instance_prefs.getString("server", ""));
                ovk_api.setAccessToken(access_token);
                account.getProfileInfo(ovk_api);
                if (path.startsWith("openvk://profile/")) {
                    args = path.substring("openvk://profile/".length());
                    downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
                    users = new Users();
                    wall = new Wall();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void installLayouts() {
        ProfileWallSelector selector = findViewById(R.id.wall_selector);
        (selector.findViewById(R.id.profile_wall_post_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewPostActivity();
            }
        });
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        profileLayout = (ProfileLayout) findViewById(R.id.profile_layout);
        progressLayout.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.profile));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            ActionBarImitation actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibillity(true);
            actionBarImitation.setTitle(getResources().getString(R.string.profile));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        activity_menu = menu;
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            } else if(item.getItemId() == R.id.add_to_friends) {
                if(user.friends_status == 0) {
                    addToFriends(user.id);
                } else if(user.friends_status == 1) {
                    deleteFromFriends(user.id);
                } else if(user.friends_status == 2) {
                    addToFriends(user.id);
                } else {
                    deleteFromFriends(user.id);
                }
            } else if(item.getItemId() == R.id.copy_link) {
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if(user.screen_name != null && user.screen_name.length() > 0) {
                        clipboard.setText(String.format("http://%s/%s", instance_prefs.getString("server", ""), user.screen_name));
                    } else {
                        clipboard.setText(String.format("http://%s/id%d", instance_prefs.getString("server", ""), user.id));
                    }
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip;
                    if(user.screen_name != null && user.screen_name.length() > 0) {
                        clip = android.content.ClipData.newPlainText("OpenVK Profile URL", String.format("http://%s/%s", instance_prefs.getString("server", ""), user.screen_name));
                    } else {
                        clip = android.content.ClipData.newPlainText("OpenVK Profile URL", String.format("http://%s/id%d", instance_prefs.getString("server", ""), user.id));
                    }
                    clipboard.setPrimaryClip(clip);
                }
            } else if(item.getItemId() == R.id.open_in_browser) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                if(user.screen_name != null && user.screen_name.length() > 0) {
                    i.setData(Uri.parse(String.format("http://%s/%s", instance_prefs.getString("server", ""), user.screen_name)));
                } else {
                    i.setData(Uri.parse(String.format("http://%s/id%d", instance_prefs.getString("server", ""), user.id)));
                }
                startActivity(i);
            }
        }
        if(item.getItemId() == R.id.newpost) {
            openNewPostActivity();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void receiveState(int message, Bundle data) {
        try {
            if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if(args.startsWith("id")) {
                    account.parse(data.getString("response"), ovk_api);
                    try {
                        users.getUser(ovk_api, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        users.search(ovk_api, args);
                    }
                } else {
                    users.search(ovk_api, args);
                }
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
                    actionBarImitation.setHomeButtonVisibillity(true);
                    actionBarImitation.setActionButton("new_post", 0, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openNewPostActivity();
                        }
                    });
                } else {
                    if(activity_menu == null) {
                        onPrepareOptionsMenu(activity_menu);
                    }
                    //MenuItem newpost = activity_menu.getItem(R.id.newpost);
                    //newpost.setVisible(true);
                }
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileLayout.updateLayout(user);
                progressLayout.setVisibility(View.GONE);
                profileLayout.setVisibility(View.VISIBLE);
                profileLayout.setDMButtonListener(this, user.id);
                profileLayout.setAddToFriendsButtonListener(this, user.id, user);
                if(user.id == account.id) {
                    profileLayout.hideHeaderButtons(this);
                }
                if(user.friends_status == 0) {
                    findViewById(R.id.add_to_friends).setVisibility(View.VISIBLE);
                    activity_menu.getItem(0).setTitle(R.string.profile_add_friend);
                } else if(user.friends_status == 1) {
                    activity_menu.getItem(0).setTitle(R.string.profile_friend_cancel);
                } else if(user.friends_status == 2) {
                    activity_menu.getItem(0).setTitle(R.string.profile_friend_accept);
                }
                user.downloadAvatar(downloadManager, global_prefs.getString("photos_quality", ""));
                wall.get(ovk_api, user.id, 50);
                friends.get(ovk_api, user.id, "profile_counter");
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = status;
                } else if(status == 2) {
                    user.friends_status = 3;
                }
                profileLayout.setAddToFriendsButtonListener(this, user.id, user);
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                profileLayout.setAddToFriendsButtonListener(this, user.id, user);
            } else if(message == HandlerMessages.USERS_SEARCH) {
                users.parseSearch(data.getString("response"));
                users.getUser(ovk_api, users.getList().get(0).id);
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, global_prefs.getString("photos_quality", ""), data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).loadAvatars();
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager, false);
                ArrayList<Friend> friendsList = friends.getFriends();
                profileLayout.setCounter(user, "friends",  friends.count);
            } else if(message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
            } else if(message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
                WallPost item = wall.getWallItems().get(item_pos);
                for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                        poll.user_votes = 0;
                        PollAnswer answer = poll.answers.get(poll_answer);
                        answer.is_voted = false;
                        poll.answers.set(poll_answer, answer);
                        item.attachments.get(attachment_index).setContent(poll);
                        wall.getWallItems().set(item_pos, item);
                        ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                    }
                }
            } else if(message == HandlerMessages.POLL_DELETE_VOTE) {
                WallPost item = wall.getWallItems().get(item_pos);
                for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                        poll.user_votes = 0;
                        PollAnswer answer = poll.answers.get(poll_answer);
                        answer.is_voted = false;
                        poll.answers.set(poll_answer, answer);
                        item.attachments.get(attachment_index).setContent(poll);
                        wall.getWallItems().set(item_pos, item);
                        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                    }
                }
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR) {
                try {
                    if (data.containsKey("method")) {
                        if (data.getString("method").equals("Account.getProfileInfo") ||
                                (data.getString("method").equals("Users.get") && user.id == 0) ||
                                (data.getString("method").equals("Users.search") && user.id == 0) ||
                                (data.getString("method").equals("Friends.get") && friends.getFriends().size() == 0)) {
                            errorLayout.setReason(message);
                            errorLayout.setData(data);
                            errorLayout.setRetryAction(this);
                            progressLayout.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                        } else {
                            if (data.getString("method").equals("Wall.get")) {
                                ((WallErrorLayout) profileLayout.findViewById(R.id.wall_error_layout)).setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.err_text), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } catch (Exception ex) {
                    errorLayout.setReason(message);
                    errorLayout.setData(data);
                    errorLayout.setRetryAction(this);
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                if(global_prefs.getString("photos_quality", "").equals("medium")) {
                    if (user.avatar_msize_url.length() > 0) {
                        profileLayout.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                } else if(global_prefs.getString("photos_quality", "").equals("high")) {
                    if (user.avatar_hsize_url.length() > 0) {
                        profileLayout.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                } else {
                    if (user.avatar_osize_url.length() > 0) {
                        profileLayout.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
            errorLayout.setData(data);
            errorLayout.setRetryAction(this);
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    public void openIntentfromCounters(String action) {
        String url = action;
        Log.d("OpenVK", "Opening intent from " + action);
        if(action.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    public void addLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) profileLayout.findViewById(R.id.wall_layout));
        item = wall.getWallItems().get(position);
        wallLayout.select(position, "likes", "add");
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) profileLayout.findViewById(R.id.wall_layout));
        item = wall.getWallItems().get(position);
        wallLayout.select(0, "likes", "delete");
        likes.delete(ovk_api, item.owner_id, item.post_id, position);
    }

    public void getConversationById(int peer_id) {
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        try {
            intent.putExtra("peer_id", peer_id);
            intent.putExtra("conv_title", String.format("%s %s", users.getList().get(0).first_name, users.getList().get(0).last_name));
            if(users.getList().get(0).online) {
                intent.putExtra("online", 1);
            } else {
                intent.putExtra("online", 0);
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openNewPostActivity() {
        try {
            Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
            intent.putExtra("owner_id", user.id);
            intent.putExtra("account_id", account.id);
            intent.putExtra("account_first_name", user.first_name);
            startActivity(intent);
        } catch (Exception ex) {

        }
    }

    public void openWallComments(int position, View view) {
        if(account != null) {
            WallPost item;
            Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
            item = wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
            try {
                intent.putExtra("post_id", item.post_id);
                intent.putExtra("owner_id", item.owner_id);
                intent.putExtra("author_name", String.format("%s %s", account.user.first_name, account.user.last_name));
                intent.putExtra("author_id", account.id);
                intent.putExtra("post_author_id", item.author_id);
                intent.putExtra("post_author_name", item.name);
                intent.putExtra("post_info", item.info);
                intent.putExtra("post_text", item.text);
                intent.putExtra("post_likes", item.counters.likes);
                boolean contains_poll = false;
                boolean is_repost = false;
                if (item.attachments.size() > 0) {
                    for (int i = 0; i < item.attachments.size(); i++) {
                        if (item.attachments.get(i).type.equals("poll")) {
                            contains_poll = true;
                            PollAttachment poll = ((PollAttachment) item.attachments.get(i).getContent());
                            intent.putExtra("poll_question", poll.question);
                            intent.putExtra("poll_anonymous", poll.anonymous);
                            //intent.putExtra("poll_answers", poll.answers);
                            intent.putExtra("poll_total_votes", poll.votes);
                            intent.putExtra("poll_user_votes", poll.user_votes);
                        }
                    }
                }
                intent.putExtra("contains_poll", contains_poll);
                if (item.repost != null) {
                    is_repost = true;
                    intent.putExtra("is_repost", is_repost);
                    intent.putExtra("repost_id", item.repost.newsfeed_item.post_id);
                    intent.putExtra("repost_owner_id", item.repost.newsfeed_item.owner_id);
                    intent.putExtra("repost_author_id", item.repost.newsfeed_item.author_id);
                    intent.putExtra("repost_author_name", item.repost.newsfeed_item.name);
                    intent.putExtra("repost_info", item.repost.newsfeed_item.info);
                    intent.putExtra("repost_text", item.repost.newsfeed_item.text);
                } else {
                    intent.putExtra("is_repost", is_repost);
                }
                startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void showAuthorPage(int position) {
        WallPost item;
        item = wall.getWallItems().get(position);
        if(item.author_id != user.id) {
            if (item.author_id < 0) {
                String url = "openvk://group/" + "id" + -item.author_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } else {
                String url = "openvk://profile/" + "id" + item.author_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    }

    public void voteInPoll(int item_pos, int answer) {
        this.item_pos = item_pos;
        this.poll_answer = answer;
        WallPost item = wall.getWallItems().get(item_pos);
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if (item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                pollAttachment.user_votes = 1;
                if (!pollAttachment.answers.get(answer).is_voted) {
                    pollAttachment.answers.get(answer).is_voted = true;
                    pollAttachment.answers.get(answer).votes = pollAttachment.answers.get(answer).votes + 1;
                }
                wall.getWallItems().set(item_pos, item);
                pollAttachment.vote(ovk_api, pollAttachment.answers.get(answer).id);
            }
        }
    }

    public void removeVoteInPoll(int item_pos) {
        this.item_pos = item_pos;
        WallPost item = wall.getWallItems().get(item_pos);
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if(item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                for (int i = 0; i < pollAttachment.answers.size(); i++) {
                    if (pollAttachment.answers.get(i).is_voted) {
                        pollAttachment.answers.get(i).is_voted = false;
                        pollAttachment.answers.get(i).votes = pollAttachment.answers.get(i).votes - 1;
                    }
                }
                pollAttachment.user_votes = 0;
                wall.getWallItems().set(item_pos, item);
                pollAttachment.unvote(ovk_api);
            }
        }
    }

    public void addToFriends(int user_id) {
        if(user_id != account.id) {
            friends.add(ovk_api, user_id);
        }
    }

    public void deleteFromFriends(int user_id) {
        if(user_id != account.id) {
            friends.delete(ovk_api, user_id);
        }
    }

    public void openWallRepostComments(int position, View view) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        item = wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
            intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
            intent.putExtra("author_name", String.format("%s %s", account.first_name, account.last_name));
            intent.putExtra("author_id", account.id);
            intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
            intent.putExtra("post_info", item.repost.newsfeed_item.info);
            intent.putExtra("post_text", item.repost.newsfeed_item.text);
            intent.putExtra("post_likes", item.repost.newsfeed_item.counters.likes);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void viewPhotoAttachment(int position) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        item = wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("local_photo_addr", String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getCacheDir(),
                    item.owner_id, item.post_id));
            if(item.attachments != null) {
                for(int i = 0; i < item.attachments.size(); i++) {
                    if(item.attachments.get(i).type.equals("photo")) {
                        PhotoAttachment photo = ((PhotoAttachment) item.attachments.get(i).getContent());
                        intent.putExtra("original_link", photo.original_url);
                    }
                }
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
