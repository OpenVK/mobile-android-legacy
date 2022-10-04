package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Arrays;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.LongPollServer;
import uk.openvk.android.legacy.api.Messages;
import uk.openvk.android.legacy.api.Newsfeed;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.ConversationsLayout;
import uk.openvk.android.legacy.layouts.ErrorLayout;
import uk.openvk.android.legacy.layouts.FriendsLayout;
import uk.openvk.android.legacy.layouts.NewsfeedLayout;
import uk.openvk.android.legacy.layouts.ProfileLayout;
import uk.openvk.android.legacy.layouts.ProgressLayout;
import uk.openvk.android.legacy.layouts.SlidingMenuLayout;
import uk.openvk.android.legacy.layouts.WallLayout;
import uk.openvk.android.legacy.list_adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.list_items.NewsfeedItem;
import uk.openvk.android.legacy.list_items.SlidingMenuItem;
import uk.openvk.android.legacy.services.LongPollService;

/**
 * Created by Dmitry on 27.09.2022.
 */
public class AppActivity extends Activity {
    private ArrayList<SlidingMenuItem> slidingMenuArray;
    private OvkAPIWrapper ovk_api;
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
    private LongPollService longPollService;
    private Account account;
    private Newsfeed newsfeed;
    private Messages messages;
    private Users users;
    private Friends friends;
    private Wall wall;
    private ArrayList<Conversation> conversations;
    private User user;
    private Likes likes;
    private Menu activity_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        setContentView(R.layout.app_layout);
        createSlidingMenu();
        installLayouts();
        Global global = new Global();
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
        account = new Account();
        account.getProfileInfo(ovk_api);
        newsfeed = new Newsfeed();
        user = new User();
        likes = new Likes();
        global_prefs_editor.putString("current_screen", "newsfeed");
        global_prefs_editor.commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                if (!menu.isMenuShowing()) menu.showMenu(true);
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

