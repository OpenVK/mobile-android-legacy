package uk.openvk.android.legacy.api.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

/*  Copyleft © 2022, 2023 OpenVK Team
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

public class Video implements Parcelable {
    public VideoFiles files;
    public long id;
    public String title;
    public String url;
    public String url_thumb;
    public int duration;
    public String filename;
    public long owner_id;

    public Video(JSONObject video) {
        try {
            title = video.getString("title");
            id = video.getLong("id");
            owner_id = video.getLong("owner_id");
            files = new VideoFiles();
            if (video.has("files") && !video.isNull("files")) {
                JSONObject videoFiles = video.getJSONObject("files");
                if (videoFiles.has("mp4_144")) {
                    files.mp4_144 = videoFiles.getString("mp4_144");
                }
                if (videoFiles.has("mp4_240")) {
                    files.mp4_240 = videoFiles.getString("mp4_240");
                }
                if (videoFiles.has("mp4_360")) {
                    files.mp4_360 = videoFiles.getString("mp4_360");
                }
                if (videoFiles.has("mp4_480")) {
                    files.mp4_480 = videoFiles.getString("mp4_480");
                }
                if (videoFiles.has("mp4_720")) {
                    files.mp4_720 = videoFiles.getString("mp4_720");
                }
                if (videoFiles.has("mp4_1080")) {
                    files.mp4_1080 = videoFiles.getString("mp4_1080");
                }
                if (videoFiles.has("ogv_480")) {
                    files.ogv_480 = videoFiles.getString("ogv_480");
                }
            }
            this.files = files;
            if (video.has("image")) {
                JSONArray thumb_array = video.getJSONArray("image");
                url_thumb = thumb_array.getJSONObject(0).getString("url");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Video(long id, String title, VideoFiles files, String url_thumb, int duration, String filename) {
        this.id = id;
        this.title = title;
        this.files = files;
        this.url_thumb = url_thumb;
        this.duration = duration;
        this.filename = filename;
    }

    public Video(Parcel in) {
        id = in.readLong();
        title = in.readString();
        files = in.readParcelable(VideoFiles.class.getClassLoader());
        url_thumb = in.readString();
        duration = in.readInt();
        filename = in.readString();
    }

    public Video() {

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

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
