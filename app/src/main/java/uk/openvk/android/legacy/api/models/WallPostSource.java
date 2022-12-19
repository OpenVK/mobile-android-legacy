package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

public class WallPostSource implements Parcelable {
    public String type;
    public String platform;

    public WallPostSource(String type, String platform) {
        this.type = type;
        this.platform = platform;
    }

    protected WallPostSource(Parcel in) {
        type = in.readString();
        platform = in.readString();
    }

    public static final Creator<WallPostSource> CREATOR = new Creator<WallPostSource>() {
        @Override
        public WallPostSource createFromParcel(Parcel in) {
            return new WallPostSource(in);
        }

        @Override
        public WallPostSource[] newArray(int size) {
            return new WallPostSource[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(platform);
    }
}
