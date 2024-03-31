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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RepostInfo implements Parcelable {
    public Date dt;
    public String name;
    public String time;
    public WallPost newsfeed_item;
    @SuppressLint("SimpleDateFormat")
    public RepostInfo(String original_author, long dt_sec, Context ctx) {
        name = original_author;
        dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
    }

    protected RepostInfo(Parcel in) {
        name = in.readString();
        time = in.readString();
        newsfeed_item = in.readParcelable(WallPost.class.getClassLoader());
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
        parcel.writeParcelable(newsfeed_item, i);
    }
}
