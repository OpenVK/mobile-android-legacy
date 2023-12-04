package uk.openvk.android.legacy.ui.core.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.ui.views.base.FullListView;
import uk.openvk.android.legacy.ui.views.SearchResultsLayout;
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

public class QuickSearchActivity extends NetworkActivity {
    public DownloadManager dlm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        dlm = new DownloadManager(this, global_prefs.getBoolean("useHTTPS", false),
                global_prefs.getBoolean("legacyHttpClient", false), handler);
        dlm.setInstance(PreferenceManager.getDefaultSharedPreferences(this).getString("current_instance", ""));
        setTextEditListener();
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBar(1, Color.parseColor("#8f8f8f"));
        } else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor( Color.parseColor("#8f8f8f"));
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            int flags = window.getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null) {
                return;
            }
            boolean isCurrentActivity = activityName.equals(getLocalClassName());
            if(!isCurrentActivity) {
                return;
            }
            Log.d("OpenVK", String.format("isCurrentActivity: true, activityName: %s",
                    data.getString("address")));
        }
        if(message == HandlerMessages.USERS_SEARCH) {
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.createUsersAdapter(this, ovk_api.users.getList());
            ((LinearLayout) searchResultsLayout.findViewById(R.id.people_ll)).setVisibility(View.VISIBLE);
        } else if(message == HandlerMessages.GROUPS_SEARCH) {
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.createGroupsAdapter(this, ovk_api.groups.getList());
            ((LinearLayout) searchResultsLayout.findViewById(R.id.community_ll)).setVisibility(View.VISIBLE);
        } else if(message == HandlerMessages.GROUP_AVATARS) {
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.groupsSearchResultAdapter.loadAvatars = true;
            searchResultsLayout.refreshGroupsAdapter();
        } else if(message == HandlerMessages.PROFILE_AVATARS) {
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.usersSearchResultAdapter.loadAvatars = true;
            searchResultsLayout.refreshUsersAdapter();
        }
    }

    private void setTextEditListener() {
        final EditText search_edit = findViewById(R.id.search_edit);
        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String query = search_edit.getText().toString();
                    try {
                        ovk_api.groups.search(ovk_api.wrapper, query);
                        ovk_api.users.search(ovk_api.wrapper, query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void hideSelectedItemBackground(int position) {
        final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
        FullListView people_listview = searchResultsLayout.findViewById(R.id.people_listview);
        people_listview.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + ovk_api.users.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setPackage("uk.openvk.android.legacy");
        startActivity(i);
    }

    public void showGroup(int position) {
        String url = "openvk://group/" + "club" + ovk_api.groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage("uk.openvk.android.legacy");
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
