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

import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;

import uk.openvk.android.client.attachments.Attachment;

public class Note extends Attachment implements Serializable {
    public long id;
    public long owner_id;
    public long date;
    public String content;
    public String title;

    public Note(long id, long owner_id, String title, String content, long date) {
        this.id = id;
        this.owner_id = owner_id;
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public Note() {

    }

    @Override
    public void serialize(JSONObject object) {
        super.serialize(object);
        try {
            JSONObject note = new JSONObject();
            note.put("id", id);
            note.put("owner_id", owner_id);
            note.put("title", title);
            note.put("content", content);
            note.put("date", date);
            object.put("note", note);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
