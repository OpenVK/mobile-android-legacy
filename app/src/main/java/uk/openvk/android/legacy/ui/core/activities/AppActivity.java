package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Messages;
import uk.openvk.android.legacy.api.Newsfeed;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.LongPollServer;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.models.WallPost;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.longpoll_api.LongPollService;
import uk.openvk.android.legacy.longpoll_api.receivers.LongPollReceiver;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.fragments.app.ConversationsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.GroupsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.MainSettingsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.list.adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuItem;
import uk.openvk.android.legacy.ui.view.layouts.ActionBarLayout;
import uk.openvk.android.legacy.ui.view.layouts.ErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.view.layouts.SlidingMenuLayout;
import uk.openvk.android.legacy.ui.view.layouts.TabSelector;
import uk.openvk.android.legacy.ui.view.layouts.WallErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

@SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
public class AppActivity extends FragmentActivity {
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
    private NewsfeedFragment newsfeedFragment;
    private ProfileFragment profileFragment;
    private FriendsFragment friendsFragment;
    private ConversationsFragment conversationsFragment;
    private MainSettingsFragment mainSettingsFragment;
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
    private GroupsFragment groupsFragment;
    private int newsfeed_count = 25;
    private String last_longpoll_response;
    private int item_pos;
    private int poll_answer;
    private uk.openvk.android.legacy.api.wrappers.NotificationManager notifMan;
    private boolean inBackground;
    public ActionBarLayout ab_layout;
    private int menu_id;
    private dev.tinelix.retro_ab.ActionBar actionBar;
    private LongPollReceiver lpReceiver;
    private FragmentTransaction ft;
    private Fragment selectedFragment;
    private static String TAG = "OpenVK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        inBackground = true;
        menu_id = R.menu.newsfeed;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        if(instance_prefs.getString("access_token", "").length() == 0 || instance_prefs.getString("server", "").length() == 0) {
            finish();
        }

