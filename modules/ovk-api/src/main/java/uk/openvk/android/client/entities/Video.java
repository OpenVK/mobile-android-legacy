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

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;

import uk.openvk.android.client.attachments.Attachment;

public class Video extends Attachment implements Parcelable, Serializable {
    public VideoFiles files;
    public long id;
    public String title;
    public String url;
    public String url_thumb;
    public int duration;
    public String filename;
    public long owner_id;

    public Video(JSONObject video) {
        type = "video";
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
        type = "video";
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

    @Override
    public void serialize(JSONObject object) {
        super.serialize(object);
        try {
            JSONObject video = new JSONObject();
            video.put("id", id);
            video.put("owner_id", owner_id);
            video.put("title", title);
            video.put("duration", duration);
            JSONArray files = new JSONArray();
            if(this.files != null) {
                files.put(this.files.mp4_144);
                files.put(this.files.mp4_240);
                files.put(this.files.mp4_360);
                files.put(this.files.mp4_480);
                files.put(this.files.mp4_720);
                files.put(this.files.mp4_1080);
                files.put(this.files.ogv_480);
            }
            video.put("files", files);
            video.put("thumb_url", url_thumb);
            video.put("filename", filename);
            object.put("video", video);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deserialize(String attach_blob) {
        try {
            super.deserialize(attach_blob);
            JSONObject video = unserialized_data.getJSONObject("video");
            id = video.getLong("id");
            if(!video.isNull("owner_id")) {
                owner_id = video.getLong("owner_id");
            }
            title = video.getString("title");
            duration = video.getInt("duration");
            url_thumb = video.getString("thumb_url");
            files = new VideoFiles();
            JSONArray json_files = video.getJSONArray("files");
            if(!video.isNull("filename")) {
                filename = video.getString("filename");
            }
            if(video.isNull("files")) {
                files.mp4_144 = json_files.getString(0);
                files.mp4_240 = json_files.getString(1);
                files.mp4_360 = json_files.getString(2);
                files.mp4_480 = json_files.getString(3);
                files.mp4_720 = json_files.getString(4);
                files.mp4_1080 = json_files.getString(5);
                files.ogv_480 = json_files.getString(6);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
