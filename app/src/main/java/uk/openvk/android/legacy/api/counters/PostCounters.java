package uk.openvk.android.legacy.api.counters;

import android.os.Parcel;
import android.os.Parcelable;

/** Copyleft © 2022, 2023 OpenVK Team
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

public class PostCounters implements Parcelable {
    public int likes;
    public int comments;
    public int reposts;
    public boolean isLiked;
    public boolean isReposted;
    public boolean enabled = true;

    public PostCounters(int likes_count, int comments_count, int reposts_count, boolean likes_selected, boolean reposts_selected) {
        likes = likes_count;
        comments = comments_count;
        reposts = reposts_count;
        isLiked = likes_selected;
        isReposted = reposts_selected;
    }

    protected PostCounters(Parcel in) {
        likes = in.readInt();
        comments = in.readInt();
        reposts = in.readInt();
        isLiked = in.readByte() != 0;
        isReposted = in.readByte() != 0;
    }

    public static final Creator<PostCounters> CREATOR = new Creator<PostCounters>() {
        @Override
        public PostCounters createFromParcel(Parcel in) {
            return new PostCounters(in);
        }

        @Override
        public PostCounters[] newArray(int size) {
            return new PostCounters[size];
        }
    };

    public PostCounters() {

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
