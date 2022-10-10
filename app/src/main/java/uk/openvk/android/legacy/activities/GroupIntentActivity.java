package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.Arrays;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.Wall;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.layouts.ActionBarImitation;
import uk.openvk.android.legacy.layouts.ErrorLayout;
import uk.openvk.android.legacy.layouts.GroupHeader;
import uk.openvk.android.legacy.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.layouts.ProfileHeader;
import uk.openvk.android.legacy.layouts.ProgressLayout;
import uk.openvk.android.legacy.layouts.WallLayout;

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
    private NestedScrollView groupScrollView;
    private Group group;

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
                if (path.startsWith("openvk://group/")) {
                    String args = path.substring("openvk://group/".length());
                    ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
                    downloadManager = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", true));
                    ovk_api.setServer(instance_prefs.getString("server", ""));
                    ovk_api.setAccessToken(access_token);
                    groups = new Groups();
                    wall = new Wall();
                    if(args.startsWith("id")) {
                        try {
                            groups.getGroupByID(ovk_api, Integer.parseInt(args.substring(2)));
                        } catch (Exception ex) {
                            groups.search(ovk_api, args);
                        }
                    } else {
                        groups.search(ovk_api, args);
                    }
                    installLayouts();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
    }

    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        groupScrollView = (NestedScrollView) findViewById(R.id.group_scrollview);
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
        if (message == HandlerMessages.GROUPS_GET_BY_ID) {
            groups.parse(data.getString("response"));
            group = groups.getList().get(0);
            updateLayout(group);
            progressLayout.setVisibility(View.GONE);
            groupScrollView.setVisibility(View.VISIBLE);
            setJoinButtonListener(group.id);
            group.downloadAvatar(downloadManager);
            wall.get(ovk_api, -group.id, 50);
        } else if(message == HandlerMessages.GROUP_AVATARS) {
            loadAvatar();
        } else if(message == HandlerMessages.GROUPS_SEARCH) {
            groups.parseSearch(data.getString("response"));
            groups.getGroupByID(ovk_api, groups.getList().get(0).id);
        } else if (message == HandlerMessages.WALL_GET) {
            wall.parse(this, downloadManager, data.getString("response"));
            ((WallLayout) findViewById(R.id.wall_layout)).createAdapter(this, wall.getWallItems());
        } else if (message == HandlerMessages.WALL_ATTACHMENTS) {
            ((WallLayout) findViewById(R.id.wall_layout)).setScrollingPositions();
        } else if (message == HandlerMessages.WALL_AVATARS) {
            ((WallLayout) findViewById(R.id.wall_layout)).loadAvatars();
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
}
