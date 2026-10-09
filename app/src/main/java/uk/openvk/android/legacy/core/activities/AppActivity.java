/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.core.activities;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.counters.AccountCounters;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.PhotoAlbum;
import uk.openvk.android.client.entities.Poll;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.models.Messages;
import uk.openvk.android.client.models.Newsfeed;
import uk.openvk.android.client.models.Users;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.core.fragments.ConversationsFragment;
import uk.openvk.android.legacy.core.fragments.FriendsFragment;
import uk.openvk.android.legacy.core.fragments.GroupsFragment;
import uk.openvk.android.legacy.core.fragments.MainSettingsFragment;
import uk.openvk.android.legacy.core.fragments.NewsfeedFragment;
import uk.openvk.android.legacy.core.fragments.NotesFragment;
import uk.openvk.android.legacy.core.fragments.PhotosFragment;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.fragments.pages.ProfilePageFragment;
import uk.openvk.android.legacy.core.fragments.VideosFragment;
import uk.openvk.android.legacy.core.listeners.AccountsUpdateListener;
import uk.openvk.android.legacy.databases.AudioCacheDB;
import uk.openvk.android.legacy.databases.NewsfeedCacheDB;
import uk.openvk.android.legacy.databases.WallCacheDB;
import uk.openvk.android.legacy.receivers.LongPollReceiver;
import uk.openvk.android.legacy.ui.FragmentNavigator;
import uk.openvk.android.legacy.ui.list.adapters.SlidingMenuAdapter;
import uk.openvk.android.legacy.ui.list.items.InstanceAccount;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuObject;
import uk.openvk.android.legacy.ui.views.ActionBarLayout;
import uk.openvk.android.legacy.ui.views.ErrorLayout;
import uk.openvk.android.legacy.ui.views.ProgressLayout;
import uk.openvk.android.legacy.ui.views.SlidingMenuLayout;
import uk.openvk.android.legacy.ui.views.WallLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;
import uk.openvk.android.legacy.utils.AccountAuthenticator;
import uk.openvk.android.legacy.utils.NotificationManager;

@SuppressWarnings({"StatementWithEmptyBody", "ConstantConditions"})
public class AppActivity extends NetworkFragmentActivity {
    private ArrayList<SlidingMenuObject> mMenuArray;
    private SlidingMenu mMenu;
    public ProgressLayout mProgressLayout;
    public ErrorLayout mErrorLayout;
    private SlidingMenuLayout mMenuLayout;
    public ArrayList<Conversation> mConversations;
    public Menu mActivityMenu;
    private int mMaxNewsfeedCount = 25;
    private NotificationManager mNotifMan;
    private boolean mInBackground;
    public ActionBarLayout ab_layout;
    public dev.tinelix.retro_ab.ActionBar actionBar;
    private FragmentTransaction ft;
    public Fragment selectedFragment;
    private FragmentNavigator mFragmNav;
    public android.support.v7.widget.PopupMenu mPopupMenu;
    private boolean mMainMenuAnimate;

    @SuppressLint({"CommitPrefEdits", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainMenuAnimate = !mGlobalPrefs.getString("mainMenuAnimation", "Contrast")
                                       .equals("Disabled");

        if(getAndroidAccounts())
            setContentView(R.layout.activity_app);
        else
            return;

        mInBackground = true;

        installFragments();

        Global.fixWindowPadding(findViewById(R.id.app_fragment), getTheme());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            Global.fixWindowPadding(getWindow(), getTheme());

        mConversations = new ArrayList<>();

        if(((OvkApplication) getApplicationContext()).isTablet) {
            if(selectedFragment instanceof ActiveFragment) {
                ((ActiveFragment) selectedFragment)
                        .adjustLayout(getResources().getConfiguration().orientation);
            }
        }
        boolean isTablet = ((OvkApplication) getApplicationContext()).isTablet;
        createSlidingMenu(isTablet);

        // Creating notification manager
        ((OvkApplication) getApplicationContext()).notifMan = new NotificationManager(AppActivity.this);

        mNotifMan = ((OvkApplication) getApplicationContext()).notifMan;
        mNotifMan.createNotificationChannel(
                "service_notifs",
                false, false, false, false
        );
        mNotifMan.createNotificationChannel(
                "audio_player",
                false, false, false, true
        );
        mNotifMan.createNotificationChannel("new_messages");
    }

    public boolean getAndroidAccounts() {
        ArrayList<InstanceAccount> accountArray = new ArrayList<>();
        AccountManager accountManager = AccountManager.get(this);
        accountManager.addOnAccountsUpdatedListener(new AccountsUpdateListener(this),
                null, false);
        AccountAuthenticator.loadAccounts(this, accountArray, accountManager, mInstancePrefs);
        if(((OvkApplication)getApplication()).androidAccount == null) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.invalid_session), Toast.LENGTH_LONG).show();
            removeAccount();

