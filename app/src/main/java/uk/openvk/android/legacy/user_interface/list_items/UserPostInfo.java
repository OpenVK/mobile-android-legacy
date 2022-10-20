package uk.openvk.android.legacy.user_interface.list_items;

import android.os.Parcel;
import android.os.Parcelable;

public class UserPostInfo implements Parcelable {
    int postId;
    int postUserId;
    int postAuthorId;
    String ownerTitle;
    public UserPostInfo(int post_id, int post_group_id, int post_author_id, String owner) {
        postId = post_id;
        postUserId = post_group_id;
        postAuthorId = post_author_id;
        ownerTitle = owner;
    }

    protected UserPostInfo(Parcel in) {
        postId = in.readInt();
        postUserId = in.readInt();
        postAuthorId = in.readInt();
        ownerTitle = in.readString();
    }

    public static final Creator<UserPostInfo> CREATOR = new Creator<UserPostInfo>() {
        @Override
        public UserPostInfo createFromParcel(Parcel in) {
            return new UserPostInfo(in);
        }

        @Override
        public UserPostInfo[] newArray(int size) {
            return new UserPostInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(postId);
        parcel.writeInt(postUserId);
        parcel.writeInt(postAuthorId);
        parcel.writeString(ownerTitle);
    }
}
