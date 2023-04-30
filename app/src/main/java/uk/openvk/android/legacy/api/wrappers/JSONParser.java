package uk.openvk.android.legacy.api.wrappers;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.Account;

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

public class JSONParser {

    public JSONParser() {
    }

    public JSONObject parseJSON(String string) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        return jsonObject;
    }

    public Account getAccount(String jsonString) {
        Account account = null;
        JSONObject json = parseJSON(jsonString);
        if(json != null) {
            try {
                JSONObject response = json.getJSONObject("response");
                account = new Account(response.getString("first_name"), response.getString("last_name"),
                        response.getInt("id"), response.getString("status"), response.getString("birthday"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return account;
    }
}
