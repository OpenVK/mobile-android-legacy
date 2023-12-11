package uk.openvk.android.legacy.api.entities;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.api.attachments.Attachment;

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

public class Photo extends Attachment implements Parcelable, Serializable {
    public long id;
    public long album_id;
    public long owner_id;
    public String url;
    public String original_url;
    public String filename;
    public int[] size;
    public Bitmap bitmap;
    public boolean is_error;
    public String exception_name;

    public Photo() {
        type = "photo";
    }

    protected Photo(Parcel in) {
        type = "photo";
        url = in.readString();
        filename = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(filename);
    }

    @Override
    public void serialize(JSONObject object) {
        super.serialize(object);
        try {
            JSONObject photo = new JSONObject();
            photo.put("id", id);
            photo.put("album_id", album_id);
            photo.put("owner_id", owner_id);
            photo.put("size", size[0] + "x" + size[1]);
            photo.put("url", url);
            photo.put("original_url", original_url);
            photo.put("filename", filename);
            object.put("status", status);
            object.put("photo", photo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deserialize(String attach_blob) {
        try {
            super.deserialize(attach_blob);
            JSONObject photo = unserialized_data.getJSONObject("photo");
            id = photo.getLong("id");
            album_id = photo.getLong("album_id");
            owner_id = photo.getLong("owner_id");
            size = new int[2];
            size[0] = Integer.parseInt(photo.getString("size").split("x")[0]);
            size[1] = Integer.parseInt(photo.getString("size").split("x")[1]);
            url = photo.getString("url");
            original_url = photo.getString("original_url");
            filename = photo.getString("filename");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
