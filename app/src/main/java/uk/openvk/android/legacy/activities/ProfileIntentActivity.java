package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.Application;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.items.GroupPostInfo;
import uk.openvk.android.legacy.items.NewsItemCountersInfo;
import uk.openvk.android.legacy.items.ProfileItem;
import uk.openvk.android.legacy.layouts.AboutProfileLayout;
import uk.openvk.android.legacy.layouts.FriendsLayout;
import uk.openvk.android.legacy.layouts.NewsLayout;
import uk.openvk.android.legacy.layouts.ProfileCounterLayout;
import uk.openvk.android.legacy.layouts.ProfileHeader;
import uk.openvk.android.legacy.layouts.ProfileLayout;
import uk.openvk.android.legacy.layouts.SlidingMenuLayout;
import uk.openvk.android.legacy.layouts.WallLayout;
import uk.openvk.android.legacy.list_adapters.FriendsListAdapter;
import uk.openvk.android.legacy.list_adapters.NewsListAdapter;
import uk.openvk.android.legacy.list_items.FriendsListItem;
import uk.openvk.android.legacy.list_items.NewsListItem;
import uk.openvk.android.legacy.list_items.SlidingMenuItem;

public class ProfileIntentActivity extends Activity {
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
    public ArrayList<SlidingMenuItem> slidingMenuItemArray;
    public ArrayList<GroupPostInfo> groupPostInfoArray;
    public ArrayList<NewsListItem> wallListItemArray;
    public NewsListAdapter newsListAdapter;
    public int postAuthorId;
    public int postOwnerId;
    public String send_request;
    public ListView news_listview;
    public int news_item_count;
    public int news_item_index;
    public SharedPreferences global_sharedPreferences;
    public ArrayList<Integer> post_author_ids;
    public StringBuilder post_author_ids_sb;
    public StringBuilder post_group_ids_sb;
    public StringBuilder post_owners_ids_sb;
    public NewsLayout newsLayout;
    public FriendsLayout friendsLayout;
    public ProfileLayout profileLayout;
    public boolean sliding_animated;
    public boolean menu_is_closed;
    public int profile_id;
    public ArrayList<NewsItemCountersInfo> newsItemCountersInfoArray;
    public ArrayList<NewsItemCountersInfo> wallItemCountersInfoArray;
    public ArrayList<Bitmap> attachments_photo;
    public ProfileItem profileItem;
    public TabHost tabHost;
    public boolean about_profile_opened;
    public static Handler handler;
    public static final int UPDATE_UI = 0;
    public static final int GET_PICTURE = 1;
    public ArrayList<FriendsListItem> friendsListItemArray;
    public FriendsListAdapter friendsListAdapter;
    public Intent address_intent;
    public String fromLayout;
    public String action;
    public int post_id;
    public int owner_id;
    public int newsfeed_id;
    public View news_item;
    public String from;
    public Bitmap photo_bmp;
    public int newsfeed_picpost_id;
    public NewsListAdapter wallListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_intent_layout);
        address_intent = getIntent();
        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor sharedPrefsEditor = global_sharedPreferences.edit();
        sharedPrefsEditor.putString("previousLayout", "");
        sharedPrefsEditor.commit();
        wallListAdapter = new NewsListAdapter(this, wallListItemArray);
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
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nClass name: " + ProfileIntentActivity.class.getSimpleName() + "\r\nConnection state: " + state + "\r\nAPI method: " + send_request);
                        updateUITask.run();
                        break;
                    case GET_PICTURE:
                        state = msg.getData().getString("State");
                        from = msg.getData().getString("From");
                        photo_bmp = (Bitmap) msg.getData().getParcelable("Picture");
                        newsfeed_picpost_id = msg.getData().getInt("ID");
                        Log.d("OpenVK Legacy", "Getting handle message!\r\nConnection state: " + state + "\r\nDownloaded picture!");
                        updateUITask.run();
                }
            }
        };

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("instance", 0);
        server = sharedPreferences.getString("server", "");

        profileLayout = findViewById(R.id.profile_layout);

        profileLayout.findViewById(R.id.send_direct_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getConversationFromProfile();
            }
        });

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

            openVK_API = new OvkAPIWrapper(ProfileIntentActivity.this, server, sharedPreferences.getString("auth_token", ""), json_response, global_sharedPreferences.getBoolean("useHTTPS", true));

            if(path.startsWith("openvk://profile/")) {
                String args = path.substring("openvk://profile/".length());
                global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileIntentActivity.this);
                auth_token = sharedPreferences.getString("auth_token", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.icon);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    getActionBar().setTitle(R.string.profile);
                }
            }
        } else {
            openVK_API = new OvkAPIWrapper(this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));
        }


        if(sharedPreferences.getString("auth_token", "").length() == 0) {
            Intent intent = new Intent(ProfileIntentActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        final ProfileCounterLayout friends_counter = profileLayout.findViewById(R.id.friends_counter);
        friends_counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url;
                url = "openvk://friends/id" + profile_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.putExtra("fromLayout", global_sharedPreferences.getString("intentLayout", ""));
                startActivity(i);
            }
        });

        final SlidingMenuLayout slidingMenuLayout = findViewById(R.id.sliding_menu_layout);

        TextView profile_name = slidingMenuLayout.findViewById(R.id.profile_name);
        profile_name.setText(getResources().getString(R.string.loading));
        final ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
        final AboutProfileLayout aboutProfile_ll = findViewById(R.id.about_profile_layout);
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
        response_sb = new StringBuilder();
        json_response_user = new JSONObject();
        json_response_group = new JSONObject();
        attachments = new JSONArray();
        newsItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
        wallItemCountersInfoArray = new ArrayList<NewsItemCountersInfo>();
        friendsListItemArray = new ArrayList<FriendsListItem>();
        wallListItemArray = new ArrayList<NewsListItem>();
        post_owners_ids_sb = new StringBuilder();
        post_author_ids_sb = new StringBuilder();
        post_group_ids_sb = new StringBuilder();
        post_author_ids = new ArrayList<Integer>();
        server_2 = new String();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }
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
                if (path.startsWith("openvk://profile/id")) {
                    openVK_API.sendMethod("Users.get", "user_ids=" + path.substring(("openvk://profile/id").length()) + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified,photo_100");
                } else {
                    openVK_API.sendMethod("Users.search", "q=" + path.substring("openvk://profile".length() + 1));
                }
            }
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
                    if (uri != null) {
                        String path = uri.toString();
                        if (path.startsWith("openvk://profile/id")) {
                            openVK_API.sendMethod("Users.get", "user_ids=" + path.substring(("openvk://profile/id").length()) + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified,photo_100");
                        } else {
                            openVK_API.sendMethod("Users.search", "q=" + path.substring("openvk://profile".length()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        TextView profile_name_tv = slidingMenuLayout.findViewById(R.id.profile_name);
        profile_name_tv.setTextColor(Color.WHITE);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            resizeTranslucentLayout();
        }
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

    public void getConversationFromProfile() {
        if(profileItem != null) {
            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
            intent.putExtra("peer_id", profileItem.id);
            intent.putExtra("conv_title", profileItem.name);
            intent.putExtra("online", profileItem.online);
            startActivity(intent);
        }
    }

    public void onSimpleListItemClicked(int position) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if(position == 0) {
                creating_another_activity = true;
                popup_menu.dismiss();
            } else if(position == 1) {
                popup_menu.dismiss();
                AlertDialog about_dlg;
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileIntentActivity.this);
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
        View view = tabHost.getTabContentView().getChildAt(0);
        WallLayout posts_ll = view.findViewById(R.id.all_posts_wll);
        ListView posts_lv = posts_ll.findViewById(R.id.news_listview);
        posts_lv.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + friendsListItemArray.get(position).id;
        Log.d("OpenVK Legacy", "Item ID: " + position + " | User ID: " + friendsListItemArray.get(position).id);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.putExtra("fromLayout", global_sharedPreferences.getString("intentLayout", ""));
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_FROM_BACKGROUND);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void addLike(int position, String type, View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("instance", 0);
        send_request = ("/method/Likes.isLiked");
        action = "add_like";
        post_id = wallListItemArray.get(position).post_id;
        owner_id = wallListItemArray.get(position).owner_id;
        newsfeed_id = position;
        openVK_API.sendMethod("Likes.isLiked", "owner_id=" + owner_id + "&user_id=" + sharedPreferences.getInt("user_id", 0) +
                "&item_id=" + post_id + "&type=" + type);
        news_item = view;
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
                                    Intent intent = new Intent(ProfileIntentActivity.this, AuthenticationActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (json_response.getInt("error_code") == 3) {
                                    AlertDialog outdated_api_dlg;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileIntentActivity.this);
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileIntentActivity.this);
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
                                if ((send_request.startsWith("/method/Users.search"))) {
                                    try {
                                        send_request = ("/method/Users.get");
                                        openVK_API.sendMethod("Users.get", "user_ids=" + json_response.getJSONObject("response").getJSONArray("items").getJSONObject(0).getInt("id") + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
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
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                } else if ((send_request.startsWith("/method/Users.get"))) {
                                    ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                                    ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                    final AboutProfileLayout aboutProfile_ll = findViewById(R.id.about_profile_layout);
                                    try {
                                        profile_id = json_response.getJSONArray("response").getJSONObject(0).getInt("id");
                                    } catch (Exception ex) {
                                        try {
                                            if (json_response.getJSONArray("response").getJSONObject(0).getString("id").equals("")) {
                                                LinearLayout progress_ll = findViewById(R.id.news_progressll);
                                                progress_ll.setVisibility(View.GONE);
                                                LinearLayout error_ll = findViewById(R.id.error_ll);
                                                error_ll.setVisibility(View.VISIBLE);
                                                ((TextView) error_ll.findViewById(R.id.error_text2)).setText(R.string.page_not_found);
                                                return;
                                            }
                                        } catch (Exception ex2) {
                                            ex.printStackTrace();
                                        }
                                    }
                                    profileItem = new ProfileItem(json_response.getJSONArray("response").getJSONObject(0).getString("first_name") + " " + json_response.getJSONArray("response").getJSONObject(0).getString("last_name"), profile_id, json_response.getJSONArray("response").getJSONObject(0).getInt("online"));
                                    String name = json_response.getJSONArray("response").getJSONObject(0).getString("first_name") + " " + json_response.getJSONArray("response").getJSONObject(0).getString("last_name") + "  ";
                                    if (tabHost.getTabWidget().getTabCount() > 1) {
                                        View view = tabHost.getTabWidget().getChildAt(1);
                                        if (view != null) {
                                            TextView title = view.findViewById(android.R.id.title);
                                            title.setText(getResources().getString(R.string.wall_owners_posts, json_response.getJSONArray("response").getJSONObject(0).getString("first_name")));
                                        }
                                    }

                                    SpannableStringBuilder sb = new SpannableStringBuilder(name);
                                    if (json_response.getJSONArray("response").getJSONObject(0).getInt("verified") == 1) {
                                        ImageSpan imageSpan = new ImageSpan(getApplicationContext(), R.drawable.ic_verified, DynamicDrawableSpan.ALIGN_BASELINE);
                                        sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                    ((TextView) profileHeader.findViewById(R.id.profile_name)).setText(sb);
                                    String status = "";
                                    if (json_response.getJSONArray("response").getJSONObject(0).has("status")) {
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

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("interests") == true) {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout)).setVisibility(View.GONE);
                                    } else {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.interests_label2)).setText(
                                                json_response.getJSONArray("response").getJSONObject(0).getString("interests")
                                        );
                                    }

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("music") == true) {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout2)).setVisibility(View.GONE);
                                    } else {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.music_label2)).setText(
                                                json_response.getJSONArray("response").getJSONObject(0).getString("music")
                                        );
                                    }

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("movies") == true) {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout3)).setVisibility(View.GONE);
                                    } else {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.movies_label2)).setText(
                                                json_response.getJSONArray("response").getJSONObject(0).getString("movies")
                                        );
                                    }

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("tv") == true) {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout4)).setVisibility(View.GONE);

                                    } else {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.movies_label2)).setText(
                                                json_response.getJSONArray("response").getJSONObject(0).getString("tv")
                                        );
                                    }

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("books") == true) {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout5)).setVisibility(View.GONE);

                                    } else {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.books_label2)).setText(
                                                json_response.getJSONArray("response").getJSONObject(0).getString("books")
                                        );
                                    }

                                    if(json_response.getJSONArray("response").getJSONObject(0).has("photo_100")) {
                                        loadProfileAvatars(json_response.getJSONArray("response").getJSONObject(0).getInt("id"), json_response.getJSONArray("response").getJSONObject(0).getString("photo_100"), "user");
                                    }

                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout6)).setVisibility(View.GONE);
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout7)).setVisibility(View.GONE);
                                    ((LinearLayout) aboutProfile_ll.findViewById(R.id.city_layout)).setVisibility(View.GONE);

                                    if (json_response.getJSONArray("response").getJSONObject(0).isNull("interests") == true &&
                                            json_response.getJSONArray("response").getJSONObject(0).isNull("music") == true &&
                                            json_response.getJSONArray("response").getJSONObject(0).isNull("movies") == true &&
                                            json_response.getJSONArray("response").getJSONObject(0).isNull("tv") == true &&
                                            json_response.getJSONArray("response").getJSONObject(0).isNull("books") == true) {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.interests_info)).setVisibility(View.GONE);
                                        ((View) aboutProfile_ll.findViewById(R.id.divider2)).setVisibility(View.GONE);
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.interests_layout_all)).setVisibility(View.GONE);
                                    }

                                    if(json_response.getJSONArray("response").getJSONObject(0).has("bdate_visibility") && json_response.getJSONArray("response").getJSONObject(0).getInt("bdate_visibility") > 0) {
                                        ((TextView) aboutProfile_ll.findViewById(R.id.birthday_label2)).setText(json_response.getJSONArray("response").getJSONObject(0).getString("bdate").split(".")[0] +
                                                getResources().getStringArray(Integer.valueOf(json_response.getJSONArray("response").getJSONObject(0).getString("bdate").split(".")[1]) - 1) +
                                                json_response.getJSONObject("response").getString("bdate").split(".")[2]);
                                    } else {
                                        ((LinearLayout) aboutProfile_ll.findViewById(R.id.birthdate_ll)).setVisibility(View.GONE);
                                    }

                                    ((TextView) ((LinearLayout) aboutProfile_ll.findViewById(R.id.tg_layout)).findViewById(R.id.telegram_label2)).setText(
                                            Html.fromHtml("<i>" + getResources().getString(R.string.not_implemented) + "</i>")
                                    );

                                    try {
                                        openVK_API.sendMethod("Friends.get", "user_id=" + profile_id);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if ((send_request.startsWith("/method/Friends.get"))) {
                                    ProfileLayout profileLayout = findViewById(R.id.profile_layout);
                                    ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                    ProfileCounterLayout friends_counter = ((LinearLayout) profileLayout.findViewById(R.id.profile_ext_header)).findViewById(R.id.friends_counter);
                                    ((TextView) friends_counter.findViewById(R.id.profile_counter_value)).setText("" + json_response.getJSONObject("response").getInt("count"));
                                    try {
                                        openVK_API.sendMethod("Wall.get", "owner_id=" + profile_id + "&extended=1&count=25");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (send_request.startsWith("/method/Wall.get")) {
                                    appendWallItem();
                                } else if (send_request.startsWith("/method/Likes.isLiked") && action.equals("add_like")) {
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
                                    NewsListAdapter wall_adapter = new NewsListAdapter(ProfileIntentActivity.this, wallListItemArray);
                                    View view = tabHost.getTabContentView().getChildAt(0);
                                    WallLayout posts_ll = view.findViewById(R.id.all_posts_wll);
                                    RecyclerView posts_lv = posts_ll.findViewById(R.id.news_listview);
                                    posts_lv.setAdapter(wall_adapter);
                                    LinearLayout.LayoutParams layoutParams;
                                    layoutParams = (LinearLayout.LayoutParams) posts_ll.getLayoutParams();
                                    int listviewHeight = -1;
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if(state.equals("getting_picture")) {
                        try {
                            if(from.equals("wall")) {
                                if (wallListItemArray.size() > newsfeed_picpost_id) {
                                    NewsListItem newsListItem = wallListItemArray.get(newsfeed_picpost_id);
                                    if (newsListItem.repost == null) {
                                        newsListItem.photo = photo_bmp;
                                        Log.d("OpenVK Legacy", "Post ID: " + newsfeed_picpost_id + "\r\nCount: " + wallListItemArray.size());
                                        wallListItemArray.set(newsfeed_picpost_id, newsListItem);
                                        WallLayout wall_layout = profileLayout.findViewById(R.id.all_posts_wll);
                                        RecyclerView wall_listview = wall_layout.findViewById(R.id.news_listview);
                                        if (wallListAdapter != null) {
                                            wallListAdapter.notifyItemChanged(newsfeed_picpost_id);
                                        } else {
                                            wallListAdapter = new NewsListAdapter(ProfileIntentActivity.this, wallListItemArray);
                                            wall_listview.setAdapter(wallListAdapter);
                                        }
                                    }
                                }
                            } else if (from.equals("profile_avatar")) {
                                ProfileHeader profileHeader = profileLayout.findViewById(R.id.profile_header);
                                ImageView profileAvatar = profileHeader.findViewById(R.id.profile_photo);
                                profileAvatar.setImageBitmap(photo_bmp);
                            } else if (from.equals("wall_author_avatar")) {
                                if(wallListItemArray.size() > newsfeed_picpost_id) {
                                    NewsListItem newsListItem = wallListItemArray.get(newsfeed_picpost_id);
                                    newsListItem.avatar = photo_bmp;
                                    if (newsListAdapter != null) {
                                        if (newsListItem.repost == null) {
                                            wallListItemArray.set(newsfeed_picpost_id, newsListItem);
                                            WallLayout wall_layout = profileLayout.findViewById(R.id.all_posts_wll);
                                            RecyclerView wall_listview = wall_layout.findViewById(R.id.news_listview);
                                            NewsListAdapter wallListAdapter = (NewsListAdapter) wall_listview.getAdapter();
                                            if(wallListAdapter != null) {
                                                wallListAdapter.notifyItemChanged(newsfeed_picpost_id);
                                            }
                                        }
                                    } else {
                                        NewsListAdapter wallListAdapter = new NewsListAdapter(ProfileIntentActivity.this, wallListItemArray);
                                        WallLayout wall_layout = profileLayout.findViewById(R.id.all_posts_wll);
                                        RecyclerView wall_listview = wall_layout.findViewById(R.id.news_listview);
                                        wall_listview.setAdapter(wallListAdapter);
                                    }
                                }
                            }
                        } catch(Exception ex) {
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
                            if(friendsLayout != null) {
                                friendsLayout.setVisibility(View.GONE);
                            }
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
            String author;
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
                                    loadWallAvatars(news_item_index, json_response.getJSONObject("response").getJSONArray("groups").getJSONObject(groups_index).getString("photo_50"), "group");
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
                                    loadWallAvatars(news_item_index, json_response.getJSONObject("response").getJSONArray("profiles").getJSONObject(users_index).getString("photo_50"), "user");
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
                                    loadWallAvatars(news_item_index, json_response.getJSONObject("response").getJSONArray("profiles").getJSONObject(users_index).getString("photo_50"), "user");
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
                            loadWallPhotos(news_item_index);
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
        View view = tabHost.getTabContentView().getChildAt(0);
        wallListAdapter = new NewsListAdapter(this, wallListItemArray);
        WallLayout posts_ll = view.findViewById(R.id.all_posts_wll);
        RecyclerView posts_lv = posts_ll.findViewById(R.id.news_listview);
        posts_lv.setAdapter(wallListAdapter);
        profileLayout.setVisibility(View.VISIBLE);
        LinearLayout progress_ll = findViewById(R.id.news_progressll);
        progress_ll.setVisibility(View.GONE);
    }

    private void loadWallPhotos(int pos) {
        try {
            if (((JSONObject) newsfeed.get(pos)).isNull("attachments") == false) {
                final Runtime runtime = Runtime.getRuntime();
                final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
                final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
                final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
                post_id = pos;
                attachments = ((JSONObject) newsfeed.get(pos)).getJSONArray("attachments");
                int attachments_length = attachments.length();
                for (int i = 0; i < attachments_length; i++) {
                    if (((JSONObject) newsfeed.get(pos)).isNull("attachments") == false) {
                        if(attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").length() >= 9) {
                            if (availHeapSizeInMB < 192) {
                                String url = attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").
                                        getJSONObject(4).getString("url");
                                if (attachments.getJSONObject(i).getString("type").equals("photo")) {
                                    if (url.startsWith("https://")) {
                                        openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                                                url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                    } else {
                                        openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                                                url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                    }
                                }
                            } else if (availHeapSizeInMB >= 192) {
                                String url = attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").
                                        getJSONObject(8).getString("url");
                                if (attachments.getJSONObject(i).getString("type").equals("photo")) {
                                    if (url.startsWith("https://")) {
                                        openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                                                url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                    } else {
                                        openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                                                url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                    }
                                }
                            }
                        } else if(attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").length() == 5) {
                            String url = attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").
                                    getJSONObject(4).getString("url");
                            if (attachments.getJSONObject(i).getString("type").equals("photo")) {
                                if (url.startsWith("https://")) {
                                    openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                                            url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                } else {
                                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                }
                            }
                        } else if(attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").length() < 5) {
                            String url = attachments.getJSONObject(i).getJSONObject("photo").getJSONArray("sizes").
                                    getJSONObject(0).getString("url");
                            if (attachments.getJSONObject(i).getString("type").equals("photo")) {
                                if (url.startsWith("https://")) {
                                    openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                                            url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                } else {
                                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_cache_" + post_id, post_id, "wall");
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadProfileAvatars(int profile_id, String url, String type) {
        try {
            final Runtime runtime = Runtime.getRuntime();
            final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
            final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
            if(url.startsWith("http://")) {
                if(type.equals("group")) {
                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "profile_avatar_cache_" + profile_id, profile_id, "profile_avatar");
                } else {
                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "profile_avatar_cache_" + profile_id, profile_id, "profile_avatar");
                }
            } else {
                openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                        url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "profile_avatar_cache_" + profile_id, profile_id, "profile_avatar");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWallAvatars(int pos, String url, String type) {
        try {
            final Runtime runtime = Runtime.getRuntime();
            final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
            final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
            post_id = pos;
            if(url.startsWith("http://")) {
                if(type.equals("group")) {
                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_avatar_cache_" + post_id, post_id, "wall_author_avatar");
                } else {
                    openVK_API.downloadRaw(url.substring("http://".length()).split("/")[0],
                            url.substring("http://".length() + url.substring("http://".length()).split("/")[0].length() + 1), "wall_avatar_cache_" + post_id, post_id, "wall_author_avatar");
                }
            } else {
                openVK_API.downloadRaw(url.substring("https://".length()).split("/")[0],
                        url.substring("https://".length() + url.substring("https://".length()).split("/")[0].length() + 1), "wall_avatar_cache_" + post_id, post_id, "wall_author_avatar");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

