package uk.openvk.android.legacy.ui.core.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.models.Groups;
import uk.openvk.android.legacy.api.models.Likes;
import uk.openvk.android.legacy.api.models.Wall;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
import uk.openvk.android.legacy.ui.core.listeners.OnNestedScrollListener;
import uk.openvk.android.legacy.ui.core.listeners.OnScrollListener;
import uk.openvk.android.legacy.ui.view.InfinityNestedScrollView;
import uk.openvk.android.legacy.ui.view.InfinityScrollView;
import uk.openvk.android.legacy.ui.view.layouts.AboutGroupLayout;
import uk.openvk.android.legacy.ui.view.layouts.ErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.GroupHeader;
import uk.openvk.android.legacy.ui.view.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.ui.view.layouts.ProfileWallSelector;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallErrorLayout;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;
import uk.openvk.android.legacy.api.entities.WallPost;
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
    public OpenVKAPI ovk_api;
    private DownloadManager downloadManager;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private String access_token;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private InfinityNestedScrollView groupNestedScrollView;
    private InfinityScrollView groupScrollView;
    private Group group;
    private String args;
    private int item_pos;
    private int poll_answer;
    private Menu activity_menu;
    private ActionBar actionBar;
    private android.support.v7.widget.PopupMenu popup_menu;
    private boolean showExtended;
    private boolean loading_more_posts;
    private String instance;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        global_prefs_editor = global_prefs.edit();
        setContentView(R.layout.layout_group_page);
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

        instance = instance_prefs.getString("server", "");

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);

        final Uri uri = getIntent().getData();

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
                            ovk_api.wrapper.parseJSONData(data, GroupIntentActivity.this);
                        }
                    }).start();
                } else {
                    receiveState(message.what, data);
                }
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
                ovk_api = new OpenVKAPI(this, global_prefs, instance_prefs);
                ovk_api.account.getProfileInfo(ovk_api.wrapper);
                args = Global.getUrlArguments(path);
                if(args.length() > 0) {
                    downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true),
                            global_prefs.getBoolean("legacyHttpClient", false));
                    downloadManager.setInstance(PreferenceManager.getDefaultSharedPreferences(this).getString("current_instance", ""));
                    downloadManager.setForceCaching(global_prefs.getBoolean("forcedCaching", true));
                    installLayouts();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
        ((WallLayout) findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().
                getConfiguration().orientation);
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
                    group.leave(ovk_api.wrapper);
                } else {
                    group.join(ovk_api.wrapper);
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
        ((WallLayout) findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().
                getConfiguration().orientation);
        super.onConfigurationChanged(newConfig);
    }

    private void createActionPopupMenu(final Menu menu, String where, boolean enable) {
        if(popup_menu == null) {
            popup_menu = new android.support.v7.widget.PopupMenu(this, null);
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionBar = findViewById(R.id.actionbar);
            if(menu.size() == 0) {
                if(where.equals("group")) {
                    getMenuInflater().inflate(R.menu.group, menu);
                }
            }
            if (enable) {
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

    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        groupScrollView = (InfinityScrollView) findViewById(R.id.group_scrollview);
        groupScrollView.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
            findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            findViewById(R.id.about_group_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v3));
            findViewById(R.id.join_to_comm)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_gray));
        } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
            findViewById(R.id.profile_ext_header)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            findViewById(R.id.about_group_layout)
                    .setBackgroundColor(getResources().getColor(R.color.color_gray_v2));
            findViewById(R.id.join_to_comm)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_light_black));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.group));
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
            if(global_prefs.getString("uiTheme", "blue").equals("Gray")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            } else if(global_prefs.getString("uiTheme", "blue").equals("Black")) {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
            } else {
                actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
            }
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
            if(popup_menu == null) {
                popup_menu = new android.support.v7.widget.PopupMenu(this, null);
            }
            createActionPopupMenu(popup_menu.getMenu(), "group", true);
            actionBar.setTitle(getResources().getString(R.string.group));
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if (args.startsWith("club")) {
                    try {
                        ovk_api.groups.getGroupByID(ovk_api.wrapper, Integer.parseInt(args.substring(4)));
                    } catch (Exception ex) {
                        ovk_api.groups.search(ovk_api.wrapper, args);
                    }
                } else {
                    ovk_api.groups.search(ovk_api.wrapper, args);
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
                group = ovk_api.groups.getList().get(0);
                updateLayout(group);
                progressLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
                setJoinButtonListener(group.id);
                group.downloadAvatar(downloadManager, global_prefs.getString("photos_quality", ""));
                ovk_api.wall.get(ovk_api.wrapper, -group.id, 25);
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
                if(activity_menu != null) {
                    for (int i = 0; i < activity_menu.size(); i++) {
                        activity_menu.getItem(i).setVisible(true);
                    }
                }
            } else if (message == HandlerMessages.GROUPS_SEARCH) {
                ovk_api.groups.getGroupByID(ovk_api.wrapper, ovk_api.groups.getList().get(0).id);
            } else if (message == HandlerMessages.GROUPS_JOIN) {
                Button join_btn = findViewById(R.id.join_to_comm);
                join_btn.setText(R.string.leave_group);
                group.is_member = 1;
            } else if (message == HandlerMessages.GROUPS_LEAVE) {
                Button join_btn = findViewById(R.id.join_to_comm);
                join_btn.setText(R.string.join_group);
                group.is_member = 0;
            } else if (message == HandlerMessages.LIKES_ADD) {
                ovk_api.likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(ovk_api.likes.position, "likes", 1);
            } else if (message == HandlerMessages.LIKES_DELETE) {
                ovk_api.likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(ovk_api.likes.position, "likes", 0);
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                loadAvatar();
            } else if (message == HandlerMessages.WALL_GET) {
                ((WallLayout) findViewById(R.id.wall_layout)).createAdapter(this, ovk_api.wall.getWallItems());
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openNewPostActivity();
                    }
                });
                selector.showNewPostIcon();
                loading_more_posts = true;
                setScrollingPositions(this, false, true);
            } else if (message == HandlerMessages.WALL_GET_MORE) {
                ((WallLayout) findViewById(R.id.wall_layout))
                        .createAdapter(this, ovk_api.wall.getWallItems());
                ProfileWallSelector selector = findViewById(R.id.wall_selector);
                selector.findViewById(R.id.profile_wall_post_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openNewPostActivity();
                    }
                });
                selector.showNewPostIcon();
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) findViewById(R.id.wall_layout)).loadAvatars();
            } else if(message == HandlerMessages.VIDEO_THUMBNAILS) {
                ((WallLayout) findViewById(R.id.wall_layout)).refreshAdapter();
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
                WallPost item = ovk_api.wall.getWallItems().get(item_pos);
                for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
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
                for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment poll = ((PollAttachment) item.attachments.get(attachment_index).getContent());
                        poll.user_votes = 0;
                        PollAnswer answer = poll.answers.get(poll_answer);
                        answer.is_voted = false;
                        poll.answers.set(poll_answer, answer);
                        item.attachments.get(attachment_index).setContent(poll);
                        ovk_api.wall.getWallItems().set(item_pos, item);
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
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
                    }
                }
            });
            if(group.is_member > 0) {
                join_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_cancel));
            } else {
                join_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_ab_add));
            }
            join_btn.setVisibility(View.VISIBLE);
        } else if(((OvkApplication)getApplicationContext()).isTablet &&
                smallestWidth < 800) {
            final ImageButton join_btn = ((ImageButton) findViewById(R.id.join_to_comm));
            join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(group.is_member > 0) {
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
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
                        group.leave(ovk_api.wrapper);
                    } else {
                        group.join(ovk_api.wrapper);
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

    public void toggleExtendedInfo() {
        this.showExtended = !this.showExtended;
        View arrow = (findViewById(R.id.group_header)).findViewById(R.id.profile_expand);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            float[] fArr = new float[2];
            fArr[0] = this.showExtended ? 0 : -180;
            fArr[1] = this.showExtended ? -180 : 0;
            ObjectAnimator.ofFloat(arrow, "rotation", fArr).setDuration(300L).start();
        } else {
            RotateAnimation anim = new RotateAnimation(this.showExtended ? 0 : -180,
                    this.showExtended ? -180 : 0, 1, 0.5f, 1, 0.5f);
            anim.setFillAfter(true);
            anim.setDuration(300L);
            arrow.startAnimation(anim);
        }
    }


    private void updateLayout(final Group group) {
        GroupHeader header = (GroupHeader) findViewById(R.id.group_header);
        header.setProfileName(String.format("%s  ", group.name));
        header.setVerified(group.verified, this);
        ((ProfileCounterLayout) findViewById(R.id.members_counter)).setCounter(group.members_count,
                Global.getPluralQuantityString(getApplicationContext(),
                        R.plurals.profile_members, (int) group.members_count), "");
        ((ProfileCounterLayout) findViewById(R.id.members_counter)).setOnCounterClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(GroupIntentActivity.this, GroupMembersActivity.class);
                        i.putExtra("group_id", GroupIntentActivity.this.group.id);
                        startActivity(i);
                    }
                });
        ((AboutGroupLayout) findViewById(R.id.about_group_layout)).setGroupInfo(group.description, group.site);
        header.findViewById(R.id.profile_head_highlight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float smallestWidth = Global.getSmalledWidth(getWindowManager());
                toggleExtendedInfo();
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
        Bitmap bitmap = BitmapFactory.decodeFile(
                String.format("%s/%s/photos_cache/group_avatars/avatar_%s",
                        getCacheDir(), instance, group.id), options);
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
        String url = "openvk://group/" + "id" + ovk_api.groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage(getPackageName());
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openWallComments(int position, View view) {
        if(ovk_api.account != null) {
            WallPost item;
            Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
            item = ovk_api.wall.getWallItems().get(position);
            intent.putExtra("where", "wall");
            try {
                intent.putExtra("post_id", item.post_id);
                intent.putExtra("owner_id", item.owner_id);
                intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                        ovk_api.account.last_name));
                intent.putExtra("account_id", ovk_api.account.id);
                intent.putExtra("post_author_id", item.author_id);
                intent.putExtra("post_author_name", item.name);
                intent.putExtra("post_json", item.getJSONString());
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
            intent.putExtra("account_id", ovk_api.account.id);
            intent.putExtra("account_first_name", group.name);
            startActivity(intent);
        } catch (Exception ignored) {

        }
    }

    public void addLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) findViewById(R.id.wall_layout));
        item = ovk_api.wall.getWallItems().get(position);
        wallLayout.select(position, "likes", "add");
        ovk_api.likes.add(ovk_api.wrapper, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        WallLayout wallLayout = ((WallLayout) findViewById(R.id.wall_layout));
        item = ovk_api.wall.getWallItems().get(position);
        wallLayout.select(0, "likes", "delete");
        ovk_api.likes.delete(ovk_api.wrapper, item.owner_id, item.post_id, position);
    }

    public void showAuthorPage(int position) {
        WallPost item;
        item = ovk_api.wall.getWallItems().get(position);
        if(item.author_id != -group.id) {
            if (item.author_id < 0) {
                String url = "openvk://group/" + "id" + -item.author_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage(getPackageName());
                i.setData(Uri.parse(url));
                startActivity(i);
            } else {
                String url = "openvk://profile/" + "id" + item.author_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage(getPackageName());
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    }

    public void voteInPoll(int item_pos, int answer) {
        try {
            this.item_pos = item_pos;
            this.poll_answer = answer;
            WallPost item = ovk_api.wall.getWallItems().get(item_pos);
            for (int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                if (item.attachments.get(attachment_index).type.equals("poll")) {
                    PollAttachment pollAttachment = ((PollAttachment)
                            item.attachments.get(attachment_index).getContent());
                    pollAttachment.user_votes = 1;
                    if (!pollAttachment.answers.get(answer).is_voted) {
                        pollAttachment.answers.get(answer).is_voted = true;
                        pollAttachment.answers.get(answer).votes = pollAttachment.answers.get(answer).votes + 1;
                    }
                    ovk_api.wall.getWallItems().set(item_pos, item);
                    pollAttachment.vote(ovk_api.wrapper, pollAttachment.answers.get(answer).id);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void removeVoteInPoll(int item_pos) {
        try {
            this.item_pos = item_pos;
            WallPost item = ovk_api.wall.getWallItems().get(item_pos);
            for (int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                if (item.attachments.get(attachment_index).type.equals("poll")) {
                    PollAttachment pollAttachment = ((PollAttachment)
                            item.attachments.get(attachment_index).getContent());
                    for (int i = 0; i < pollAttachment.answers.size(); i++) {
                        if (pollAttachment.answers.get(i).is_voted) {
                            pollAttachment.answers.get(i).is_voted = false;
                            pollAttachment.answers.get(i).votes = pollAttachment.answers.get(i).votes - 1;
                        }
                    }
                    pollAttachment.user_votes = 0;
                    ovk_api.wall.getWallItems().set(item_pos, item);
                    pollAttachment.unvote(ovk_api.wrapper);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void openWallRepostComments(int position, View view) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), WallPostActivity.class);
        item = ovk_api.wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("post_id", item.post_id);
            intent.putExtra("owner_id", item.owner_id);
            intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                    ovk_api.account.last_name));
            intent.putExtra("account_id", ovk_api.account.id);
            intent.putExtra("post_author_id", item.author_id);
            intent.putExtra("post_author_name", item.name);
            intent.putExtra("post_json", item.getJSONString());
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void viewPhotoAttachment(int position) {
        WallPost item;
        Intent intent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
        item = ovk_api.wall.getWallItems().get(position);
        intent.putExtra("where", "wall");
        try {
            intent.putExtra("local_photo_addr",
                    String.format("%s/%s/wall_photo_attachments/wall_attachment_o%sp%s", getCacheDir(), instance, item.owner_id, item.post_id));
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
        final WallPost post = ovk_api.wall.getWallItems().get(position);
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
                                    ovk_api.wall.repost(ovk_api.wrapper, post.owner_id, post.post_id, msg_text);
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

    public void loadMoreWallPosts() {
        if(ovk_api.wall != null) {
            ovk_api.wall.get(ovk_api.wrapper, -group.id, 25, ovk_api.wall.next_from);
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean load_photos,
                                      final boolean infinity_scroll) {
        loading_more_posts = false;
        if(load_photos) {
            ((WallLayout) findViewById(R.id.wall_layout)).loadPhotos();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final InfinityScrollView scrollView = findViewById(R.id.group_scrollview);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if (ctx instanceof AppActivity) {
                                loading_more_posts = true;
                                ((AppActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof ProfileIntentActivity) {
                                ((ProfileIntentActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof GroupIntentActivity) {
                                ((GroupIntentActivity) ctx).loadMoreWallPosts();
                            }
                        }
                    }
                }
            });
        } else {
            final InfinityScrollView scrollView = findViewById(R.id.group_scrollview);
            scrollView.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScroll(InfinityScrollView infinityScrollView, int x, int y, int old_x, int old_y) {
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                    if (!loading_more_posts) {
                        if (diff == 0) {
                            if (ctx instanceof AppActivity) {
                                loading_more_posts = true;
                                ((AppActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof ProfileIntentActivity) {
                                ((ProfileIntentActivity) ctx).loadMoreWallPosts();
                            } else if(ctx instanceof GroupIntentActivity) {
                                ((GroupIntentActivity) ctx).loadMoreWallPosts();
                            }
                        }
                    }
                }
            });
        }
    }
}