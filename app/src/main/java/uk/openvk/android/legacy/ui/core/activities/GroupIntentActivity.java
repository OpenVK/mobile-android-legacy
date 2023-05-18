package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.ui.view.layouts.AboutGroupLayout;
import uk.openvk.android.legacy.ui.view.layouts.ErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.GroupHeader;
import uk.openvk.android.legacy.ui.view.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;
import uk.openvk.android.legacy.api.models.WallPost;
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

public class GroupIntentActivity extends TranslucentActivity {
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private String access_token;
    private Groups groups;
    private Wall wall;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private ScrollView groupScrollView;
    private Group group;
    private Account account;
    private String args;
    private Likes likes;
    private int item_pos;
    private int poll_answer;
    private Menu activity_menu;
    private ActionBar actionBar;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        setContentView(R.layout.group_page_layout);
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

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);

        final Uri uri = getIntent().getData();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d(OvkApplication.APP_TAG,
                        String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };

        if(activity_menu == null) {
            android.support.v7.widget.PopupMenu p  = new android.support.v7.widget.PopupMenu(this, null);
            activity_menu = p.getMenu();
            getMenuInflater().inflate(R.menu.group, activity_menu);
            onCreateOptionsMenu(activity_menu);
        }

        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                account = new Account(this);
                likes = new Likes();
                ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                ovk_api.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                        global_prefs.getString("proxy_address", ""));
                ovk_api.setServer(instance_prefs.getString("server", ""));
                ovk_api.setAccessToken(access_token);
                account.getProfileInfo(ovk_api);
                if (path.startsWith("openvk://group/")) {
                    args = path.substring("openvk://group/".length());
                    downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
                    groups = new Groups();
                    wall = new Wall();
                    installLayouts();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
        ((WallLayout) findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(activity_menu != null) {
            activity_menu.clear();
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group, menu);
        activity_menu = menu;
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        if(item.getItemId() == R.id.newpost) {
            openNewPostActivity();
        } else if(item.getItemId() == R.id.leave_group) {
            if(group != null) {
                if (group.is_member > 0) {
                    group.leave(ovk_api);
                } else {
                    group.join(ovk_api);
                }
            }
        } else if(item.getItemId() == R.id.copy_link) {
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(String.format("http://%s/club%s", instance_prefs.getString("server", ""), group.id));
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("OpenVK User URL",
                        String.format("http://%s/club%s", instance_prefs.getString("server", ""), group.id));
                clipboard.setPrimaryClip(clip);
            }
        } else if(item.getItemId() == R.id.open_in_browser) {
            String user_url = String.format("http://%s/club%s", instance_prefs.getString("server", ""), group.id);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(user_url));
            startActivity(i);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ((WallLayout) findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void createActionPopupMenu(final Menu menu) {
        final View menu_container = (View) getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
        final PopupWindow popupMenu = new PopupWindow(menu_container, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popupMenu.setOutsideTouchable(true);
        popupMenu.setFocusable(true);
        final ActionBar actionBar = findViewById(R.id.actionbar);
    }

    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        groupScrollView = (ScrollView) findViewById(R.id.group_scrollview);
        groupScrollView.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.group));
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
            actionBar.setTitle(getResources().getString(R.string.group));
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                if (args.startsWith("club")) {
                    try {
                        groups.getGroupByID(ovk_api, Integer.parseInt(args.substring(4)));
                    } catch (Exception ex) {
                        groups.search(ovk_api, args);
                    }
                } else {
                    groups.search(ovk_api, args);
                }
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ActionBar actionBar = findViewById(R.id.actionbar);
                    createActionPopupMenu(activity_menu);
                }
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                (selector.findViewById(R.id.profile_wall_post_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNewPostActivity();
                    }
                });
                selector.setToGroup();
            } else if (message == HandlerMessages.GROUPS_GET_BY_ID) {
                groups.parse(data.getString("response"));
                group = groups.getList().get(0);
                updateLayout(group);
                progressLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
                setJoinButtonListener(group.id);
                group.downloadAvatar(downloadManager, global_prefs.getString("photos_quality", ""));
                wall.get(ovk_api, -group.id, 50);
                if(group.is_member > 0) {
                    findViewById(R.id.join_to_comm).setVisibility(View.GONE);
                    if(activity_menu != null) {
                        activity_menu.findItem(R.id.leave_group).setTitle(R.string.leave_group);
                    }
                } else {
                    findViewById(R.id.join_to_comm).setVisibility(View.VISIBLE);
                    if(activity_menu != null) {
                        activity_menu.findItem(R.id.leave_group).setTitle(R.string.join_group);
                    }
                }
                for (int i = 0; i < activity_menu.size(); i++) {
                    activity_menu.getItem(i).setVisible(true);
                }
            } else if (message == HandlerMessages.GROUPS_SEARCH) {
                groups.parseSearch(data.getString("response"));
                groups.getGroupByID(ovk_api, groups.getList().get(0).id);
            } else if (message == HandlerMessages.GROUPS_JOIN) {
                Button join_btn = findViewById(R.id.join_to_comm);
                join_btn.setText(R.string.leave_group);
                group.is_member = 1;
            } else if (message == HandlerMessages.GROUPS_LEAVE) {
                Button join_btn = findViewById(R.id.join_to_comm);
                join_btn.setText(R.string.join_group);
                group.is_member = 0;
            } else if (message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
            } else if (message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                loadAvatar();
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager,  global_prefs.getString("photos_quality", ""),
                        data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.showNewPostIcon();
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) findViewById(R.id.wall_layout)).loadAvatars();
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
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
                        ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                    }
                }
            } else if(message == HandlerMessages.POLL_DELETE_VOTE) {
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
                        ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
                    }
                }
            } else if (message == HandlerMessages.NO_INTERNET_CONNECTION
                    || message == HandlerMessages.INSTANCE_UNAVAILABLE
                    || message == HandlerMessages.INVALID_JSON_RESPONSE
                    || message == HandlerMessages.CONNECTION_TIMEOUT ||
                    message == HandlerMessages.INTERNAL_ERROR) {
                if (data.getString("method").equals("Wall.get")) {
                    ((WallErrorLayout) findViewById(R.id.wall_error_layout)).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.err_text),
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setJoinButtonListener(long id) {
        View aboutGroup = findViewById(R.id.about_group_ll);
        float smallestWidth = Global.getSmalledWidth(getWindowManager());
        if(((OvkApplication)getApplicationContext()).isTablet && smallestWidth >= 800) {
            final ImageButton join_btn = ((ImageButton) findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api);
                    } else {
                        group.join(ovk_api);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
            } else {
                join_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
            }
            join_btn.setVisibility(View.VISIBLE);
        } else {
            final Button join_btn = ((Button) findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api);
                    } else {
                        group.join(ovk_api);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setText(R.string.leave_group);
            } else {
                join_btn.setText(R.string.join_group);
            }
            join_btn.setVisibility(View.VISIBLE);
        }
    }


    private void updateLayout(Group group) {
        GroupHeader header = (GroupHeader) findViewById(R.id.group_header);
        header.setProfileName(String.format("%s  ", group.name));
        header.setVerified(group.verified, this);
        ((ProfileCounterLayout) findViewById(R.id.members_counter)).setCounter(group.members_count,
                Arrays.asList(getResources().getStringArray(R.array.profile_members)).get(2), "");
        ((AboutGroupLayout) findViewById(R.id.about_group_layout)).setGroupInfo(group.description, group.site);
        header.findViewById(R.id.profile_head_highlight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float smallestWidth = Global.getSmalledWidth(getWindowManager());
                if(((OvkApplication)getApplicationContext()).isTablet && smallestWidth >= 800) {
                    View aboutGroup = findViewById(R.id.about_group_ll);
                    if (aboutGroup.getVisibility() == View.GONE) {
                        aboutGroup.setVisibility(View.VISIBLE);
                    } else {
                        aboutGroup.setVisibility(View.GONE);
                    }
                } else {
                    View aboutGroup = findViewById(R.id.about_group_layout);
                    if (aboutGroup.getVisibility() == View.GONE) {
                        aboutGroup.setVisibility(View.VISIBLE);
                    } else {
                        aboutGroup.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void loadAvatar() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/group_avatars/avatar_%s",
                getCacheDir(), group.id), options);
        if (bitmap != null) {
            group.avatar = bitmap;
        } else if(group.avatar_msize_url.length() > 0 || group.avatar_hsize_url.length() > 0
                || group.avatar_osize_url.length() > 0) {
            group.avatar = null;
        } else {
            group.avatar = null;
        }
        if(group.avatar != null) ((ImageView) findViewById(R.id.profile_photo)).setImageBitmap(group.avatar);
    }

    public void hideSelectedItemBackground(int position) {
        ((ListView) findViewById(R.id.groups_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showGroup(int position) {
        String url = "openvk://group/" + "id" + groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openWallComments(int position, View view) {
        if(account != null) {
            WallPost item;
            Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
            item = wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
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
                        }
                    }
                }
                intent.putExtra("contains_poll", contains_poll);
                if (item.repost != null) {
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
    }

    private void openNewPostActivity() {
        try {
            Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
            intent.putExtra("owner_id", -group.id);
            intent.putExtra("account_id", account.id);
            intent.putExtra("account_first_name", group.name);
            startActivity(intent);
        } catch (Exception ex) {

        }
    }

    public void addLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) findViewById(R.id.wall_layout));
        item = wall.getWallItems().get(position);
        wallLayout.select(position, "likes", "add");
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) findViewById(R.id.wall_layout));
        item = wall.getWallItems().get(position);
        wallLayout.select(0, "likes", "delete");
        likes.delete(ovk_api, item.owner_id, item.post_id, position);
    }

    public void showAuthorPage(int position) {
        WallPost item;
        item = wall.getWallItems().get(position);
        if(item.author_id != -group.id) {
            if (item.author_id < 0) {
                String url = "openvk://group/" + "id" + -item.author_id;
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
    }

    public void voteInPoll(int item_pos, int answer) {
        this.item_pos = item_pos;
        this.poll_answer = answer;
        WallPost item = wall.getWallItems().get(item_pos);
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if (item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment)
                        item.attachments.get(attachment_index).getContent());
                pollAttachment.user_votes = 1;
                if (!pollAttachment.answers.get(answer).is_voted) {
                    pollAttachment.answers.get(answer).is_voted = true;
                    pollAttachment.answers.get(answer).votes = pollAttachment.answers.get(answer).votes + 1;
                }
                wall.getWallItems().set(item_pos, item);
                pollAttachment.vote(ovk_api, pollAttachment.answers.get(answer).id);
            }
        }
    }

    public void removeVoteInPoll(int item_pos) {
        this.item_pos = item_pos;
        WallPost item = wall.getWallItems().get(item_pos);
        for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
            if(item.attachments.get(attachment_index).type.equals("poll")) {
                PollAttachment pollAttachment = ((PollAttachment)
                        item.attachments.get(attachment_index).getContent());
                for (int i = 0; i < pollAttachment.answers.size(); i++) {
                    if (pollAttachment.answers.get(i).is_voted) {
                        pollAttachment.answers.get(i).is_voted = false;
                        pollAttachment.answers.get(i).votes = pollAttachment.answers.get(i).votes - 1;
                    }
                }
                pollAttachment.user_votes = 0;
                wall.getWallItems().set(item_pos, item);
                pollAttachment.unvote(ovk_api);
            }
        }
    }

    public void openWallRepostComments(int position, View view) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        item = wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
            intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
            intent.putExtra("author_name", String.format("%s %s", account.first_name, account.last_name));
            intent.putExtra("author_id", account.id);
            intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
            intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
            intent.putExtra("post_info", item.repost.newsfeed_item.info);
            intent.putExtra("post_text", item.repost.newsfeed_item.text);
            intent.putExtra("post_likes", item.repost.newsfeed_item.counters.likes);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void viewPhotoAttachment(int position) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        item = wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("local_photo_addr",
                    String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getCacheDir(),
                    item.owner_id, item.post_id));
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

    public void repost(int position) {
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
                                    String msg_text =
                                            ((EditText)repost_view.findViewById(R.id.text_edit))
                                                    .getText().toString();
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
