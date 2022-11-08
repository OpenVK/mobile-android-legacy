package uk.openvk.android.legacy.api.models;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Conversation {
    public String title;
    public int peer_id;
    public int online;
    public Bitmap avatar;
    public Bitmap lastMsgAvatar;
    public int lastMsgAuthorId;
    public String lastMsgText;
    public int lastMsgTime;
    public String avatar_url;
    private ArrayList<Message> history;
    private JSONParser jsonParser;

    public Conversation() {
        jsonParser = new JSONParser();
        history = new ArrayList<Message>();
    }

    public void getHistory(OvkAPIWrapper ovk, int peer_id) {
        this.peer_id = peer_id;
        ovk.sendAPIMethod("Messages.getHistory", String.format("peer_id=%d&count=150&rev=1", peer_id));
    }

    public ArrayList<Message> parseHistory(Context ctx, String response) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                JSONArray items = json.getJSONObject("response").getJSONArray("items");
                history = new ArrayList<Message>();
                for(int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    boolean incoming = false;
                    if(item.getInt("out") == 1) {
                        incoming = false;
                    } else {
                        incoming = true;
                    }
                    Message message = new Message(incoming, false, item.getInt("date"), item.getString("text"), ctx);
                    message.author_id = item.getInt("from_id");
                    history.add(message);
                }
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return history;
    }

    public void sendMessage(OvkAPIWrapper ovk, String text) {
        ovk.sendAPIMethod("Messages.send", String.format("peer_id=%d&message=%s", peer_id, URLEncoder.encode(text)));
    }
}
