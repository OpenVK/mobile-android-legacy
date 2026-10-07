/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.client.entities.LongPollUpdate;
import uk.openvk.android.client.wrappers.JSONParser;

public class LongPollServer {
    public String address;
    public String key;
    public int ts;

    public LongPollServer() {

    }

    public LongPollServer(String address, String key, int ts) {
        this.address = address;
        this.key = key;
        this.ts = ts;
    }

    public ArrayList<LongPollUpdate> parseUpdates(String response) {
        ArrayList<LongPollUpdate> updates = new ArrayList<>();
        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONArray updatesJson = jsonObj.getJSONArray("updates");

            for(int i = 0; i < updatesJson.length(); i++) {
                JSONArray updateArray = updatesJson.getJSONArray(i);

                LongPollUpdate update = null;

                if(updateArray.getInt(0) == 4) {
                    update = new LongPollUpdate(
                            updateArray.getInt(0),
                            updateArray.getLong(1),
                            updateArray.getLong(2),
                            updateArray.getLong(3),
                            updateArray.getLong(4),
                            updateArray.getString(6)
                    );
                }

                if(update != null)
                    updates.add(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updates;
    }
}
