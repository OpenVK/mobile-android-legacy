package uk.openvk.android.legacy.api.attachments;

import android.os.Parcel;
import android.os.Parcelable;

import uk.openvk.android.legacy.api.entities.VideoFiles;

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

public class VideoAttachment implements Parcelable {
    public VideoFiles files;
    public long id;
    public String title;
    public String url;
    public String url_thumb;
    public int duration;
    public String filename;

    public VideoAttachment() {

    }

    public VideoAttachment(long id, String title, VideoFiles files, String url_thumb, int duration, String filename) {
        this.id = id;
        this.title = title;
        this.files = files;
        this.url_thumb = url_thumb;
        this.duration = duration;
        this.filename = filename;
    }

    protected VideoAttachment(Parcel in) {
        id = in.readLong();
        title = in.readString();
        files = in.readParcelable(VideoFiles.class.getClassLoader());
        url_thumb = in.readString();
        duration = in.readInt();
        filename = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeParcelable(files, flags);
        dest.writeString(url_thumb);
        dest.writeInt(duration);
        dest.writeString(filename);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoAttachment> CREATOR = new Creator<VideoAttachment>() {
        @Override
        public VideoAttachment createFromParcel(Parcel in) {
            return new VideoAttachment(in);
        }

        @Override
        public VideoAttachment[] newArray(int size) {
            return new VideoAttachment[size];
        }
    };
}
