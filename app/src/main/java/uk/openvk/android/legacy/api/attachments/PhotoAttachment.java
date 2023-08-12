package uk.openvk.android.legacy.api.attachments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

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

public class PhotoAttachment implements Parcelable {
    public long id;
    public String url;
    public String original_url;
    public String filename;
    public int[] size;
    public Bitmap photo;
    public String error;

    public PhotoAttachment() {
    }

    protected PhotoAttachment(Parcel in) {
        url = in.readString();
        filename = in.readString();
    }

    public static final Creator<PhotoAttachment> CREATOR = new Creator<PhotoAttachment>() {
        @Override
        public PhotoAttachment createFromParcel(Parcel in) {
            return new PhotoAttachment(in);
        }

        @Override
        public PhotoAttachment[] newArray(int size) {
            return new PhotoAttachment[size];
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
}
