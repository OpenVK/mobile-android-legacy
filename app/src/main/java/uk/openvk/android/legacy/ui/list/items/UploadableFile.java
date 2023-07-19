package uk.openvk.android.legacy.ui.list.items;

import java.io.File;

import uk.openvk.android.legacy.api.entities.Photo;

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

public class UploadableFile {
    public String filename;
    public File file;
    public String title;
    public String mime;
    public long progress;
    public long length;
    private Photo photo;
    public String status;

    public UploadableFile(String filename, File file) {
        this.filename = filename;
        this.file = file;
        this.length = file.length();
        String mime = "application/octet-stream";
        if (file.getName().endsWith(".jpeg") || file.getName().endsWith(".jpg")) {
            mime = "image/jpeg";
            photo = new Photo();
        } else if (file.getName().endsWith(".png")) {
            mime = "image/png";
            photo = new Photo();
        } else if (file.getName().endsWith(".gif")) {
            mime = "image/gif";
            photo = new Photo();
        }
        this.mime = mime;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}
