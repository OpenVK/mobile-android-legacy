package uk.openvk.android.legacy.api.models;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

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

public class PhotoUploadParams {
    private JSONParser jsonParser;
    public String server;
    public String photo;
    public String hash;

    public PhotoUploadParams(String response) {
        try {
            jsonParser = new JSONParser();
            JSONObject json = jsonParser.parseJSON(response);
            server = json.getString("server");
            photo = json.getString("photo");
            hash = json.getString("hash");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
