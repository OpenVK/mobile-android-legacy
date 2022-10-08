package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.ErrorLayout;
import uk.openvk.android.legacy.layouts.ProfileLayout;
import uk.openvk.android.legacy.layouts.ProgressLayout;
import uk.openvk.android.legacy.layouts.WallLayout;
import uk.openvk.android.legacy.list_items.NewsfeedItem;
import uk.openvk.android.legacy.list_items.SlidingMenuItem;

public class ProfileIntentActivity extends Activity {

    private ArrayList<SlidingMenuItem> slidingMenuArray;
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
                if (path.startsWith("openvk://profile/")) {
                    String args = path.substring("openvk://profile/".length());
                    ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                    downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
                    ovk_api.setServer(instance_prefs.getString("server", ""));
                    ovk_api.setAccessToken(access_token);
                    users = new Users();
                    wall = new Wall();
                    if(args.startsWith("id")) {
                        try {
                            users.getUser(ovk_api, Integer.parseInt(args.substring(2)));
                        } catch (Exception ex) {
                            users.search(ovk_api, args);
                        }
                    } else {
                        users.search(ovk_api, args);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
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
            actionBarImitation.setTitle(getResources().getString(R.string.profile));
            actionBarImitation.setOnMenuClickListener(new View.OnClickListener() {
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
            if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileLayout.updateLayout(user);
                progressLayout.setVisibility(View.GONE);
                profileLayout.setVisibility(View.VISIBLE);
                profileLayout.setDMButtonListener(this, user.id);
                user.downloadAvatar(new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true)));
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
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR) {
                errorLayout.setReason(message);
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
            errorLayout.setRetryAction(this, data.getString("method"), data.getString("args"));
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
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        NewsfeedItem item = wall.getWallItems().get(position);
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
}
