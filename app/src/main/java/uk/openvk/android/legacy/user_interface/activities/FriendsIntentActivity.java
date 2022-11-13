package uk.openvk.android.legacy.user_interface.activities;

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
import android.widget.ListView;
import android.widget.TabHost;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ErrorLayout;
import uk.openvk.android.legacy.user_interface.layouts.FriendsLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProgressLayout;
import uk.openvk.android.legacy.user_interface.layouts.TabSelector;
import uk.openvk.android.legacy.user_interface.list_items.SlidingMenuItem;

/**
 * Created by Dmitry on 30.09.2022.
 */
public class FriendsIntentActivity extends Activity {

    private ArrayList<SlidingMenuItem> slidingMenuArray;
    private OvkAPIWrapper ovk_api;
    private Account account;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private FriendsLayout friendsLayout;
    private Friends friends;
    private Users users;
    private String access_token;

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
                if (path.startsWith("openvk://friends/")) {
                    String args = path.substring("openvk://friends/".length());
                    ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                    ovk_api.setServer(instance_prefs.getString("server", ""));
                    ovk_api.setAccessToken(access_token);
                    users = new Users();
                    friends = new Friends();
                    if(args.startsWith("id")) {
                        try {
                            friends.get(ovk_api, Integer.parseInt(args.substring(2)), "friends_list");
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
        friendsLayout = (FriendsLayout) findViewById(R.id.friends_layout);
        progressLayout.setVisibility(View.VISIBLE);
        TabHost friends_tabhost = friendsLayout.findViewById(R.id.friends_tabhost);
        setupTabHost(friends_tabhost, "friends");
        ((TabSelector) friendsLayout.findViewById(R.id.selector)).setLength(1);
        ((TabSelector) friendsLayout.findViewById(R.id.selector)).setTabTitle(0, getResources().getString(R.string.friends));
        ((TabSelector) friendsLayout.findViewById(R.id.selector)).setup(friends_tabhost, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.friends));
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
            actionBarImitation.setTitle(getResources().getString(R.string.friends));
            actionBarImitation.setOnMenuClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void setupTabHost(TabHost tabhost, String where) {
        tabhost.setup();
        if(where.equals("friends")) {
            TabHost.TabSpec tabSpec = tabhost.newTabSpec("main");
            tabSpec.setContent(R.id.tab1);
            tabSpec.setIndicator(getResources().getString(R.string.friends));
            tabhost.addTab(tabSpec);
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
            if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"), new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true)), true);
                ArrayList<Friend> friendsList = friends.getFriends();
                progressLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.VISIBLE);
                friendsLayout.createAdapter(this, friendsList, "friends");
                ((TabSelector) friendsLayout.findViewById(R.id.selector)).setTabTitle(0, String.format("%s (%d)", getResources().getString(R.string.friends), friends.count));
            }  else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsLayout.loadAvatars();
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR) {
                errorLayout.setReason(message);
                errorLayout.setData(data);
                errorLayout.setRetryAction(this);
                progressLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
            errorLayout.setRetryAction(this);
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + friends.getFriends().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void hideSelectedItemBackground(int position) {
        ((ListView) friendsLayout.findViewById(R.id.friends_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
    }
}
