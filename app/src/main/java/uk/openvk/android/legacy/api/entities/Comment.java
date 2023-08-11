package uk.openvk.android.legacy.api.entities;

import android.graphics.Bitmap;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.Attachment;
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

public class Comment {
    public String author;
    public long author_id;
    public long date;
    public String text;
    public long id;
    public Bitmap avatar;
    public String avatar_url;
    private JSONParser jsonParser;
    public ArrayList<Attachment> attachments;

    public Comment() {
        jsonParser = new JSONParser();
    }

    public Comment(int id, long author_id, String author, int date, String text,
                   ArrayList<Attachment> attachments) {
        this.author_id = author_id;
        this.author = author;
        this.date = date;
        this.text = text;
        this.id = id;
        this.attachments = attachments;
    }
}