            if(accountArray.size() >= 1
                    && mGlobalPrefs.getString("current_instance", "").length() == 0) {
                AccountAuthenticator.openChangeAccountDialog(this, mGlobalPrefs, false);
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
                if(mNotifMan != null) mNotifMan.clearAudioPlayerNotification();
                if(!getAudioPlayerService().isPlaying())
                    exitApplication();
            } else {
                if (selectedFragment instanceof AudiosFragment)
                    ((AudiosFragment) selectedFragment).closeSearchItem();
                mFragmNav.navigateTo("newsfeed", getSupportFragmentManager().beginTransaction());

                mProgressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            exitApplication();
        }
    }

    private void exitApplication() {
        NewsfeedCacheDB.freeDatabases();
        WallCacheDB.freeDatabases();
        AudioCacheDB.freeDatabase();
        finish();
        System.exit(0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    public void setActionBar(String layout_name) {
        try {
            ab_layout.setOnHomeButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMenu.toggle(mMainMenuAnimate);
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
                        ab_layout.setNotificationCount(getOpenVKAPI().account.counters);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Adjusting layout for Mobile UI and Tablet UI

        ((OvkApplication) getApplicationContext()).config = newConfig;

        if(selectedFragment instanceof ActiveFragment) {
            ((ActiveFragment) selectedFragment).adjustLayout(newConfig.orientation);
        }
        if(!((OvkApplication) getApplicationContext()).isTablet) {
            mMenu.setBehindWidth((int) (getResources().getDisplayMetrics().density * 260));
        }

        ab_layout.adjustLayout();
        Global.fixWindowPadding(findViewById(R.id.app_fragment), getTheme());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Global.fixWindowPadding(getWindow(), getTheme());
        }
    }

    private void createSlidingMenu(boolean isTablet) {
        while(mMenuLayout == null) {
            mMenuLayout = new SlidingMenuLayout(this);
        }
        mMenu = new SlidingMenu(this);

        Global.setSlidingMenu(this, mMenuLayout, mMenu);
        mMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                if(mMenuLayout.isVisibleAccountMenu()) {
                    mMenuLayout.toogleAccountMenu(false);
                }
            }
        });

        mOpenVK.account.id = mInstancePrefs.getLong("uid", 0);
        mOpenVK.account.user.id = mInstancePrefs.getLong("uid", 0);

        mMenuLayout.setProfileName(
                mInstancePrefs.getString(
                        "profile_name",
                        getResources().getString(R.string.loading)
                )
        );

        mMenuLayout.loadAccountAvatar(
                mOpenVK, mGlobalPrefs.getString("photos_quality", ""), true
        );

        mMenuArray = Global.createSlidingMenuItems(this);

        mMenuLayout.setMenuItems(mMenuArray);

        ArrayList<SlidingMenuObject> accountSlidingMenuArray =
                Global.createAccountSlidingMenuItems(this);

        SlidingMenuAdapter menuAdapter = new SlidingMenuAdapter(
                this, mMenuArray, false
        );
        SlidingMenuAdapter accountMenuAdapter = new SlidingMenuAdapter(
                this, accountSlidingMenuArray, true
        );

        RecyclerView menuListView = mMenu.getMenu().findViewById(R.id.menu_view);
        RecyclerView accountMenuListView = mMenu.getMenu().findViewById(R.id.account_menu_view);

        menuListView.setLayoutManager(new LinearLayoutManager(this));
        menuListView.setAdapter(menuAdapter);

        accountMenuListView.setLayoutManager(new LinearLayoutManager(this));
        accountMenuListView.setAdapter(accountMenuAdapter);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                if(selectedFragment instanceof MainSettingsFragment)
                    ((MainSettingsFragment) selectedFragment).setNotificationSound(uri.toString());
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private void installFragments() {
        mProgressLayout = findViewById(R.id.progress_layout);
        mErrorLayout = findViewById(R.id.error_layout);

        selectedFragment = new NewsfeedFragment();

        mFragmNav = new FragmentNavigator(this);
        if(mActivityMenu == null) {
            mPopupMenu  = new android.support.v7.widget
                    .PopupMenu(this, null);
            mActivityMenu = mPopupMenu.getMenu();
            getMenuInflater().inflate(R.menu.newsfeed, mActivityMenu);
            onCreateOptionsMenu(mActivityMenu);
        }

        FragmentManager fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.app_fragment, selectedFragment);
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.commit();

        if(mGlobalPrefs.getBoolean("refreshOnOpen", true)) {
            SharedPreferences.Editor editor = getGlobalPreferencesEditor();
            editor.putString("current_screen", "newsfeed");
            editor.commit();
        } else {
            if (selectedFragment instanceof ProfilePageFragment)
                openAccountProfile();
            else if (selectedFragment instanceof FriendsFragment)
                onSlidingMenuItemClicked(0, false);
            else if (selectedFragment instanceof ConversationsFragment)
                onSlidingMenuItemClicked(1, false);
            else if (selectedFragment instanceof GroupsFragment)
                onSlidingMenuItemClicked(2, false);
        }

        mProgressLayout.setVisibility(View.VISIBLE);
        ab_layout = new ActionBarLayout(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(mGlobalPrefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(mGlobalPrefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            actionBar = findViewById(R.id.actionbar);
            actionBar.setCustomView(ab_layout);
            ab_layout.createSpinnerAdapter(this);
            switch (mGlobalPrefs.getString("uiTheme", "blue")) {
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


    public void setActionBarTitle(String title) {
        try {
            ab_layout.setAppTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshPage(String screen) {
        mErrorLayout.setVisibility(View.GONE);

        if(selectedFragment instanceof NewsfeedFragment) {

            if (screen.equals("subscriptions_newsfeed") || screen.equals("global_newsfeed")) {
                if (mOpenVK.newsfeed == null) mOpenVK.newsfeed = new Newsfeed();
            }

            if (screen.equals("subscriptions_newsfeed")) {
                setActionBarTitle(getResources().getString(R.string.newsfeed));
                if (((NewsfeedFragment) selectedFragment).getCount() == 0) {
                    findViewById(R.id.app_fragment).setVisibility(View.GONE);
                    mProgressLayout.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    mProgressLayout.setVisibility(View.GONE);
                }
                mOpenVK.newsfeed.get(mOpenVK.wrapper, mMaxNewsfeedCount);

            } else if (screen.equals("global_newsfeed")) {
                setActionBarTitle(getResources().getString(R.string.newsfeed));
                if (((NewsfeedFragment) selectedFragment).getCount() == 0) {
                    findViewById(R.id.app_fragment).setVisibility(View.GONE);
                    mProgressLayout.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    mProgressLayout.setVisibility(View.GONE);
                }
                if (mOpenVK.newsfeed == null) mOpenVK.newsfeed = new Newsfeed();
                mMaxNewsfeedCount = 25;
                mOpenVK.newsfeed.getGlobal(mOpenVK.wrapper, mMaxNewsfeedCount);
            }
        }
    }

    public void onAccountSlidingMenuItemClicked(int position) {
        if(position == 0) {
            AccountAuthenticator.openChangeAccountDialog(this, mGlobalPrefs, true);
        } else if(position == 1) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show();
        } else {
            AccountAuthenticator.openLogoutConfirmationDialog(this, mGlobalPrefs);
        }
    }

    @SuppressLint({"CommitTransaction", "CommitPrefEdits"})
    public void onSlidingMenuItemClicked(int position, boolean is_menu) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionBar = findViewById(R.id.actionbar);
            actionBar.removeAllActions();
        }

        SharedPreferences.Editor editor = getGlobalPreferencesEditor();

        if(is_menu) {
            try {
                if(!((OvkApplication) getApplication()).isTablet)
                    mMenu.toggle(mMainMenuAnimate);

                if(position == 7) {
                    getMenuInflater().inflate(R.menu.newsfeed, mActivityMenu);
                    onPrepareOptionsMenu(mActivityMenu);
                } else if (mActivityMenu != null) {
                    mActivityMenu.clear();
                }
            } catch (Exception ignored) {
            }
        }

        ft = getSupportFragmentManager().beginTransaction();
        if(position < 6 || position == 7) setActionBar("");

        switch (position) {
            case 0:
                setActionBarTitle(getResources().getString(R.string.friends));
                mFragmNav.navigateTo("friends", ft);
                mOpenVK.friends.get(mOpenVK.wrapper, mOpenVK.account.id, 25, false);
                mOpenVK.friends.getRequests(mOpenVK.wrapper);
                break;
            case 1:
                setActionBarTitle(getResources().getStringArray(R.array.leftmenu)[1]);
                mFragmNav.navigateTo("photos", ft);
                mOpenVK.photos.getAlbums(
                        mOpenVK.wrapper, mOpenVK.account.id, 25,
                        true, true, true);
                break;
            case 2:
                setActionBarTitle(getResources().getStringArray(R.array.leftmenu)[2]);
                mFragmNav.navigateTo("videos", ft);
                mOpenVK.videos.getVideos(mOpenVK.wrapper, mOpenVK.account.id, 25);
                break;
            case 3:
                onPrepareOptionsMenu(mActivityMenu);
                setActionBarTitle(getResources().getStringArray(R.array.leftmenu)[3]);
                mFragmNav.navigateTo("audios", ft);
                mOpenVK.audios.get(mOpenVK.wrapper, mOpenVK.account.id, 80, true);
                break;
            case 4:
                setActionBarTitle(getResources().getString(R.string.messages));
                mFragmNav.navigateTo("messages", ft);
                mOpenVK.messages.getConversations(mOpenVK.wrapper);
                break;
            case 5:
                setActionBarTitle(getResources().getString(R.string.groups));
                mFragmNav.navigateTo("groups", ft);
                mOpenVK.groups.getGroups(mOpenVK.wrapper, mOpenVK.account.id, 25);
                break;
            case 6:
                setActionBarTitle(getResources().getString(R.string.notes));
                mFragmNav.navigateTo("notes", ft);
                mOpenVK.notes.get(mOpenVK.wrapper, mOpenVK.account.id, 80, 1);
                break;
            case 7:
                setActionBarTitle(getResources().getString(R.string.newsfeed));
                mFragmNav.navigateTo("newsfeed", ft);
                if (mOpenVK.newsfeed == null) {
                    mOpenVK.newsfeed = new Newsfeed();
                    mMaxNewsfeedCount = 25;
                }
                break;
            case 8:
                setActionBarTitle(getResources().getString(R.string.menu_settings));
                mFragmNav.navigateTo("settings", ft);
                break;
            default:
                Toast.makeText(
                        this,
                        R.string.not_supported,
                        Toast.LENGTH_LONG
                ).show();
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
                boolean isCurrentActivity = activityName.equals(
                        String.format("%s_%s", getLocalClassName(), getSessionId())
                );
                if(!isCurrentActivity) {
                    return;
                }
            }
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {

                String profile_name = "";
                if(mOpenVK.account.first_name != null && mOpenVK.account.last_name != null)
                    profile_name =
                        String.format("%s %s", mOpenVK.account.first_name, mOpenVK.account.last_name);
                else if(mOpenVK.account.first_name != null)
                    profile_name = mOpenVK.account.first_name;

                SharedPreferences.Editor editor = getInstancePreferenceEditor();

                editor.putString("profile_name", profile_name);
                editor.commit();

                if(selectedFragment instanceof MainSettingsFragment) {
                    ((MainSettingsFragment) selectedFragment).setAccount(mOpenVK.account);
                }

                mMenuLayout.setProfileName(profile_name);

                NewsfeedCacheDB.initDatabases(this);
                ArrayList<WallPost> cached_posts = NewsfeedCacheDB.getPostsList(this);

                if(cached_posts != null && cached_posts.size() > 0) {
                    if(selectedFragment instanceof NewsfeedFragment) {
                        ((NewsfeedFragment) selectedFragment).loadFromCache(this);
                    }
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                } else {
                    mOpenVK.newsfeed.get(mOpenVK.wrapper, mMaxNewsfeedCount);
                }

                mOpenVK.messages.getLongPollServer(mOpenVK.wrapper);

                if(selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment).loadAccount(mOpenVK);
                }

                mOpenVK.account.getCounters(mOpenVK.wrapper);
                mOpenVK.users.getAccountUser(mOpenVK.wrapper, mOpenVK.account.id);

                mMenuLayout.loadAccountAvatar(
                        mOpenVK, mGlobalPrefs.getString("photos_quality", ""), true
                );

                // Displaying friends list in the sliding menu
                mOpenVK.friends.get(mOpenVK.wrapper, mOpenVK.account.id, 5, "sliding_menu");

                if(mOpenVK.messages == null)
                    mOpenVK.messages = new Messages();

            } else if (message == HandlerMessages.ACCOUNT_COUNTERS) {
                SlidingMenuObject friends_item = mMenuArray.get(0);
                RecyclerView menuView = mMenu.getMenu().findViewById(R.id.menu_view);
                SlidingMenuAdapter adapter = ((SlidingMenuAdapter) menuView.getAdapter());

                friends_item.counter = mOpenVK.account.counters.friends_requests;
                mMenuArray.set(0, friends_item);
                SlidingMenuObject messages_item = mMenuArray.get(4);
                messages_item.counter = mOpenVK.account.counters.new_messages;
                mMenuArray.set(4, messages_item);

                if(adapter != null) {
                    adapter.updateArray(mMenuArray);
                    adapter.notifyDataSetChanged();
                }

                try {
                    ab_layout.setNotificationCount(mOpenVK.account.counters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else if (message == HandlerMessages.NEWSFEED_GET ||
                    message == HandlerMessages.NEWSFEED_GET_GLOBAL ||
                    message == HandlerMessages.NEWSFEED_GET_MORE ||
                    message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL) {
                if (selectedFragment instanceof NewsfeedFragment) {
                    Spinner ab_spinner = ab_layout.findViewById(R.id.spinner);
                    boolean clear =
                            message != HandlerMessages.NEWSFEED_GET_MORE &&
                            message != HandlerMessages.NEWSFEED_GET_MORE_GLOBAL;

                    ((NewsfeedFragment) selectedFragment).loadAPIData(
                            this, mOpenVK, ab_spinner, isFromGlobalNewsfeed(message), clear
                    );

                    mProgressLayout.setVisibility(View.GONE);

                    if(clear) {
                        if (mOpenVK.newsfeed.getWallPosts().size() > 0)
                            findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                        else
                            setErrorPage(data, "ovk", message, false);
                    }
                }

            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                mOpenVK.messages.getConversations(mOpenVK.wrapper);
                bindLongPollService();
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                mMenuLayout.loadAccountAvatar(
                        mOpenVK, mGlobalPrefs.getString("photos_quality", ""), false
                );
            } else if (message == HandlerMessages.NEWSFEED_ATTACHMENTS) {
                if(selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment).loadPhotos();
                }
            } else if(message == HandlerMessages.NEWSFEED_AVATARS) {
                if(selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment).loadAvatars();
                }
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                if(selectedFragment instanceof ProfilePageFragment) {
                    ((ProfilePageFragment) selectedFragment).loadPhotos();
                }
            } else if(message == HandlerMessages.VIDEO_THUMBNAILS) {
                if(selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment).refreshAdapter();
                } else if(selectedFragment instanceof ProfilePageFragment) {
                    ((ProfilePageFragment) selectedFragment).refreshWallAdapter();
                } else if(selectedFragment instanceof VideosFragment) {
                    ((VideosFragment) selectedFragment).createAdapter(this, mOpenVK.videos.getList());
                }
            } else if (message == HandlerMessages.WALL_AVATARS) {
                if(selectedFragment instanceof ProfilePageFragment) {
                    ((WallLayout) selectedFragment.getView().findViewById(R.id.wall_layout))
                            .loadAvatars();
                }
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                if(selectedFragment instanceof FriendsFragment)
                    ((FriendsFragment) selectedFragment).updateFriendsAdapters();
                mMenuLayout.updateAdapter();
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                if(selectedFragment instanceof GroupsFragment)
                    ((GroupsFragment) selectedFragment).loadAvatars();
                mMenuLayout.updateAdapter();
            } else if (message == HandlerMessages.USERS_GET) {
                mOpenVK.user = mOpenVK.users.getList().get(0);
                mOpenVK.account.user = mOpenVK.user;
                if (selectedFragment instanceof ProfilePageFragment) {
                    ((ProfilePageFragment) selectedFragment)
                            .loadAPIData(this, mOpenVK, getWindowManager());
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    mProgressLayout.setVisibility(View.GONE);
                }
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                mOpenVK.account.user = mOpenVK.users.getList().get(0);
                mOpenVK.account.user.downloadAvatar(
                        mOpenVK.dlman,
                        mGlobalPrefs.getString("photos_quality", ""),
                        "account_avatar"
                );
            } else if (message == HandlerMessages.WALL_GET ||
                    message == HandlerMessages.WALL_GET_MORE) {
                if (selectedFragment instanceof ProfilePageFragment) {
                    ((ProfilePageFragment) selectedFragment).loadWall(this, mOpenVK);
                }
            } else if (message == HandlerMessages.FRIENDS_GET) {
                if (selectedFragment instanceof FriendsFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);

                    if(mOpenVK.friends.getFriends().size() > 0)
                        ((FriendsFragment) selectedFragment).loadAPIData(this, 0, mOpenVK);
                    else
                        setErrorPage(data, "ovk", message, false);

                    mMenu.clearIgnoredViews();
                    mMenu.addIgnoredView(((FriendsFragment) selectedFragment).getViewPager());
                }
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                ((FriendsFragment) selectedFragment).loadAPIData(this, 0, mOpenVK);
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                if(selectedFragment instanceof FriendsFragment) {
                    mOpenVK.friends.requests.remove(((FriendsFragment) selectedFragment).requests_cursor_index);
                } else {
                    JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                    int status = response.getInt("response");

                    if (status == 1)
                        mOpenVK.user.friends_status = status;
                    else if (status == 2)
                        mOpenVK.user.friends_status = 3;

                    if(selectedFragment instanceof ProfilePageFragment)
                        ((ProfilePageFragment) selectedFragment)
                                .setAddToFriendsButtonListener(this, mOpenVK.user.id, mOpenVK.user);
                }
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    mOpenVK.user.friends_status = 0;
                }

                ((ProfilePageFragment) selectedFragment)
                        .setAddToFriendsButtonListener(this, mOpenVK.user.id, mOpenVK.user);

            } else if (message == HandlerMessages.FRIENDS_REQUESTS) {
                if (selectedFragment instanceof FriendsFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);

                    ((FriendsFragment) selectedFragment).loadAPIData(this, 0, mOpenVK);
                }
            } else if (message == HandlerMessages.PHOTOS_GETALBUMS) {
                ArrayList<PhotoAlbum> albumsList = mOpenVK.photos.albumsList;

                if (selectedFragment instanceof PhotosFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);

                    ((PhotosFragment) selectedFragment).createAdapter(this, albumsList, "photos");
                    ((PhotosFragment) selectedFragment).setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.VIDEOS_GET) {
                if (selectedFragment instanceof VideosFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    ((VideosFragment) selectedFragment).createAdapter(this, mOpenVK.videos.getList());
                    ((VideosFragment) selectedFragment).setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.AUDIOS_GET) {
                if (selectedFragment instanceof AudiosFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    ((AudiosFragment) selectedFragment)
                            .createAdapter(this, mOpenVK.audios.getList(), mOpenVK.account.id);
                    ((AudiosFragment) selectedFragment)
                            .setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.GROUPS_GET) {
                ArrayList<Group> groupsList = mOpenVK.groups.getList();
                if (selectedFragment instanceof GroupsFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    if(mOpenVK.groups.getList().size() > 0) {
                        ((GroupsFragment) selectedFragment).createAdapter(this, groupsList);
                        ((GroupsFragment) selectedFragment).setScrollingPositions(this, true);
                    } else
                        setErrorPage(data, "ovk", message, false);
                }
            } else if (message == HandlerMessages.GROUPS_GET_ALT) {
                String where = data.getString("where");
                switch (where) {
                    case "sliding_menu":
                        mMenuLayout.createGroupsList(mOpenVK.groups.getList());
                        break;
                }
            } else if (message == HandlerMessages.GROUPS_GET_MORE) {
                ArrayList<Group> groupsList = mOpenVK.groups.getList();
                if (selectedFragment instanceof GroupsFragment) {
                    mProgressLayout.setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    ((GroupsFragment) selectedFragment).createAdapter(this, groupsList);
                }
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                String where = data.getString("where");
                switch (where) {
                    case "sliding_menu":
                        mOpenVK.groups.getGroups(mOpenVK.wrapper, mOpenVK.account.id, 5, "sliding_menu");
                        mMenuLayout.createFriendsList(mOpenVK.friends.getFriends());
                        break;
                }
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                if (selectedFragment instanceof ConversationsFragment) {
                    mConversations = mOpenVK.messages.getConversations();
                    if (mConversations.size() > 0) {
                        ((ConversationsFragment) selectedFragment)
                                .createAdapter(this, mConversations, mOpenVK.account);
                        mProgressLayout.setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    } else {
                        mProgressLayout.setVisibility(View.GONE);
                        setErrorPage(data, "ovk", message, false);
                    }
                }
            } else if(message == HandlerMessages.LIKES_ADD) {
                if (selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment)
                            .select(mOpenVK.likes.position, "likes", 1);
                } else if (selectedFragment instanceof ProfilePageFragment) {
                    ((WallLayout) selectedFragment.getView().findViewById(R.id.wall_layout))
                            .select(mOpenVK.likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                mOpenVK.likes.parse(data.getString("response"));
                if (selectedFragment instanceof NewsfeedFragment) {
                    ((NewsfeedFragment) selectedFragment)
                            .select(mOpenVK.likes.position, "likes", 0);
                } else if (selectedFragment instanceof ProfilePageFragment) {
                    ((WallLayout) selectedFragment.getView().findViewById(R.id.wall_layout))
                            .select(mOpenVK.likes.position, "likes", 0);
                }
            } else if(message == HandlerMessages.POLL_ADD_VOTE
                    || message == HandlerMessages.POLL_DELETE_VOTE) {
                boolean addVote = message == HandlerMessages.POLL_ADD_VOTE
                        || message == HandlerMessages.POLL_DELETE_VOTE;
                WallPost item = null;
                int item_pos = -1;
                if (selectedFragment instanceof NewsfeedFragment) {
                    item = mOpenVK.newsfeed.getWallPosts().get(item_pos);
                } else if(selectedFragment instanceof ProfilePageFragment) {
                    item = mOpenVK.wall.getWallItems().get(item_pos);
                }
                if(item != null) {
                    for (int attachment_index = 0; attachment_index < item.attachments.size();
                         attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            Poll poll = ((Poll) item.attachments.get(attachment_index));
                            int poll_answer = -1;
                            Poll.PollAnswer answer = poll.answers.get(poll_answer);
                            poll.user_votes = addVote ? 0 : 1;
                            answer.is_voted = addVote;
                            poll.answers.set(poll_answer, answer);
                            mOpenVK.wall.getWallItems().set(item_pos, item);
                            ((WallLayout) selectedFragment.getView().findViewById(R.id.wall_layout))
                                    .updateItem(item, item_pos);
                        }
                    }
                }
            } else if(message == HandlerMessages.WALL_REPOST) {
                Toast.makeText(this, getResources().getString(R.string.repost_ok_wall),
                        Toast.LENGTH_LONG).show();
            } else if(message == HandlerMessages.NOTES_GET) {
                if(mOpenVK.notes.list.size() > 0) {
                    if (selectedFragment instanceof NotesFragment) {
                        ((NotesFragment) selectedFragment).createAdapter(this, mOpenVK.notes.list);
                        mProgressLayout.setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    }
                } else {
                    mProgressLayout.setVisibility(View.GONE);
                    setErrorPage(data, "ovk", message, false);
                }
            } else if(message == HandlerMessages.OVK_CHECK_HTTP) {
                if(selectedFragment instanceof MainSettingsFragment) {
                    ((MainSettingsFragment) selectedFragment).setConnectionType(
                            HandlerMessages.OVK_CHECK_HTTP, mOpenVK.wrapper.proxyEnabled);
                    mOpenVK.ovk.getVersion(mOpenVK.wrapper);
                    mOpenVK.ovk.aboutInstance(mOpenVK.wrapper);
                }
            } else if(message == HandlerMessages.OVK_CHECK_HTTPS) {
                if(selectedFragment instanceof MainSettingsFragment) {
                    ((MainSettingsFragment) selectedFragment).setConnectionType(HandlerMessages.OVK_CHECK_HTTPS,
                            mOpenVK.wrapper.proxyEnabled);
                    mOpenVK.ovk.getVersion(mOpenVK.wrapper);
                    mOpenVK.ovk.aboutInstance(mOpenVK.wrapper);
                }
            } else if(message == HandlerMessages.OVK_ABOUTINSTANCE) {
                if(selectedFragment instanceof MainSettingsFragment) {
                    ((MainSettingsFragment) selectedFragment).setAboutInstanceData(mOpenVK.ovk);
                }
            } else if(message == HandlerMessages.OVK_VERSION) {
                if(selectedFragment instanceof MainSettingsFragment) {
                    ((MainSettingsFragment) selectedFragment).setInstanceVersion(mOpenVK.ovk);
                }
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                if(selectedFragment instanceof ProfilePageFragment) {
                    ((ProfilePageFragment) selectedFragment).loadAvatar(
                            mOpenVK.user,
                            mGlobalPrefs.getString("photos_quality", "")
                    );
                }
                mMenuLayout.loadAccountAvatar(
                        mOpenVK, mGlobalPrefs.getString("photos_quality", ""), false
                );
            } else if(message == HandlerMessages.PHOTOS_GETALBUMS) {
                if(selectedFragment instanceof PhotosFragment) {
                    ((PhotosFragment) selectedFragment).refresh();
                }
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                    if(selectedFragment instanceof ConversationsFragment) {
                        ((ConversationsFragment) selectedFragment).refresh();
                    }
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
                    AccountAuthenticator.loadAccounts(this, accounts, accountManager, mInstancePrefs);
            } else if (message < 0) {
                try {
                        mOpenVK.audios.resetState();
                        if (data.containsKey("method")) {
                            String method = data.getString("method");
                            String where = data.getString("where");
                            if (Global.checkShowErrorLayout(method, (ActiveFragment) selectedFragment)) {
                                if (!data.containsKey("where") ||
                                        !where.startsWith("more")) {

                                    if (mOpenVK.account == null)
                                        mMenuLayout.setProfileName(getResources().getString(R.string.error));

                                    setErrorPage(data, "error", message, true);
                                } else {

                                    if (!mInBackground) {
                                        Toast.makeText(this,
                                                getResources().getString(R.string.err_text), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else if (method.equals("Account.getCounters")) {
                                ab_layout.setNotificationCount(
                                        new AccountCounters(0, 0, 0)
                                );
                            }
                    } else {
                        if (mOpenVK.account.first_name == null && mOpenVK.account.last_name == null) {
                            mMenuLayout.setProfileName(getResources().getString(R.string.error));
                        }
                        setErrorPage(data, "error", message, false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setErrorPage(data, "error", message, false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorPage(data, "error", HandlerMessages.INVALID_JSON_RESPONSE, false);
        }
    }

    private void removeAccount() {
        try {
            AccountManager am = AccountManager.get(this);
            if(((OvkApplication) getApplication()).androidAccount != null)
                am.removeAccount(
                        ((OvkApplication) getApplication()).androidAccount, null, null
                );
        } finally {
            SharedPreferences.Editor editor = getInstancePreferenceEditor();
            editor.clear();
            editor.commit();
        }
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

    private void setErrorPage(Bundle data, String icon, int reason, boolean showRetry) {
        if(!(selectedFragment instanceof MainSettingsFragment)) {
            mProgressLayout.setVisibility(View.GONE);
            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            mErrorLayout.setVisibility(View.VISIBLE);
            mErrorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
            mErrorLayout.setIcon(icon);
            mErrorLayout.setData(data);
            mErrorLayout.setRetryAction(mOpenVK.wrapper, mOpenVK.account);
            mErrorLayout.setReason(reason);
            mErrorLayout.setProgressLayout(mProgressLayout);
            Spinner news_spinner = ab_layout.findViewById(R.id.spinner);

            if (icon.equals("ovk")) {
                if(reason == HandlerMessages.NOTES_GET)
                    mErrorLayout.setTitle(
                            getResources().getString(R.string.no_notes));
                else if(reason == HandlerMessages.MESSAGES_CONVERSATIONS)
                    mErrorLayout.setTitle(
                            getResources().getString(R.string.no_messages));
                else if(reason == HandlerMessages.FRIENDS_GET)
                    mErrorLayout.setTitle(
                            getResources().getString(R.string.no_friends));
                else if(reason == HandlerMessages.GROUPS_GET)
                    mErrorLayout.setTitle(
                            getResources().getString(R.string.no_groups));
                else
                    mErrorLayout.setTitle(
                            news_spinner.getSelectedItemPosition() == 0 ?
                                    getResources().getString(R.string.local_newsfeed_no_posts) :
                                    getResources().getString(R.string.no_news)
                    );

            } else {
                mErrorLayout.setTitle(getResources().getString(R.string.err_text));
            }
            if (!showRetry) {
                mErrorLayout.hideRetryButton();
            }
            mProgressLayout.setVisibility(View.GONE);
            mErrorLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("CommitTransaction")
    public void openAccountProfile() {
        try {
            if (!((OvkApplication) getApplicationContext()).isTablet) {
                if (mMenu == null)
                    mMenu = new SlidingMenu(this);

                if(mMenu.isMenuShowing())
                    mMenu.toggle(mMainMenuAnimate);
            }

            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            ft = getSupportFragmentManager().beginTransaction();
            mFragmNav.navigateTo("profile", ft);
            setActionBar("");
            setActionBarTitle(getResources().getString(R.string.profile));

            if(mOpenVK.users == null)
                mOpenVK.users = new Users();

            mOpenVK.users.getUser(mOpenVK.wrapper, mOpenVK.account.id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadMoreNews() {
        if(mOpenVK.newsfeed != null) {
            if(mOpenVK.newsfeed.next_from != null && mOpenVK.newsfeed.next_from.length() > 0) {
                mOpenVK.newsfeed.get(mOpenVK.wrapper, 25, mOpenVK.newsfeed.next_from);
            } else if (selectedFragment instanceof NewsfeedFragment) {
                NewsfeedFragment fragment = ((NewsfeedFragment) selectedFragment);
                if(fragment.getCount() > 2) {
                    int lastPostIndex = fragment.getCount() - 2;
                    WallPost post = fragment.getPost(lastPostIndex);
                    if(post != null)
                        mOpenVK.newsfeed.get(mOpenVK.wrapper, 25, String.valueOf(post.post_id));
                }
            }
        }
    }

    public void selectNewsSpinnerItem(int position) {
        Spinner spinner;
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

            if(position == 0)
                mOpenVK.newsfeed.get(mOpenVK.wrapper, 25);
            else
                mOpenVK.newsfeed.getGlobal(mOpenVK.wrapper, 25);

            findViewById(R.id.app_fragment).setVisibility(View.GONE);
            if(selectedFragment instanceof NewsfeedFragment)
                ((RecyclerView) selectedFragment.getView().findViewById(R.id.news_listview))
                    .scrollToPosition(0);
            mProgressLayout.setVisibility(View.VISIBLE);
        }
    }

    public NotificationManager getNotificationManager() {
        return mNotifMan;
    }

    @Override
    protected void onPause() {
        mInBackground = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mInBackground = false;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public float getSlidingMenuWidth() {
        if(mMenuLayout.getVisibility() == View.GONE)
            return 0;
        float width;

        width = mMenuLayout.getMeasuredWidth() * mMenu.getPercentOpen();
        float dp = getResources().getDisplayMetrics().scaledDensity;
        return width / dp;
    }

    public FragmentNavigator getFragmentNavigator() {
        return mFragmNav;
    }

    @Override
    public Fragment getSelectedFragment() {
        return selectedFragment;
    }

    public void applySlidingMenuAnimation() {
        String value = mGlobalPrefs.getString("mainMenuAnimation", "Contrast");
        switch (value) {
            case "Disabled":
                mMenu.setBehindScrollScale(0.0f);
                mMenu.setFadeEnabled(false);
                mMainMenuAnimate = false;
                break;
            case "Contrast":
                mMenu.setBehindScrollScale(0.25f);
                mMenu.setFadeEnabled(true);
                mMainMenuAnimate = true;
                break;
            default:
                mMenu.setBehindScrollScale(0.0f);
                mMenu.setFadeEnabled(false);
                mMainMenuAnimate = true;
                break;
        }
    }

    public void showContent(int state) {
        switch (state) {
            case 0:
                mErrorLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                break;
            case 1:
                mErrorLayout.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.GONE);
                break;
            case 2:
                mErrorLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                break;
        }
    }

    public ProgressLayout getProgressLayout() {
        return mProgressLayout;
    }
}