    private void createSlidingMenu() {
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
        slidingmenuLayout = new SlidingMenuLayout(this);
        menu.setMenu(slidingmenuLayout);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setFadeDegree(0.8f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setSlidingEnabled(true);
        slidingMenuArray = new ArrayList<SlidingMenuItem>();
        if (slidingMenuArray != null) {
            for (int slider_menu_item_index = 0; slider_menu_item_index < getResources().getStringArray(R.array.leftmenu).length; slider_menu_item_index++) {
                if (slider_menu_item_index == 0) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_friends)));
                } else if (slider_menu_item_index == 1) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_photos)));
                } else if (slider_menu_item_index == 2) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_video)));
                } else if (slider_menu_item_index == 3) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_messages)));
                } else if (slider_menu_item_index == 4) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_groups)));
                } else if (slider_menu_item_index == 5) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_news)));
                } else if (slider_menu_item_index == 6) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_feedback)));
                } else if (slider_menu_item_index == 7) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_fave)));
                } else if (slider_menu_item_index == 8) {
                    slidingMenuArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_settings)));
                }
            }
            SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this, slidingMenuArray);
            ((ListView) menu.getMenu().findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
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
        conversationsLayout = (ConversationsLayout) findViewById(R.id.conversations_layout);
        progressLayout.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setIcon(R.drawable.ic_left_menu);
            }
        } else {
            ActionBarImitation actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setTitle(getResources().getString(R.string.app_name));
            actionBarImitation.setOnMenuClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!menu.isMenuShowing()) menu.showMenu(true);
                }
            });
        }
        if(activity_menu == null) {
            onPrepareOptionsMenu(activity_menu);
        }
        //MenuItem newpost = activity_menu.findItem(R.id.newpost);
        //newpost.setVisible(false);
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
        if(position == 0) {
            menu.toggle(true);
            if(friendsLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.VISIBLE);
            }
            global_prefs_editor.putString("current_screen", "friends");
            global_prefs_editor.commit();
            friends.get(ovk_api, account.id, "friends_list");
        } else if(position == 3) {
            menu.toggle(true);
            if(conversationsLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
            }
            global_prefs_editor.putString("current_screen", "messages");
            global_prefs_editor.commit();
            messages.getConversations(ovk_api);
        } else if(position == 5) {
            menu.toggle(true);
            if(newsfeedLayout.getCount() == 0) {
                profileLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                profileLayout.setVisibility(View.GONE);
                friendsLayout.setVisibility(View.GONE);
                conversationsLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                newsfeedLayout.setVisibility(View.VISIBLE);
            }
            global_prefs_editor.putString("current_screen", "newsfeed");
            global_prefs_editor.commit();
            newsfeed.get(ovk_api, 50);
        } else if(position == 8) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, MainSettingsActivity.class);
            startActivity(intent);
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"));
                String profile_name = String.format("%s %s", account.first_name, account.last_name);
                slidingmenuLayout.setProfileName(profile_name);
                newsfeed.get(ovk_api, 50);
                messages = new Messages();
                messages.getLongPollServer(ovk_api);
                users = new Users();
                friends = new Friends();
                wall = new Wall();
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
            } else if (message == HandlerMessages.NEWSFEED_GET) {
                newsfeed.parse(this, data.getString("response"));
                newsfeedLayout.createAdapter(this, newsfeed.getNewsfeedItems());
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    progressLayout.setVisibility(View.GONE);
                    newsfeedLayout.setVisibility(View.VISIBLE);
                }
            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                LongPollServer longPollServer = messages.parseLongPollServer(data.getString("response"));
                longPollService = new LongPollService(this, instance_prefs.getString("access_token", ""));
                longPollService.start(instance_prefs.getString("server", ""), longPollServer.address, longPollServer.key, longPollServer.ts, global_prefs.getBoolean("useHTTPS", true));
            } else if (message == HandlerMessages.NEWSFEED_ATTACHMENT) {
                newsfeedLayout.setScrollingPositions();
            } else if(message == HandlerMessages.NEWSFEED_ITEM_AVATAR) {
                newsfeedLayout.updateAllItems();
            } else if (message == HandlerMessages.WALL_ATTACHMENT) {
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileLayout.updateLayout(user);
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    progressLayout.setVisibility(View.GONE);
                    profileLayout.setVisibility(View.VISIBLE);
                    profileLayout.setDMButtonListener(this, user.id);
                    wall.get(ovk_api, user.id, 50);
                }
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, data.getString("response"));
                ((WallLayout) profileLayout.findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"));
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("friends")) {
                    progressLayout.setVisibility(View.GONE);
                    friendsLayout.setVisibility(View.VISIBLE);
                }
                friendsLayout.createAdapter(this, friendsList);
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                // not implemented yet
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
                    message == HandlerMessages.INTERNAL_ERROR) {
                errorLayout.setReason(message);
                progressLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            } else if(message == HandlerMessages.INVALID_TOKEN) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_session), Toast.LENGTH_LONG).show();
                instance_prefs_editor.putString("access_token", "");
                instance_prefs_editor.putString("server", "");
                instance_prefs_editor.commit();
                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
            errorLayout.setRetryAction(this, data.getString("method"), data.getString("args"));
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    public void openAccountProfile() {
        menu.toggle(true);
        profileLayout.setVisibility(View.GONE);
        newsfeedLayout.setVisibility(View.GONE);
        friendsLayout.setVisibility(View.GONE);
        conversationsLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        global_prefs_editor.putString("current_screen", "profile");
        global_prefs_editor.commit();
        users.getUser(ovk_api, account.id);
    }

    public void openFriendsList() {
        profileLayout.setVisibility(View.GONE);
        newsfeedLayout.setVisibility(View.GONE);
        friendsLayout.setVisibility(View.GONE);
        conversationsLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        global_prefs_editor.putString("current_screen", "friends");
        global_prefs_editor.commit();
        friends.get(ovk_api, account.id, "friends_list");
    }

    public void retryConnection(String method, String args) {
        errorLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        if(method.equals("Newsfeed.get")) {
            newsfeed.get(ovk_api, 50);
        } else if(method.equals("Account.getProfileInfo")) {
            account.getProfileInfo(ovk_api);
        } else if(method.equals("Messages.getLongPollServer")) {
            messages.getLongPollServer(ovk_api);
        } else if(method.equals("Friends.get")) {
            friends.get(ovk_api, account.id, "friends_list");
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
        NewsfeedItem item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
        } else {
            item = newsfeed.getNewsfeedItems().get(position);
        }
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        NewsfeedItem item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
        } else {
            item = newsfeed.getNewsfeedItems().get(position);
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
        NewsfeedItem item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
        } else {
            item = newsfeed.getNewsfeedItems().get(position);
        }
        Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
        try {
            intent.putExtra("post_id", item.post_id);
            intent.putExtra("owner_id", item.owner_id);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
