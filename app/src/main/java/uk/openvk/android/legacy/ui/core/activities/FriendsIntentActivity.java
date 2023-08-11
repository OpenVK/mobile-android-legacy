package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.models.Friends;
import uk.openvk.android.legacy.api.models.Users;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentFragmentActivity;
import uk.openvk.android.legacy.ui.view.layouts.ErrorLayout;
import uk.openvk.android.legacy.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.view.layouts.TabSelector;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuItem;
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

public class FriendsIntentActivity extends TranslucentFragmentActivity {

    private ArrayList<SlidingMenuItem> slidingMenuArray;
    public OpenVKAPI ovk_api;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private FriendsFragment friendsFragment;
    private String access_token;
    private int user_id = 0;
    private FragmentTransaction ft;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        global_prefs_editor = global_prefs.edit();
        setContentView(R.layout.activity_intent);
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
                final Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d(OvkApplication.APP_TAG,
                        String.format("Handling API message: %s", message.what));
                if(message.what == HandlerMessages.PARSE_JSON){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ovk_api.wrapper.parseJSONData(data, FriendsIntentActivity.this);
                        }
                    }).start();
                } else {
                    receiveState(message.what, data);
                }
            }
        };

        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                String args = Global.getUrlArguments(path);
                if(args.length() > 0) {
                    ovk_api = new OpenVKAPI(this, global_prefs, instance_prefs);
                    ovk_api.users = new Users();
                    ovk_api.friends = new Friends();
                    if(args.startsWith("id")) {
                        try {
                            user_id = Integer.parseInt(args.substring(2));
                            ovk_api.friends.get(ovk_api.wrapper,
                                    Integer.parseInt(args.substring(2)), 25, "friends_list");
                        } catch (Exception ex) {
                            ovk_api.users.search(ovk_api.wrapper, args);
                        }
                    } else {
                        ovk_api.users.search(ovk_api.wrapper, args);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @SuppressWarnings("ConstantConditions")
    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        friendsFragment = new FriendsFragment();

        friendsFragment.setActivityContext(this);

        progressLayout.setVisibility(View.VISIBLE);
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, friendsFragment, "friends");
        ft.commit();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.friends));
                    if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                        getActionBar().setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.bg_actionbar_gray));
                    } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                        getActionBar().setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.bg_actionbar_black));
                    }
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
            actionBar.setTitle(getResources().getString(R.string.friends));
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
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
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                progressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                friendsFragment.createAdapter(this, friendsList, "friends");
                try {
                    ((TabSelector) friendsFragment.getView().findViewById(R.id.selector))
                            .setTabTitle(0, String.format("%s (%s)",
                                    getResources().getString(R.string.friends), ovk_api.friends.count));
                } catch (Exception ignored) {

                }
                friendsFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsFragment.loadAvatars();
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                int old_friends_size = ovk_api.friends.getFriends().size();
                ovk_api.friends.parse(data.getString("response"), ovk_api.dlman, true, false);
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                friendsFragment.createAdapter(this, friendsList, "friends");
                if(old_friends_size == ovk_api.friends.getFriends().size()) {
                    friendsFragment.setScrollingPositions(this, false);
                } else {
                    friendsFragment.setScrollingPositions(this, true);
                }
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION
                    || message == HandlerMessages.INVALID_JSON_RESPONSE
                    || message == HandlerMessages.CONNECTION_TIMEOUT ||
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

    public void loadMoreFriends() {
        if(ovk_api.friends != null) {
            ovk_api.friends.get(ovk_api.wrapper, user_id, 25, ovk_api.friends.offset);
        }
    }
}
