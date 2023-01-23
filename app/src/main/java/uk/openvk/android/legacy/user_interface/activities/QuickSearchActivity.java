package uk.openvk.android.legacy.user_interface.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Groups;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.user_interface.layouts.ActionBarImitation;
import uk.openvk.android.legacy.user_interface.layouts.FullListView;
import uk.openvk.android.legacy.user_interface.layouts.SearchResultsLayout;
import uk.openvk.android.legacy.user_interface.wrappers.LocaleContextWrapper;

public class QuickSearchActivity extends Activity {
    private OvkAPIWrapper ovk_api;
    private Users users;
    private Groups groups;
    public Handler handler;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private SharedPreferences.Editor global_prefs_editor;
    private SharedPreferences.Editor instance_prefs_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibility(true);
            actionBarImitation.setTitle(getResources().getString(R.string.search_global));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        setTextEditListener();
        users = new Users();
        groups = new Groups();
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
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

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.USERS_SEARCH) {
            users.parseSearch(data.getString("response"));
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.createUsersAdapter(this, users.getList());
            ((LinearLayout) searchResultsLayout.findViewById(R.id.people_ll)).setVisibility(View.VISIBLE);
        } else if(message == HandlerMessages.GROUPS_SEARCH) {
            groups.parseSearch(data.getString("response"));
            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            searchResultsLayout.createGroupsAdapter(this, groups.getList());
            ((LinearLayout) searchResultsLayout.findViewById(R.id.community_ll)).setVisibility(View.VISIBLE);
        }
    }

    private void setTextEditListener() {
        final EditText search_edit = findViewById(R.id.search_edit);
        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String query = search_edit.getText().toString();
                    try {
                        groups.search(ovk_api, query);
                        users.search(ovk_api, query);
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
        String url = "openvk://profile/" + "id" + users.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void showGroup(int position) {
        String url = "openvk://group/" + "club" + groups.getList().get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
