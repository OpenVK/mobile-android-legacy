package uk.openvk.android.legacy.list_items;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class NewsItemCountersInfo implements Parcelable {
    public int likes;
    public int comments;
    public int reposts;
    public boolean isLiked;
    public boolean isReposted;

    public NewsItemCountersInfo(int likes_count, int comments_count, int reposts_count, boolean likes_selected, boolean reposts_selected) {
        likes = likes_count;
        comments = comments_count;
        reposts = reposts_count;
        isLiked = likes_selected;
        isReposted = reposts_selected;
    }

    protected NewsItemCountersInfo(Parcel in) {
        likes = in.readInt();
        comments = in.readInt();
        reposts = in.readInt();
        isLiked = in.readByte() != 0;
        isReposted = in.readByte() != 0;
    }

    public static final Creator<NewsItemCountersInfo> CREATOR = new Creator<NewsItemCountersInfo>() {
        @Override
        public NewsItemCountersInfo createFromParcel(Parcel in) {
            return new NewsItemCountersInfo(in);
        }

        @Override
        public NewsItemCountersInfo[] newArray(int size) {
            return new NewsItemCountersInfo[size];
        }
    };

    public NewsItemCountersInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(likes);
        parcel.writeInt(comments);
        parcel.writeInt(reposts);
        parcel.writeByte((byte) (isLiked ? 1 : 0));
        parcel.writeByte((byte) (isReposted ? 1 : 0));
    }
}
