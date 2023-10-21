package uk.openvk.android.legacy.api.entities;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class Conversation {
    public String title;
    public long peer_id;
    public int online;
    public Bitmap avatar;
    public Bitmap lastMsgAvatar;
    public long lastMsgAuthorId;
    public String lastMsgText;
    public long lastMsgTime;
    public String avatar_url;
    private ArrayList<Message> history;
    private JSONParser jsonParser;

    public Conversation() {
        jsonParser = new JSONParser();
        history = new ArrayList<Message>();
    }

    public void getHistory(OvkAPIWrapper wrapper, long peer_id) {
        this.peer_id = peer_id;
        wrapper.sendAPIMethod("Messages.getHistory",
                String.format("peer_id=%s&count=150&rev=1", peer_id));
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
                    Message message = new Message(item.getLong("id"), incoming, false, item.getLong("date"), item.getString("text"), ctx);
                    message.author_id = item.getLong("from_id");
                    history.add(message);
                }
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return history;
    }

    public void sendMessage(OvkAPIWrapper wrapper, String text) {
        wrapper.sendAPIMethod("Messages.send", String.format("peer_id=%s&message=%s", peer_id, URLEncoder.encode(text)));
    }
}
