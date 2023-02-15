package uk.openvk.android.legacy.user_interface.list.items;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupPostInfo implements Parcelable {
    int postId;
    int postAuthorId;
    String ownerTitle;
    public GroupPostInfo(int post_id, int post_author_id, String author) {
        postId = post_id;
        postAuthorId = post_author_id;
        ownerTitle = author;
    }

    protected GroupPostInfo(Parcel in) {
        postId = in.readInt();
        postAuthorId = in.readInt();
        ownerTitle = in.readString();
    }

    public static final Creator<GroupPostInfo> CREATOR = new Creator<GroupPostInfo>() {
        @Override
        public GroupPostInfo createFromParcel(Parcel in) {
            return new GroupPostInfo(in);
        }

        @Override
        public GroupPostInfo[] newArray(int size) {
            return new GroupPostInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(postId);
        parcel.writeInt(postAuthorId);
        parcel.writeString(ownerTitle);
    }
}
