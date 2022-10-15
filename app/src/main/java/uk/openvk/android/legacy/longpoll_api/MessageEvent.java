package uk.openvk.android.legacy.longpoll_api;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/**
 * Created by Dmitry on 14.10.2022.
 */

public class MessageEvent {
    public int event_type;
    public int message_id;
    public int peer_id;
    public int msg_time;
    public String msg_text;
    private JSONParser jsonParser;

    public MessageEvent(String response) {
        jsonParser = new JSONParser();
        msg_time = 0;
        msg_text = "";
        event_type = 0;
        message_id = 0;
        peer_id = 0;
        parse(response);
    }

    public void parse(String response) {
        if (response.startsWith("{") && response.endsWith("}")) {
            try {
                JSONObject json = jsonParser.parseJSON(response);
                if(json.has("updates")) {
                    JSONArray updates = json.getJSONArray("updates");
                    if(updates.length() > 0) {
                        JSONArray event = updates.getJSONArray(0);
                        if(event.length() >= 6) {
                            event_type = event.getInt(0);
                            message_id = event.getInt(1);
                            peer_id = event.getInt(3);
                            msg_time = event.getInt(4);
                            msg_text = event.getString(5);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
