package uk.openvk.android.legacy.list_items;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.list_items.NewsfeedItem;

public class RepostInfo implements Parcelable {
    public String name;
    public String time;
    public NewsfeedItem nLI;
    public RepostInfo(String original_author, int dt_sec) {
        name = original_author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        String info = new SimpleDateFormat("dd MMMMM yyyy at HH:mm").format(dt);
    }

    protected RepostInfo(Parcel in) {
        name = in.readString();
        time = in.readString();
        nLI = in.readParcelable(NewsfeedItem.class.getClassLoader());
    }

    public static final Creator<RepostInfo> CREATOR = new Creator<RepostInfo>() {
        @Override
        public RepostInfo createFromParcel(Parcel in) {
            return new RepostInfo(in);
        }

        @Override
        public RepostInfo[] newArray(int size) {
            return new RepostInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(time);
        parcel.writeParcelable(nLI, i);
    }
}
