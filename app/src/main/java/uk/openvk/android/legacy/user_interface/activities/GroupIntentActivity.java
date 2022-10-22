package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.Arrays;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Likes;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.ErrorLayout;
import uk.openvk.android.legacy.user_interface.layouts.GroupHeader;
import uk.openvk.android.legacy.user_interface.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.user_interface.layouts.ProgressLayout;
import uk.openvk.android.legacy.user_interface.layouts.WallLayout;
import uk.openvk.android.legacy.api.models.WallPost;

public class GroupIntentActivity extends Activity {
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

        final Uri uri = getIntent().getData();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newsfeed, menu);
        activity_menu = menu;
        return true;
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
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ((WallLayout) findViewById(R.id.wall_layout)).adjustLayoutSize(getResources().getConfiguration().orientation);
        super.onConfigurationChanged(newConfig);
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
            ActionBarImitation actionBarImitation = (ActionBarImitation) findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibillity(true);
            actionBarImitation.setTitle(getResources().getString(R.string.group));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
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
                    ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
                    actionBarImitation.setHomeButtonVisibillity(true);
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
            } else if (message == HandlerMessages.GROUPS_GET_BY_ID) {
                groups.parse(data.getString("response"));
                group = groups.getList().get(0);
                updateLayout(group);
                progressLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
                setJoinButtonListener(group.id);
                group.downloadAvatar(downloadManager);
                wall.get(ovk_api, -group.id, 50);
            } else if (message == HandlerMessages.GROUPS_SEARCH) {
                groups.parseSearch(data.getString("response"));
                groups.getGroupByID(ovk_api, groups.getList().get(0).id);
            } else if (message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
            } else if (message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).select(likes.position, "likes", 0);
            } else if (message == HandlerMessages.GROUP_AVATARS) {
                loadAvatar();
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, data.getString("response"));
                ((WallLayout) findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
                ((WallLayout) findViewById(R.id.wall_layout)).setScrollingPositions();
            } else if (message == HandlerMessages.WALL_AVATARS) {
                ((WallLayout) findViewById(R.id.wall_layout)).loadAvatars();
            } else if(message == HandlerMessages.POLL_ADD_VOTE) {
                WallPost item = wall.getWallItems().get(item_pos);
                ((PollAttachment) item.attachments.get(0).getContent()).answers.get(poll_answer).is_voted = true;
                wall.getWallItems().set(item_pos, item);
                ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
            } else if(message == HandlerMessages.POLL_DELETE_VOTE) {
                WallPost item = wall.getWallItems().get(item_pos);
                ((PollAttachment) item.attachments.get(0).getContent()).answers.get(poll_answer).is_voted = false;
                wall.getWallItems().set(item_pos, item);
                ((WallLayout) findViewById(R.id.wall_layout)).updateItem(item, item_pos);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setJoinButtonListener(int id) {
    }


    private void updateLayout(Group group) {
        GroupHeader header = (GroupHeader) findViewById(R.id.group_header);
        header.setProfileName(String.format("%s  ", group.name));
        header.setVerified(group.verified, this);
        ((ProfileCounterLayout) findViewById(R.id.members_counter)).setCounter(group.members_count, Arrays.asList(getResources().getStringArray(R.array.profile_members)).get(2), "");
    }

    public void loadAvatar() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/group_avatars/avatar_%s", getCacheDir(), group.id), options);
        if (bitmap != null) {
            group.avatar = bitmap;
        } else if(group.avatar_url.length() > 0) {
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
            if(item.attachments.size() > 0) {
                for(int i = 0; i < item.attachments.size(); i++) {
                    if(item.attachments.get(i).type.equals("poll")) {
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
            if(item.repost != null) {
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

    private void openNewPostActivity() {
        try {
            Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
            intent.putExtra("owner_id", -group.id);
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
        PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(0).getContent());
        pollAttachment.user_votes = 1;
        pollAttachment.answers.get(answer).votes = pollAttachment.answers.get(answer).votes + 1;
        wall.getWallItems().set(item_pos, item);
        pollAttachment.vote(ovk_api, pollAttachment.id);
    }

    public void removeVoteInPoll(int item_pos) {
        this.item_pos = item_pos;
        WallPost item = wall.getWallItems().get(item_pos);
        PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(0).getContent());
        for(int i = 0; i < pollAttachment.answers.size(); i++) {
            if(pollAttachment.answers.get(i).is_voted) {
                pollAttachment.answers.get(i).is_voted = false;
                pollAttachment.answers.get(i).votes = pollAttachment.answers.get(i).votes - 1;
            }
        }
        pollAttachment.user_votes = 0;
        wall.getWallItems().set(item_pos, item);
        pollAttachment.unvote(ovk_api);
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
}