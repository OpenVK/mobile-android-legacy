package uk.openvk.android.legacy.ui.list.items;

import android.os.Parcel;
import android.os.Parcelable;

/*  Copyleft © 2022, 2023 OpenVK Team
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
