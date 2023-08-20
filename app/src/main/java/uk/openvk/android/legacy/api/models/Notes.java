package uk.openvk.android.legacy.api.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.entities.Note;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class Notes {
    private JSONParser jsonParser;
    public ArrayList<Note> list;
    public Notes() {
        list = new ArrayList<>();
        jsonParser = new JSONParser();
    }

    public void get(OvkAPIWrapper wrapper, long user_id, int count, int sort) {
        wrapper.sendAPIMethod("Notes.get",
                String.format("user_id=%s&count=%s&sort=%s", user_id, count, sort)
        );
    }

    public void parse(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray notes = json.getJSONObject("response").getJSONArray("notes");
            list = new ArrayList<>();
            for(int i = 0; i < notes.length(); i++) {
                JSONObject item = notes.getJSONObject(i);
                Note note = new Note();
                note.id = item.getLong("id");
                note.owner_id = item.getLong("owner_id");
                note.title = item.getString("title");
                note.content = item.getString("text");
                note.date = item.getLong("date");
                try { // handle floating crash
                    list.add(note);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    Log.e(OvkApplication.API_TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
