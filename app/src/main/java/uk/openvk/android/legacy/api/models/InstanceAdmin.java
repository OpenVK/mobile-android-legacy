package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Dmitry on 07.10.2022.
 */

public class InstanceAdmin implements Parcelable {
    public String first_name;
    public String last_name;
    public int id;
    public InstanceAdmin(String first_name, String last_name, int id) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
    }

    protected InstanceAdmin(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        id = in.readInt();
    }

    public static final Creator<InstanceAdmin> CREATOR = new Creator<InstanceAdmin>() {
        @Override
        public InstanceAdmin createFromParcel(Parcel in) {
            return new InstanceAdmin(in);
        }

        @Override
        public InstanceAdmin[] newArray(int size) {
            return new InstanceAdmin[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(first_name);
        parcel.writeString(last_name);
        parcel.writeInt(id);
    }
}
