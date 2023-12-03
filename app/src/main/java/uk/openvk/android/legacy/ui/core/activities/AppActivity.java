package uk.openvk.android.legacy.ui.core.activities;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Poll;
import uk.openvk.android.legacy.api.counters.*;
import uk.openvk.android.legacy.api.entities.*;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.*;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.receivers.LongPollReceiver;
import uk.openvk.android.legacy.services.LongPollService;
import uk.openvk.android.legacy.ui.FragmentNavigator;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.*;
import uk.openvk.android.legacy.ui.core.listeners.AccountsUpdateListener;
import uk.openvk.android.legacy.ui.list.adapters.AccountSlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.items.*;
import uk.openvk.android.legacy.ui.view.layouts.*;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/*  Copyleft © 2022, 2023 OpenVK Team
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

@SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
public class AppActivity extends NetworkFragmentActivity {
    private ArrayList<SlidingMenuItem> slidingMenuArray;
    private ArrayList<SlidingMenuItem> accountSlidingMenuArray;
    private SlidingMenu menu;
    public ProgressLayout progressLayout;
    public ErrorLayout errorLayout;
    public NewsfeedFragment newsfeedFragment;
    public ProfileFragment profileFragment;
    public FriendsFragment friendsFragment;
    public PhotosFragment photosFragment;
    public VideosFragment videosFragment;
    public ConversationsFragment conversationsFragment;
    public MainSettingsFragment mainSettingsFragment;
    private SlidingMenuLayout slidingmenuLayout;
    public NotesFragment notesFragment;
    public ArrayList<Conversation> conversations;
    public Menu activity_menu;
    public GroupsFragment groupsFragment;
    private int newsfeed_count = 25;
    private String last_longpoll_response;
    private int item_pos;
    private int poll_answer;
    private uk.openvk.android.legacy.api.wrappers.NotificationManager notifMan;
    private boolean inBackground;
    public ActionBarLayout ab_layout;
    public int menu_id;
    public dev.tinelix.retro_ab.ActionBar actionBar;
    private LongPollReceiver lpReceiver;
    private FragmentTransaction ft;
    public Fragment selectedFragment;
    private FragmentNavigator fn;
    public android.support.v7.widget.PopupMenu popup_menu;
    public LongPollServer longPollServer;
    public int old_friends_size;
    public boolean profile_loaded = false;
    public android.accounts.Account androidAccount;

    @SuppressLint({"CommitPrefEdits", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getAndroidAccounts()) {
            setContentView(R.layout.activity_app);
        } else {
            return;
        }
        inBackground = true;
        menu_id = R.menu.newsfeed;
        last_longpoll_response = "";

        installFragments();
        Global.fixWindowPadding(findViewById(R.id.app_fragment), getTheme());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Global.fixWindowPadding(getWindow(), getTheme());
        }
        conversations = new ArrayList<>();
        registerBroadcastReceiver();
        if(((OvkApplication) getApplicationContext()).isTablet) {
            newsfeedFragment.adjustLayoutSize(getResources().getConfiguration().orientation);
            try {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .adjustLayoutSize(getResources().getConfiguration().orientation);
            } catch (Exception ignored) {
            }
        }
        boolean isTablet = ((OvkApplication) getApplicationContext()).isTablet;
        createSlidingMenu(isTablet);
        // Creating notification manager
        ((OvkApplication) getApplicationContext()).notifMan =
                new uk.openvk.android.legacy.api.wrappers.NotificationManager(AppActivity.this,
                        global_prefs.getBoolean("notifyLED", true), global_prefs
                        .getBoolean("notifyVibrate", true), global_prefs.getBoolean("notifySound", true),
                        global_prefs.getString("notifyRingtone", ""));
        notifMan = ((OvkApplication) getApplicationContext()).notifMan;
        if(activity_menu == null) {
            popup_menu  = new android.support.v7.widget.PopupMenu(this, null);
            activity_menu = popup_menu.getMenu();
            getMenuInflater().inflate(R.menu.newsfeed, activity_menu);
            onCreateOptionsMenu(activity_menu);
        }
    }

    public boolean getAndroidAccounts() {
        ArrayList<InstanceAccount> accountArray = new ArrayList<>();
        AccountManager accountManager = AccountManager.get(this);
        accountManager.addOnAccountsUpdatedListener(new AccountsUpdateListener(this),
                null, false);
        Global.loadAccounts(this, accountArray, accountManager, instance_prefs);
        if(androidAccount == null) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.invalid_session), Toast.LENGTH_LONG).show();
            removeAccount();
            if(accountArray.size() >= 1
                    && global_prefs.getString("current_instance", "").length() == 0) {
                Global global = new Global();
                global.openChangeAccountDialog(this, global_prefs, false);
                return false;
            } else {
                Intent activity = new Intent(getApplicationContext(), MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(activity);
                finish();
                return false;
            }
        } else {
            return true;
        }
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void onBackPressed() {
        try {
            if (selectedFragment instanceof NewsfeedFragment) {
                super.onBackPressed();
                if (lpReceiver != null) {
                    unregisterReceiver(lpReceiver);
                }
                exitApplication();
            } else {
                fn.navigateTo("newsfeed", getSupportFragmentManager().beginTransaction());
            }
        } catch (Exception ex) {
            exitApplication();
        }
    }

    private void exitApplication() {
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
        // Register LongPoll Broadcast Receiver
        registerReceiver(lpReceiver, new IntentFilter(
                "uk.openvk.android.legacy.LONGPOLL_RECEIVE"));
    }

    public void setActionBar(String layout_name) {
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
                        ab_layout.setNotificationCount(ovk_api.account.counters);
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
            Global.openNewPostActivity(this, ovk_api);
        } else if(item.getItemId() == R.id.copy_link) {
            Global.copyToClipboard(
                    this,
                    String.format("http://%s/id%s",
                    instance_prefs.getString("server", ""),
                    ovk_api.user.id)
            );
        } else if(item.getItemId() == R.id.open_in_browser) {
            String user_url = String.format("http://%s/id%s",
                    instance_prefs.getString("server", ""),
                    ovk_api.user.id);
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
            if (ovk_api.account == null || ovk_api.account.id == 0) {
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
        ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                .adjustLayoutSize(newConfig.orientation);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            menu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
        }
        ab_layout.adjustLayout();
        Global.fixWindowPadding(findViewById(R.id.app_fragment), getTheme());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Global.fixWindowPadding(getWindow(), getTheme());
        }
        ((OvkApplication) getApplicationContext()).config = newConfig;
        super.onConfigurationChanged(newConfig);
    }

    private void createSlidingMenu(boolean isTablet) {
        slidingmenuLayout = new SlidingMenuLayout(this);
        if(isTablet) {
            while(slidingmenuLayout == null) {
                slidingmenuLayout = new SlidingMenuLayout(this);
            }
            menu = new SlidingMenu(this);
            Global.setSlidingMenu(this, slidingmenuLayout, menu);
            if (Global.isXmas()) {
                ((ImageView) slidingmenuLayout.findViewById(R.id.menu_background)).setImageDrawable(
                        getResources().getDrawable(R.drawable.xmas_left_menu)
                );
            }
            menu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
                @Override
                public void onClosed() {
                    if(slidingmenuLayout.isVisibleAccountMenu()) {
                        slidingmenuLayout.toogleAccountMenu(false);
                    }
                }
            });
        } else {
            try {
                slidingmenuLayout = findViewById(R.id.sliding_menu);
                slidingmenuLayout.setAccountProfileListener(this);
                slidingmenuLayout.setVisibility(View.VISIBLE);
                if (Global.isXmas()) {
                    ((ImageView) slidingmenuLayout.findViewById(R.id.menu_background)).setImageDrawable(
                            getResources().getDrawable(R.drawable.xmas_left_menu)
                    );
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    slidingmenuLayout.setOnSystemUiVisibilityChangeListener(
                            new View.OnSystemUiVisibilityChangeListener() {
                                @Override
                                public void onSystemUiVisibilityChange(int visibility) {
                                    if (visibility == View.GONE) {
                                        slidingmenuLayout.toogleAccountMenu(!slidingmenuLayout.
                                                isVisibleAccountMenu());
                                    }
                                    slidingmenuLayout.showAccountMenu = visibility == View.VISIBLE;
                                }
                            });
                }
            } catch (Exception ex) {
                createSlidingMenu(true);
                return;
            }
        }
        slidingmenuLayout.setProfileName(getResources().getString(R.string.loading));
        slidingMenuArray = Global.createSlidingMenuItems(this);
        accountSlidingMenuArray = Global.createAccountSlidingMenuItems(this);
        SlidingMenuAdapter menuAdapter = new SlidingMenuAdapter(this, slidingMenuArray);
        AccountSlidingMenuAdapter accountMenuAdapter = new AccountSlidingMenuAdapter(this,
                accountSlidingMenuArray);
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            ((ListView) menu.getMenu().findViewById(R.id.account_menu_view))
                    .setAdapter(accountMenuAdapter);
            ((ListView) menu.getMenu().findViewById(R.id.menu_view))
                    .setAdapter(menuAdapter);
        } else {
            ((ListView) slidingmenuLayout.findViewById(R.id.account_menu_view))
                    .setAdapter(accountMenuAdapter);
            ((ListView) slidingmenuLayout.findViewById(R.id.menu_view))
                    .setAdapter(menuAdapter);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                mainSettingsFragment.setNotificationSound(uri.toString());
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private void installFragments() {
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        profileFragment = new ProfileFragment();
        newsfeedFragment = new NewsfeedFragment();
        friendsFragment = new FriendsFragment();
        photosFragment = new PhotosFragment();
        videosFragment = new VideosFragment();
        groupsFragment = new GroupsFragment();
        notesFragment = new NotesFragment();
        mainSettingsFragment = new MainSettingsFragment();
        friendsFragment.setActivityContext(this);
        fn = new FragmentNavigator(this);
        if(activity_menu == null) {
            popup_menu  = new android.support.v7.widget
                    .PopupMenu(this, null);
            activity_menu = popup_menu.getMenu();
            getMenuInflater().inflate(R.menu.newsfeed, activity_menu);
            onCreateOptionsMenu(activity_menu);
        }
        conversationsFragment = new ConversationsFragment();
        FragmentManager fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.app_fragment, newsfeedFragment, "newsfeed");
        ft.add(R.id.app_fragment, friendsFragment, "friends");
        ft.add(R.id.app_fragment, photosFragment, "photos");
        ft.add(R.id.app_fragment, videosFragment, "videos");
        ft.add(R.id.app_fragment, groupsFragment, "groups");
        ft.add(R.id.app_fragment, conversationsFragment, "messages");
        ft.add(R.id.app_fragment, profileFragment, "profile");
        ft.add(R.id.app_fragment, notesFragment, "notes");
        ft.add(R.id.app_fragment, mainSettingsFragment, "settings");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(friendsFragment);
        ft.hide(photosFragment);
        ft.hide(videosFragment);
        ft.hide(groupsFragment);
        ft.hide(conversationsFragment);
        ft.hide(profileFragment);
        ft.hide(notesFragment);
        selectedFragment = newsfeedFragment;
        ft.show(newsfeedFragment);
        ft.hide(mainSettingsFragment);
        ft.commit();
        if(global_prefs.getBoolean("refreshOnOpen", true)) {
            global_prefs_editor.putString("current_screen", "newsfeed");
            global_prefs_editor.commit();
        } else {
            if (selectedFragment instanceof ProfileFragment) {
                openAccountProfile();
            } else if (selectedFragment instanceof FriendsFragment) {
                onSlidingMenuItemClicked(0, false);
            } else if (selectedFragment instanceof ConversationsFragment) {
                onSlidingMenuItemClicked(1, false);
            } else if (selectedFragment instanceof GroupsFragment) {
                onSlidingMenuItemClicked(2, false);
            }
        }
        progressLayout.setVisibility(View.VISIBLE);
        ab_layout = new ActionBarLayout(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            actionBar = findViewById(R.id.actionbar);
            actionBar.setCustomView(ab_layout);
            ab_layout.createSpinnerAdapter(this);
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
        setActionBar("custom_newsfeed");
        setActionBarTitle(getResources().getString(R.string.newsfeed));
    }

    public void createActionPopupMenu(final Menu menu, String where, boolean enable) {
        if(popup_menu == null) {
            popup_menu = new android.support.v7.widget.PopupMenu(this, null);
        }
        menu.clear();
        if(where.equals("account")) {
            getMenuInflater().inflate(R.menu.profile, menu);
            menu.getItem(0).setVisible(false);
        }
        if(enable) {
            dev.tinelix.retro_ab.ActionBar.PopupMenuAction action =
                    new dev.tinelix.retro_ab.ActionBar.PopupMenuAction(this, "", menu,
                            R.drawable.ic_overflow_holo_dark, new PopupMenu.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(dev.tinelix.retro_pm.MenuItem item) {
                            onMenuItemSelected(0, menu.getItem(item.getItemId()));
                        }
                    });
            actionBar.addAction(action);
        }
    }

    public void setActionBarTitle(String title) {
        try {
            ab_layout.setAppTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshPage(String screen) {
        errorLayout.setVisibility(View.GONE);
        if(screen.equals("subscriptions_newsfeed") || screen.equals("global_newsfeed")) {
            if (ovk_api.newsfeed == null) ovk_api.newsfeed = new Newsfeed();
        }
        if(screen.equals("subscriptions_newsfeed")) {
            menu_id = R.menu.newsfeed;
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if (newsfeedFragment.getCount() == 0) {
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            newsfeed_count = 25;
            ovk_api.newsfeed.get(ovk_api.wrapper, newsfeed_count);
        } else if(screen.equals("global_newsfeed")) {
            menu_id = R.menu.newsfeed;
            setActionBarTitle(getResources().getString(R.string.newsfeed));
            if (newsfeedFragment.getCount() == 0) {
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.GONE);
            }
            if (ovk_api.newsfeed == null) ovk_api.newsfeed = new Newsfeed();
            newsfeed_count = 25;
            ovk_api.newsfeed.getGlobal(ovk_api.wrapper, newsfeed_count);
        }
    }

    public void onAccountSlidingMenuItemClicked(int position) {
        if(position == 0) {
            mainSettingsFragment.openChangeAccountDialog();
        } else if(position == 1) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show();
        } else {
            mainSettingsFragment.openLogoutConfirmationDialog();
        }
    }

    @SuppressLint({"CommitTransaction", "CommitPrefEdits"})
    public void onSlidingMenuItemClicked(int position, boolean is_menu) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionBar = findViewById(R.id.actionbar);
            actionBar.removeAllActions();
            //createActionPopupMenu(activity_menu, false);
        }
        global_prefs_editor = global_prefs.edit();
        if(is_menu) {
            try {
                if (!((OvkApplication) getApplicationContext()).isTablet) {
                    menu.toggle(true);
                }
                if (activity_menu != null) {
                    activity_menu.clear();
                }
            } catch (Exception ignored) {
            }
        }
        ft = getSupportFragmentManager().beginTransaction();
        if(position < 6 || position == 7) setActionBar("");
        switch (position) {
            case 0:
                setActionBarTitle(getResources().getString(R.string.friends));
                fn.navigateTo("friends", ft);
                ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.id, 25, "friends_list");
                break;
            case 1:
                setActionBarTitle(getResources().getStringArray(R.array.leftmenu)[1]);
                fn.navigateTo("photos", ft);
                ovk_api.photos.getAlbums(ovk_api.wrapper, ovk_api.account.id, 25,
                        true, true, true);
                break;
            case 2:
                setActionBarTitle(getResources().getStringArray(R.array.leftmenu)[2]);
                fn.navigateTo("videos", ft);
                ovk_api.videos.getVideos(ovk_api.wrapper, ovk_api.account.id, 25);
                break;
            case 3:
                setActionBarTitle(getResources().getString(R.string.messages));
                fn.navigateTo("messages", ft);
                ovk_api.messages.getConversations(ovk_api.wrapper);
                break;
            case 4:
                setActionBarTitle(getResources().getString(R.string.groups));
                fn.navigateTo("groups", ft);
                ovk_api.groups.getGroups(ovk_api.wrapper, ovk_api.account.id, 25);
                break;
            case 5:
                setActionBarTitle(getResources().getString(R.string.notes));
                fn.navigateTo("notes", ft);
                ovk_api.notes.get(ovk_api.wrapper, ovk_api.account.id, 25, 1);
                break;
            case 6:
                menu_id = R.menu.newsfeed;
                onCreateOptionsMenu(activity_menu);
                setActionBarTitle(getResources().getString(R.string.newsfeed));
                fn.navigateTo("newsfeed", ft);
                if (ovk_api.newsfeed == null) {
                    ovk_api.newsfeed = new Newsfeed();
                    newsfeed_count = 25;
                    ovk_api.newsfeed.get(ovk_api.wrapper, newsfeed_count);
                }
                break;
            case 7:
                setActionBarTitle(getResources().getString(R.string.menu_settings));
                fn.navigateTo("settings", ft);
                break;
            default:
                Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void receiveState(int message, Bundle data) {
        try {
            if(data.containsKey("address")) {
                String activityName = data.getString("address");
                if(activityName == null) {
                    return;
                }
                boolean isCurrentActivity = activityName.equals(getLocalClassName());
                if(!isCurrentActivity) {
                    return;
                }
            }
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                String profile_name =
                        String.format("%s %s", ovk_api.account.first_name, ovk_api.account.last_name);
                instance_prefs_editor.putString("profile_name", profile_name);
                instance_prefs_editor.commit();
                mainSettingsFragment.setAccount(ovk_api.account);
                slidingmenuLayout.setProfileName(profile_name);
                ovk_api.newsfeed.get(ovk_api.wrapper, newsfeed_count);
                ovk_api.messages.getLongPollServer(ovk_api.wrapper);
                if(selectedFragment == newsfeedFragment) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        dev.tinelix.retro_ab.ActionBar actionBar = findViewById(R.id.actionbar);
                        if(actionBar.getActionCount() > 0) {
                            actionBar.removeAllActions();
                        }
                        dev.tinelix.retro_ab.ActionBar.Action newpost =
                                new dev.tinelix.retro_ab.ActionBar.Action() {
                                    @Override
                                    public int getDrawable() {
                                        return R.drawable.ic_ab_write;
                                    }

                                    @Override
                                    public void performAction(View view) {
                                        Global.openNewPostActivity(AppActivity.this, ovk_api);
                                    }
                                };
                        actionBar.addAction(newpost);
                    } else {
                        if (activity_menu == null) {
                            onPrepareOptionsMenu(activity_menu);
                        }
                        try {
                            MenuItem newpost = activity_menu.findItem(R.id.newpost);
                            newpost.setVisible(true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                ovk_api.account.getCounters(ovk_api.wrapper);
                ovk_api.users.getAccountUser(ovk_api.wrapper, ovk_api.account.id);
                slidingmenuLayout.loadAccountAvatar(ovk_api.account,
                        global_prefs.getString("photos_quality", ""));
                if(ovk_api.messages == null) {
                    ovk_api.messages = new Messages();
                }
            } else if (message == HandlerMessages.ACCOUNT_COUNTERS) {
                SlidingMenuItem friends_item = slidingMenuArray.get(0);
                friends_item.counter = ovk_api.account.counters.friends_requests;
                slidingMenuArray.set(0, friends_item);
                SlidingMenuItem messages_item = slidingMenuArray.get(3);
                messages_item.counter = ovk_api.account.counters.new_messages;
                slidingMenuArray.set(3, messages_item);
                SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this,
                        slidingMenuArray);
                if(!((OvkApplication) getApplicationContext()).isTablet) {
                    ((ListView) menu.getMenu().findViewById(R.id.menu_view))
                            .setAdapter(slidingMenuAdapter);
                } else {
                    ((ListView) slidingmenuLayout.findViewById(R.id.menu_view))
                            .setAdapter(slidingMenuAdapter);
                }
                try {
                    ab_layout.setNotificationCount(ovk_api.account.counters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (message == HandlerMessages.NEWSFEED_GET ||
                    message == HandlerMessages.NEWSFEED_GET_GLOBAL ||
                    message == HandlerMessages.NEWSFEED_GET_MORE ||
                    message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL) {
                if (selectedFragment instanceof NewsfeedFragment) {
                    Spinner ab_spinner = ab_layout.findViewById(R.id.spinner);
                    boolean notScroll = false;
                    if(message == HandlerMessages.NEWSFEED_GET_GLOBAL ||
                            message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL) {
                        notScroll = true;
                    }
                    newsfeedFragment.loadAPIData(this, ovk_api,
                            ab_spinner, isFromGlobalNewsfeed(message), notScroll);
                    progressLayout.setVisibility(View.GONE);
                    if(ovk_api.newsfeed.getWallPosts().size() > 0) {
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    } else {
                        setErrorPage(data, "ovk", message, false);
                    }
                }
            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                ovk_api.messages.getConversations(ovk_api.wrapper);
                activateLongPollService();
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                slidingmenuLayout.loadAccountAvatar(ovk_api.account,
                        global_prefs.getString("photos_quality", ""));
            } else if (message == HandlerMessages.NEWSFEED_ATTACHMENTS) {
                newsfeedFragment.setScrollingPositions(this, true, true);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS) {
                newsfeedFragment.loadAvatars();
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .setScrollingPositions();
            } else if(message == HandlerMessages.VIDEO_THUMBNAILS) {
                if(selectedFragment instanceof NewsfeedFragment) {
                    newsfeedFragment.refreshAdapter();
                } else if(selectedFragment instanceof ProfileFragment) {
                    profileFragment.refreshWallAdapter();
                } else if(selectedFragment instanceof VideosFragment) {
                    videosFragment.createAdapter(this, ovk_api.videos.getList());
                }
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .loadAvatars();
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsFragment.loadAvatars();
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                groupsFragment.loadAvatars();
            } else if (message == HandlerMessages.USERS_GET) {
                ovk_api.user = ovk_api.users.getList().get(0);
                ovk_api.account.user = ovk_api.user;
                profileFragment.loadAPIData(this, ovk_api, getWindowManager());
                if (selectedFragment instanceof ProfileFragment) {
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    profile_loaded = true;
                    progressLayout.setVisibility(View.GONE);
                }
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                ovk_api.account.user = ovk_api.users.getList().get(0);
                ovk_api.account.user.downloadAvatar(ovk_api.dlman, global_prefs.
                        getString("photos_quality", ""), "account_avatar");
            } else if (message == HandlerMessages.WALL_GET ||
                    message == HandlerMessages.WALL_GET_MORE) {
                profileFragment.loadWall(this, ovk_api);
            } else if (message == HandlerMessages.FRIENDS_GET) {
                if (selectedFragment instanceof FriendsFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    friendsFragment.loadAPIData(this, ovk_api, true);
                }
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                boolean infinity_scroll = old_friends_size != ovk_api.friends.getFriends().size();
                friendsFragment.loadAPIData(this, ovk_api, infinity_scroll);
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                if(selectedFragment instanceof FriendsFragment) {
                    ovk_api.friends.requests.remove(friendsFragment.requests_cursor_index);
                } else {
                    JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                    int status = response.getInt("response");
                    if (status == 1) {
                        ovk_api.user.friends_status = status;
                    } else if (status == 2) {
                        ovk_api.user.friends_status = 3;
                    }
                    profileFragment.setAddToFriendsButtonListener(this, ovk_api.user.id, ovk_api.user);
                }
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    ovk_api.user.friends_status = 0;
                }
                profileFragment.setAddToFriendsButtonListener(this, ovk_api.user.id, ovk_api.user);
            } else if (message == HandlerMessages.FRIENDS_REQUESTS) {
                ArrayList<Friend> requestsList = ovk_api.friends.requests;
                if (selectedFragment instanceof FriendsFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                friendsFragment.createAdapter(this, requestsList, "requests");
            } else if (message == HandlerMessages.PHOTOS_GETALBUMS) {
                ArrayList<PhotoAlbum> albumsList = ovk_api.photos.albumsList;
                if (selectedFragment instanceof PhotosFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                photosFragment.createAdapter(this, albumsList, "photos");
                photosFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.VIDEOS_GET) {
                if (selectedFragment instanceof VideosFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    videosFragment.createAdapter(this, ovk_api.videos.getList());
                    videosFragment.setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.GROUPS_GET) {
                ArrayList<Group> groupsList = ovk_api.groups.getList();
                if (selectedFragment instanceof GroupsFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                groupsFragment.createAdapter(this, groupsList);
                groupsFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.GROUPS_GET_MORE) {
                ArrayList<Group> groupsList = ovk_api.groups.getList();
                if (selectedFragment instanceof GroupsFragment) {
                    progressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                }
                groupsFragment.createAdapter(this, groupsList);
                groupsFragment.setScrollingPositions(this,
                        old_friends_size != ovk_api.groups.getList().size());
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                ovk_api.friends.parse(data.getString("response"), ovk_api.dlman,
                        false, true);
                if (selectedFragment instanceof ProfileFragment) {
                    profileFragment.setCounter(ovk_api.user, "friends", ovk_api.friends.count);
                }
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                if (selectedFragment instanceof ConversationsFragment) {
                    if (conversations.size() > 0) {
                        conversationsFragment.createAdapter(this, conversations, ovk_api.account);
                        progressLayout.setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    } else {
                        progressLayout.setVisibility(View.GONE);
                        setErrorPage(data, "ovk", message, false);
                    }
                }
            } else if(message == HandlerMessages.LIKES_ADD) {
                if (selectedFragment instanceof NewsfeedFragment) {
                    newsfeedFragment.select(ovk_api.likes.position, "likes", 1);
                } else if (selectedFragment instanceof ProfileFragment) {
                    ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                            .select(ovk_api.likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                ovk_api.likes.parse(data.getString("response"));
                if (selectedFragment instanceof NewsfeedFragment) {
                    newsfeedFragment.select(ovk_api.likes.position, "likes", 0);
                } else if (selectedFragment instanceof ProfileFragment) {
                    ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                            .select(ovk_api.likes.position, "likes", 0);
                }
            } else if(message == HandlerMessages.POLL_ADD_VOTE
                    || message == HandlerMessages.POLL_DELETE_VOTE) {
                boolean addVote = message == HandlerMessages.POLL_ADD_VOTE
                        || message == HandlerMessages.POLL_DELETE_VOTE;
                WallPost item = null;
                if (selectedFragment instanceof NewsfeedFragment) {
                    item = ovk_api.newsfeed.getWallPosts().get(item_pos);
                } else if(selectedFragment instanceof ProfileFragment) {
                    item = ovk_api.wall.getWallItems().get(item_pos);
                }
                if(item != null) {
                    for (int attachment_index = 0; attachment_index < item.attachments.size();
                         attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            Poll poll = ((Poll) item.attachments.
                                    get(attachment_index).getContent());
                            PollAnswer answer = poll.answers.get(poll_answer);
                            poll.user_votes = addVote ? 0 : 1;
                            answer.is_voted = addVote;
                            poll.answers.set(poll_answer, answer);
                            item.attachments.get(attachment_index).setContent(poll);
                            ovk_api.wall.getWallItems().set(item_pos, item);
                            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                                    .updateItem(item, item_pos);
                        }
                    }
                }
            } else if(message == HandlerMessages.WALL_REPOST) {
                Toast.makeText(this, getResources().getString(R.string.repost_ok_wall),
                        Toast.LENGTH_LONG).show();
            } else if(message == HandlerMessages.NOTES_GET) {
                if(ovk_api.notes.list.size() > 0) {
                    notesFragment.createAdapter(this, ovk_api.notes.list);
                    if (selectedFragment instanceof NotesFragment) {
                        progressLayout.setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    }
                } else {
                    progressLayout.setVisibility(View.GONE);
                    setErrorPage(data, "ovk", message, false);
                }
            } else if(message == HandlerMessages.OVK_CHECK_HTTP) {
                mainSettingsFragment.setConnectionType(
                        HandlerMessages.OVK_CHECK_HTTP, ovk_api.wrapper.proxy_connection);
                ovk_api.ovk.getVersion(ovk_api.wrapper);
                ovk_api.ovk.aboutInstance(ovk_api.wrapper);
            } else if(message == HandlerMessages.OVK_CHECK_HTTPS) {
                mainSettingsFragment.setConnectionType(HandlerMessages.OVK_CHECK_HTTPS,
                        ovk_api.wrapper.proxy_connection);
                ovk_api.ovk.getVersion(ovk_api.wrapper);
                ovk_api.ovk.aboutInstance(ovk_api.wrapper);
            } else if(message == HandlerMessages.OVK_ABOUTINSTANCE) {
                mainSettingsFragment.setAboutInstanceData(ovk_api.ovk);
            } else if(message == HandlerMessages.OVK_VERSION) {
                mainSettingsFragment.setInstanceVersion(ovk_api.ovk);
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                profileFragment.loadAvatar(ovk_api.user,
                        global_prefs.getString("photos_quality", ""));
                slidingmenuLayout.loadAccountAvatar(ovk_api.account,
                        global_prefs.getString("photos_quality", ""));
            } else if(message == HandlerMessages.PHOTOS_GETALBUMS) {
                photosFragment.refresh();
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                conversationsFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.LONGPOLL) {
                notifMan.buildDirectMsgNotification(this, conversations, data, global_prefs.
                                getBoolean("enableNotification", true),
                        notifMan.isRepeat(last_longpoll_response, data.getString("response")));
                last_longpoll_response = data.getString("response");
            } else if(message == HandlerMessages.INVALID_TOKEN
                    || message == HandlerMessages.BANNED_ACCOUNT) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.invalid_session), Toast.LENGTH_LONG).show();
                removeAccount();
                ArrayList<InstanceAccount> accounts = new ArrayList<>();
                AccountManager accountManager = AccountManager.get(this);
                accountManager.addOnAccountsUpdatedListener(
                        new AccountsUpdateListener(this),
                        null, false);
                Global.loadAccounts(this, accounts, accountManager, instance_prefs);
            } else if (message < 0) {
                if(data.containsKey("method")) {
                    try {
                        String method = data.getString("method");
                        String where = data.getString("where");
                        if (Global.checkShowErrorLayout(method, selectedFragment)) {
                            if(!data.containsKey("where") ||
                                    !where.startsWith("more")) {
                                if(ovk_api.account == null)
                                    slidingmenuLayout.setProfileName(getResources().getString(R.string.error));
                                setErrorPage(data,"error", message, true);
                            } else {
                                if(!inBackground) {
                                    Toast.makeText(this,
                                            getResources().getString(R.string.err_text), Toast.LENGTH_LONG).show();
                                }
                            }
                        } else if(method.equals("Account.getCounters")) {
                            ab_layout.setNotificationCount(
                                    new AccountCounters(0, 0, 0)
                            );
                        } else {
                            if(selectedFragment instanceof ProfileFragment) {
                                if (data.getString("method").equals("Wall.get")) {
                                    profileFragment.getView().
                                            findViewById(R.id.wall_error_layout)
                                            .setVisibility(View.VISIBLE);
                                    profileFragment.getWallSelector()
                                            .findViewById(R.id.profile_wall_progress)
                                            .setVisibility(View.GONE);
                                } else {
                                    if (!inBackground)
                                        Toast.makeText(this,
                                                getResources().getString(R.string.err_text),
                                                Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        progressLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(ovk_api.account.first_name == null && ovk_api.account.last_name == null) {
                        slidingmenuLayout.setProfileName(getResources().getString(R.string.error));
                    }
                    setErrorPage(data, "error", message, false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorPage(data, "error", HandlerMessages.INVALID_JSON_RESPONSE, false);
        }
    }

    private void removeAccount() {
        AccountManager am = AccountManager.get(this);
        am.removeAccount(androidAccount, null, null);
        instance_prefs_editor = instance_prefs.edit();
        instance_prefs_editor.clear();
        instance_prefs_editor.commit();
    }

    private int isFromGlobalNewsfeed(int message) {
        if(message == HandlerMessages.NEWSFEED_GET
           || message == HandlerMessages.NEWSFEED_GET_MORE)
            return 0;
        else if(message == HandlerMessages.NEWSFEED_GET_GLOBAL
                || message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL)
            return 1;
        else
            return 2;
    }

    private void activateLongPollService() {
        OvkApplication ovk_app = ((OvkApplication) getApplicationContext());
        ovk_app.longPollService =
                new LongPollService(this, handler,
                        instance_prefs.getString("access_token", ""),
                        global_prefs.getBoolean("use_https", true),
                        global_prefs.getBoolean("debugUseLegacyHttpClient", false));
        ovk_app.longPollService.setProxyConnection(
                global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        ovk_app.longPollService.run(instance_prefs.
                        getString("server", ""), longPollServer.address, longPollServer.key,
                longPollServer.ts, global_prefs.getBoolean("useHTTPS", true),
                global_prefs.getBoolean("legacyHttpClient", false));
    }

    private void setErrorPage(Bundle data, String icon, int reason, boolean showRetry) {
        if(selectedFragment != mainSettingsFragment) {
            progressLayout.setVisibility(View.GONE);
            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
            errorLayout.setIcon(icon);
            errorLayout.setData(data);
            errorLayout.setRetryAction(ovk_api.wrapper, ovk_api.account);
            errorLayout.setReason(reason);
            errorLayout.setProgressLayout(progressLayout);
            Spinner news_spinner = ab_layout.findViewById(R.id.spinner);
            if (icon.equals("ovk")) {
                if(reason == HandlerMessages.NOTES_GET) {
                    errorLayout.setTitle(
                            getResources().getString(R.string.no_notes));
                } else if(reason == HandlerMessages.MESSAGES_CONVERSATIONS) {
                    errorLayout.setTitle(
                            getResources().getString(R.string.no_messages));
                } else {
                    if (news_spinner.getSelectedItemPosition() == 0) {
                        errorLayout.setTitle(
                                getResources().getString(R.string.local_newsfeed_no_posts));
                    } else {
                        errorLayout.setTitle(
                                getResources().getString(R.string.no_news));
                    }
                }
            } else {
                errorLayout.setTitle(getResources().getString(R.string.err_text));
            }
            if (!showRetry) {
                errorLayout.hideRetryButton();
            }
            progressLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("CommitTransaction")
    public void openAccountProfile() {
        try {
            if (!((OvkApplication) getApplicationContext()).isTablet) {
                if (menu == null) {
                    menu = new SlidingMenu(this);
                }
                if(menu.isMenuShowing()) {
                    menu.toggle(true);
                }
            }

            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            ft = getSupportFragmentManager().beginTransaction();
            fn.navigateTo("profile", ft);
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.profile));
            if(ovk_api.users == null) {
                ovk_api.users = new Users();
            }
            ovk_api.users.getUser(ovk_api.wrapper, ovk_api.account.id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hideSelectedItemBackground() {
        (friendsFragment.getView().findViewById(R.id.friends_listview))
                .setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void loadMoreNews() {
        if(ovk_api.newsfeed != null) {
            ovk_api.newsfeed.get(ovk_api.wrapper, 25, ovk_api.newsfeed.next_from);
        }
    }

    public void selectNewsSpinnerItem(int position) {
        Spinner spinner = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            spinner = (getActionBar().getCustomView().findViewById(R.id.spinner));
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
                ovk_api.newsfeed.get(ovk_api.wrapper, 25);
            } else {
                ovk_api.newsfeed.getGlobal(ovk_api.wrapper, 25);
            }
            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            ((RecyclerView) newsfeedFragment.getView().findViewById(R.id.news_listview))
                    .scrollToPosition(0);
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

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(lpReceiver);
        } catch (Exception ignored) {

        }
        super.onDestroy();
    }
}