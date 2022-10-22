package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.LongPollServer;
import uk.openvk.android.legacy.api.Messages;
import uk.openvk.android.legacy.api.Newsfeed;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.longpoll_api.MessageEvent;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ConversationsLayout;
import uk.openvk.android.legacy.user_interface.layouts.ErrorLayout;
import uk.openvk.android.legacy.user_interface.layouts.FriendsLayout;
import uk.openvk.android.legacy.user_interface.layouts.GroupsLayout;
import uk.openvk.android.legacy.user_interface.layouts.NewsfeedLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProfileLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProgressLayout;
import uk.openvk.android.legacy.user_interface.layouts.SlidingMenuLayout;
import uk.openvk.android.legacy.user_interface.layouts.WallLayout;
import uk.openvk.android.legacy.user_interface.list_adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.api.models.WallPost;
import uk.openvk.android.legacy.user_interface.list_items.SlidingMenuItem;
import uk.openvk.android.legacy.longpoll_api.LongPollService;

public class AppActivity extends Activity {
    private ArrayList<SlidingMenuItem> slidingMenuArray;
    private OvkAPIWrapper ovk_api;
    private LongPollService longPollService;
    private DownloadManager downloadManager;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;
    private SlidingMenu menu;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private NewsfeedLayout newsfeedLayout;
    private ProfileLayout profileLayout;
    private FriendsLayout friendsLayout;
    private ConversationsLayout conversationsLayout;
    private SlidingMenuLayout slidingmenuLayout;
    private Account account;
    private Newsfeed newsfeed;
    private Messages messages;
    private Users users;
    private Groups groups;
    private Friends friends;
    private Wall wall;
    private ArrayList<Conversation> conversations;
    private User user;
    private Likes likes;
    private Menu activity_menu;
    private GroupsLayout groupsLayout;
    private int newsfeed_count = 25;
    private int notification_id;
    private String last_longpoll_response;
    private int item_pos;
    private int poll_answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(instance_prefs.getString("access_token", "").length() == 0 || instance_prefs.getString("server", "").length() == 0) {
            finish();
        }
        last_longpoll_response = "";
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        setContentView(R.layout.app_layout);
        createSlidingMenu();
        installLayouts();
        Global global = new Global();
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        account = new Account(this);
        account.getProfileInfo(ovk_api);
        newsfeed = new Newsfeed();
        user = new User();
        likes = new Likes();
        messages = new Messages();
        users = new Users();
        friends = new Friends();
        groups = new Groups();
        wall = new Wall();
        global_prefs_editor.putString("current_screen", "newsfeed");
        global_prefs_editor.commit();
        if(((OvkApplication) getApplicationContext()).isTablet) {
            newsfeedLayout.adjustLayoutSize(getResources().getConfiguration().orientation);
            ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
        }
        Bundle data = new Bundle();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                if(!((OvkApplication) getApplicationContext()).isTablet) {
                    menu.toggle(true);
                } else {
                    if(slidingmenuLayout.getVisibility() == View.VISIBLE) {
                        slidingmenuLayout.setVisibility(View.GONE);
                    } else {
                        slidingmenuLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if(item.getItemId() == R.id.newpost) {
            openNewPostActivity();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newsfeed, menu);
        activity_menu = menu;
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newsfeedLayout.adjustLayoutSize(newConfig.orientation);
        ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).adjustLayoutSize(newConfig.orientation);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
        }
        super.onConfigurationChanged(newConfig);
    }

    private void createSlidingMenu() {
        slidingmenuLayout = new SlidingMenuLayout(this);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            menu = new SlidingMenu(this);
            menu.setMode(SlidingMenu.LEFT);
            menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
            menu.setMenu(slidingmenuLayout);
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            menu.setFadeDegree(0.8f);
            menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
            menu.setSlidingEnabled(true);
        } else {
            try {
                slidingmenuLayout = findViewById(R.id.sliding_menu);
                slidingmenuLayout.setAccountProfileListener(this);
                slidingmenuLayout.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ((OvkApplication) getApplicationContext()).isTablet = false;
                menu = new SlidingMenu(this);
                menu.setMode(SlidingMenu.LEFT);
                menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
                menu.setMenu(slidingmenuLayout);
                menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                menu.setFadeDegree(0.8f);
                menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
                menu.setSlidingEnabled(true);
            }
        }
        slidingmenuLayout.setProfileName(getResources().getString(R.string.loading));
        slidingMenuArray = new ArrayList<SlidingMenuItem>();
        if (slidingMenuArray != null) {
            for (int slider_menu_item_index = 0; slider_menu_item_index < getResources().getStringArray(R.array.leftmenu).length; slider_menu_item_index++) {
                if (slider_menu_item_index == 0) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_friends)));
                } else if (slider_menu_item_index == 1) {
                    //slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_photos)));
                } else if (slider_menu_item_index == 2) {
                    //slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_video)));
                } else if (slider_menu_item_index == 3) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_messages)));
                } else if (slider_menu_item_index == 4) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_groups)));
                } else if (slider_menu_item_index == 5) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_news)));
                } else if (slider_menu_item_index == 6) {
                    //slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_feedback)));
                } else if (slider_menu_item_index == 7) {
                    //slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_fave)));
                } else if (slider_menu_item_index == 8) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_settings)));
                }
            }
            SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this, slidingMenuArray);
            if(!((OvkApplication) getApplicationContext()).isTablet) {
                ((ListView) menu.getMenu().findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
            } else {
                ((ListView) slidingmenuLayout.findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
            }
        }
        slidingmenuLayout.setSearchListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), QuickSearchActivity.class);
                try {
                    startActivity(intent);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        profileLayout = (ProfileLayout) findViewById(R.id.profile_layout);
        newsfeedLayout = (NewsfeedLayout) findViewById(R.id.newsfeed_layout);
        friendsLayout = (FriendsLayout) findViewById(R.id.friends_layout);
        groupsLayout = (GroupsLayout) findViewById(R.id.groups_layout);
        conversationsLayout = (ConversationsLayout) findViewById(R.id.conversations_layout);
        progressLayout.setVisibility(View.VISIBLE);
        newsfeedLayout.adjustLayoutSize(getResources().getConfiguration().orientation);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setIcon(R.drawable.ic_left_menu);
            }
        } else {
            ActionBarImitation actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setOnMenuClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!menu.isMenuShowing()) menu.showMenu(true);
                }
            });
        }
        setActionBarTitle(getResources().getString(R.string.newsfeed));
        if(activity_menu == null) {
            onPrepareOptionsMenu(activity_menu);
        }
        //MenuItem newpost = activity_menu.findItem(R.id.newpost);
        //newpost.setVisible(false);
    }

    public void setActionBarTitle(String title) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setTitle(title);
        } else {
            ActionBarImitation actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setTitle(title);
        }
    }

    private void openNewPostActivity() {
        try {
            Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                intent.putExtra("owner_id", user.id);
            } else {
                intent.putExtra("owner_id", account.id);
            }
            startActivity(intent);
        } catch (Exception ex) {

        }
    }

    public void onSlidingMenuItemClicked(int position) {
        global_prefs_editor = global_prefs.edit();
        if(position < 4) {
            if (!((OvkApplication) getApplicationContext()).isTablet) {
                menu.toggle(true);
            }
        }
        if(position == 0) {
            setActionBarTitle(getResources().getString(R.string.friends));
            if(friendsLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.VISIBLE);
            }
            global_prefs_editor.putString("current_screen", "friends");
            global_prefs_editor.commit();
            if(friends == null) {
                friends = new Friends();
            }
            friends.get(ovk_api, account.id, "friends_list");
        } else if(position == 1) {
            setActionBarTitle(getResources().getString(R.string.messages));
            if(conversationsLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
            }
            global_prefs_editor.putString("current_screen", "messages");
            global_prefs_editor.commit();
            if(messages == null) {
                messages = new Messages();
            }
            messages.getConversations(ovk_api);
        } else if(position == 2) {
            setActionBarTitle(getResources().getString(R.string.groups));
            if(groupsLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.VISIBLE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
            }
            global_prefs_editor.putString("current_screen", "groups");
            global_prefs_editor.commit();
            if(groups == null) {
                groups = new Groups();
            }
            groups.getGroups(ovk_api, account.id);
        } else if(position == 3) {
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if(newsfeedLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                groupsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            global_prefs_editor.putString("current_screen", "newsfeed");
            global_prefs_editor.commit();
            if(newsfeed == null) {
                newsfeed = new Newsfeed();
            }
            newsfeed_count = 25;
            newsfeed.get(ovk_api, newsfeed_count);
        } else if(position == 4) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, MainSettingsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show();
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                String profile_name = String.format("%s %s", account.first_name, account.last_name);
                slidingmenuLayout.setProfileName(profile_name);
                newsfeed.get(ovk_api, newsfeed_count);
                messages.getLongPollServer(ovk_api);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
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
                account.getCounters(ovk_api);
                users.getAccountUser(ovk_api, account.id);
                if(messages == null) {
                    messages = new Messages();
                }
                messages.getConversations(ovk_api);
            } else if (message == HandlerMessages.ACCOUNT_COUNTERS) {
                account.parseCounters(data.getString("response"));
                SlidingMenuItem friends_item = slidingMenuArray.get(0);
                friends_item.counter = account.counters.friends_requests;
                slidingMenuArray.set(0, friends_item);
                SlidingMenuItem messages_item = slidingMenuArray.get(1);
                messages_item.counter = account.counters.new_messages;
                slidingMenuArray.set(1, messages_item);
                //SlidingMenuItem notifications_item = slidingMenuArray.get(6);
                //notifications_item.counter = account.counters.notifications;
                //slidingMenuArray.set(6, notifications_item);
                SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this, slidingMenuArray);
                if(!((OvkApplication) getApplicationContext()).isTablet) {
                    ((ListView) menu.getMenu().findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
                } else {
                    ((ListView) slidingmenuLayout.findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
                }
            } else if (message == HandlerMessages.NEWSFEED_GET) {
                newsfeed.parse(this, downloadManager, data.getString("response"));
                newsfeedLayout.createAdapter(this, newsfeed.getWallPosts());
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    progressLayout.setVisibility(View.GONE);
                    newsfeedLayout.setVisibility(View.VISIBLE);
                }
                newsfeedLayout.loading_more_posts = true;
                newsfeedLayout.setScrollingPositions(this, false, true);
            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                LongPollServer longPollServer = messages.parseLongPollServer(data.getString("response"));
                longPollService = new LongPollService(this, instance_prefs.getString("access_token", ""));
                longPollService.run(instance_prefs.getString("server", ""), longPollServer.address, longPollServer.key, longPollServer.ts, global_prefs.getBoolean("useHTTPS", true));
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                slidingmenuLayout.loadAccountAvatar(account);
            } else if (message == HandlerMessages.NEWSFEED_ATTACHMENTS) {
                newsfeedLayout.setScrollingPositions(this, true, true);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS) {
                newsfeedLayout.loadAvatars();
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).loadAvatars();
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsLayout.loadAvatars();
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                groupsLayout.loadAvatars();
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileLayout.updateLayout(user);
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    progressLayout.setVisibility(View.GONE);
                    profileLayout.setVisibility(View.VISIBLE);
                    profileLayout.setDMButtonListener(this, user.id);
                    profileLayout.setAddToFriendsButtonListener(this, user.id, user);
                    if(user.id == account.id) {
                        profileLayout.hideHeaderButtons(this);
                    }
                    user.downloadAvatar(downloadManager);
                    wall.get(ovk_api, user.id, 50);
                    friends.get(ovk_api, user.id, "profile_counter");
                }
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                users.parse(data.getString("response"));
                account.user = users.getList().get(0);
                account.user.downloadAvatar(downloadManager, "account_avatar");
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"), downloadManager, true);
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("friends")) {
                    progressLayout.setVisibility(View.GONE);
                    friendsLayout.setVisibility(View.VISIBLE);
                }
                friendsLayout.createAdapter(this, friendsList);
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
            } else if (message == HandlerMessages.GROUPS_GET) {
                groups.parse(data.getString("response"), downloadManager, true);
                ArrayList<Group> groupsList = groups.getList();
                if (global_prefs.getString("current_screen", "").equals("groups")) {
                    progressLayout.setVisibility(View.GONE);
                    groupsLayout.setVisibility(View.VISIBLE);
                }
                groupsLayout.createAdapter(this, groupsList);
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager, false);
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    profileLayout.setCounter(user, "friends",  friends.count);
                }
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                conversations = messages.parseConversationsList(data.getString("response"));
                conversationsLayout.createAdapter(this, conversations);
                if (global_prefs.getString("current_screen", "").equals("messages")) {
                    progressLayout.setVisibility(View.GONE);
                    conversationsLayout.setVisibility(View.VISIBLE);
                }
            } else if(message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedLayout.select(likes.position, "likes", 1);
                } else if (global_prefs.getString("current_screen", "").equals("profile")) {
                    ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedLayout.select(likes.position, "likes", 0);
                } else if (global_prefs.getString("current_screen", "").equals("profile")) {
                    ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
                }
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR || message == HandlerMessages.BROKEN_SSL_CONNECTION || message == HandlerMessages.UNKNOWN_ERROR) {
                if(data.containsKey("method")) {
                    if (data.getString("method").equals("Account.getProfileInfo") || (data.getString("method").equals("Newsfeed.get") && newsfeed.getWallPosts().size() == 0) ||
                            (data.getString("method").equals("Messages.getConversations") && conversations.size() == 0) ||
                            (data.getString("method").equals("Friends.get") && friends.getFriends().size() == 0)) {
                        errorLayout.setReason(message);
                        errorLayout.setData(data);
                        errorLayout.setRetryAction(this);
                        progressLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                }
            } else if(message == HandlerMessages.INVALID_TOKEN) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_session), Toast.LENGTH_LONG).show();
                instance_prefs_editor.putString("access_token", "");
                instance_prefs_editor.putString("server", "");
                instance_prefs_editor.commit();
                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                startActivity(intent);
                finish();
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                if(user.avatar_url.length() > 0) {
                    profileLayout.loadAvatar(user);
                }
            } else if(message == HandlerMessages.LONGPOLL) {
                MessageEvent msg_event = new MessageEvent(data.getString("response"));
                if(msg_event.peer_id > 0 && global_prefs.getBoolean("enableNotification", true)) {
                    if (!last_longpoll_response.equals(data.getString("response"))) {
                        String msg_author = String.format("Unknown ID %d", msg_event.peer_id);
                        if(conversations != null) {
                            for (int i = 0; i < conversations.size(); i++) {
                                if (conversations.get(i).peer_id == msg_event.peer_id) {
                                    msg_author = conversations.get(i).title;
                                }
                            }
                        }
                        notification_id = notification_id + 1;
                        last_longpoll_response = data.getString("response");
                        NotificationManager notifMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        Notification notification = createNotification(notifMan, "lp_updates", "LongPoll Updates", R.drawable.ic_stat_notify, msg_author, msg_event.msg_text);
                        notification.contentIntent = createConversationIntent(msg_event.peer_id, msg_author);
                        notifMan.notify(notification_id, notification);
                    }
                }
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    WallPost item = newsfeed.getWallPosts().get(item_pos);
                    for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                            poll.user_votes = 1;
                            PollAnswer answer = poll.answers.get(poll_answer);
                            answer.is_voted = true;
                            poll.answers.set(poll_answer, answer);
                            item.attachments.get(attachment_index).setContent(poll);
                            newsfeed.getWallPosts().set(item_pos, item);
                            newsfeedLayout.updateItem(item, item_pos);
                        }
                    }
                } else if(global_prefs.getString("current_screen", "").equals("profile")) {
                    WallPost item = wall.getWallItems().get(item_pos);
                    for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                            poll.user_votes = 1;
                            PollAnswer answer = poll.answers.get(poll_answer);
                            answer.is_voted = true;
                            poll.answers.set(poll_answer, answer);
                            item.attachments.get(attachment_index).setContent(poll);
                            wall.getWallItems().set(item_pos, item);
                            ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                        }
                    }
                }
            } else if(message == HandlerMessages.POLL_DELETE_VOTE) {
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    WallPost item = newsfeed.getWallPosts().get(item_pos);
                    for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                            poll.user_votes = 0;
                            PollAnswer answer = poll.answers.get(poll_answer);
                            answer.is_voted = false;
                            poll.answers.set(poll_answer, answer);
                            item.attachments.get(attachment_index).setContent(poll);
                            newsfeed.getWallPosts().set(item_pos, item);
                            newsfeedLayout.updateItem(item, item_pos);
                        }
                    }
                } else if(global_prefs.getString("current_screen", "").equals("profile")) {
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

    public Notification createNotification(NotificationManager notifMan, String channel_id, String channel, int icon, String title, String description) {
        NotificationManager notificationManager;
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channel_id, channel, importance);
            notifMan.createNotificationChannel(mChannel);
            Notification.Builder builder =
                    new Notification.Builder(this)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setChannelId("lp_updates");
            notification = builder.build();
            Intent notificationIntent = new Intent(this, ConversationActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.contentIntent = contentIntent;
            notifMan.notify(notification_id, notification);
        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description);

            notification = builder.build();
        }
        return notification;
    }

    public PendingIntent createConversationIntent(int peer_id, String title) {
        Intent notificationIntent = new Intent(this, ConversationActivity.class);
        notificationIntent.putExtra("peer_id", peer_id);
        notificationIntent.putExtra("conv_title", title);
        notificationIntent.putExtra("online", 1);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        return contentIntent;
    }

    public void openAccountProfile() {
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            menu.toggle(true);
        }
        profileLayout.setVisibility(View.GONE);
        newsfeedLayout.setVisibility(View.GONE);
        friendsLayout.setVisibility(View.GONE);
        groupsLayout.setVisibility(View.GONE);
        conversationsLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        global_prefs_editor.putString("current_screen", "profile");
        global_prefs_editor.commit();
        if(users == null) {
            users = new Users();
        }
        users.getUser(ovk_api, account.id);
        setActionBarTitle(getResources().getString(R.string.profile));
    }

    public void openFriendsList() {
        profileLayout.setVisibility(View.GONE);
        newsfeedLayout.setVisibility(View.GONE);
        friendsLayout.setVisibility(View.GONE);
        conversationsLayout.setVisibility(View.GONE);
        groupsLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        global_prefs_editor.putString("current_screen", "friends");
        global_prefs_editor.commit();
        friends.get(ovk_api, account.id, "friends_list");
    }

    public void retryConnection(String method, String args) {
        profileLayout.setVisibility(View.GONE);
        newsfeedLayout.setVisibility(View.GONE);
        friendsLayout.setVisibility(View.GONE);
        groupsLayout.setVisibility(View.GONE);
        conversationsLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        if(account.id == 0) {
            account.addQueue(method, args);
            account.getProfileInfo(ovk_api);
        } else {
            if(method != null) {
                if (method.equals("Newsfeed.get")) {
                    if (newsfeed == null) {
                        newsfeed = new Newsfeed();
                    }
                    newsfeed.get(ovk_api, 50);
                } else if (method.equals("Messages.getLongPollServer")) {
                    if (messages == null) {
                        messages = new Messages();
                    }
                    messages.getLongPollServer(ovk_api);
                } else if (method.equals("Messages.getConversations")) {
                    if (messages == null) {
                        messages = new Messages();
                    }
                    messages.getConversations(ovk_api);
                } else if (method.equals("Friends.get")) {
                    if (friends == null) {
                        friends = new Friends();
                    }
                    friends.get(ovk_api, account.id, "friends_list");
                } else if (method.equals("Groups.get")) {
                    if (groups == null) {
                        groups = new Groups();
                    }
                    groups.getGroups(ovk_api, account.id);
                } else if (method.equals("Users.get")) {
                    if (users == null) {
                        users = new Users();
                    }
                    users.getUser(ovk_api, account.id);
                }
            }
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

    public void openIntentfromCounters(String action) {
        String url = action;
        if(action.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

    public void getConversation(int position) {
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        try {
            intent.putExtra("peer_id", conversations.get(position).peer_id);
            intent.putExtra("conv_title", conversations.get(position).title);
            intent.putExtra("online", conversations.get(position).online);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addLike(int position, String post, View view) {
        WallPost item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(position, "likes", "add");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedLayout.select(position, "likes", "add");
        }
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(0, "likes", "delete");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedLayout.select(0, "likes", "delete");
        }
        likes.delete(ovk_api, item.owner_id, item.post_id, position);
    }

    public void getConversationById(int peer_id) {
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        try {
            intent.putExtra("peer_id", peer_id);
            intent.putExtra("conv_title", String.format("%s %s", user.first_name, user.last_name));
            if(user.online) {
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
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
        } else {
            item = newsfeed.getWallPosts().get(position);
            intent.putExtra("where", "newsfeed");
        }
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
            boolean contains_poll = false;
            boolean is_repost = false;
            if(item.attachments.size() > 0) {
                for(int i = 0; i < item.attachments.size(); i++) {
                    if(item.attachments.get(i).type.equals("poll")) {
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
            if(item.repost != null) {
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

    public void showGroup(int position) {
        String url = "openvk://group/" + "club" + groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void showAuthorPage(int position) {
        WallPost item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
        } else {
            item = newsfeed.getWallPosts().get(position);
        }
        if(item.author_id < 0) {
            String url = "openvk://group/" + "club" + -item.author_id;
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

    public void loadMoreNews() {
        newsfeed_count = newsfeed_count + 25;
        if(newsfeed != null) {
            newsfeed.get(ovk_api, newsfeed_count);
        }
    }

    public void voteInPoll(int item_pos, int answer) {
        this.item_pos = item_pos;
        this.poll_answer = answer;
        WallPost item = newsfeed.getWallPosts().get(item_pos);
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if (item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                pollAttachment.user_votes = 1;
                if (!pollAttachment.answers.get(answer).is_voted) {
                    pollAttachment.answers.get(answer).is_voted = true;
                    pollAttachment.answers.get(answer).votes = pollAttachment.answers.get(answer).votes + 1;
                }
                newsfeed.getWallPosts().set(item_pos, item);
                pollAttachment.vote(ovk_api, pollAttachment.id);
            }
        }
    }

    public void removeVoteInPoll(int item_pos) {
        this.item_pos = item_pos;
        WallPost item = newsfeed.getWallPosts().get(item_pos);
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
                newsfeed.getWallPosts().set(item_pos, item);
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
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
        } else {
            item = newsfeed.getWallPosts().get(position);
            intent.putExtra("where", "newsfeed");
        }
        try {
            intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
            intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
            intent.putExtra("author_name", String.format("%s %s", account.first_name, account.last_name));
            intent.putExtra("author_id", account.id);
            intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
            intent.putExtra("post_info", item.repost.newsfeed_item.info);
            intent.putExtra("post_text", item.repost.newsfeed_item.text);
            intent.putExtra("post_likes", 0);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
