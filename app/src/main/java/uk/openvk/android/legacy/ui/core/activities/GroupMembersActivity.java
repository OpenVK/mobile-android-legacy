package uk.openvk.android.legacy.ui.core.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Users;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.entities.Group;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.core.activities.base.UsersListActivity;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class GroupMembersActivity extends UsersListActivity {
    private ArrayList<Users> users;
    public Group group;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private DownloadManager dlm;
    private OvkAPIWrapper ovk_api;
    private String access_token;
    private PopupMenu popup_menu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            ovk_api.parseJSONData(data, GroupMembersActivity.this);
                        }
                    }).start();
                } else {
                    receiveState(message.what, data);
                }
            }
        };
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        instance_prefs = getSharedPreferences("instance", 0);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                access_token = instance_prefs.getString("access_token", "");
            } else {
                access_token = instance_prefs.getString("access_token", "");
                group = new Group();
                group.id = extras.getLong("group_id");
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }
        if(group != null) {
            setOvkAPIWrapper();
        } else {
            Log.e("OpenVK", "Cannot load Group object");
            finish();
        }
        setActionBar();
    }

    private void setActionBar() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.group_members));
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
            actionBar.setTitle(getResources().getString(R.string.group_members));
        }
    }

    private void setOvkAPIWrapper() {
        users = new ArrayList<Users>();
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", false));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(access_token);
        ovk_api.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        dlm = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", false));
        group.getMembers(ovk_api, 25, "");
    }

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.GROUP_MEMBERS) {
            if(group != null) {
                createAdapter(group.members);
                disableProgressBar();
            }
        }
    }
}
