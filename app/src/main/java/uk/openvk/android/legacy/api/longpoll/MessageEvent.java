package uk.openvk.android.legacy.api.longpoll;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

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
