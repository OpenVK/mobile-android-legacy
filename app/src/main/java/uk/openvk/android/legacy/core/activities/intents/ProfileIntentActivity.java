package uk.openvk.android.legacy.core.activities.intents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Poll;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.ProfileFragment;
import uk.openvk.android.legacy.ui.views.ErrorLayout;
import uk.openvk.android.legacy.ui.views.ProfileHeader;
import uk.openvk.android.legacy.ui.views.ProgressLayout;
import uk.openvk.android.legacy.ui.views.WallLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
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

@SuppressWarnings("ConstantConditions")
public class ProfileIntentActivity extends NetworkFragmentActivity {

    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    public ProfileFragment profileFragment;
    private String access_token;
    public User user;
    private String args;
    private int item_pos;
    private int poll_answer;
    private Menu activity_menu;
    private ActionBar actionBar;
    private FragmentTransaction ft;
    private android.support.v7.widget.PopupMenu popup_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        installLayouts();
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        user = new User();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            access_token = instance_prefs.getString("access_token", "");
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }

        final Uri uri = intent.getData();

        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                ovk_api.account.getProfileInfo(ovk_api.wrapper);
                args = Global.getUrlArguments(path);
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout)).adjustLayoutSize(
                getResources().getConfiguration().orientation);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @SuppressLint("CommitTransaction")
    private void installLayouts() {
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        profileFragment = new ProfileFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, profileFragment, "profile");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.show(profileFragment);
        ft.commit();
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
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
            actionBar.setTitle(getResources().getString(R.string.profile));
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
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
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
            if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if(args.startsWith("id")) {
                    ovk_api.users.getUser(ovk_api.wrapper, Integer.parseInt(args.substring(2)));
                } else {
                    ovk_api.users.search(ovk_api.wrapper, args);
                }
            } else if (message == HandlerMessages.USERS_GET) {
                ovk_api.user = user;
                profileFragment.loadAPIData(this, ovk_api, getWindowManager());
                ((ProfileHeader) findViewById(R.id.profile_header)).setAvatarPlaceholder("common_user");
                progressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = status;
                    activity_menu.getItem(0).setTitle(R.string.profile_friend_cancel);
                } else if(status == 2) {
                    user.friends_status = 3;
                }
                profileFragment.setAddToFriendsButtonListener(this, user.id, user);
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                activity_menu.getItem(0).setTitle(R.string.profile_add_friend);
                profileFragment.setAddToFriendsButtonListener(this, user.id, user);
            } else if(message == HandlerMessages.USERS_SEARCH) {
                ovk_api.users.getUser(ovk_api.wrapper, ovk_api.users.getList().get(0).id);
            } else if (message == HandlerMessages.WALL_GET ||
                    message == HandlerMessages.WALL_GET_MORE) {
                profileFragment.loadWall(this, ovk_api);
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .loadAvatars();
            } else if(message == HandlerMessages.VIDEO_THUMBNAILS) {
                profileFragment.refreshWallAdapter();
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                profileFragment.setCounter(user, "friends",  ovk_api.friends.count);
            } else if(message == HandlerMessages.LIKES_ADD) {
                ovk_api.likes.parse(data.getString("response"));
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .select(ovk_api.likes.position, "likes", 1);
            } else if(message == HandlerMessages.LIKES_DELETE) {
                ovk_api.likes.parse(data.getString("response"));
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .select(ovk_api.likes.position, "likes", 0);
            } else if(message == HandlerMessages.POLL_ADD_VOTE ||
                    message == HandlerMessages.POLL_DELETE_VOTE) {
                boolean addVote = message == HandlerMessages.POLL_ADD_VOTE
                        || message == HandlerMessages.POLL_DELETE_VOTE;
                WallPost item = ovk_api.wall.getWallItems().get(item_pos);
                if(item != null) {
                    for (int attachment_index = 0; attachment_index < item.attachments.size();
                         attachment_index++) {
                        if (item.attachments.get(attachment_index).type.equals("poll")) {
                            Poll poll = ((Poll) item.attachments.get(attachment_index));
                            PollAnswer answer = poll.answers.get(poll_answer);
                            poll.user_votes = addVote ? 0 : 1;
                            answer.is_voted = addVote;
                            poll.answers.set(poll_answer, answer);
                            ovk_api.wall.getWallItems().set(item_pos, item);
                            ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                                    .updateItem(item, item_pos);
                        }
                    }
                }
            } else if (message < 0) {
                try {
                    if (data.containsKey("method")) {
                        String method = data.getString("method");
                        if (Global.checkShowErrorLayout(method, profileFragment)) {
                            setErrorPage(data, message);
                        } else {
                            if (data.getString("method").equals("Wall.get")) {
                                profileFragment.getView()
                                        .findViewById(R.id.wall_error_layout)
                                        .setVisibility(View.VISIBLE);
                                profileFragment.getWallSelector()
                                        .findViewById(R.id.profile_wall_progress)
                                        .setVisibility(View.GONE);
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.err_text),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } catch (Exception ex) {
                    setErrorPage(data, HandlerMessages.INVALID_JSON_RESPONSE);
                }
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                switch (global_prefs.getString("photos_quality", "")) {
                    case "medium":
                        if (user.avatar_msize_url.length() > 0) {
                            profileFragment.loadAvatar(
                                    user, global_prefs.getString("photos_quality", ""));
                        }
                        break;
                    case "high":
                        if (user.avatar_hsize_url.length() > 0) {
                            profileFragment.loadAvatar(
                                    user, global_prefs.getString("photos_quality", ""));
                        }
                        break;
                    default:
                        if (user.avatar_osize_url.length() > 0) {
                            profileFragment.loadAvatar(
                                    user, global_prefs.getString("photos_quality", ""));
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorPage(data, HandlerMessages.INVALID_JSON_RESPONSE);
        }
    }

    private void setErrorPage(Bundle data, int reason) {
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
        errorLayout.setData(data);
        errorLayout.setRetryAction(ovk_api.wrapper, ovk_api.account);
        errorLayout.setReason(reason);
        errorLayout.setProgressLayout(progressLayout);
        errorLayout.setTitle(getResources().getString(R.string.err_text));
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }
}