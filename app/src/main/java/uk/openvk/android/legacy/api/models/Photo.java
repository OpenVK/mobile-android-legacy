package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Dmitry on 05.10.2022.
 */

public class Photo implements Parcelable {
    public String url;
    public String filename;
    public Photo() {
        this.url = url;
        this.filename = filename;
    }

    protected Photo(Parcel in) {
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
}
