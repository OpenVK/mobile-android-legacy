package uk.openvk.android.legacy.ui.list.items;

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
