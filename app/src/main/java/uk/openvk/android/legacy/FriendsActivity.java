package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

public class FriendsActivity extends Activity {
    public String server;
    public String state;
    public String auth_token;
    private FriendsActivity.UpdateUITask updateUITask;
    public boolean connection_status;
    public String send_request;
    public Boolean inputStream_isClosed;
    public SharedPreferences global_sharedPreferences;
    public int owner_id;
    public OvkAPIWrapper openVK_API;
    private JSONObject json_response;
    public static final int UPDATE_UI = 0;
    public static Handler handler;
    public ArrayList<FriendsListItem> friendsListItemArray;
    public FriendsListAdapter friendsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActionBar().setHomeButtonEnabled(true);
            }
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        global_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        inputStream_isClosed = new Boolean(false);
        server = getApplicationContext().getSharedPreferences("instance", 0).getString("server", "");
        auth_token = getApplicationContext().getSharedPreferences("instance", 0).getString("auth_token", "");
        owner_id = getApplicationContext().getSharedPreferences("instance", 0).getInt("user_id", 0);
        updateUITask = new FriendsActivity.UpdateUITask();

        friendsListItemArray = new ArrayList<FriendsListItem>();

        openVK_API = new OvkAPIWrapper(FriendsActivity.this, server, auth_token, json_response, global_sharedPreferences.getBoolean("useHTTPS", true));

        if(connection_status == false) {
            try {
                openVK_API.sendMethod("Friends.get", "user_id=" + getIntent().getExtras().getInt("user_id") + "&count=" + 50);
                findViewById(R.id.friends_layout).setVisibility(View.GONE);
                findViewById(R.id.news_progressll).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
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
                }
            }
        };
    }

    void loadFriends() {
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
            findViewById(R.id.friends_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.news_progressll).setVisibility(View.GONE);
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
                                Log.e("OpenVK Legacy", "Invalid API token"); // ржекич
                                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("instance", 0).edit();
                                editor.putString("auth_token", "");
                                editor.putInt("user_id", 0);
                                editor.commit();
                                Intent intent = new Intent(FriendsActivity.this, AuthenticationActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (json_response.getInt("error_code") == 3) {
                                AlertDialog outdated_api_dlg;
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle(R.string.deprecated_openvk_api_error_title);
                                builder.setMessage(R.string.deprecated_openvk_api_error);
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                try{
                                    outdated_api_dlg = builder.create();
                                    outdated_api_dlg.show();
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (json_response.getInt("error_code") == 28) {
                                AlertDialog wrong_userdata_dlg;
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle(R.string.auth_error_title);
                                builder.setMessage(R.string.auth_error);
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                wrong_userdata_dlg = builder.create();
                                wrong_userdata_dlg.show();
                            }
                        } else if(send_request.startsWith("/method/Friends.get")) {
                            loadFriends();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                }
            });
        }
    }
}
