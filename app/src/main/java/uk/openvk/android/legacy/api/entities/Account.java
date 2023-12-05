package uk.openvk.android.legacy.api.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.models.Users;
import uk.openvk.android.legacy.api.counters.AccountCounters;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

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

public class Account implements Parcelable {
    public String first_name;
    public String last_name;
    public long id;
    public String status;
    public String birthdate;
    public AccountCounters counters;
    private JSONParser jsonParser;
    private String queue_method;
    private String queue_args;
    private Context ctx;
    private boolean retryConnection;
    public User user;
    private Users users;

    public Account(Context ctx) {
        retryConnection = false;
        jsonParser = new JSONParser();
        this.user = new User();
        this.users = new Users();
        this.ctx = ctx;
        counters = new AccountCounters(0, 0, 0);
    }

    public Account(String response, Context ctx, OvkAPIWrapper wrapper) {
        retryConnection = false;
        jsonParser = new JSONParser();
        this.user = new User();
        this.users = new Users();
        this.ctx = ctx;
        counters = new AccountCounters(0, 0, 0);
        parse(response, wrapper);
    }

    public Account(String first_name, String last_name, long id, String status, String birthdate) {
        retryConnection = false;
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.status = status;
        this.birthdate = birthdate;
        this.user = new User();
        this.users = new Users();
        jsonParser = new JSONParser();
        counters = new AccountCounters(0, 0, 0);
    }

    protected Account(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        id = in.readInt();
        status = in.readString();
        birthdate = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public void parse(String response, OvkAPIWrapper wrapper) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject account = json.getJSONObject("response");
                first_name = account.getString("first_name");
                user.first_name = account.getString("first_name");
                last_name = account.getString("last_name");
                user.last_name = account.getString("last_name");
                id = account.getLong("id");
                user.id = account.getLong("id");
                status = account.getString("status");
                user.status = account.getString("status");
                birthdate = account.getString("bdate");
                user.birthdate = account.getString("bdate");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(queue_method != null || queue_args != null) {
            wrapper.sendAPIMethod(queue_method, queue_args);
            queue_method = "";
            queue_args = "";
        }
    }

    public void parseCounters(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json == null || json.isNull("response")) {
                this.counters = new AccountCounters(0, 0, 0);
            } else {
                JSONObject counters = json.getJSONObject("response");
                int friends = counters.getInt("friends");
                int messages = counters.getInt("messages");
                int notifications = counters.getInt("notifications");
                this.counters = new AccountCounters(friends, messages, notifications);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getProfileInfo(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Account.getProfileInfo");
    }

    public void getCounters(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Account.getCounters");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(first_name);
        parcel.writeString(last_name);
        parcel.writeLong(id);
        parcel.writeString(status);
        parcel.writeString(birthdate);
    }

    public void addQueue(String method, String args) {
        queue_method = method;
        queue_args = args;
        this.retryConnection = true;
    }
}
