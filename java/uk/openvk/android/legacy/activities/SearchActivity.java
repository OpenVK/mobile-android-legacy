package uk.openvk.android.legacy.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

import uk.openvk.android.legacy.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.items.GroupPostInfo;
import uk.openvk.android.legacy.items.SearchResultItem;
import uk.openvk.android.legacy.items.UserPostInfo;
import uk.openvk.android.legacy.layouts.FullListView;
import uk.openvk.android.legacy.layouts.SearchResultsLayout;

public class SearchActivity extends Activity {
    public String server;
    public String state;
    public String auth_token;
    private UpdateUITask updateUITask;
    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public JSONObject json_response;
    public JSONArray newsfeed;
    public JSONArray attachments;
    public String connectionErrorString;
    public boolean connection_status;
    public String send_request;
    public Boolean inputStream_isClosed;
    public SharedPreferences global_sharedPreferences;
    public ArrayList<SearchResultItem> usersSearchResultsArray;
    public ArrayList<GroupPostInfo> groupPostInfoArray;
    public ArrayList<UserPostInfo> userPostInfoArray;
    public StringBuilder groups_sb;
    public StringBuilder users_sb;
    public int owner_id;
    public OvkAPIWrapper openVK_API;
    public static final int UPDATE_UI = 0;
    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        inputStream_isClosed = false;
        server = getApplicationContext().getSharedPreferences("instance", 0).getString("server", "");
        auth_token = getApplicationContext().getSharedPreferences("instance", 0).getString("auth_token", "");
        owner_id = getApplicationContext().getSharedPreferences("instance", 0).getInt("user_id", 0);

        openVK_API = new OvkAPIWrapper(SearchActivity.this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));

        final EditText search_edit = findViewById(R.id.search_edit);
        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = search_edit.getText().toString();
                    try {
                        send_request = "/method/Users.search";
                        openVK_API.sendMethod("Users.search", "q=" + query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                return false;
            }
        });

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            final TextView titlebar_title = findViewById(R.id.titlebar_title);
            titlebar_title.setText(getResources().getString(R.string.new_status));
            final ImageButton back_btn = findViewById(R.id.backButton);
            final ImageButton ovk_btn = findViewById(R.id.ovkButton);
            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            ovk_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            titlebar_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        updateUITask = new UpdateUITask();
        usersSearchResultsArray = new ArrayList<SearchResultItem>();
        groupPostInfoArray = new ArrayList<GroupPostInfo>();
        userPostInfoArray = new ArrayList<UserPostInfo>();

        if(owner_id == 0) {
            finish();
        }
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
                        connectionErrorString = msg.getData().getString("Error_message");
                        updateUITask.run();
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }

    public void hideSelectedItemBackground(int position) {
        final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
        FullListView people_listview = searchResultsLayout.findViewById(R.id.people_listview);
        people_listview.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void showProfile(int position) {
        String url = "openvk://profile/" + "id" + usersSearchResultsArray.get(position).id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.putExtra("fromLayout", global_sharedPreferences.getString("currentLayout", ""));
        startActivity(i);
    }

    class UpdateUITask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state.equals("getting_response")) {
                        try {
                            if(state.equals("getting_response")) {
                                if((send_request.startsWith("/method/Users.search"))) {
                                    try {
                                        int results_count = json_response.getJSONObject("response").getInt("count");
                                        if(results_count > 0) {
                                            users_sb = new StringBuilder();
                                            for (int result_index = 0; result_index < results_count; result_index++) {
                                                users_sb.append("," + json_response.getJSONObject("response").getJSONArray("items").getJSONObject(result_index).getInt("id"));
                                            }
                                            if(users_sb.toString().length() > 0) {
                                                openVK_API.sendMethod("Users.get", "user_ids=" + users_sb.toString() + "&fields=last_seen,status,sex,interests,music,movies,city,books,verified");
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if(send_request.startsWith("/method/Users.get")) {
                                    try {
                                        appendUsersSearchResultsList();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if(state.equals("no_connection")) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                            } else if(state.equals("timeout")) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void appendUsersSearchResultsList() {
        try {
            usersSearchResultsArray.clear();

            final SearchResultsLayout searchResultsLayout = findViewById(R.id.sr_ll);
            ((LinearLayout) searchResultsLayout.findViewById(R.id.people_ll)).setVisibility(View.VISIBLE);

            int results_count = json_response.getJSONArray("response").length();

            if(results_count > 0) {
                for (int result_index = 0; result_index < results_count; result_index++) {
                    try {
                        if(json_response.getJSONArray("response").getJSONObject(result_index).isNull("city") == false) {
                            usersSearchResultsArray.add(new SearchResultItem(json_response.getJSONArray("response").getJSONObject(result_index).getInt("id"),
                                    json_response.getJSONArray("response").getJSONObject(result_index).getString("first_name") + " " +
                                            json_response.getJSONArray("response").getJSONObject(result_index).getString("last_name"),
                                    json_response.getJSONArray("response").getJSONObject(result_index).getString("city"), null, 0));
                        } else {
                            usersSearchResultsArray.add(new SearchResultItem(json_response.getJSONArray("response").getJSONObject(result_index).getInt("id"),
                                    json_response.getJSONArray("response").getJSONObject(result_index).getString("first_name") + " " +
                                            json_response.getJSONArray("response").getJSONObject(result_index).getString("last_name"),
                                    "", null, json_response.getJSONArray("response").getJSONObject(result_index).getInt("online")));

                        }
                    } catch (Exception e) {

                    }
                }
                SearchResultAdapter usersSearchResultsAdapter = new SearchResultAdapter(SearchActivity.this, usersSearchResultsArray);
                FullListView people_listview = searchResultsLayout.findViewById(R.id.people_listview);
                people_listview.setAdapter(usersSearchResultsAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
