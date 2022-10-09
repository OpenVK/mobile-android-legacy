package uk.openvk.android.legacy.list_items;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ProfileItem implements Parcelable {
    public String name;
    public int id;
    public int online;
    public ProfileItem(String _name, int _id, int _online) {
        name = _name;
        id = _id;
        online = _online;
    }

    protected ProfileItem(Parcel in) {
        name = in.readString();
        id = in.readInt();
        online = in.readInt();
    }

    public static final Creator<ProfileItem> CREATOR = new Creator<ProfileItem>() {
        @Override
        public ProfileItem createFromParcel(Parcel in) {
            return new ProfileItem(in);
        }

        @Override
        public ProfileItem[] newArray(int size) {
            return new ProfileItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(id);
        parcel.writeInt(online);
    }
}
