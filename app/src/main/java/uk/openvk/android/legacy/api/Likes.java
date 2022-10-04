package uk.openvk.android.legacy.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Likes {
    private JSONParser jsonParser;
    public int owner_id;
    public int item_id;
    public int count;
    public int position;

    public Likes() {
        jsonParser = new JSONParser();
    }

    public void add(OvkAPIWrapper ovk, int owner_id, int post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        ovk.sendAPIMethod("Likes.add", String.format("type=post&owner_id=%d&item_id=%d", owner_id, post_id));
    }

    public void delete(OvkAPIWrapper ovk, int owner_id, int post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        ovk.sendAPIMethod("Likes.delete", String.format("type=post&owner_id=%d&item_id=%d", owner_id, post_id));
    }

    public void parse(String response) {
        JSONObject json = jsonParser.parseJSON(response);
        try {
            if(json != null) {
                JSONObject result = json.getJSONObject("response");
                count = result.getInt("likes");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
