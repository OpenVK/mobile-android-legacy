package uk.openvk.android.legacy.api.entities;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class Message {
    public long id;
    public boolean isIncoming;
    public boolean isError;
    public String timestamp;
    public long timestamp_int;
    public String text;
    public boolean sending;
    public long author_id;
    private JSONParser parser;

    @SuppressLint("SimpleDateFormat")
    public Message(long id, boolean incoming, boolean error, long _timestamp, String _text,
                   Context ctx) {
        this.id = id;
        isIncoming = incoming;
        isError = error;
        text = _text;
        timestamp_int = _timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(_timestamp));
        timestamp = new SimpleDateFormat("HH:mm").format(dt);
    }

    public void getSendedId(String response) {
        parser = new JSONParser();
        try {
            JSONObject json = parser.parseJSON(response);
            if (json != null) {
                id = json.getLong("response");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
