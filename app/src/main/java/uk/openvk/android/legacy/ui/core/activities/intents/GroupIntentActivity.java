package uk.openvk.android.legacy.ui.core.activities.intents;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.ui.core.activities.GroupMembersActivity;
import uk.openvk.android.legacy.ui.core.activities.NewPostActivity;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.ui.core.activities.base.TranslucentActivity;
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

public class GroupIntentActivity extends NetworkFragmentActivity {
    public Handler handler;
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

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if(activity_menu == null) {
            android.support.v7.widget.PopupMenu p  =
                    new android.support.v7.widget.PopupMenu(this, null);
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
                args = Global.getUrlArguments(path);
                if(args.length() > 0) {
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
            Global.copyToClipboard(
                    this,
                    String.format("http://%s/club%s",
                            instance_prefs.getString("server", ""),
                            group.id)
            );
        } else if(item.getItemId() == R.id.open_in_browser) {
            String user_url = String.format("http://%s/club%s",
                    instance_prefs.getString("server", ""), group.id);
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

    @SuppressWarnings("ConstantConditions")
    private void createActionPopupMenu(final Menu menu) {
        if(popup_menu == null) {
            popup_menu = new android.support.v7.widget.PopupMenu(this, null);
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            actionBar = findViewById(R.id.actionbar);
            if(menu.size() == 0) {
                getMenuInflater().inflate(R.menu.group, menu);
            }
            ActionBar.PopupMenuAction action =
                    new ActionBar.PopupMenuAction(this, "", menu,
                            R.drawable.ic_overflow_holo_dark, new PopupMenu.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(dev.tinelix.retro_pm.MenuItem item) {
                            onMenuItemSelected(0, menu.getItem(item.getItemId()));
                        }
                    });
            actionBar.addAction(action);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void installLayouts() {
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        groupScrollView = findViewById(R.id.group_scrollview);
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
                    if(getActionBar() != null) {
                        getActionBar().setDisplayShowHomeEnabled(true);
                        getActionBar().setDisplayHomeAsUpEnabled(true);
                        getActionBar().setTitle(getResources().getString(R.string.group));
                    }
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
            createActionPopupMenu(popup_menu.getMenu());
            actionBar.setTitle(getResources().getString(R.string.group));
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
            } else if (message == HandlerMessages.GROUPS_GET_BY_ID
                    || message == HandlerMessages.GROUPS_SEARCH) {
                group = ovk_api.groups.getList().get(0);
                updateLayout(group);
                progressLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
                setJoinButtonListener(group.id);
                group.downloadAvatar(ovk_api.dlman, global_prefs.getString("photos_quality", ""));
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
                setScrollingPositions(this, false, -group.id);
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
                if (data.containsKey("method")) {
                    if ("Wall.get".equals(data.getString("method"))) {
                        ((WallErrorLayout) findViewById(R.id.wall_error_layout)).setVisibility(View.VISIBLE);
                    } else if("Users.get".equals(data.getString("method"))) {
                        setErrorPage(data, message);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.err_text),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    setErrorPage(data, message);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setErrorPage(Bundle data, int reason) {
        progressLayout.setVisibility(View.GONE);
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
            final Button join_btn = (findViewById(R.id.join_to_comm));
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
        GroupHeader header = findViewById(R.id.group_header);
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
        if(group.avatar != null) {
            ((ImageView) findViewById(R.id.profile_photo)).setImageBitmap(group.avatar);
            getHeader().createGroupPhotoViewer(group.id, group.avatar_url);
        }
    }

    public void hideSelectedItemBackground(int position) {
        ((ListView) findViewById(R.id.groups_listview))
                .setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showGroup(int position) {
        String url = "openvk://group/" + "id" + ovk_api.groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage(getPackageName());
        i.setData(Uri.parse(url));
        startActivity(i);
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

    public void repost(int position) {
        final WallPost post = ovk_api.wall.getWallItems().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList<String> functions = new ArrayList<>();
        builder.setTitle(R.string.repost_dlg_title);
        functions.add(getResources().getString(R.string.repost_own_wall));
        ArrayAdapter<String> adapter = new
                ArrayAdapter<>(this, android.R.layout.simple_list_item_1, functions);
        builder.setSingleChoiceItems(adapter, -1, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(functions.get(position).equals(getResources().getString(R.string.repost_own_wall))) {
                    Global.openRepostDialog(GroupIntentActivity.this, ovk_api,
                            "own_wall", post);
                    dialog.dismiss();
                }
            }
        });
    }

    public void setScrollingPositions(final Context ctx, final boolean load_photos, final long owner_id) {
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
                            Global.loadMoreWallPosts(ovk_api, owner_id);
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
                            Global.loadMoreWallPosts(ovk_api, owner_id);
                        }
                    }
                }
            });
        }
    }

    public GroupHeader getHeader() {
        return findViewById(R.id.group_header);
    }
}