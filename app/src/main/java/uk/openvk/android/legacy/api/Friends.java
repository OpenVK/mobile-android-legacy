package uk.openvk.android.legacy.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Friends {
    private JSONParser jsonParser;
    private ArrayList<Friend> friends;

    public Friends() {
        jsonParser = new JSONParser();
        friends = new ArrayList<Friend>();
    }

    public Friends(String response) {
        jsonParser = new JSONParser();
        parse(response);
    }

    public void parse(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            JSONArray users = json.getJSONArray("items");
            for (int i = 0; i < users.length(); i++) {
                Friend friend = new Friend(users.getJSONObject(i));
                this.friends.add(friend);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper ovk, int user_id, String where) {
        ovk.sendAPIMethod("Friends.get", String.format("user_id=%d&fields=verified,online,photo_100", user_id), where);
    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }
}
