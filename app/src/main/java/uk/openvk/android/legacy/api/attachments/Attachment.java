package uk.openvk.android.legacy.api.attachments;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.entities.Poll;
import uk.openvk.android.legacy.api.entities.Video;

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

public class Attachment implements Parcelable, Serializable {
    public String type;
    public String status;
    public JSONObject unserialized_data;

    public Attachment(String type) {
        this.type = type;
    }

    protected Attachment(Parcel in) {
        type = in.readString();
        status = in.readString();
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public Attachment() {
        type = "";
        status = "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeString(status);
    }

    public void serialize(JSONObject object) {
        try {
            object.put("type", type);
            object.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deserialize(String attach_blob) {
        try {
            unserialized_data = new JSONObject(attach_blob);
            type = unserialized_data.getString("type");
            if(!unserialized_data.isNull("status"))
                status = unserialized_data.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void clone(Attachment attachment) {
        this.status = attachment.status;
        this.type = attachment.type;
    }
}
