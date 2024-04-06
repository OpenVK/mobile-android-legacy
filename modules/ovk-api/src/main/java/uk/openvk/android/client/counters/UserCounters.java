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

package uk.openvk.android.client.counters;

import android.os.Parcel;
import android.os.Parcelable;

public class UserCounters implements Parcelable {
    public long friends_count;
    public long photos_count;
    public long videos_count;
    public long audios_count;
    public long notes_count;

    public UserCounters(long friends, long photos, long videos, long audios, long notes) {
        this.friends_count = friends;
        this.photos_count = photos;
        this.videos_count = videos;
        this.audios_count = audios;
        this.notes_count = notes;
    }

    public UserCounters(Parcel in) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Creator<UserCounters> CREATOR = new Creator<UserCounters>() {
        @Override
        public UserCounters createFromParcel(Parcel in) {
            return new UserCounters(in);
        }

        @Override
        public UserCounters[] newArray(int size) {
            return new UserCounters[size];
        }
    };
}
