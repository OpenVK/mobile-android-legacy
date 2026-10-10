/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.list.items;

import java.io.File;

import uk.openvk.android.client.attachments.Attachment;
import uk.openvk.android.client.entities.Photo;

public class UploadableAttachment {

    public class Result {
        public final static int RESULT_ATTACH_LOCAL_PHOTO    =   4;
        public final static int RESULT_ATTACH_PHOTO          =   5;
        public final static int RESULT_ATTACH_VIDEO          =   6;
        public final static int RESULT_ATTACH_AUDIO          =   7;
        public final static int RESULT_ATTACH_NOTE           =   8;
    }

    public String filename;
    public File file;
    public String type;
    public String title;
    public String mime;
    public long progress;
    public long length;
    public Attachment content;
    public String status;
    public String id;

    public UploadableAttachment(String filename, File file) {
        this.filename = filename;
        this.file = file;
        this.length = file.length();
        String mime = "application/octet-stream";
        if (file.getName().endsWith(".jpeg") || file.getName().endsWith(".jpg")) {
            mime = "image/jpeg";
            content = new Photo();
        } else if (file.getName().endsWith(".png")) {
            mime = "image/png";
            content = new Photo();
        } else if (file.getName().endsWith(".gif")) {
            mime = "image/gif";
            content = new Photo();
        }
        this.status = "prepared";
        this.mime = mime;
    }

    public UploadableAttachment() {

    }

    /**
        @deprecated getContent and setContent for any content types.
     */

    @Deprecated
    public Photo getPhoto() {
        return null;
    }

    @Deprecated
    public void setPhoto(Photo photo) {

    }

    public void setContent(Attachment attach) {
        this.content = attach;
    }

    public Object getContent() {
        return content;
    }
}
