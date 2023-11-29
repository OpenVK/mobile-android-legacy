package uk.openvk.android.legacy.ui.core.activities.intents;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.ui.core.activities.ConversationActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.view.layouts.ErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileHeader;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;
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
            if (extras == null) {
                access_token = instance_prefs.getString("access_token", "");
            } else {
                access_token = instance_prefs.getString("access_token", "");
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }

        FriendsFragment friendsFragment = new FriendsFragment();

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
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(activity_menu != null) {
            activity_menu.clear();
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        activity_menu = menu;
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        if(item.getItemId() == R.id.copy_link) {
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(String.format("http://%s/id%s",
                        instance_prefs.getString("server", ""), user.id));
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("OpenVK User URL",
                                String.format("http://%s/id%s",
                                        instance_prefs.getString("server", ""), user.id));
                clipboard.setPrimaryClip(clip);
            }
        } else if(item.getItemId() == R.id.open_in_browser) {
            String user_url = String.format("http://%s/id%s",
                    instance_prefs.getString("server", ""), user.id);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(user_url));
            startActivity(i);
        } else if(item.getItemId() == R.id.remove_friend) {
            if(user.friends_status > 1) {
                deleteFromFriends(user.id);
            } else {
                addToFriends(user.id);
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
                boolean isCurrentActivity = activityName.equals(getLocalClassName());
                if(!isCurrentActivity) {
                    return;
                }
            }
            if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if(args.startsWith("id")) {
                    try {
                       ovk_api.users.getUser(ovk_api.wrapper, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        ovk_api.users.search(ovk_api.wrapper, args);
                    }
                } else {
                    ovk_api.users.search(ovk_api.wrapper, args);
                }
            } else if (message == HandlerMessages.USERS_GET) {
                profileFragment.updateLayout(user, getWindowManager());
                progressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                profileFragment.setDMButtonListener(this, user.id, getWindowManager());
                profileFragment.setAddToFriendsButtonListener(this, user.id, user);
                if(user.id == ovk_api.account.id) {
                    profileFragment.hideHeaderButtons(this, getWindowManager());
                    try {
                        if(activity_menu != null) {
                            for (int i = 0; i < activity_menu.size(); i++) {
                                if (i > 0) {
                                    activity_menu.getItem(i).setVisible(true);
                                }
                            }
                            activity_menu.removeItem(0);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    ((ProfileHeader) findViewById(R.id.profile_header)).setAvatarPlaceholder("common_user");
                    popup_menu = new android.support.v7.widget.PopupMenu(this, null);
                    getMenuInflater().inflate(R.menu.profile, popup_menu.getMenu());
                    if (user.friends_status == 0) {
                        findViewById(R.id.add_to_friends).setVisibility(View.VISIBLE);
                        if(activity_menu != null) {
                            activity_menu.findItem(R.id.remove_friend).setTitle(R.string.profile_add_friend);
                        }
                        if(popup_menu.getMenu() != null) {
                            popup_menu.getMenu().findItem(R.id.remove_friend)
                                    .setTitle(R.string.profile_add_friend);
                        }
                    } else if (user.friends_status == 1) {
                        findViewById(R.id.add_to_friends).setVisibility(View.VISIBLE);
                        if(activity_menu != null) {
                            activity_menu.findItem(R.id.remove_friend).setTitle(R.string.profile_friend_cancel);
                        }
                        if(popup_menu.getMenu() != null) {
                            popup_menu.getMenu().findItem(R.id.remove_friend)
                                    .setTitle(R.string.profile_friend_cancel);
                        }
                    } else if (user.friends_status == 2) {
                        findViewById(R.id.add_to_friends).setVisibility(View.VISIBLE);
                        if(activity_menu != null) {
                            activity_menu.findItem(R.id.remove_friend).setTitle(R.string.profile_friend_accept);
                        }
                        if(popup_menu.getMenu() != null) {
                            popup_menu.getMenu().findItem(R.id.remove_friend)
                                    .setTitle(R.string.profile_friend_accept);
                        }
                    }
                    if(activity_menu != null) {
                        for (int i = 0; i < activity_menu.size(); i++) {
                            activity_menu.getItem(i).setVisible(true);
                        }
                    }
                }
                if(user.deactivated == null) {
                    user.downloadAvatar(ovk_api.dlman, global_prefs.getString("photos_quality", ""));
                    ovk_api.wall.get(ovk_api.wrapper, user.id, 25);
                    ovk_api.friends.get(ovk_api.wrapper, user.id, 10, "profile_counter");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        createActionPopupMenu(popup_menu.getMenu(), "account", true);
                    }
                } else {
                    profileFragment.hideHeaderButtons(this, getWindowManager());
                    if(activity_menu != null) {
                        activity_menu.getItem(0).setVisible(false);
                    }
                    if(popup_menu != null) {
                        popup_menu.getMenu().getItem(0).setVisible(false);
                    }
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        if(ovk_api.account.id == user.id) {
                            createActionPopupMenu(popup_menu.getMenu(), "account", true);
                        } else {
                            createActionPopupMenu(popup_menu.getMenu(), "profile", true);
                        }
                    }
                    profileFragment.hideTabSelector();
                    profileFragment.getHeader().hideExpandArrow();
                    if(user.deactivated.equals("banned")) {
                        if(user.ban_reason.length() > 0) {
                            ((TextView) findViewById(R.id.deactivated_info)).setText(
                                    String.format("%s\r\n%s: %s",
                                            getResources().getString(R.string.profile_inactive_banned),
                                            getResources().getString(R.string.reason), user.ban_reason
                                    )
                            );
                        } else {
                            ((TextView) findViewById(R.id.deactivated_info)).setText(
                                    getResources().getString(R.string.profile_inactive_banned)
                            );
                        }
                    }
                }
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
            } else if (message == HandlerMessages.WALL_GET) {
                if(ovk_api.wall.getWallItems().size() > 0) {
                    ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                            .createAdapter(this, ovk_api.wall.getWallItems());
                    profileFragment.loading_more_posts = true;
                    profileFragment.setScrollingPositions(this, false, true);
                } else {
                    WallErrorLayout wall_error = profileFragment.getView()
                            .findViewById(R.id.wall_error_layout);
                    wall_error.setErrorText(getResources().getString(R.string.no_news));
                    wall_error.setVisibility(View.VISIBLE);
                }
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        profileFragment.openNewPostActivity(user, ovk_api);
                    }
                });
                selector.showNewPostIcon();
            } else if (message == HandlerMessages.WALL_GET_MORE) {
                ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                        .createAdapter(this, ovk_api.wall.getWallItems());
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        profileFragment.openNewPostActivity(user, ovk_api);
                    }
                });
                selector.showNewPostIcon();
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
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
                WallPost item = ovk_api.wall.getWallItems().get(item_pos);
                for(int attachment_index = 0; attachment_index < item.attachments.size();
                    attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment)
                                item.attachments.get(attachment_index).getContent());
                        poll.user_votes = 0;
                        PollAnswer answer = poll.answers.get(poll_answer);
                        answer.is_voted = false;
                        poll.answers.set(poll_answer, answer);
                        item.attachments.get(attachment_index).setContent(poll);
                        ovk_api.wall.getWallItems().set(item_pos, item);
                        ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                    }
                }
            } else if(message == HandlerMessages.POLL_DELETE_VOTE) {
                WallPost item = ovk_api.wall.getWallItems().get(item_pos);
                for(int attachment_index = 0; attachment_index < item.attachments.size();
                    attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment) item.attachments
                                .get(attachment_index).getContent());
                        poll.user_votes = 0;
                        PollAnswer answer = poll.answers.get(poll_answer);
                        answer.is_voted = false;
                        poll.answers.set(poll_answer, answer);
                        item.attachments.get(attachment_index).setContent(poll);
                        ovk_api.wall.getWallItems().set(item_pos, item);
                        ((WallLayout) profileFragment.getView().findViewById(R.id.wall_layout))
                                .updateItem(item, item_pos);
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

    public void addToFriends(long user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.add(ovk_api.wrapper, user_id);
        }
    }

    public void deleteFromFriends(long user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.delete(ovk_api.wrapper, user_id);
        }
    }

    private void createActionPopupMenu(final Menu menu, String where, boolean enable) {
        if(popup_menu == null) {
            popup_menu = new android.support.v7.widget.PopupMenu(this, null);
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionBar = findViewById(R.id.actionbar);
            if(menu.size() == 0) {
                if (where.equals("account")) {
                    getMenuInflater().inflate(R.menu.profile, menu);
                    menu.getItem(0).setVisible(false);
                } else if (where.equals("profile")) {
                    getMenuInflater().inflate(R.menu.profile, menu);
                }
            }
            if (enable) {
                if(actionBar.getActionCount() > 0) {
                    actionBar.removeAllActions();
                }
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
    }

    public void repost(int position) {
        final WallPost post = ovk_api.wall.getWallItems().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> functions = new ArrayList<>();
        builder.setTitle(R.string.repost_dlg_title);
        functions.add(getResources().getString(R.string.repost_own_wall));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, functions);
        builder.setSingleChoiceItems(adapter, -1, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(functions.get(position).equals(getResources().getString(R.string.repost_own_wall))) {
                    Global.openRepostDialog(ProfileIntentActivity.this, ovk_api,
                            "own_wall", post);
                    dialog.dismiss();
                }
            }
        });
    }
}