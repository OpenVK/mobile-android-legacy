package uk.openvk.android.legacy.api.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.entities.Photo;
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

public class Photos {
    private JSONParser jsonParser;
    public String wallUploadServer;
    public String ownerPhotoUploadServer;
    public ArrayList<Photo> list;

    public Photos() {
        jsonParser = new JSONParser();
    }

    public void parseUploadServer(String response, String method) {
        try {
            if (method.equals("Photos.getOwnerPhotoUploadServer")) {
                JSONObject json = jsonParser.parseJSON(response);
                ownerPhotoUploadServer = json.getJSONObject("response").getString("upload_url");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parse(String response) {
        try {
            list = new ArrayList<>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray photos = json.getJSONObject("response").getJSONArray("photos");
            for(int i = 0; i < photos.length(); i++) {
                JSONObject item = photos.getJSONObject(i);
                Photo photo = new Photo();
                photo.id = item.getLong("id");
                photo.album_id = item.getLong("album_id");
                photo.owner_id = item.getLong("owner_id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getOwnerUploadServer(OvkAPIWrapper wrapper, long owner_id) {
        wrapper.sendAPIMethod("Photos.getOwnerPhotoUploadServer", String.format("owner_id=%s", owner_id));
    }

    public void getWallUploadServer(OvkAPIWrapper wrapper, long group_id) {
        wrapper.sendAPIMethod("Photos.getWallUploadServer", String.format("group_id=%s", group_id));
    }

    public void saveWallPhoto(OvkAPIWrapper wrapper, String photo, String hash) {
        wrapper.sendAPIMethod("Photos.saveWallPhoto", String.format("photo=%s&hash=%s", photo, hash));
    }
}
