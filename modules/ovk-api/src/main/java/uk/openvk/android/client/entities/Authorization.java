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

package uk.openvk.android.client.entities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Error;

import uk.openvk.android.client.wrappers.JSONParser;

public class Authorization {
    private String access_token;
    private String response;
    private String error_msg;
    private JSONParser jsonParser;
    public static String ACCOUNT_TYPE = "uk.openvk.android.legacy.account";

    public Authorization(String response) {
        this.response = response;
        jsonParser = new JSONParser();
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                if (json.has("error")) {
                    error_msg = json.getString("error");
                } else if (json.has("access_token")) {
                    this.access_token = json.getString("access_token");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getAccessToken() {
        return access_token;
    }

    public String getErrorMessage() {
        return error_msg;
    }
}
