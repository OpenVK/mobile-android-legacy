package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 30.09.2022.
 */
public class Friend {
    public String first_name;
    public String last_name;
    public int id;
    public boolean verified;
    public boolean online;
    private JSONParser jsonParser;

    public Friend(JSONObject user) {
        parse(user);
    }

    public Friend(String response, int position) {
        parse(response, position);
    }

    public Friend(String first_name, String last_name, int id, String status, String city, String screen_name, String avatar_url, int friends_status, int ls_date, String birthdate,
                  String interests, String movies, String music, String tv, String books, boolean verified) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.verified = verified;
        jsonParser = new JSONParser();
    }

    public void parse(JSONObject user) {
        try {
            if(user != null) {
                first_name = user.getString("first_name");
                last_name = user.getString("last_name");
                id = user.getInt("id");
                if(user.getInt("verified") == 1) {
                    verified = true;
                } else {
                    verified = false;
                }
                if(user.getInt("online") == 1) {
                    online = true;
                } else {
                    online = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void parse(String response, int position) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONArray("items");
            if(users != null) {
                for (int i = 0; i < users.length(); i++) {
                    if(i == position) {
                        JSONObject user = (JSONObject) users.get(i);
                        first_name = user.getString("first_name");
                        last_name = user.getString("last_name");
                        id = user.getInt("id");
                        if(user.getInt("verified") == 1) {
                            verified = true;
                        } else {
                            verified = false;
                        }
                        if(user.getInt("online") == 1) {
                            online = true;
                        } else {
                            online = false;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
