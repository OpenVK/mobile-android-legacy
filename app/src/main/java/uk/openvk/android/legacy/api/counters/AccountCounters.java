package uk.openvk.android.legacy.api.counters;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dmitry on 11.10.2022.
 */

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