        last_longpoll_response = "";
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        installFragments();
        Global global = new Global();
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setProxyConnection(global_prefs.getBoolean("useProxy", false), global_prefs.getString("proxy_address", ""));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d(OvkApplication.APP_TAG, String.format("Handling API message: %s", message.what));
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
        registerBroadcastReceiver();
        if(((OvkApplication) getApplicationContext()).isTablet) {
            newsfeedFragment.adjustLayoutSize(getResources().getConfiguration().orientation);
            try {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Bundle data = new Bundle();
        createSlidingMenu();
        ((OvkApplication) getApplicationContext()).notifMan = new uk.openvk.android.legacy.api.wrappers.NotificationManager(AppActivity.this,
                global_prefs.getBoolean("notifyLED", true), global_prefs.getBoolean("notifyVibrate", true), global_prefs.getBoolean("notifySound", true),
                global_prefs.getString("notifyRingtone", ""));
        notifMan = ((OvkApplication) getApplicationContext()).notifMan;
        if(activity_menu == null) {
            android.support.v7.widget.PopupMenu p  = new android.support.v7.widget.PopupMenu(this, null);
            activity_menu = p.getMenu();
            getMenuInflater().inflate(R.menu.newsfeed, activity_menu);
            onCreateOptionsMenu(activity_menu);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(lpReceiver != null) {
            unregisterReceiver(lpReceiver);
        }
        finish();
        System.exit(0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void registerBroadcastReceiver() {
        lpReceiver = new LongPollReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Bundle data = intent.getExtras();
                receiveState(HandlerMessages.LONGPOLL, data);
            }
        };
        registerReceiver(lpReceiver, new IntentFilter(
                "uk.openvk.android.legacy.LONGPOLL_RECEIVE"));
    }

    private void setActionBar(String layout_name) {
        try {
            ab_layout.setOnHomeButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((OvkApplication) getApplicationContext()).isTablet) {
                        menu.toggle(true);
                    } else {
                        if (slidingmenuLayout.getVisibility() == View.VISIBLE) {
                            slidingmenuLayout.setVisibility(View.GONE);
                        } else {
                            slidingmenuLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            if(layout_name.equals("custom_newsfeed")) {
                ab_layout.selectItem(0);
                ab_layout.setMode("spinner");
                int actionbar_height = 0;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    try {
                        getActionBar().setCustomView(ab_layout);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            getActionBar().setHomeButtonEnabled(true);
                        }
                        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    actionBar = findViewById(R.id.actionbar);
                }
            } else {
                ab_layout.setMode("title");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    try {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            getActionBar().setHomeButtonEnabled(true);
                        }
                        ab_layout.setNotificationCount(account.counters);
                        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    actionBar = findViewById(R.id.actionbar);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        } else if(item.getItemId() == R.id.copy_link) {
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(String.format("http://%s/id%s", instance_prefs.getString("server", ""), user.id));
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("OpenVK User URL", String.format("http://%s/id%s", instance_prefs.getString("server", ""), user.id));
                clipboard.setPrimaryClip(clip);
            }
        } else if(item.getItemId() == R.id.open_in_browser) {
            String user_url = String.format("http://%s/id%s", instance_prefs.getString("server", ""), user.id);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(user_url));
            startActivity(i);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        try {
            inflater.inflate(menu_id, menu);
            if (account == null || account.id == 0) {
                menu.findItem(R.id.newpost).setVisible(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        activity_menu = menu;
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newsfeedFragment.adjustLayoutSize(newConfig.orientation);
        ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).adjustLayoutSize(newConfig.orientation);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
        }
        ab_layout.adjustLayout();
        super.onConfigurationChanged(newConfig);
    }

    private void createSlidingMenu() {
        slidingmenuLayout = new SlidingMenuLayout(this);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            while(slidingmenuLayout == null) {
                slidingmenuLayout = new SlidingMenuLayout(this);
            }
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
                while(slidingmenuLayout == null) {
                    slidingmenuLayout = new SlidingMenuLayout(this);
                }
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

    @SuppressLint("CommitTransaction")
    private void installFragments() {
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        profileFragment = new ProfileFragment();
        newsfeedFragment = new NewsfeedFragment();
        friendsFragment = new FriendsFragment();
        groupsFragment = new GroupsFragment();
        mainSettingsFragment = new MainSettingsFragment();
        friendsFragment.setActivityContext(this);

        if(activity_menu == null) {
            android.support.v7.widget.PopupMenu p  = new android.support.v7.widget.PopupMenu(this, null);
            activity_menu = p.getMenu();
            getMenuInflater().inflate(R.menu.newsfeed, activity_menu);
            onCreateOptionsMenu(activity_menu);
        }
        conversationsFragment = new ConversationsFragment();
        FragmentManager fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.app_fragment, newsfeedFragment, "newsfeed");
        ft.add(R.id.app_fragment, friendsFragment, "friends");
        ft.add(R.id.app_fragment, groupsFragment, "groups");
        ft.add(R.id.app_fragment, conversationsFragment, "messages");
        ft.add(R.id.app_fragment, profileFragment, "profile");
        ft.add(R.id.app_fragment, mainSettingsFragment, "settings");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(friendsFragment);
        ft.hide(groupsFragment);
        ft.hide(conversationsFragment);
        ft.hide(profileFragment);
        selectedFragment = newsfeedFragment;
        ft.show(newsfeedFragment);
        ft.hide(mainSettingsFragment);
        ft.commit();
        if(global_prefs.getBoolean("refreshOnOpen", true)) {
            global_prefs_editor.putString("current_screen", "newsfeed");
            global_prefs_editor.commit();
        } else {
            try {
                if (global_prefs.getString("current_screen", "newsfeed").equals("profile")) {
                    openAccountProfile();
                } else if (global_prefs.getString("current_screen", "newsfeed").equals("friends")) {
                    onSlidingMenuItemClicked(0);
                }
                if (global_prefs.getString("current_screen", "newsfeed").equals("messages")) {
                    onSlidingMenuItemClicked(1);
                }
                if (global_prefs.getString("current_screen", "newsfeed").equals("groups")) {
                    onSlidingMenuItemClicked(2);
                }
            } catch (Exception ignored) {

            }
        }
        selectedFragment = newsfeedFragment;
        progressLayout.setVisibility(View.VISIBLE);
        ab_layout = new ActionBarLayout(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            actionBar = findViewById(R.id.actionbar);
            actionBar.setCustomView(ab_layout);
            ab_layout.createSpinnerAdapter(this);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
        }
        setActionBar("custom_newsfeed");
        setActionBarTitle(getResources().getString(R.string.newsfeed));
        //MenuItem newpost = activity_menu.findItem(R.id.newpost);
        //newpost.setVisible(false);
    }

    private void createActionPopupMenu(final Menu menu, boolean enable) {
        if(enable) {
            final View menu_container = getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
            final PopupWindow popupMenu = new PopupWindow(menu_container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            popupMenu.setOutsideTouchable(true);
            popupMenu.setFocusable(true);
            final ListView menu_list = popupMenu.getContentView().findViewById(R.id.popup_menulist);
            actionBar.addAction(new dev.tinelix.retro_ab.ActionBar.PopupMenuAction(this, "", R.drawable.ic_overflow_holo_dark, new PopupMenu.OnItemSelectedListener() {
                @Override
                public void onItemSelected(dev.tinelix.retro_pm.MenuItem item) {
                    onMenuItemSelected(0, menu.getItem(item.getItemId()));
                }
            }));
            menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupMenu.dismiss();
                }
            });
            (popupMenu.getContentView().findViewById(R.id.overlay_layout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.dismiss();
                }
            });
        } else {

        }
    }

