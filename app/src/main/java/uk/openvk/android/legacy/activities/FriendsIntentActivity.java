package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

import uk.openvk.android.legacy.Application;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.items.NewsItemCountersInfo;
import uk.openvk.android.legacy.layouts.FriendsLayout;
import uk.openvk.android.legacy.layouts.NewsLayout;
import uk.openvk.android.legacy.layouts.ProfileLayout;
import uk.openvk.android.legacy.layouts.SlidingMenuLayout;
import uk.openvk.android.legacy.list_adapters.FriendsListAdapter;
import uk.openvk.android.legacy.list_items.FriendsListItem;

public class FriendsIntentActivity extends Activity {
    public String auth_token;
    public TextView titlebar_title;
    public String server;
    public String server_2;
    public String state;
    public OvkAPIWrapper openVK_API;
    public UpdateUITask updateUITask;
    public StringBuilder response_sb;
    public JSONObject json_response;
    public JSONObject json_response_user;
    public JSONObject json_response_group;
    public JSONArray attachments;
    public boolean creating_another_activity;
    public PopupWindow popup_menu;
    public String send_request;
    public ListView news_listview;
    public int news_item_count;
    public SharedPreferences global_sharedPreferences;
    public ArrayList<Integer> post_author_ids;
    public NewsLayout newsLayout;
    public FriendsLayout friendsLayout;
    public ProfileLayout profileLayout;
    public boolean sliding_animated;
    public boolean menu_is_closed;
    public ArrayList<NewsItemCountersInfo> newsItemCountersInfoArray;
    public ArrayList<Bitmap> attachments_photo;
    public static Handler handler;
    public static final int UPDATE_UI = 0;
    public static final int GET_PICTURE = 1;
    public ArrayList<FriendsListItem> friendsListItemArray;
    public FriendsListAdapter friendsListAdapter;
    public Intent address_intent;
    public String fromLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_intent_layout);
        address_intent = getIntent();
        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor sharedPrefsEditor = global_sharedPreferences.edit();
        sharedPrefsEditor.putString("previousLayout", "");
        sharedPrefsEditor.commit();
        menu_is_closed = true;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                auth_token = getSharedPreferences("instance", 0).getString("auth_token", "");
            } else {
                auth_token = extras.getString("auth_token");
                fromLayout = extras.getString("fromLayout");
            }
        } else {
            auth_token = (String) savedInstanceState.getSerializable("auth_token");
        }

        attachments_photo = new ArrayList<Bitmap>();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch(what) {
                    case UPDATE_UI:
                        state = msg.getData().getString("State");
                        send_request = msg.getData().getString("API_method");
                        try {
                            json_response = new JSONObject(msg.getData().getString("JSON_response"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                        break;
                    case GET_PICTURE:
                        state = msg.getData().getString("State");
                        attachments_photo.add((Bitmap) msg.getData().getParcelable("Parcelable"));
                        try {
                            json_response = new JSONObject(msg.getData().getString("JSON_response"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                }
            }
        };

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        server = sharedPreferences.getString("server", "");

        profileLayout = findViewById(R.id.profile_layout);

        if(auth_token == null) {
            auth_token = sharedPreferences.getString("auth_token", "");
        }

        final Uri uri = address_intent.getData();

        if (uri!=null){
            String path = uri.toString();
            if(sharedPreferences.getString("auth_token", "").length() == 0) {
                finish();
                return;
            }

            openVK_API = new OvkAPIWrapper(FriendsIntentActivity.this, server, sharedPreferences.getString("auth_token", ""), json_response, global_sharedPreferences.getBoolean("useHTTPS", true));

            if(path.startsWith("openvk://profile/")) {
                String args = path.substring("openvk://profile/".length());
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(FriendsIntentActivity.this);
                auth_token = sharedPreferences.getString("auth_token", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.icon);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(R.string.profile);
                }
            }
        } else {
            openVK_API = new OvkAPIWrapper(FriendsIntentActivity.this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));
        }


        if(sharedPreferences.getString("auth_token", "").length() == 0) {
            Intent intent = new Intent(FriendsIntentActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        System.setProperty("http.keepAlive", "false");
        updateUITask = new UpdateUITask();
        sliding_animated = true;
        json_response = new JSONObject();
        response_sb = new StringBuilder();
        json_response_user = new JSONObject();
        json_response_group = new JSONObject();
        attachments = new JSONArray();
        newsItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
        friendsListItemArray = new ArrayList<FriendsListItem>();
        server_2 = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (uri != null) {
                    String path = uri.toString();
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                } else {
                    getActionBar().setIcon(R.drawable.ic_left_menu);
                }
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                resizeTranslucentLayout();
            }
        } else {
            ((ImageButton) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = getIntent().getData();
                    if(uri != null) {
                        finish();
                    }
                }
            });
            ((ImageButton) findViewById(R.id.ovkButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = getIntent().getData();
                    if(uri != null) {
                        finish();
                    }
                }
            });
        }
        post_author_ids = new ArrayList<Integer>();
        final LinearLayout progress_ll = findViewById(R.id.news_progressll);
        progress_ll.setVisibility(View.VISIBLE);

        profileLayout.setVisibility(View.GONE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.profile));
        } else {
            getActionBar().setTitle(getResources().getString(R.string.profile));
        }
        if(auth_token != null) {
            Log.i("OpenVK Legacy", "About instance:\r\n\r\nServer: " + server + "\r\nAuth token length: " + auth_token.length());
        } else {
            SharedPreferences global_prefs = getApplicationContext().getSharedPreferences("instance", 0);
            auth_token = global_prefs.getString("auth_token", "");
            Log.i("OpenVK Legacy", "About instance:\r\n\r\nServer: " + server + "\r\nAuth token length: " + auth_token.length());
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.newsfeed));
            final View menu_container = (View) getLayoutInflater().inflate(R.layout.popup_menu, null);
            popup_menu = new PopupWindow(menu_container, 200, ViewGroup.LayoutParams.WRAP_CONTENT);
            final ImageButton title_menu_btn = findViewById(R.id.title_menu_btn);
            final ImageButton new_post_btn = findViewById(R.id.new_post_btn);
        }
        try {
            if (uri != null) {
                String path = uri.toString();
                if (path.startsWith("openvk://friends/id")) {
                    openVK_API.sendMethod("Friends.get", "user_id=" + path.substring(("openvk://profile/id").length()));
                } else {
                    openVK_API.sendMethod("Users.search", "q=" + path.substring("openvk://profile".length()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        news_item_count = -1;
        final LinearLayout error_ll = findViewById(R.id.error_ll);
        final TextView error_button = findViewById(R.id.error_button2);
        friendsLayout = findViewById(R.id.friends_layout);
        error_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error_ll.setVisibility(View.GONE);
                progress_ll.setVisibility(View.VISIBLE);
                try {
                    if (uri != null) {
                        String path = uri.toString();
                        if (path.startsWith("openvk://friends/id")) {
                            openVK_API.sendMethod("Friends.get", "user_id=" + path.substring(("openvk://profile/id").length()));
                        } else {
                            openVK_API.sendMethod("Users.search", "q=" + path.substring("openvk://profile".length()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void resizeTranslucentLayout() {
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusbarView = findViewById(R.id.statusbarView);
            LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) statusbarView.getLayoutParams();
            int statusbar_height = getResources().getIdentifier("status_bar_height", "dimen", "android");
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                    new int[]{android.R.attr.actionBarSize});
            int actionbar_height = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            if (statusbar_height > 0) {
                ll_layoutParams.height = getResources().getDimensionPixelSize(statusbar_height) + actionbar_height;
            }
            statusbarView.setLayoutParams(ll_layoutParams);
        } catch (Exception ex) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusbarView = findViewById(R.id.statusbarView);
            statusbarView.setVisibility(View.GONE);
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newsfeed, menu);
        return true;
    }

    @Override
    protected void onResume() {
        creating_another_activity = false;
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.newpost) {
            openNewPostActivity();
        } else if(id == R.id.main_menu_exit) {
            finish();
            System.exit(0);
        } else if(id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void openNewPostActivity() {
        Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
        startActivity(intent);
    }

    public void showFriends(int user_id) {
        Intent intent = new Intent(getApplicationContext(), ProfileIntentActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    public void onSimpleListItemClicked(int position) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if(position == 0) {
                creating_another_activity = true;
                popup_menu.dismiss();
            } else if(position == 1) {
                popup_menu.dismiss();
                AlertDialog about_dlg;
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsIntentActivity.this);
                View about_view = getLayoutInflater().inflate(R.layout.about_application_layout, null, false);
                TextView about_text = about_view.findViewById(R.id.about_text);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    about_text.setText(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number) + "</font>"));
                } else {
                    about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number)));
                }
                about_text.setMovementMethod(LinkMovementMethod.getInstance());
                builder.setView(about_view);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                about_dlg = builder.create();
                about_dlg.show();
            } else if(position == 2) {
                finish();
                System.exit(0);
            }
        }
    }

    public void hideSelectedItemBackground(int position) {
        ((ListView) friendsLayout.findViewById(R.id.friends_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + friendsListItemArray.get(position).id;
        Log.d("OpenVK Legacy", "Item ID: " + position + " | User ID: " + friendsListItemArray.get(position).id);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.putExtra("fromLayout", global_sharedPreferences.getString("intentLayout", ""));
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state.equals("getting_response")) {
                        try {
                            if (json_response.has("error_code")) {
                                if (json_response.getInt("error_code") == 5 && json_response.getString("error_msg").startsWith("User authorization failed")) {
                                    Log.e("OpenVK Legacy", "Invalid API token");
                                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                                    editor.putString("auth_token", "");
                                    editor.putInt("user_id", 0);
                                    editor.commit();
                                    Intent intent = new Intent(FriendsIntentActivity.this, AuthenticationActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (json_response.getInt("error_code") == 3) {
                                    AlertDialog outdated_api_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsIntentActivity.this);
                                    builder.setTitle(R.string.incompatible_openvk_api);
                                    builder.setMessage(R.string.incompatible_openvk_api_title);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    try {
                                        if (creating_another_activity == false) {
                                            outdated_api_dlg = builder.create();
                                            outdated_api_dlg.show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (json_response.getInt("error_code") == 28) {
                                    AlertDialog wrong_userdata_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsIntentActivity.this);
                                    builder.setTitle(R.string.auth_error_title);
                                    builder.setMessage(R.string.auth_error);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    if (creating_another_activity == false) {
                                        wrong_userdata_dlg = builder.create();
                                        wrong_userdata_dlg.show();
                                    }
                                }
                            } else {
                                Uri uri = address_intent.getData();
                                if (send_request.startsWith("/method/Friends.get")) {
                                    loadFriends();
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (state.equals("connection_lost")) {
                        if (creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLayout.setVisibility(View.GONE);
                            error_ll.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = global_sharedPreferences.edit();
                            editor.putString("previousLayout", "");
                            editor.commit();
                        }
                    } else if (state.equals("timeout")) {
                        if (creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLayout.setVisibility(View.GONE);
                            error_ll.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = global_sharedPreferences.edit();
                            editor.putString("previousLayout", "");
                            editor.commit();
                        }
                    } else if (state.equals("no_connection")) {
                        if (creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLayout.setVisibility(View.GONE);
                            error_ll.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = global_sharedPreferences.edit();
                            editor.putString("previousLayout", "");
                            editor.commit();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(global_sharedPreferences.getString("previousLayout", "").equals("NewsLinearLayout")) {
            newsLayout.setVisibility(View.VISIBLE);
            profileLayout.setVisibility(View.GONE);
            friendsLayout.setVisibility(View.GONE);
            SharedPreferences.Editor editor = global_sharedPreferences.edit();
            editor.putString("intentLayout", "NewsLinearLayout");
            editor.commit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(getResources().getString(R.string.newsfeed));
            } else {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.newsfeed));
            }
        } else if(global_sharedPreferences.getString("previousLayout", "").equals("ProfileLayout")) {
            newsLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);
            friendsLayout.setVisibility(View.GONE);
            SharedPreferences.Editor editor = global_sharedPreferences.edit();
            editor.putString("intentLayout", "ProfileLayout");
            editor.commit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(getResources().getString(R.string.profile));
            } else {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.profile));
            }
        } else if(global_sharedPreferences.getString("previousLayout", "").equals("FriendsLayout")) {
            newsLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.GONE);
            friendsLayout.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = global_sharedPreferences.edit();
            editor.putString("intentLayout", "FriendsLayout");
            editor.commit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(getResources().getString(R.string.friends));
            } else {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.friends));
            }
        } else {
            finish();
        }
        SharedPreferences.Editor sharedPrefsEditor = global_sharedPreferences.edit();
        sharedPrefsEditor.putString("previousLayout", "");
        sharedPrefsEditor.commit();
    }

    void loadFriends() {
        friendsListItemArray.clear();
        Log.d("OpenVK Legacy", "Clearing friends list...");
        int friendsCount = 0; // zero rn
        try {
            friendsCount = json_response.getJSONObject("response").getJSONArray("items").length(); // we will use count of items this time bc we still don't have infinity loading or smth like that
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(friendsCount > 0)
        {
            if (send_request.startsWith("/method/Friends.get")) {
                for (int i = 0; i < friendsCount; i++) {
                    try {
                        JSONObject item = (JSONObject) json_response.getJSONObject("response").getJSONArray("items").get(i);
                        friendsListItemArray.add(new FriendsListItem(item.getInt("id"), item.getString("first_name") + " " + item.getString("last_name"), null, item.getInt("online")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            ListView friendsList = (ListView) findViewById(R.id.friends_listview);
            friendsListAdapter = new FriendsListAdapter(this, friendsListItemArray);
            friendsList.setAdapter(friendsListAdapter);
            LinearLayout progress_ll = findViewById(R.id.news_progressll);
            progress_ll.setVisibility(View.GONE);
            friendsLayout.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(getResources().getString(R.string.friends));
            } else {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.friends));
            }
        }
    }
}
