package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AppActivity extends Activity {
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
    public JSONArray newsfeed;
    public JSONArray attachments;
    public boolean creating_another_activity;
    public PopupWindow popup_menu;
    public ArrayList<NewsListItem> newsListItemArray;
    public ArrayList<NewsListItem> wallListItemArray;
    public ArrayList<SlidingMenuItem> slidingMenuItemArray;
    public ArrayList<GroupPostInfo> groupPostInfoArray;
    public NewsListAdapter newsListAdapter;
    public int postAuthorId;
    public int postOwnerId;
    public String send_request;
    public ListView news_listview;
    public int news_item_count;
    public int news_item_index;
    public SharedPreferences global_sharedPreferences;
    public ArrayList<Integer> post_owners_ids;
    public ArrayList<Integer> post_author_ids;
    public StringBuilder post_author_ids_sb;
    public StringBuilder post_owners_ids_sb;
    public StringBuilder post_group_ids_sb;
    public NewsLinearLayout newsLinearLayout;
    public FriendsLinearLayout friendsLinearLayout;
    public ProfileLayout profileLayout;
    public boolean sliding_animated;
    public boolean menu_is_closed;
    public boolean connection_status;
    public int profile_id;
    public ArrayList<NewsItemCountersInfo> newsItemCountersInfoArray;
    public ArrayList<NewsItemCountersInfo> wallItemCountersInfoArray;
    public ArrayList<Bitmap> attachments_photo;
    public TabHost tabHost;
    public boolean about_profile_opened;
    public static Handler handler;
    public static final int UPDATE_UI = 0;
    public static final int GET_PICTURE = 1;
    public int current_user_id = 0;
    public ArrayList<FriendsListItem> friendsListItemArray;
    public FriendsListAdapter friendsListAdapter;
    public ArrayList<UserPostInfo> userPostInfoArray;
    public Intent address_intent;
    public String action;
    public int post_id;
    public int owner_id;
    public int newsfeed_id;
    public View news_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);
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
                        if(state != "no_connection" && state != "timeout") {
                            try {
                                json_response = new JSONObject(msg.getData().getString("JSON_response"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

        newsLinearLayout = findViewById(R.id.news_layout);
        profileLayout = findViewById(R.id.profile_layout);
        friendsLinearLayout = findViewById(R.id.friends_layout);

        if(getIntent().getData() == null && global_sharedPreferences.getBoolean("refreshOnOpen", true) == true) {
            SharedPreferences.Editor editor = global_sharedPreferences.edit();
            editor.putString("currentLayout", "NewsLinearLayout");
            editor.commit();
        }

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

            openVK_API = new OvkAPIWrapper(AppActivity.this, server, sharedPreferences.getString("auth_token", ""), json_response, global_sharedPreferences.getBoolean("useHTTPS", true));

            if(path.startsWith("openvk://profile/")) {
                String args = path.substring("openvk://profile/".length());
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppActivity.this);
                auth_token = sharedPreferences.getString("auth_token", "");
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("currentLayout", "ProfileLayout");
                editor.commit();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.icon);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(R.string.profile);
                }
            } else if(path.startsWith("openvk://friends/")) {
                String args = path.substring("openvk://friends/".length());
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppActivity.this);
                auth_token = sharedPreferences.getString("auth_token", "");
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("currentLayout", "FriendsLayout");
                editor.commit();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.icon);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(R.string.friends);
                }
            }
        } else {
            openVK_API = new OvkAPIWrapper(AppActivity.this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));
        }


        if(sharedPreferences.getString("auth_token", "").length() == 0) {
            Intent intent = new Intent(AppActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        final ProfileCounterLayout friends_counter = profileLayout.findViewById(R.id.friends_counter);
        friends_counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = new String();
                url = "openvk://friends/id" + profile_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(i);
            }
        });

        final SlidingMenuLayout slidingMenuLayout = findViewById(R.id.sliding_menu_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        ((EditText) slidingMenuLayout.findViewById(R.id.sliding_menu_search).findViewById(R.id.left_quick_search_btn)).setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String url = "openvk://profile/" + ((EditText) slidingMenuLayout.findViewById(R.id.sliding_menu_search).findViewById(R.id.left_quick_search_btn)).getText().toString();
                    ((EditText) slidingMenuLayout.findViewById(R.id.sliding_menu_search).findViewById(R.id.left_quick_search_btn)).setText("");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    i.putExtra("fromLayout", global_sharedPreferences.getString("currentLayout", ""));
                    startActivity(i);
                    return true;
                }

                return false;
            }
        });

        TextView profile_name = slidingMenuLayout.findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));
        final ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
        final AboutProfileLinearLayout aboutProfile_ll = findViewById(R.id.about_profile_layout);
        ((View) profileHeader.findViewById(R.id.profile_head_highlight)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(about_profile_opened == false) {
                    about_profile_opened = true;
                    aboutProfile_ll.setVisibility(View.VISIBLE);
                } else {
                    about_profile_opened = false;
                    aboutProfile_ll.setVisibility(View.GONE);
                }
            }
        });

        ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.loading));
        ((TextView) profileHeader.findViewById(R.id.profile_activity)).setText("");
        ProfileCounterLayout photos_counter = ((LinearLayout) profileLayout.findViewById(R.id.profile_ext_header)).findViewById(R.id.photos_counter);
        ((TextView) photos_counter.findViewById(R.id.profile_counter_value)).setText("0");
        ((TextView) photos_counter.findViewById(R.id.profile_counter_title)).setText(getResources().getStringArray(R.array.profile_photos)[2]);
        ((TextView) friends_counter.findViewById(R.id.profile_counter_value)).setText("0");
        ((TextView) friends_counter.findViewById(R.id.profile_counter_title)).setText(getResources().getStringArray(R.array.profile_friends)[2]);
        ProfileCounterLayout mutual_counter = ((LinearLayout) profileLayout.findViewById(R.id.profile_ext_header)).findViewById(R.id.mutual_counter);
        ((TextView) mutual_counter.findViewById(R.id.profile_counter_value)).setText("0");
        ((TextView) mutual_counter.findViewById(R.id.profile_counter_title)).setText(getResources().getStringArray(R.array.profile_mutual_friends)[2]);
        tabHost = (TabHost) profileLayout.findViewById(R.id.profile_tabhost);
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("all_posts_tab");
        tabSpec.setContent(R.id.all_posts_tab);
        tabSpec.setIndicator(getResources().getString(R.string.wall_all_posts));
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                tabHost.setCurrentTab(tabHost.getCurrentTab());
            }
        });
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            View view = tabHost.getTabWidget().getChildAt(0);
            if (view != null) {
                tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = (int) (32 * getResources().getDisplayMetrics().density);
                View tabImage = view.findViewById(android.R.id.icon);
                view.setBackgroundResource(R.drawable.tabwidget);
                TextView textView = view.findViewById(android.R.id.title);
                textView.getLayoutParams().height = (int) (26 * getResources().getDisplayMetrics().density);
                textView.setTextColor(Color.BLACK);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                if (tabImage != null) {
                    tabImage.setVisibility(View.GONE);
                    Log.d("Client", "TabIcon View");
                }
            }
        }

        tabSpec = tabHost.newTabSpec("owners_posts_tab");
        tabSpec.setContent(R.id.owners_posts_tab);
        tabSpec.setIndicator(getResources().getString(R.string.wall_owners_posts, ""));
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                tabHost.setCurrentTab(tabHost.getCurrentTab());
            }
        });
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            View view = tabHost.getTabWidget().getChildAt(1);
            if (view != null) {
                tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = (int) (32 * getResources().getDisplayMetrics().density);
                View tabImage = view.findViewById(android.R.id.icon);
                view.setBackgroundResource(R.drawable.tabwidget);
                TextView textView = view.findViewById(android.R.id.title);
                textView.getLayoutParams().height = (int) (26 * getResources().getDisplayMetrics().density);
                textView.setTextColor(Color.BLACK);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                if (tabImage != null) {
                    tabImage.setVisibility(View.GONE);
                    Log.d("Client", "TabIcon View");
                }
            }
        }
        System.setProperty("http.keepAlive", "false");
        updateUITask = new UpdateUITask();
        sliding_animated = true;
        json_response = new JSONObject();
        slidingMenuItemArray = new ArrayList<SlidingMenuItem>();
        response_sb = new StringBuilder();
        post_author_ids_sb = new StringBuilder();
        post_owners_ids_sb = new StringBuilder();
        post_group_ids_sb = new StringBuilder();
        post_owners_ids = new ArrayList<Integer>();
        json_response_user = new JSONObject();
        json_response_group = new JSONObject();
        attachments = new JSONArray();
        newsListItemArray = new ArrayList<NewsListItem>();
        wallListItemArray = new ArrayList<NewsListItem>();
        groupPostInfoArray = new ArrayList<GroupPostInfo>();
        newsItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
        wallItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
        friendsListItemArray = new ArrayList<FriendsListItem>();
        userPostInfoArray = new ArrayList<UserPostInfo>();
        server_2 = new String();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (uri != null) {
                    String path = uri.toString();
                    if (path.startsWith("openvk://profile/")) {
                        getActionBar().setIcon(R.drawable.ic_ab_app);
                    }
                } else {
                    getActionBar().setIcon(R.drawable.ic_left_menu);
                }
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            ((ImageButton) findViewById(R.id.menuButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = getIntent().getData();
                    if(uri != null) {
                        String path = uri.toString();
                        if(path.startsWith("openvk://profile/")) {
                        }
                    } else {
                        openSlidingMenu();
                    }
                }
            });
        }
        createSlidingMenu();
        SlidingMenuAdapter slidingMenuAdapter = new SlidingMenuAdapter(this, slidingMenuItemArray);
        ((ListView) slidingMenuLayout.findViewById(R.id.menu_view)).setAdapter(slidingMenuAdapter);
        ((ListView) slidingMenuLayout.findViewById(R.id.menu_view)).setBackgroundColor(getResources().getColor(R.color.transparent));
        ((ListView) slidingMenuLayout.findViewById(R.id.menu_view)).setCacheColorHint(getResources().getColor(R.color.transparent));
        ((LinearLayout) slidingMenuLayout.findViewById(R.id.profile_menu_ll)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("previousLayout", global_sharedPreferences.getString("currentLayout", ""));
                editor.commit();
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppActivity.this);
                editor.putString("currentLayout", "ProfileLayout");
                editor.commit();
                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                if(connection_status == false) {
                    address_intent = getIntent();
                    newsLinearLayout.setVisibility(View.GONE);
                    profileLayout.setVisibility(View.GONE);
                    friendsLinearLayout.setVisibility(View.GONE);
                    LinearLayout progress_ll = findViewById(R.id.news_progressll);
                    progress_ll.setVisibility(View.VISIBLE);
                    LinearLayout error_ll = findViewById(R.id.error_ll);
                    error_ll.setVisibility(View.GONE);
                    try {
                        openVK_API.sendMethod("Account.getProfileInfo", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        titlebar_title.setText(getResources().getString(R.string.profile));
                    } else {
                        getActionBar().setTitle(getResources().getString(R.string.profile));
                    }
                }
                openSlidingMenu();
            }
        });
        ((TextView) slidingMenuLayout.findViewById(R.id.profile_name)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppActivity.this);
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("previousLayout", global_sharedPreferences.getString("currentLayout", ""));
                editor.commit();
                editor.putString("currentLayout", "ProfileLayout");
                editor.commit();
                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                if(connection_status == false) {
                    address_intent = getIntent();
                    newsLinearLayout.setVisibility(View.GONE);
                    profileLayout.setVisibility(View.GONE);
                    friendsLinearLayout.setVisibility(View.GONE);
                    LinearLayout progress_ll = findViewById(R.id.news_progressll);
                    progress_ll.setVisibility(View.VISIBLE);
                    LinearLayout error_ll = findViewById(R.id.error_ll);
                    error_ll.setVisibility(View.GONE);
                    try {
                        openVK_API.sendMethod("Account.getProfileInfo", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        titlebar_title.setText(getResources().getString(R.string.profile));
                    } else {
                        getActionBar().setTitle(getResources().getString(R.string.profile));
                    }
                }
                openSlidingMenu();
            }
        });
        post_author_ids = new ArrayList<Integer>();
        final LinearLayout progress_ll = findViewById(R.id.news_progressll);
        progress_ll.setVisibility(View.VISIBLE);

        if(global_sharedPreferences.getString("currentLayout", "").equals("NewsLinearLayout")) {
            newsLinearLayout.setVisibility(View.GONE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.newsfeed));
            } else {
                getActionBar().setTitle(getResources().getString(R.string.newsfeed));
            }
        } else if(global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
            profileLayout.setVisibility(View.GONE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.profile));
            } else {
                getActionBar().setTitle(getResources().getString(R.string.profile));
            }
        } else if(global_sharedPreferences.getString("currentLayout", "").equals("FriendsLayout")) {
            friendsLinearLayout.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.friends));
            } else {
                getActionBar().setTitle(getResources().getString(R.string.friends));
            }
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
            new_post_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNewPostActivity();
                }
            });
            List<String> itemsArray = new ArrayList<String>();
            ArrayList<SimpleListItem> itemsList = new ArrayList<SimpleListItem>();
            final SimpleListAdapter itemsAdapter;
            itemsList = new ArrayList<SimpleListItem>();
            itemsAdapter = new SimpleListAdapter(this, itemsList);
            itemsList.clear();
            for (int i = 0; i < 3; i++) {
                itemsList.add(new SimpleListItem(getResources().getStringArray(R.array.popup_menu_api_v8)[i]));
            }
            final ListView menu_list = (ListView) menu_container.findViewById(R.id.popup_menulist);
            title_menu_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(popup_menu.isShowing()) {
                        popup_menu.dismiss();
                    } else {
                        menu_list.setAdapter(itemsAdapter);
                        if(creating_another_activity == false) {
                            popup_menu.showAtLocation(title_menu_btn, Gravity.TOP | Gravity.RIGHT, 0, 100);
                        } else {
                            popup_menu.dismiss();
                        }
                    }
                }
            });
        }
        try {
            openVK_API.sendMethod("Account.getProfileInfo", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        news_item_count = -1;
        final LinearLayout error_ll = findViewById(R.id.error_ll);
        final TextView error_button = findViewById(R.id.error_button2);
        error_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                error_ll.setVisibility(View.GONE);
                progress_ll.setVisibility(View.VISIBLE);
                try {
                    openVK_API.sendMethod("Account.getProfileInfo", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TextView profile_name_tv = slidingMenuLayout.findViewById(R.id.profile_name);
        profile_name_tv.setTextColor(Color.WHITE);
    }

    private void createSlidingMenu() {
        if(slidingMenuItemArray != null) {
            for (int slider_menu_item_index = 0; slider_menu_item_index < getResources().getStringArray(R.array.leftmenu).length; slider_menu_item_index++) {
                if (slider_menu_item_index == 0) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_friends)));
                } else if (slider_menu_item_index == 1) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_photos)));
                } else if (slider_menu_item_index == 2) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_video)));
                } else if (slider_menu_item_index == 3) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_messages)));
                } else if (slider_menu_item_index == 4) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_groups)));
                } else if (slider_menu_item_index == 5) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_news)));
                } else if (slider_menu_item_index == 6) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_feedback)));
                } else if (slider_menu_item_index == 7) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_fave)));
                } else if (slider_menu_item_index == 8) {
                    slidingMenuItemArray.add(new SlidingMenuItem(getResources().getStringArray(R.array.leftmenu)[slider_menu_item_index], 0, getResources().getDrawable(R.drawable.ic_left_settings)));
                }
            }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_menu_settings) {
            showMainSettings();
        } else if(id == R.id.main_menu_about) {
            AlertDialog about_dlg;
            AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
            View about_view = getLayoutInflater().inflate(R.layout.about_application_layout, null, false);
            TextView about_text = about_view.findViewById(R.id.about_text);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                about_text.setText(Html.fromHtml("<font color='#ffffff'>" + getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number) + "</font>"));
            } else {
                about_text.setText(Html.fromHtml(getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME, ((Application) getApplicationContext()).build_number)));
            }
            Log.d("Application", getResources().getString(R.string.about_text, "0", 0));
            about_text.setMovementMethod(LinkMovementMethod.getInstance());
            builder.setView(about_view);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            about_dlg = builder.create();
            about_dlg.show();
        } else if(id == R.id.newpost) {
            openNewPostActivity();
        } else if(id == R.id.main_menu_exit) {
            finish();
            System.exit(0);
        } else if(id == android.R.id.home) {
            Uri uri = getIntent().getData();
            if(uri != null) {
                String path = uri.toString();
                if(path.startsWith("openvk://profile/")) {
                    finish();
                }
            } else {
                openSlidingMenu();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void openNewPostActivity() {
        Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
        startActivity(intent);
    }

    public void showMainSettings() {
        Intent intent = new Intent(getApplicationContext(), MainSettingsActivity.class);
        startActivity(intent);
    }

    public void showFriends(int user_id) {
        Intent intent = new Intent(getApplicationContext(), AppActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

    public void onSimpleListItemClicked(int position) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if(position == 0) {
                creating_another_activity = true;
                popup_menu.dismiss();
                showMainSettings();
            } else if(position == 1) {
                popup_menu.dismiss();
                AlertDialog about_dlg;
                AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
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
        news_listview = findViewById(R.id.news_listview);
        news_listview.setBackgroundColor(getResources().getColor(R.color.transparent));
        ((ListView) friendsLinearLayout.findViewById(R.id.friends_listview)).setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void openSlidingMenu() {
        if(menu_is_closed == true) {
            menu_is_closed = false;
            final SlidingMenuLayout slidingMenuLayout = findViewById(R.id.sliding_menu_layout);
            TranslateAnimation animate = new TranslateAnimation(
                    -(300 * getResources().getDisplayMetrics().scaledDensity),                 // fromXDelta
                    0,                 // toXDelta
                    0,  // fromYDelta
                    0);                // toYDelta
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animate.setDuration(200);
            animate.setFillAfter(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) slidingMenuLayout.getLayoutParams();
                        lp.setMargins(0, 0, 0, 0);
                        slidingMenuLayout.setLayoutParams(lp);
                        sliding_animated = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sliding_animated = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                sliding_animated = false;
                slidingMenuLayout.startAnimation(animate);
            } else {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) slidingMenuLayout.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                slidingMenuLayout.setLayoutParams(lp);
            }
            slidingMenuLayout.setVisibility(View.VISIBLE);

        } else {
            menu_is_closed = true;
            final SlidingMenuLayout slidingMenuLayout = findViewById(R.id.sliding_menu_layout);
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    -(300 * getResources().getDisplayMetrics().scaledDensity),                 // toXDelta
                    0,  // fromYDelta
                    0);                  // toYDelta
            animate.setDuration(200);
            animate.setFillAfter(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        sliding_animated = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) slidingMenuLayout.getLayoutParams();
                        lp.setMargins((int) -(300 * getResources().getDisplayMetrics().scaledDensity), 0, 0, 0);
                        slidingMenuLayout.setLayoutParams(lp);
                        sliding_animated = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                slidingMenuLayout.startAnimation(animate);
            } else {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) slidingMenuLayout.getLayoutParams();
                lp.setMargins((int) -(300 * getResources().getDisplayMetrics().scaledDensity), 0, 0, 0);
                slidingMenuLayout.setLayoutParams(lp);
            }
        }
    }

    public boolean getAnimationState() {
        return sliding_animated;
    }

    public boolean getSlidingMenuState() {
        return menu_is_closed;
    }

    public void onSlidingMenuItemClicked(int position) {
        if(position == 0) {
            if(connection_status == false) {
                SharedPreferences.Editor sharedPrefsEditor = global_sharedPreferences.edit();
                sharedPrefsEditor.putString("previousLayout", global_sharedPreferences.getString("currentLayout", ""));
                sharedPrefsEditor.commit();
                address_intent = getIntent();
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("currentLayout", "FriendsLayout");
                editor.commit();
                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                profileLayout.setVisibility(View.GONE);
                NewsLinearLayout newsLinearLayout = findViewById(R.id.news_layout);
                newsLinearLayout.setVisibility(View.GONE);
                LinearLayout error_ll = findViewById(R.id.error_ll);
                friendsLinearLayout = findViewById(R.id.friends_layout);
                friendsLinearLayout.setVisibility(View.GONE);
                error_ll.setVisibility(View.GONE);
                LinearLayout progress_ll = findViewById(R.id.news_progressll);
                progress_ll.setVisibility(View.VISIBLE);
                openSlidingMenu();
                try {
                    openVK_API.sendMethod("Account.getProfileInfo", "access_token=" + URLEncoder.encode(auth_token, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    titlebar_title.setText(getResources().getString(R.string.friends));
                } else {
                    getActionBar().setTitle(getResources().getString(R.string.friends));
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_wait_network, Toast.LENGTH_LONG).show();
            }
        } else if(position == 5) {
            if(connection_status == false) {
                SharedPreferences.Editor sharedPrefsEditor = global_sharedPreferences.edit();
                sharedPrefsEditor.putString("previousLayout", global_sharedPreferences.getString("currentLayout", ""));
                sharedPrefsEditor.commit();
                address_intent = getIntent();
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = global_sharedPreferences.edit();
                editor.putString("currentLayout", "NewsLinearLayout");
                editor.commit();
                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                profileLayout.setVisibility(View.GONE);
                NewsLinearLayout newsLinearLayout = findViewById(R.id.news_layout);
                newsLinearLayout.setVisibility(View.GONE);
                LinearLayout error_ll = findViewById(R.id.error_ll);
                error_ll.setVisibility(View.GONE);
                friendsLinearLayout.setVisibility(View.GONE);
                LinearLayout progress_ll = findViewById(R.id.news_progressll);
                progress_ll.setVisibility(View.VISIBLE);
                openSlidingMenu();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    titlebar_title.setText(getResources().getString(R.string.newsfeed));
                } else {
                    getActionBar().setTitle(getResources().getString(R.string.newsfeed));
                }
                    try {
                        groupPostInfoArray = new ArrayList<GroupPostInfo>();
                        newsListItemArray = new ArrayList<NewsListItem>();
                        newsItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
                        newsfeed = new JSONArray();
                        json_response_group = new JSONObject();
                        json_response_user = new JSONObject();
                        post_group_ids_sb = new StringBuilder();
                        post_author_ids_sb = new StringBuilder();
                        post_author_ids = new ArrayList<Integer>();
                        openVK_API.sendMethod("Account.getProfileInfo", "access_token=" + URLEncoder.encode(auth_token, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_wait_network, Toast.LENGTH_LONG).show();
            }
        } else if(position == 8) {
            creating_another_activity = true;
            if(popup_menu != null) {
                popup_menu.dismiss();
            }
            showMainSettings();
        }  else {
            Toast.makeText(AppActivity.this, getResources().getString(R.string.not_implemented), Toast.LENGTH_LONG).show();
        }
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + friendsListItemArray.get(position).id;
        Log.d("OpenVK Legacy", "Item ID: " + position + " | User ID: " + friendsListItemArray.get(position).id);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.putExtra("fromLayout", global_sharedPreferences.getString("currentLayout", ""));
        startActivity(i);
    }

    public void addLike(int position, String type, View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("instance", 0);
        if(connection_status == false) {
            if(global_sharedPreferences.getString("currentLayout", "").equals("NewsLinearLayout")) {
                send_request = ("/method/Likes.isLiked");
                action = "add_like";
                post_id = newsListItemArray.get(position).post_id;
                owner_id = newsListItemArray.get(position).owner_id;
                newsfeed_id = position;
                openVK_API.sendMethod("Likes.isLiked", "owner_id=" + owner_id + "&user_id=" + sharedPreferences.getInt("user_id", 0) +
                        "&item_id=" + post_id + "&type=" + type);
                news_item = view;
            } else if(global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                send_request = ("/method/Likes.isLiked");
                action = "add_like";
                post_id = wallListItemArray.get(position).post_id;
                owner_id = wallListItemArray.get(position).owner_id;
                newsfeed_id = position;
                openVK_API.sendMethod("Likes.isLiked", "owner_id=" + owner_id + "&user_id=" + sharedPreferences.getInt("user_id", 0) +
                        "&item_id=" + post_id + "&type=" + type);
                news_item = view;
            }
        } else {
            Log.d("OpenVK Legacy", "Already connected!");
        }
    }

    public void getOwnerProfile(NewsListItem item) {
        if(item != null) {
            String url = "openvk://profile/id" + item.owner_id;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            i.putExtra("fromLayout", global_sharedPreferences.getString("currentLayout", ""));
            startActivity(i);
        }
    }


    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(state == "getting_response") {
                        try {
                            if(json_response.has("error_code")) {
                                if (json_response.getInt("error_code") == 5 && json_response.getString("error_msg").startsWith("User authorization failed")) {
                                    Log.e("OpenVK Legacy", "Invalid API token");
                                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                                    editor.putString("auth_token", "");
                                    editor.putInt("user_id", 0);
                                    editor.commit();
                                    Intent intent = new Intent(AppActivity.this, AuthenticationActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (json_response.getInt("error_code") == 3) {
                                    AlertDialog outdated_api_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
                                    builder.setTitle(R.string.deprecated_openvk_api_error_title);
                                    builder.setMessage(R.string.deprecated_openvk_api_error);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    try{
                                        if(creating_another_activity == false) {
                                            outdated_api_dlg = builder.create();
                                            outdated_api_dlg.show();
                                        }
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (json_response.getInt("error_code") == 28) {
                                    AlertDialog wrong_userdata_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this);
                                    builder.setTitle(R.string.auth_error_title);
                                    builder.setMessage(R.string.auth_error);
                                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    if(creating_another_activity == false) {
                                        wrong_userdata_dlg = builder.create();
                                        wrong_userdata_dlg.show();
                                    }
                                }
                            } else if(send_request.startsWith("/method/Account.getProfileInfo")) {
                                SlidingMenuLayout slidingMenuLayout = findViewById(R.id.sliding_menu_layout);
                                TextView profile_name = slidingMenuLayout.findViewById(R.id.profile_name);
                                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                                editor.putInt("user_id", json_response.getJSONObject("response").getInt("id"));
                                editor.commit();
                                profile_name.setText(json_response.getJSONObject("response").getString("first_name") + " " + json_response.getJSONObject("response").getString("last_name"));
                                current_user_id = json_response.getJSONObject("response").getInt("id");
                                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                                ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                View view = tabHost.getTabWidget().getChildAt(1);
                                if(view != null && tabHost.getTabWidget().getTabCount() > 1) {
                                    ((TextView) view.findViewById(android.R.id.title)).setText(getResources().getString(R.string.wall_owners_posts, json_response.getJSONObject("response").getString("first_name")));
                                }
                                final AboutProfileLinearLayout aboutProfile_ll = findViewById(R.id.about_profile_layout);
                                if(json_response.getJSONObject("response").has("bdate_visibility") && json_response.getJSONObject("response").getInt("bdate_visibility") > 0) {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.birthday_label2)).setText(json_response.getJSONObject("response").getString("bdate").split(".")[0] +
                                            getResources().getStringArray(Integer.valueOf(json_response.getJSONObject("response").getString("bdate").split(".")[1]) - 1) +
                                            json_response.getJSONObject("response").getString("bdate").split(".")[2]);
                                } else {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.birthdate_ll)).setVisibility(View.GONE);
                                }
                                if(global_sharedPreferences.getString("currentLayout", "").equals("NewsLinearLayout")) {
                                    if(connection_status == false) {
                                        try {
                                            openVK_API.sendMethod("Newsfeed.get", "count=" + 100 + "&extended=1");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else if(global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                    Uri uri = address_intent.getData();
                                    if (uri != null) {
                                        String args = uri.toString().substring("openvk://profile/".length());
                                        if (connection_status == false) {
                                            if(args.startsWith("id")) {
                                                try {
                                                    openVK_API.sendMethod("Users.get", "user_ids=" + URLEncoder.encode(args.substring(2), "UTF-8") + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    openVK_API.sendMethod("Users.search", "q=" + URLEncoder.encode(args, "UTF-8"));
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } else {
                                        if (connection_status == false) {
                                            try {
                                                profile_id = json_response.getJSONObject("response").getInt("id");
                                                openVK_API.sendMethod("Users.get", "user_ids=" + json_response.getJSONObject("response").getInt("id") + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } else if(global_sharedPreferences.getString("currentLayout", "").equals("FriendsLayout")) {
                                    Uri uri = address_intent.getData();
                                    if (uri != null) {
                                        if(uri.toString().startsWith("openvk://friends/")) {
                                            String args = uri.toString().substring("openvk://friends/".length());
                                            if (connection_status == false) {
                                                if (args.startsWith("id")) {
                                                    try {
                                                        Log.d("OpenVK Legacy", "Loading friends by user ID: " + args.substring(2) + "...");
                                                        openVK_API.sendMethod("Friends.get", "user_id=" + URLEncoder.encode(args.substring(2), "UTF-8"));
                                                    } catch (UnsupportedEncodingException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    try {
                                                        Log.d("OpenVK Legacy", "Loading friends by username: " + args + "...");
                                                        openVK_API.sendMethod("Users.search", "q=" + URLEncoder.encode(args, "UTF-8"));
                                                    } catch (UnsupportedEncodingException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if(json_response.getJSONObject("response").getInt("id") > 0) {
                                            openVK_API.sendMethod("Friends.get", "user_id=" + json_response.getJSONObject("response").getInt("id"));
                                        }
                                    }
                                }
                            } else if((send_request.startsWith("/method/Newsfeed.get") || send_request.startsWith("/method/Users.get") || send_request.startsWith("/method/Groups.get")) && global_sharedPreferences.getString("currentLayout", "").equals("NewsLinearLayout")) {
                                appendNewsItem();
                            } else if((send_request.startsWith("/method/Newsfeed.get") || send_request.startsWith("/method/Users.get")) && global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                                ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                final AboutProfileLinearLayout aboutProfile_ll = findViewById(R.id.about_profile_layout);
                                try {
                                    profile_id = json_response.getJSONArray("response").getJSONObject(0).getInt("id");
                                } catch(Exception ex) {
                                    try {
                                        if (json_response.getJSONArray("response").getJSONObject(0).getString("id").equals("")) {
                                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                                            progress_ll.setVisibility(View.GONE);
                                            LinearLayout error_ll = findViewById(R.id.error_ll);
                                            error_ll.setVisibility(View.VISIBLE);
                                            ((TextView) error_ll.findViewById(R.id.error_text2)).setText(R.string.page_not_found);
                                            return;
                                        }
                                    } catch(Exception ex2) {
                                        ex.printStackTrace();
                                    }
                                }
                                String name = json_response.getJSONArray("response").getJSONObject(0).getString("first_name") + " " + json_response.getJSONArray("response").getJSONObject(0).getString("last_name") + "  ";
                                if(tabHost.getTabWidget().getTabCount() > 1) {
                                    View view = tabHost.getTabWidget().getChildAt(1);
                                    if (view != null) {
                                        TextView title = view.findViewById(android.R.id.title);
                                        title.setText(getResources().getString(R.string.wall_owners_posts, json_response.getJSONArray("response").getJSONObject(0).getString("first_name")));
                                    }
                                }

                                SpannableStringBuilder sb = new SpannableStringBuilder(name);
                                if(json_response.getJSONArray("response").getJSONObject(0).getInt("verified") == 1) {
                                    ImageSpan imageSpan = new ImageSpan(getApplicationContext(), R.drawable.ic_verified, DynamicDrawableSpan.ALIGN_BASELINE);
                                    sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                ((TextView) profileHeader.findViewById(R.id.profile_name)).setText(sb);
                                String status = "";
                                if(json_response.getJSONArray("response").getJSONObject(0).has("status")) {
                                    status = json_response.getJSONArray("response").getJSONObject(0).getString("status");
                                    ((EditText) aboutProfile_ll.findViewById(R.id.status_editor)).setVisibility(View.VISIBLE);
                                }
                                ((TextView) profileHeader.findViewById(R.id.profile_activity)).setText(status);
                                ((EditText) aboutProfile_ll.findViewById(R.id.status_editor)).setText(status);
                                String last_seen_time = new SimpleDateFormat("HH:mm").format(new Date(TimeUnit.SECONDS.toMillis(json_response.getJSONArray("response").getJSONObject(0).getJSONObject("last_seen").getInt("time"))));
                                String last_seen_date = new SimpleDateFormat("dd MMMM yyyy").format(new Date(TimeUnit.SECONDS.toMillis(json_response.getJSONArray("response").getJSONObject(0).getJSONObject("last_seen").getInt("time"))));
                                if (json_response.getJSONArray("response").getJSONObject(0).getInt("online") == 0) {
                                    if ((TimeUnit.SECONDS.toMillis(json_response.getJSONArray("response").getJSONObject(0).getJSONObject("last_seen").getInt("time")) - System.currentTimeMillis()) < 86400000) {
                                        if (json_response.getJSONArray("response").getJSONObject(0).getInt("sex") == 1) {
                                            ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, getResources().getString(R.string.date_at) + " " + last_seen_time));
                                        } else {
                                            ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, getResources().getString(R.string.date_at) + " " + last_seen_time));
                                        }
                                    } else {
                                        if (json_response.getJSONArray("response").getJSONObject(0).getInt("sex") == 1) {
                                            ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_f, last_seen_date));
                                        } else {
                                            ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.last_seen_profile_m, last_seen_date));
                                        }
                                    }
                                } else {
                                    ((TextView) profileHeader.findViewById(R.id.profile_last_seen)).setText(getResources().getString(R.string.online));
                                }

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("interests") == true) {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout)).setVisibility(View.GONE);
                                } else {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.interests_label2)).setText(
                                            json_response.getJSONArray("response").getJSONObject(0).getString("interests")
                                    );
                                }

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("music") == true) {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout2)).setVisibility(View.GONE);
                                } else {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.music_label2)).setText(
                                            json_response.getJSONArray("response").getJSONObject(0).getString("music")
                                    );
                                }

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("movies") == true) {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout3)).setVisibility(View.GONE);
                                } else {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.movies_label2)).setText(
                                            json_response.getJSONArray("response").getJSONObject(0).getString("movies")
                                    );
                                }

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("tv") == true) {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout4)).setVisibility(View.GONE);

                                } else {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.movies_label2)).setText(
                                            json_response.getJSONArray("response").getJSONObject(0).getString("tv")
                                    );
                                }

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("books") == true) {
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout5)).setVisibility(View.GONE);

                                } else {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.books_label2)).setText(
                                            json_response.getJSONArray("response").getJSONObject(0).getString("books")
                                    );
                                }

                                ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout6)).setVisibility(View.GONE);
                                ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout7)).setVisibility(View.GONE);
                                ((LinearLayout) aboutProfile_ll.findViewById(R.id.city_layout)).setVisibility(View.GONE);

                                if(json_response.getJSONArray("response").getJSONObject(0).isNull("interests") == true &&
                                        json_response.getJSONArray("response").getJSONObject(0).isNull("music") == true &&
                                        json_response.getJSONArray("response").getJSONObject(0).isNull("movies") == true &&
                                        json_response.getJSONArray("response").getJSONObject(0).isNull("tv") == true &&
                                        json_response.getJSONArray("response").getJSONObject(0).isNull("books") == true) {
                                    ((TextView) aboutProfile_ll.findViewById(R.id.interests_info)).setVisibility(View.GONE);
                                    ((View) aboutProfile_ll.findViewById(R.id.divider2)).setVisibility(View.GONE);
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout_all)).setVisibility(View.GONE);
                                }

                                ((TextView) ((LinearLayout) aboutProfile_ll.findViewById(R.id.tg_layout)).findViewById(R.id.telegram_label2)).setText(
                                        Html.fromHtml("<i>" + getResources().getString(R.string.not_implemented) + "</i>")
                                );

                                try {
                                    openVK_API.sendMethod("Friends.get", "user_id=" + profile_id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else if((send_request.startsWith("/method/Users.search")) && global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                try {
                                    send_request = ("/method/Users.get");
                                    openVK_API.sendMethod("Users.get", "user_ids=" + json_response.getJSONObject("response").getJSONArray("items").getJSONObject(0).getInt("id")  + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
                                } catch (Exception e) {
                                    try {
                                        if (json_response.getJSONObject("response").getJSONArray("items").getJSONObject(0).getString("id").equals("")) {
                                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                                            progress_ll.setVisibility(View.GONE);
                                            LinearLayout error_ll = findViewById(R.id.error_ll);
                                            error_ll.setVisibility(View.VISIBLE);
                                            ((TextView) error_ll.findViewById(R.id.error_text2)).setText(R.string.page_not_found);
                                            return;
                                        }
                                    } catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } if((send_request.startsWith("/method/Users.search")) && global_sharedPreferences.getString("currentLayout", "").equals("FriendsLayout")) {
                                try {
                                    send_request = ("/method/Friends.get");
                                    openVK_API.sendMethod("Friends.get", "user_id=" + json_response.getJSONObject("response").getJSONArray("items").getJSONObject(0).getInt("id")  + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
                                } catch (Exception e) {
                                    try {
                                        if (json_response.getJSONObject("response").getJSONArray("items").getJSONObject(0).getString("id").equals("")) {
                                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                                            progress_ll.setVisibility(View.GONE);
                                            LinearLayout error_ll = findViewById(R.id.error_ll);
                                            error_ll.setVisibility(View.VISIBLE);
                                            ((TextView) error_ll.findViewById(R.id.error_text2)).setText(R.string.page_not_found);
                                            return;
                                        }
                                    } catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else if((send_request.startsWith("/method/Friends.get")) && global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                                ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                ProfileCounterLayout friends_counter = ((LinearLayout) profileLayout.findViewById(R.id.profile_ext_header)).findViewById(R.id.friends_counter);
                                ((TextView) friends_counter.findViewById(R.id.profile_counter_value)).setText("" + json_response.getJSONObject("response").getInt("count"));
                                try {
                                    openVK_API.sendMethod("Wall.get", "owner_id=" + profile_id + "&extended=1&count=100");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if(send_request.startsWith("/method/Friends.get") && global_sharedPreferences.getString("currentLayout", "").equals("FriendsLayout")) {
                                loadFriends();
                            } else if(send_request.startsWith("/method/Wall.get") && global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                appendWallItem();
                            } else if(send_request.startsWith("/method/Likes.isLiked") && action.equals("add_like") && global_sharedPreferences.getString("currentLayout", "").equals("NewsLinearLayout")) {
                                if(json_response.getJSONObject("response").getInt("liked") == 0) {
                                    try {
                                        send_request = ("/method/Likes.add");
                                        openVK_API.sendMethod("Likes.add", "owner_id=" + owner_id + "&item_id=" + post_id + "&type=post");
                                        NewsListItem current_item = newsListItemArray.get(newsfeed_id);
                                        current_item.counters.likes = current_item.counters.likes + 1;
                                        current_item.counters.isLiked = true;
                                        newsListItemArray.set(newsfeed_id, current_item);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if(json_response.getJSONObject("response").getInt("liked") == 1) {
                                    try {
                                        send_request = ("/method/Likes.remove");
                                        openVK_API.sendMethod("Likes.remove", "owner_id=" + owner_id + "&item_id=" + post_id + "&type=post");
                                        NewsListItem current_item = newsListItemArray.get(newsfeed_id);
                                        current_item.counters.likes = current_item.counters.likes - 1;
                                        current_item.counters.isLiked = false;
                                        newsListItemArray.set(newsfeed_id, current_item);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                newsListAdapter = new NewsListAdapter(AppActivity.this, newsListItemArray);
                                news_listview = newsLinearLayout.findViewById(R.id.news_listview);
                                Parcelable state = news_listview.onSaveInstanceState();
                                news_listview.setAdapter(newsListAdapter);
                                news_listview.onRestoreInstanceState(state);
                            } else if(send_request.startsWith("/method/Likes.isLiked") && action.equals("add_like") && global_sharedPreferences.getString("currentLayout", "").equals("ProfileLayout")) {
                                if (json_response.getJSONObject("response").getInt("liked") == 0) {
                                    try {
                                        send_request = ("/method/Likes.add");
                                        openVK_API.sendMethod("Likes.add", "owner_id=" + owner_id + "&item_id=" + post_id + "&type=post");
                                        NewsListItem current_item = wallListItemArray.get(newsfeed_id);
                                        current_item.counters.likes = current_item.counters.likes + 1;
                                        current_item.counters.isLiked = true;
                                        wallListItemArray.set(newsfeed_id, current_item);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (json_response.getJSONObject("response").getInt("liked") == 1) {
                                    try {
                                        send_request = ("/method/Likes.remove");
                                        openVK_API.sendMethod("Likes.remove", "owner_id=" + owner_id + "&item_id=" + post_id + "&type=post");
                                        NewsListItem current_item = wallListItemArray.get(newsfeed_id);
                                        current_item.counters.likes = current_item.counters.likes - 1;
                                        current_item.counters.isLiked = false;
                                        wallListItemArray.set(newsfeed_id, current_item);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                NewsListAdapter wall_adapter = new NewsListAdapter(AppActivity.this, wallListItemArray);
                                View view = tabHost.getTabContentView().getChildAt(0);
                                WallLinearLayout posts_ll = view.findViewById(R.id.all_posts_wll);
                                ListView posts_lv = posts_ll.findViewById(R.id.news_listview);
                                Parcelable state = news_listview.onSaveInstanceState();
                                posts_lv.setAdapter(wall_adapter);
                                LinearLayout.LayoutParams layoutParams;
                                layoutParams = (LinearLayout.LayoutParams) posts_ll.getLayoutParams();
                                int listviewHeight = -1;
                                posts_lv.onRestoreInstanceState(state);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if(state == "connection_lost") {
                        if(creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLinearLayout.setVisibility(View.GONE);
                            error_ll.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = global_sharedPreferences.edit();
                            editor.putString("previousLayout", "");
                            editor.commit();
                        }
                    } else if(state == "timeout") {
                        if(creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLinearLayout.setVisibility(View.GONE);
                            error_ll.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = global_sharedPreferences.edit();
                            editor.putString("previousLayout", "");
                            editor.commit();
                        }
                    } else if(state == "no_connection") {
                        if(creating_another_activity == false) {
                            LinearLayout error_ll = findViewById(R.id.error_ll);
                            LinearLayout progress_ll = findViewById(R.id.news_progressll);
                            ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                            profileLayout.setVisibility(View.GONE);
                            progress_ll.setVisibility(View.GONE);
                            friendsLinearLayout.setVisibility(View.GONE);
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

    public void appendNewsItem() {
        NewsLinearLayout newsLinearLayout = findViewById(R.id.news_layout);
        try {
            if (send_request.startsWith("/method/Newsfeed.get")) {
                news_item_count = json_response.getJSONObject("response").getJSONArray("items").length();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("OpenVK Legacy", "News count: " + news_item_count + "\r\nJSON output: " + json_response.toString());
        if (news_item_count > 0) {
            String author = new String();
            if (send_request.startsWith("/method/Newsfeed.get")) {
                for (int news_item_index = 0; news_item_index < news_item_count; news_item_index++) {
                    try {
                        newsfeed = ((JSONArray) json_response.getJSONObject("response").getJSONArray("items"));
                        postOwnerId = ((JSONObject) newsfeed.get(news_item_index)).getInt("owner_id");
                        postAuthorId = ((JSONObject) newsfeed.get(news_item_index)).getInt("from_id");
                        if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("user_likes") == 0) {
                            if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("user_reposted") == 0) {
                                newsItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), false, false));
                            } else {
                                newsItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), false, true));
                            }
                        } else {
                            if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("user_reposted") == 0) {
                                newsItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), true, false));
                            } else {
                                newsItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), true, true));
                            }
                        }
                        newsListItemArray.add(new NewsListItem("(Unknown)", ((JSONObject) newsfeed.get(news_item_index))
                                .getInt("date"), null, ((JSONObject) newsfeed.get(news_item_index)).getString("text"), newsItemCountersInfoArray.get(news_item_index), null, null,
                                newsfeed.getJSONObject(news_item_index).getInt("owner_id"), newsfeed.getJSONObject(news_item_index).getInt("id"), getApplicationContext()));
                        NewsListItem item = newsListItemArray.get(news_item_index);
                        if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") < 0 && json_response.getJSONObject("response").isNull("groups") == false) {
                            for (int groups_index = 0; groups_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).length(); groups_index++) {
                                if (-newsfeed.getJSONObject(news_item_index).getInt("owner_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                        getJSONObject(groups_index).getInt("id")) {
                                    item.name = ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                            getJSONObject(groups_index).getString("name");
                                    newsListItemArray.set(news_item_index, item);
                                }
                            }
                        } else if(json_response.getJSONObject("response").isNull("profiles") == false) {
                            for (int users_index = 0; users_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).length(); users_index++) {
                                if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getInt("id")) {
                                    item.name = ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("first_name") + " " + ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("last_name");
                                    newsListItemArray.set(news_item_index, item);
                                }
                            }
                        }
                        if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") != newsfeed.getJSONObject(news_item_index).getInt("from_id") && newsfeed.getJSONObject(news_item_index).getInt("from_id") > 0) {
                            for (int users_index = 0; users_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).length(); users_index++) {
                                if (newsfeed.getJSONObject(news_item_index).getInt("from_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getInt("id")) {
                                    item.name = getResources().getString(R.string.on_wall, ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("first_name") + " " + ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("last_name"), item.name);
                                    newsListItemArray.set(news_item_index, item);
                                }
                            }
                        }

                        if (news_item_index == 0) {
                            post_owners_ids_sb.append(postOwnerId);
                        } else if (news_item_index > 0) {
                            post_owners_ids_sb.append("," + postOwnerId);
                        } else {
                            post_owners_ids_sb.append(postOwnerId);
                        }
                        if (((JSONObject) newsfeed.get(news_item_index)).isNull("attachments") == false) {
                            // loadPhotos(news_item_index); > does not work with openvk.uk instance (401 code returned)
                        }
                    } catch (JSONException jEx) {
                        jEx.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Log.d("OpenVK Legacy", "Done!");
        newsListAdapter = new NewsListAdapter(this, newsListItemArray);
        news_listview = newsLinearLayout.findViewById(R.id.news_listview);
        news_listview.setAdapter(newsListAdapter);
        LinearLayout progress_ll = findViewById(R.id.news_progressll);
        ProfileLayout profile_ll = findViewById(R.id.profile_layout);
        friendsLinearLayout.setVisibility(View.GONE);
        profile_ll.setVisibility(View.GONE);
        progress_ll.setVisibility(View.GONE);
        newsLinearLayout.setVisibility(View.VISIBLE);
    }

    private void appendWallItem() {
        wallListItemArray.clear();
        wallItemCountersInfoArray.clear();
        try {
            if (send_request.startsWith("/method/Wall.get")) {
                news_item_count = json_response.getJSONObject("response").getJSONArray("items").length();
                if(news_item_count > 50) {
                    news_item_count = 50;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("OpenVK Legacy", "Wall posts count: " + news_item_count + "\r\nJSON output: " + json_response.toString());
        if (news_item_count > 0) {
            String author = new String();
            if (send_request.startsWith("/method/Wall.get")) {
                for (int news_item_index = 0; news_item_index < news_item_count; news_item_index++) {
                    try {
                        newsfeed = ((JSONArray) json_response.getJSONObject("response").getJSONArray("items"));
                        postOwnerId = ((JSONObject) newsfeed.get(news_item_index)).getInt("owner_id");
                        postAuthorId = ((JSONObject) newsfeed.get(news_item_index)).getInt("from_id");
                        if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("user_likes") == 0) {
                            if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("user_reposted") == 0) {
                                wallItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), false, false));
                            } else {
                                wallItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), false, true));
                            }
                        } else {
                            if (((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("user_reposted") == 0) {
                                wallItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), true, false));
                            } else {
                                wallItemCountersInfoArray.add(new NewsItemCountersInfo(((JSONObject) newsfeed.get(news_item_index)).getJSONObject("likes").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).
                                        getJSONObject("comments").getInt("count"), ((JSONObject) newsfeed.get(news_item_index)).getJSONObject("reposts").getInt("count"), true, true));
                            }
                        }
                        wallListItemArray.add(new NewsListItem("(Unknown)", ((JSONObject) newsfeed.get(news_item_index))
                                .getInt("date"), null, ((JSONObject) newsfeed.get(news_item_index)).getString("text"), wallItemCountersInfoArray.get(news_item_index), null, null,
                                newsfeed.getJSONObject(news_item_index).getInt("owner_id"), newsfeed.getJSONObject(news_item_index).getInt("id"), getApplicationContext()));
                        NewsListItem item = wallListItemArray.get(news_item_index);
                        if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") < 0 && json_response.getJSONObject("response").isNull("groups") == false) {
                            for (int groups_index = 0; groups_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).length(); groups_index++) {
                                if (-newsfeed.getJSONObject(news_item_index).getInt("owner_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                        getJSONObject(groups_index).getInt("id")) {
                                    item.name = ((JSONArray) json_response.getJSONObject("response").getJSONArray("groups")).
                                            getJSONObject(groups_index).getString("name");
                                    wallListItemArray.set(news_item_index, item);
                                }
                            }
                        } else if(json_response.getJSONObject("response").isNull("profiles") == false) {
                            for (int users_index = 0; users_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).length(); users_index++) {
                                if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getInt("id")) {
                                    item.name = ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("first_name") + " " + ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("last_name");
                                    wallListItemArray.set(news_item_index, item);
                                }
                            }
                        }
                        if (newsfeed.getJSONObject(news_item_index).getInt("owner_id") != newsfeed.getJSONObject(news_item_index).getInt("from_id") && newsfeed.getJSONObject(news_item_index).getInt("from_id") > 0) {
                            for (int users_index = 0; users_index < ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).length(); users_index++) {
                                if (newsfeed.getJSONObject(news_item_index).getInt("from_id") == ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                        getJSONObject(users_index).getInt("id")) {
                                    item.name = getResources().getString(R.string.on_wall, ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("first_name") + " " + ((JSONArray) json_response.getJSONObject("response").getJSONArray("profiles")).
                                            getJSONObject(users_index).getString("last_name"), item.name);
                                    wallListItemArray.set(news_item_index, item);
                                }
                            }
                        }

                        if (news_item_index == 0) {
                            post_owners_ids_sb.append(postOwnerId);
                        } else if (news_item_index > 0) {
                            post_owners_ids_sb.append("," + postOwnerId);
                        } else {
                            post_owners_ids_sb.append(postOwnerId);
                        }
                        if (((JSONObject) newsfeed.get(news_item_index)).isNull("attachments") == false) {
                            // loadPhotos(news_item_index); > does not work with openvk.uk instance (401 code returned)
                        }
                    } catch (JSONException jEx) {
                        jEx.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Log.d("OpenVK Legacy", "Done!");
        NewsListAdapter all_posts_adapter = new NewsListAdapter(this, wallListItemArray);
        View view = tabHost.getTabContentView().getChildAt(0);
        WallLinearLayout posts_ll = view.findViewById(R.id.all_posts_wll);
        ListView posts_lv = posts_ll.findViewById(R.id.news_listview);
        posts_lv.setAdapter(all_posts_adapter);
        LinearLayout.LayoutParams layoutParams;
        layoutParams = (LinearLayout.LayoutParams) posts_ll.getLayoutParams();
        int listviewHeight = -1;
        Log.d("LayoutParams", "Height: " + layoutParams.height + "px");
        for(int items_index = 0; items_index < all_posts_adapter.getCount(); items_index++) {
            View view_from_adapter = all_posts_adapter.getView(items_index, null, posts_lv);
            view_from_adapter.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            listviewHeight += view_from_adapter.getMeasuredHeight();
        }
        layoutParams.height = listviewHeight + (int)(80 * getResources().getDisplayMetrics().scaledDensity);
        posts_ll.setLayoutParams(layoutParams);
        profileLayout.setVisibility(View.VISIBLE);
        LinearLayout progress_ll = findViewById(R.id.news_progressll);
        progress_ll.setVisibility(View.GONE);
    }

    private void loadPhotos(int pos) {
        try {
                attachments = ((JSONObject) newsfeed.get(pos)).getJSONArray("attachments");
                int attachments_length = attachments.length();
                Log.d("OpenVK Legacy", "Downloading photos...");
                for (int i = 0; i < attachments_length; i++) {
                    if(((JSONObject) newsfeed.get(pos)).isNull("attachments") == false) {
                        String url = attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").
                                getJSONObject(0).getString("url");
                        if (attachments.getJSONObject(i).getString("type").equals("photo")) {
                            if (url.startsWith("https://")) {
                                openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                                        url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1));
                            } else {
                                openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                                        url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1));
                            }
                        }
                    } else {
                        Log.e("OpenVK Legacy", "No attachments");
                    }
                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(menu_is_closed == false) {
            openSlidingMenu();
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
            friendsLinearLayout.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getActionBar().setTitle(getResources().getString(R.string.friends));
            } else {
                titlebar_title = findViewById(R.id.titlebar_title);
                titlebar_title.setText(getResources().getString(R.string.friends));
            }
        }
    }
}
