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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

public class Audios {
    private JSONParser jsonParser;
    private ArrayList<Audio> audios;
    public Audios() {
        jsonParser = new JSONParser();
        audios = new ArrayList<>();
    }

    public void get(OvkAPIWrapper wrapper, long owner_id, int count, boolean extended) {
        wrapper.sendAPIMethod("Audio.get",
                String.format("owner_id=%s&count=%s&extended=%s", owner_id, count, extended ? 0 : 1)
        );
    }

    public void getLyrics(OvkAPIWrapper wrapper, long lyrics_id) {
        wrapper.sendAPIMethod("Audio.getLyrics", String.format("lyrics_id=%s", lyrics_id));
    }

    public void parseAudioTracks(String response, boolean clear) {
        JSONObject json = jsonParser.parseJSON(response);
        if(clear) {
            audios = new ArrayList<>();
        }
        if(json != null) {
            try {
                JSONArray array = json.getJSONObject("response").getJSONArray("items");
                for(int i = 0; i < array.length(); i++) {
                    JSONObject audio_track = array.getJSONObject(i);
                    Audio audio = new Audio();
                    audio.unique_id = audio_track.getString("unique_id");
                    audio.id = audio_track.getLong("aid");
                    if(audio_track.has("owner_id")) {
                        audio.owner_id = audio_track.getLong("owner_id");
                    }
                    audio.title = audio_track.getString("title");
                    audio.artist = audio_track.getString("artist");
                    audio.album = audio_track.getString("album");
                    audio.genre = audio_track.getString("genre_str");
                    audio.setDuration(audio_track.getInt("duration"));
                    audio.lyrics =
                            audio_track.isNull("lyrics") ?
                            0 : audio_track.getLong("lyrics");
                    audio.url = audio_track.getString("url");
                    if(audio_track.has("user")) {
                        JSONObject sender = audio_track.getJSONObject("user");
                        audio.sender = new User();
                        audio.sender.id = sender.getLong("id");
                        audio.sender.first_name = sender.getString("name").split(" ")[0];
                        if(sender.getString("name").split(" ").length == 2) {
                            audio.sender.last_name = sender.getString("name").split(" ")[1];
                        }
                    }
                    audios.add(audio);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Audio> getList() {
        return audios;
    }

    public void parseLyrics(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            for(int i = 0; i < audios.size(); i++) {
                Audio track = audios.get(i);
                if(track.lyrics == json.getLong("lyrics_id"))
                    track.lyrics_text = json.getString("text");
                audios.set(i, track);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fillList(ArrayList<Audio> audios) {
        this.audios = audios;
    }
}
