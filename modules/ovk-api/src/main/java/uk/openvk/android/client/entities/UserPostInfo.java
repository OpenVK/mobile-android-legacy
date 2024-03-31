/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.entities;

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
