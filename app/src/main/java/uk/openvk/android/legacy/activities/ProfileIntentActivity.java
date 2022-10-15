package uk.openvk.android.legacy.activities;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.ErrorLayout;
import uk.openvk.android.legacy.layouts.ProfileLayout;
import uk.openvk.android.legacy.layouts.ProgressLayout;
import uk.openvk.android.legacy.layouts.WallLayout;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

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
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void receiveState(int message, Bundle data) {
        try {
            if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if(args.startsWith("id")) {
                    try {
                        users.getUser(ovk_api, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        users.search(ovk_api, args);
                    }
                } else {
                    users.search(ovk_api, args);
                }
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileLayout.updateLayout(user);
                progressLayout.setVisibility(View.GONE);
                profileLayout.setVisibility(View.VISIBLE);
                profileLayout.setDMButtonListener(this, user.id);
                user.downloadAvatar(new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true)));
                friends.get(ovk_api, user.id, "profile_counter");
                wall.get(ovk_api, user.id, 50);
            } else if(message == HandlerMessages.USERS_SEARCH) {
                users.parseSearch(data.getString("response"));
                users.getUser(ovk_api, users.getList().get(0).id);
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).loadAvatars();
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager, false);
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    profileLayout.setCounter(user, "friends", friendsList.size());
                }
            } else if(message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
            } else if(message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR) {
                errorLayout.setReason(message);
                errorLayout.setData(data);
                errorLayout.setRetryAction(this);
                progressLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                if(user.avatar_url.length() > 0) {
                    profileLayout.loadAvatar(user);
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
        NewsfeedItem item = wall.getWallItems().get(position);
        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(1, "likes", "add");
        likes.add(ovk_api, item.owner_id, item.post_id, 1);
    }

    public void deleteLike(int position, String post, View view) {
        NewsfeedItem item = wall.getWallItems().get(position);
        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(0, "likes", "delete");
        likes.delete(ovk_api, item.owner_id, item.post_id, 0);
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

    public void openWallComments(int position, View view) {
        NewsfeedItem item;
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        item = wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("post_id", item.post_id);
            intent.putExtra("owner_id", item.owner_id);
            intent.putExtra("author_name", String.format("%s %s", account.first_name, account.last_name));
            intent.putExtra("author_id", account.id);
            intent.putExtra("post_author_id", item.author_id);
            intent.putExtra("post_author_name", item.name);
            intent.putExtra("post_info", item.info);
            intent.putExtra("post_text", item.text);
            intent.putExtra("post_likes", item.counters.likes);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showAuthorPage(int position) {
        NewsfeedItem item;
        item = wall.getWallItems().get(position);
        if(item.author_id < 0) {
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
