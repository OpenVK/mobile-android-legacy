package uk.openvk.android.legacy.api.counters;

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

public class AccountCounters implements Parcelable {
    public int friends_requests;
    public int new_messages;
    public int notifications;

    public AccountCounters(int friends, int messages, int notifications) {
        this.friends_requests = friends;
        this.new_messages = messages;
        this.notifications = notifications;
    }

    protected AccountCounters(Parcel in) {
        friends_requests = in.readInt();
        new_messages = in.readInt();
        notifications = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(friends_requests);
        dest.writeInt(new_messages);
        dest.writeInt(notifications);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AccountCounters> CREATOR = new Creator<AccountCounters>() {
        @Override
        public AccountCounters createFromParcel(Parcel in) {
            return new AccountCounters(in);
        }

        @Override
        public AccountCounters[] newArray(int size) {
            return new AccountCounters[size];
        }
    };
}
