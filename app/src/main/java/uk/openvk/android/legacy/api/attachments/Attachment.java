package uk.openvk.android.legacy.api.attachments;

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

public class Attachment {
    public String type;
    public String status;
    private Object content;
    public Attachment(String type) {
        this.type = type;
        switch (type) {
            case "photo":
                content = new PhotoAttachment();
                break;
            case "video":
                content = new VideoAttachment();
                break;
            case "poll":
                content = new PollAttachment();
                break;
            default:
                content = null;
                break;
        }
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
