/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.client.models;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Note;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

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

    public void getById(OvkAPIWrapper wrapper, long owner_id, long note_id) {
        wrapper.sendAPIMethod("Notes.getById",
                String.format("owner_id=%s&note_id=%s", owner_id, note_id)
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
                    Log.e(OpenVKAPI.TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseNote(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            list = new ArrayList<>();
            JSONObject item = json.getJSONObject("response");
            Note note = new Note();
            note.id = item.getLong("id");
            note.owner_id = item.getLong("owner_id");
            note.title = item.getString("title");
            note.content = item.getString("text");
            note.date = item.getLong("date");
            try { // handle floating crash
                list.add(note);
            } catch (ArrayIndexOutOfBoundsException ignored) {
                Log.e(OpenVKAPI.TAG, "WTF? The length itself in an array must not " +
                        "be overestimated.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void edit(OvkAPIWrapper wrapper, long note_id, String new_title, String new_content) {
        wrapper.sendAPIMethod("Notes.edit",
                String.format("note_id=%s&title=%s&text=%s",
                        note_id, URLEncoder.encode(new_title), URLEncoder.encode(new_content)
                )
        );
    }
}
