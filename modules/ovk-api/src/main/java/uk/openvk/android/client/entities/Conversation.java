/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.entities;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.utils.AuthorResolver;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

public class Conversation extends LazyEntity {

    public static final long PEER_ID_USER_UPPER_START = 200000000;

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
    public String peer_type;
    public long members_count;

    public Conversation() {
        jsonParser = new JSONParser();
        history = new ArrayList<>();
    }

    public void getHistory(OvkAPIWrapper wrapper, long peer_id) {
        this.peer_id = peer_id;
        wrapper.sendAPIMethod("Messages.getHistory",
                String.format(
                        "extended=1&fields=online,sex,photo_50,verified&" +
                        "peer_id=%s&count=30",
                        peer_id
                )
        );
    }

    public ArrayList<Message> parseHistory(String response) {
        JSONObject json = jsonParser.parseJSON(response);

        if(json != null) {
            try {
                JSONArray items = json.getJSONObject("response").getJSONArray("items");
                history = new ArrayList<>();
                for(int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    boolean incoming;
                    incoming = item.getInt("out") != 1;
                    Message message = new Message(
                            item.getLong("id"),
                            incoming,
                            item.getLong("date"),
                            item.getString("text")
                    );

                    if(item.has("action"))
                        message.parseAction(item.getJSONObject("action"));

                    message.author = AuthorResolver.resolveAuthorFromJSON(
                            json.getJSONObject("response"), item.getLong("from_id")
                    );

                    history.add(message);
                }
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        return history;
    }



    public void sendMessage(OvkAPIWrapper wrapper, String text) {
        wrapper.sendAPIMethod(
                "Messages.send",
                String.format("peer_id=%s&message=%s",
                        peer_id, URLEncoder.encode(text))
        );
    }
}
