package uk.openvk.android.legacy.api.models;

import org.json.JSONException;
import org.json.JSONObject;

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
public class Error {
    public String description;
    public int code;
    private JSONParser jsonParser;

    public Error() {
        jsonParser = new JSONParser();
    }

    public Error(String response) {
        jsonParser = new JSONParser();
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                description = json.getString("error_msg");
                code = json.getInt("error_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void parse(String response) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                description = json.getString("error_msg");
                code = json.getInt("error_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Error(String description, int code) {
        this.description = description;
        this.code = code;
    }
}
