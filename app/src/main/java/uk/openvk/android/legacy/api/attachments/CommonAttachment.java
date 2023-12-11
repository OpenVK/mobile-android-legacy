package uk.openvk.android.legacy.api.attachments;

import org.json.JSONException;
import org.json.JSONObject;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class CommonAttachment extends Attachment {
    public String title;
    public String text;
    public CommonAttachment(String title, String text) {
        type = "common";
        this.title = title;
        this.text = text;
    }

    public CommonAttachment() {

    }

    @Override
    public void serialize(JSONObject object) {
        try {
            super.serialize(object);
            JSONObject common_attach = new JSONObject();
            common_attach.put("title", title);
            common_attach.put("text", text);
            object.put("common", common_attach);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deserialize(String attach_blob) {
        try {
            super.deserialize(attach_blob);
            JSONObject common = unserialized_data.getJSONObject("common");
            title = common.getString("title");
            text = common.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
