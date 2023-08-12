package uk.openvk.android.legacy.api.entities;

import android.os.Parcel;
import android.os.Parcelable;

/** OPENVK LEGACY LICENSE NOTIFICATION
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