    public void setActionBarTitle(String title) {
        try {
            ab_layout.setAppTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openNewPostActivity() {
        try {
            Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                intent.putExtra("owner_id", user.id);
            } else {
                intent.putExtra("owner_id", account.id);
            }
            intent.putExtra("account_id", account.id);
            intent.putExtra("account_first_name", account.user.first_name);
            startActivity(intent);
        } catch (Exception ex) {

        }
    }

    public void refreshPage(String screen) {
        if(screen.equals("subscriptions_newsfeed")) {
            menu_id = R.menu.newsfeed;
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if (newsfeedFragment.getCount() == 0) {
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
            }
            if (newsfeed == null) {
                newsfeed = new Newsfeed();
            }
            newsfeed_count = 25;
            newsfeed.get(ovk_api, newsfeed_count);
        } else if(screen.equals("global_newsfeed")) {
            menu_id = R.menu.newsfeed;
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if (newsfeedFragment.getCount() == 0) {
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
            }
            if (newsfeed == null) {
                newsfeed = new Newsfeed();
            }
            newsfeed_count = 25;
            newsfeed.getGlobal(ovk_api, newsfeed_count);
        }
    }

    @SuppressLint("CommitTransaction")
    public void onSlidingMenuItemClicked(int position) {
        global_prefs_editor = global_prefs.edit();
        try {
            if (position < 4) {
                if (!((OvkApplication) getApplicationContext()).isTablet) {
                    menu.toggle(true);
                }
                if (activity_menu != null) {
                    activity_menu.clear();
                }
            }
        } catch (Exception ignored) {

        }
        ft = getSupportFragmentManager().beginTransaction();
        if(position == 0) {
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.friends));
            if(friendsFragment.getCount() == 0) {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("friends"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("friends"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            ft.commit();
            selectedFragment = friendsFragment;
            global_prefs_editor.putString("current_screen", "friends");
            global_prefs_editor.commit();
            if(friends == null) {
                friends = new Friends();
            }
            friends.get(ovk_api, account.id, 25, "friends_list");
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                actionBar = findViewById(R.id.actionbar);
                actionBar.removeAllActions();
                createActionPopupMenu(activity_menu, false);
            }
        } else if(position == 1) {
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.messages));
            if(conversationsFragment.getCount() == 0) {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("messages"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("messages"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            ft.commit();
            selectedFragment = conversationsFragment;
            global_prefs_editor.putString("current_screen", "messages");
            global_prefs_editor.commit();
            if(messages == null) {
                messages = new Messages();
            }
            messages.getConversations(ovk_api);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                actionBar = findViewById(R.id.actionbar);
                actionBar.removeAllActions();
                createActionPopupMenu(activity_menu, false);
            }
        } else if(position == 2) {
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.groups));
            if(groupsFragment.getCount() == 0) {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("groups"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("groups"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            ft.commit();
            selectedFragment = groupsFragment;
            global_prefs_editor.putString("current_screen", "groups");
            global_prefs_editor.commit();
            if(groups == null) {
                groups = new Groups();
            }
            groups.getGroups(ovk_api, account.id, 25);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                actionBar = findViewById(R.id.actionbar);
                actionBar.removeAllActions();
                createActionPopupMenu(activity_menu, false);
            }
        } else if(position == 3) {
            setActionBar("custom_newsfeed");
            menu_id = R.menu.newsfeed;
            onCreateOptionsMenu(activity_menu);
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if(newsfeedFragment.getCount() == 0) {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("newsfeed"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                ft.hide(selectedFragment);
                ft.show(getSupportFragmentManager().findFragmentByTag("newsfeed"));
                errorLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            ft.commit();
            selectedFragment = newsfeedFragment;
            global_prefs_editor.putString("current_screen", "newsfeed");
            global_prefs_editor.commit();
            if(newsfeed == null) {
                newsfeed = new Newsfeed();
            }
            newsfeed_count = 25;
            newsfeed.get(ovk_api, newsfeed_count);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                actionBar = findViewById(R.id.actionbar);
                actionBar.addAction(new dev.tinelix.retro_ab.ActionBar.Action() {
                    @Override
                    public int getDrawable() {
                        return R.drawable.ic_ab_write;
                    }

                    @Override
                    public void performAction(View view) {
                        openNewPostActivity();
                    }
                });
                createActionPopupMenu(activity_menu, false);
            }
        } else if(position == 4) {
            Context context = getApplicationContext();
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.menu_settings));
            ft.hide(selectedFragment);
            ft.show(getSupportFragmentManager().findFragmentByTag("settings"));
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
            ft.commit();
            selectedFragment = mainSettingsFragment;
            global_prefs_editor.putString("current_screen", "settings");
            global_prefs_editor.commit();
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
                    dev.tinelix.retro_ab.ActionBar actionBar = findViewById(R.id.actionbar);
                    dev.tinelix.retro_ab.ActionBar.Action newpost = new dev.tinelix.retro_ab.ActionBar.Action() {
                        @Override
                        public int getDrawable() {
                            return R.drawable.ic_ab_write;
                        }

                        @Override
                        public void performAction(View view) {
                            openNewPostActivity();
                        }
                    };
                    actionBar.addAction(newpost);
                } else {
                    if(activity_menu == null) {
                        onPrepareOptionsMenu(activity_menu);
                    }
                    try {
                        MenuItem newpost = activity_menu.findItem(R.id.newpost);
                        newpost.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
                try {
                    ab_layout.setNotificationCount(account.counters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (message == HandlerMessages.NEWSFEED_GET) {
                ((SwipeRefreshLayout) newsfeedFragment.getView().findViewById(R.id.refreshable_layout)).setRefreshing(false);
                if(((Spinner) ab_layout.findViewById(R.id.spinner)).getSelectedItemPosition() == 0) {
                    downloadManager.setProxyConnection(global_prefs.getBoolean("useProxy", false), global_prefs.getString("proxy_address", ""));
                    newsfeed.parse(this, downloadManager, data.getString("response"), global_prefs.getString("photos_quality", ""), true);
                    newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                    if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                        if(newsfeed.getWallPosts().size() > 0) {
                            progressLayout.setVisibility(View.GONE);
                            findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                        } else {
                            progressLayout.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            errorLayout.setTitle(getResources().getString(R.string.local_newsfeed_no_posts));
                            errorLayout.setIcon("ovk");
                            errorLayout.setReason(0);
                            errorLayout.hideRetryButton();
                        }
                    }
                    newsfeedFragment.loading_more_posts = true;
                    newsfeedFragment.setScrollingPositions(this, false, true);
                    ((RecyclerView) newsfeedFragment.getView().findViewById(R.id.news_listview)).scrollToPosition(0);
                }
            } else if (message == HandlerMessages.NEWSFEED_GET_GLOBAL) {
                ((SwipeRefreshLayout) newsfeedFragment.getView().findViewById(R.id.refreshable_layout)).setRefreshing(false);
                if(((Spinner) ab_layout.findViewById(R.id.spinner)).getSelectedItemPosition() == 1) {
                    downloadManager.setProxyConnection(global_prefs.getBoolean("useProxy", false), global_prefs.getString("proxy_address", ""));
                    newsfeed.parse(this, downloadManager, data.getString("response"), global_prefs.getString("photos_quality", ""), true);
                    newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                    if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                        progressLayout.setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    }
                    newsfeedFragment.loading_more_posts = true;
                    newsfeedFragment.setScrollingPositions(this, false, true);
                    ((RecyclerView) newsfeedFragment.getView().findViewById(R.id.news_listview)).scrollToPosition(0);
                }
            } else if (message == HandlerMessages.NEWSFEED_GET_MORE) {
                newsfeed.parse(this, downloadManager, data.getString("response"), global_prefs.getString("photos_quality", ""), false);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                newsfeedFragment.loading_more_posts = true;
                newsfeedFragment.setScrollingPositions(this, false, true);
            } else if (message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL) {
                newsfeed.parse(this, downloadManager, data.getString("response"),  global_prefs.getString("photos_quality", ""), false);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                newsfeedFragment.loading_more_posts = true;
                newsfeedFragment.setScrollingPositions(this, false, true);
            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                LongPollServer longPollServer = messages.parseLongPollServer(data.getString("response"));
                ((OvkApplication) getApplicationContext()).longPollService = new LongPollService(this, instance_prefs.getString("access_token", ""), global_prefs.getBoolean("use_https", true));
                ((OvkApplication) getApplicationContext()).longPollService.setProxyConnection(global_prefs.getBoolean("useProxy", false), global_prefs.getString("proxy_address", ""));
                ((OvkApplication) getApplicationContext()).longPollService.run(instance_prefs.getString("server", ""), longPollServer.address, longPollServer.key, longPollServer.ts, global_prefs.getBoolean("useHTTPS", true));
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                slidingmenuLayout.loadAccountAvatar(account, global_prefs.getString("photos_quality", ""));
            } else if (message == HandlerMessages.NEWSFEED_ATTACHMENTS) {
                newsfeedFragment.setScrollingPositions(this, true, true);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS) {
                newsfeedFragment.loadAvatars();
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).loadAvatars();
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsFragment.loadAvatars();
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                groupsFragment.loadAvatars();
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileFragment.updateLayout(user, getWindowManager());
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    profileFragment.setDMButtonListener(this, user.id, getWindowManager());
                    profileFragment.setAddToFriendsButtonListener(this, user.id, user);
                    if(user.id == account.id) {
                        profileFragment.hideHeaderButtons(this, getWindowManager());
                    }
                    user.downloadAvatar(downloadManager, global_prefs.getString("photos_quality", ""));
                    wall.get(ovk_api, user.id, 50);
                    friends.get(ovk_api, user.id, 25, "profile_counter");
                }
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                users.parse(data.getString("response"));
                account.user = users.getList().get(0);
                account.user.downloadAvatar(downloadManager, global_prefs.getString("photos_quality", ""), "account_avatar");
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, global_prefs.getString("photos_quality", ""), data.getString("response"));
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.showNewPostIcon();
            } else if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"), downloadManager, true, true);
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("friends")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                friendsFragment.createAdapter(this, friendsList, "friends");
                friends.getRequests(ovk_api);
                ((TabSelector) friendsFragment.getView().findViewById(R.id.selector)).setTabTitle(0, String.format("%s (%s)", getResources().getString(R.string.friends), friends.count));
                friendsFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                int old_friends_size = friends.getFriends().size();
                friends.parse(data.getString("response"), downloadManager, true, false);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createAdapter(this, friendsList, "friends");
                if(old_friends_size == friends.getFriends().size()) {
                    friendsFragment.setScrollingPositions(this, false);
                } else {
                    friendsFragment.setScrollingPositions(this, true);
                }
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                if(global_prefs.getString("current_screen", "").equals("friends")) {
                    friends.requests.remove(friendsFragment.requests_cursor_index);
                } else {
                    JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                    int status = response.getInt("response");
                    if (status == 1) {
                        user.friends_status = status;
                    } else if (status == 2) {
                        user.friends_status = 3;
                    }
                    profileFragment.setAddToFriendsButtonListener(this, user.id, user);
                }
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                profileFragment.setAddToFriendsButtonListener(this, user.id, user);
            } else if (message == HandlerMessages.FRIENDS_REQUESTS) {
                friends.parseRequests(data.getString("response"), downloadManager, true);
                ArrayList<Friend> requestsList = friends.requests;
                if (global_prefs.getString("current_screen", "").equals("friends")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                ((TabSelector) friendsFragment.getView().findViewById(R.id.selector)).setTabTitle(1, String.format("%s (%s)", getResources().getString(R.string.friend_requests), account.counters.friends_requests));
                friendsFragment.createAdapter(this, requestsList, "requests");
            } else if (message == HandlerMessages.GROUPS_GET) {
                groups.parse(data.getString("response"), downloadManager, global_prefs.getString("photos_quality", ""), true, true);
                ArrayList<Group> groupsList = groups.getList();
                if (global_prefs.getString("current_screen", "").equals("groups")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                groupsFragment.createAdapter(this, groupsList);
                groupsFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.GROUPS_GET_MORE) {
                int old_friends_size = groups.getList().size();
                groups.parse(data.getString("response"), downloadManager, global_prefs.getString("photos_quality", ""), true, false);
                ArrayList<Group> groupsList = groups.getList();
                if (global_prefs.getString("current_screen", "").equals("groups")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                groupsFragment.createAdapter(this, groupsList);
                if(old_friends_size == groups.getList().size()) {
                    groupsFragment.setScrollingPositions(this, false);
                } else {
                    groupsFragment.setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager, false, true);
                ArrayList<Friend> friendsList = friends.getFriends();
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    profileFragment.setCounter(user, "friends",  friends.count);
                }
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                conversations = messages.parseConversationsList(data.getString("response"), downloadManager);
                conversationsFragment.createAdapter(this, conversations, account);
                if (global_prefs.getString("current_screen", "").equals("messages")) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
            } else if(message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedFragment.select(likes.position, "likes", 1);
                } else if (global_prefs.getString("current_screen", "").equals("profile")) {
                    ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedFragment.select(likes.position, "likes", 0);
                } else if (global_prefs.getString("current_screen", "").equals("profile")) {
                    ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
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
                if(global_prefs.getString("photos_quality", "").equals("medium")) {
                    if (user.avatar_msize_url.length() > 0) {
                        profileFragment.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                } else if(global_prefs.getString("photos_quality", "").equals("high")) {
                    if (user.avatar_hsize_url.length() > 0) {
                        profileFragment.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                } else {
                    if (user.avatar_osize_url.length() > 0) {
                        profileFragment.loadAvatar(user, global_prefs.getString("photos_quality", ""));
                    }
                }
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                conversationsFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.LONGPOLL) {
                notifMan.buildDirectMsgNotification(this, conversations, data, global_prefs.getBoolean("enableNotification", true),
                        notifMan.isRepeat(last_longpoll_response, data.getString("response")));
                last_longpoll_response = data.getString("response");
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
                            newsfeedFragment.updateItem(item, item_pos);
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
                            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).updateItem(item, item_pos);
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
                            newsfeedFragment.updateItem(item, item_pos);
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
                            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                        }
                    }
                }
            } else if(message == HandlerMessages.WALL_REPOST) {
                Toast.makeText(this, getResources().getString(R.string.repost_ok_wall), Toast.LENGTH_LONG).show();
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION || message == HandlerMessages.INVALID_JSON_RESPONSE || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR || message == HandlerMessages.INSTANCE_UNAVAILABLE || message == HandlerMessages.BROKEN_SSL_CONNECTION || message == HandlerMessages.UNKNOWN_ERROR) {
                if(data.containsKey("method")) {
                    try {
                        if (data.getString("method").equals("Account.getProfileInfo") ||
                                (data.getString("method").equals("Newsfeed.get") && newsfeed.getWallPosts().size() == 0) ||
                                (data.getString("method").equals("Messages.getConversations") && conversations.size() == 0) ||
                                (data.getString("method").equals("Friends.get") && friends.getFriends().size() == 0) ||
                                (data.getString("method").equals("Users.get") && global_prefs.getString("current_screen", "").equals("profile")) ||
                                (data.getString("method").equals("Groups.get") && (groups.getList() == null || groups.getList().size() == 0))) {
                            slidingmenuLayout.setProfileName(getResources().getString(R.string.error));
                            errorLayout.setTitle(getResources().getString(R.string.err_text));
                            errorLayout.setIcon("error");
                            errorLayout.setReason(message);
                            errorLayout.setData(data);
                            errorLayout.setRetryAction(this);
                            progressLayout.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                        } else {
                            if(data.getString("method").equals("Wall.get") && global_prefs.getString("current_screen", "").equals("profile")) {
                                ((WallErrorLayout) profileFragment.getView().findViewById(R.id.wall_error_layout)).setVisibility(View.VISIBLE);
                            } else {
                                if(!inBackground) {
                                    Toast.makeText(this, getResources().getString(R.string.err_text), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        errorLayout.setTitle(getResources().getString(R.string.err_text));
                        errorLayout.setIcon("error");
                        errorLayout.setReason(message);
                        errorLayout.setData(data);
                        errorLayout.setRetryAction(this);
                        progressLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(account.first_name == null && account.last_name == null) {
                        slidingmenuLayout.setProfileName(getResources().getString(R.string.error));
                    }
                    errorLayout.setTitle(getResources().getString(R.string.err_text));
                    errorLayout.setIcon("error");
                    errorLayout.setReason(message);
                    errorLayout.setData(data);
                    errorLayout.hideRetryButton();
                    progressLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
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

    @SuppressLint("CommitTransaction")
    public void openAccountProfile() {
        try {
            if (!((OvkApplication) getApplicationContext()).isTablet) {
                if(menu == null) {
                    menu = new SlidingMenu(this);
                }
                menu.toggle(true);
            }

            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            ft = getSupportFragmentManager().beginTransaction();
            if(selectedFragment != null) {
                ft.hide(selectedFragment);
            } else {
                ft.hide(newsfeedFragment);
            }
            ft.show(profileFragment);
            ft.commit();
            selectedFragment = profileFragment;
            errorLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            global_prefs_editor.putString("current_screen", "profile");
            global_prefs_editor.commit();

            if(users == null) {
                users = new Users();
            }
            users.getUser(ovk_api, account.id);
            menu_id = R.menu.profile;
            if(activity_menu != null) {
                activity_menu.clear();
            }
            try {
                activity_menu.findItem(R.id.remove_friend).setVisible(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.profile));
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                actionBar.removeAllActions();
                createActionPopupMenu(activity_menu, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openFriendsList() {
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(selectedFragment);
        ft.show(getSupportFragmentManager().findFragmentByTag("friends"));
        ft.commit();
        selectedFragment = friendsFragment;
        progressLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        global_prefs_editor.putString("current_screen", "friends");
        global_prefs_editor.commit();
        friends.get(ovk_api, account.id, 25, "friends_list");
    }

    public void retryConnection(String method, String args) {
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
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
                    friends.get(ovk_api, account.id, 25, "friends_list");
                } else if (method.equals("Groups.get")) {
                    if (groups == null) {
                        groups = new Groups();
                    }
                    groups.getGroups(ovk_api, 25, account.id);
                } else if (method.equals("Users.get")) {
                    if (users == null) {
                        users = new Users();
                    }
                    users.getUser(ovk_api, account.id);
                }
            }
        }
    }

    public void showGroup(int position) {
        String url = "openvk://group/" + "club" + groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        final PackageManager pm = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activityList = pm.queryIntentActivities(i, 0);
        for (int index = 0; index < activityList.size(); index++) {
            ResolveInfo app = activityList.get(index);
            if (app.activityInfo.name.contains("uk.openvk.android.legacy")) {
                i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
            }
        }
        startActivity(i);
    }

    public void showProfile(int user_id) {
        String url = "openvk://profile/" + "id" + user_id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        final PackageManager pm = getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activityList = pm.queryIntentActivities(i, 0);
        for (int index = 0; index < activityList.size(); index++) {
            ResolveInfo app = activityList.get(index);
            if (app.activityInfo.name.contains("uk.openvk.android.legacy")) {
                i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
            }
        }
        startActivity(i);
    }

    public void hideSelectedItemBackground(int position) {
        ((ListView) friendsFragment.getView().findViewById(R.id.friends_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
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
            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).select(position, "likes", "add");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedFragment.select(position, "likes", "add");
        }
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).select(0, "likes", "delete");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedFragment.select(0, "likes", "delete");
        }
        likes.delete(ovk_api, item.owner_id, item.post_id, position);
    }

    public void getConversationById(long peer_id) {
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
        if(account != null) {
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
                boolean contains_photo = false;
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
                        } else if(item.attachments.get(i).type.equals("photo")) {
                            contains_photo = true;
                            PhotoAttachment photo = ((PhotoAttachment) item.attachments.get(i).getContent());
                            intent.putExtra("photo_id", photo.id);
                        }
                    }
                }
                intent.putExtra("contains_poll", contains_poll);
                intent.putExtra("contains_photo", contains_photo);
                if (item.repost != null) {
                    intent.putExtra("is_repost", true);
                    intent.putExtra("repost_id", item.repost.newsfeed_item.post_id);
                    intent.putExtra("repost_owner_id", item.repost.newsfeed_item.owner_id);
                    intent.putExtra("repost_author_id", item.repost.newsfeed_item.author_id);
                    intent.putExtra("repost_author_name", item.repost.newsfeed_item.name);
                    intent.putExtra("repost_info", item.repost.newsfeed_item.info);
                    intent.putExtra("repost_text", item.repost.newsfeed_item.text);
                } else {
                    intent.putExtra("is_repost", false);
                }
                startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
        if(newsfeed != null) {
            newsfeed.get(ovk_api, 25, newsfeed.next_from);
        }
    }

    public void voteInPoll(int item_pos, int answer) {
        this.item_pos = item_pos;
        this.poll_answer = answer;
        WallPost item;
        if(global_prefs.getString("current_screen", "").equals("newsfeed")) {
            item = newsfeed.getWallPosts().get(item_pos);
        } else {
            item = wall.getWallItems().get(item_pos);
        }
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if (item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                pollAttachment.user_votes = 1;
                if (!pollAttachment.answers.get(answer).is_voted) {
                    pollAttachment.answers.get(answer).is_voted = true;
                }
                if(global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeed.getWallPosts().set(item_pos, item);
                } else {
                    wall.getWallItems().set(item_pos, item);
                }
                pollAttachment.vote(ovk_api, pollAttachment.answers.get(answer).id);
            }
        }
    }

    public void removeVoteInPoll(int item_pos) {
        this.item_pos = item_pos;
        WallPost item;
        if(global_prefs.getString("current_screen", "").equals("newsfeed")) {
            item = newsfeed.getWallPosts().get(item_pos);
        } else {
            item = wall.getWallItems().get(item_pos);
        }
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if(item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                pollAttachment.user_votes = 0;
                for (int i = 0; i < pollAttachment.answers.size(); i++) {
                    if (pollAttachment.answers.get(i).is_voted) {
                        pollAttachment.answers.get(i).is_voted = false;
                    }
                }
                if(global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeed.getWallPosts().set(item_pos, item);
                } else {
                    wall.getWallItems().set(item_pos, item);
                }
                pollAttachment.unvote(ovk_api);
            }
        }
    }

    public void addToFriends(long user_id) {
        if(user_id != account.id) {
            friends.add(ovk_api, user_id);
        }
    }
    public void deleteFromFriends(long user_id) {
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
            boolean contains_poll = false;
            boolean contains_photo = false;
            boolean is_repost = false;
            if (item.repost.newsfeed_item.attachments.size() > 0) {
                for (int i = 0; i < item.repost.newsfeed_item.attachments.size(); i++) {
                    if (item.repost.newsfeed_item.attachments.get(i).type.equals("poll")) {
                        contains_poll = true;
                        PollAttachment poll = ((PollAttachment) item.repost.newsfeed_item.attachments.get(i).getContent());
                        intent.putExtra("poll_question", poll.question);
                        intent.putExtra("poll_anonymous", poll.anonymous);
                        //intent.putExtra("poll_answers", poll.answers);
                        intent.putExtra("poll_total_votes", poll.votes);
                        intent.putExtra("poll_user_votes", poll.user_votes);
                    } else if(item.repost.newsfeed_item.attachments.get(i).type.equals("photo")) {
                        contains_photo = true;
                        PhotoAttachment photo = ((PhotoAttachment) item.repost.newsfeed_item.attachments.get(i).getContent());
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

    public void viewPhotoAttachment(int position) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        if (global_prefs.getString("current_screen", "").equals("profile")) {
            item = wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
        } else {
            item = newsfeed.getWallPosts().get(position);
            intent.putExtra("where", "newsfeed");
        }
        try {
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                intent.putExtra("local_photo_addr", String.format("%s/wall_photo_attachments/wall_attachment_o%sp%s", getCacheDir(),
                        item.owner_id, item.post_id));
            } else {
                intent.putExtra("local_photo_addr", String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s", getCacheDir(),
                        item.owner_id, item.post_id));
            }
            if(item.attachments != null) {
                for(int i = 0; i < item.attachments.size(); i++) {
                    if(item.attachments.get(i).type.equals("photo")) {
                        PhotoAttachment photo = ((PhotoAttachment) item.attachments.get(i).getContent());
                        intent.putExtra("original_link", photo.original_url);
                        intent.putExtra("author_id", item.author_id);
                        intent.putExtra("photo_id", photo.id);
                    }
                }
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void selectNewsSpinnerItem(int position) {
        Spinner spinner = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            spinner = (Spinner) (getActionBar().getCustomView().findViewById(R.id.spinner));
        } else {
            spinner = ab_layout.findViewById(R.id.spinner);
        }
        if (spinner != null) {
            try {
                spinner.setSelection(position);
                Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
                method.setAccessible(true);
                method.invoke(spinner);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(position == 0) {
                newsfeed.get(ovk_api, 50);
            } else {
                newsfeed.getGlobal(ovk_api, 50);
            }
            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            ((RecyclerView) newsfeedFragment.getView().findViewById(R.id.news_listview)).scrollToPosition(0);
            progressLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        inBackground = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        inBackground = false;
        super.onResume();
    }

    public void loadMoreFriends() {
        if(friends != null) {
            friends.get(ovk_api, account.id, 25, friends.offset);
        }
    }

    public void loadMoreGroups() {
        if(groups != null) {
            groups.getGroups(ovk_api, account.id, 25, groups.getList().size());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(lpReceiver);
        super.onDestroy();
    }

    public void repost(int position) {
        if(global_prefs.getString("current_screen", "").equals("newsfeed")) {
            final WallPost post = newsfeed.getWallPosts().get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final ArrayList<String> functions = new ArrayList<>();
            builder.setTitle(R.string.repost_dlg_title);
            functions.add(getResources().getString(R.string.repost_own_wall));
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(functions.get(position).equals(getResources().getString(R.string.repost_own_wall))) {
                        openRepostDialog("own_wall", post);
                    }
                }
            });
        } else if(global_prefs.getString("current_screen", "").equals("profile")) {
            final WallPost post = wall.getWallItems().get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final ArrayList<String> functions = new ArrayList<>();
            builder.setTitle(R.string.repost_dlg_title);
            functions.add(getResources().getString(R.string.repost_own_wall));
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(functions.get(position).equals(getResources().getString(R.string.repost_own_wall))) {
                        openRepostDialog("own_wall", post);
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public void openRepostDialog(String where, final WallPost post) {
        if(where.equals("own_wall")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View repost_view = getLayoutInflater().inflate(R.layout.dialog_repost_msg, null, false);
            final EditText text_edit = ((EditText) repost_view.findViewById(R.id.text_edit));
            builder.setView(repost_view);
            builder.setPositiveButton(R.string.ok, null);
            builder.setNegativeButton(R.string.cancel, null);
            final OvkAlertDialog dialog = new OvkAlertDialog(this);
            dialog.build(builder, getResources().getString(R.string.repost_dlg_title), "", repost_view);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Button ok_btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if(ok_btn != null) {
                        ok_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    String msg_text = ((EditText)repost_view.findViewById(R.id.text_edit)).getText().toString();
                                    wall.repost(ovk_api, post.owner_id, post.post_id, msg_text);
                                    dialog.close();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
            dialog.show();
        }
    }
}
