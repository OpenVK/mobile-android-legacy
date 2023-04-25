package uk.openvk.android.legacy.api;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

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

public class Users implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<User> users;
    public User user;

    public Users() {
        jsonParser = new JSONParser();
        users = new ArrayList<User>();
    }


    public Users(String response) {
        jsonParser = new JSONParser();
        parse(response);
    }

    protected Users(Parcel in) {
        users = in.createTypedArrayList(User.CREATOR);
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public void parse(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONArray("response");
            if(this.users.size() > 0) {
                this.users.clear();
            }
            for (int i = 0; i < users.length(); i++) {
                User user = new User(users.getJSONObject(i));
                this.users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSearch(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONObject("response").getJSONArray("items");
            if(this.users.size() > 0) {
                this.users.clear();
            }
            for (int i = 0; i < users.length(); i++) {
                User user = new User(users.getJSONObject(i));
                this.users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUser(OvkAPIWrapper ovk, long user_id) {
        ovk.sendAPIMethod("Users.get", String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen,interests,music,movies,tv,books,city", user_id));
    }

    public void getAccountUser(OvkAPIWrapper ovk, long user_id) {
        ovk.sendAPIMethod("Users.get", String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen,interests,music,movies,tv,books,city", user_id), "account_user");
    }

    public void getPeerUsers(OvkAPIWrapper ovk, ArrayList<Conversation> conversations) {
        ArrayList<Long> user_ids = new ArrayList<>();
        for(int i = 0; i < conversations.size(); i++) {
            user_ids.add(conversations.get(i).peer_id);
        }
        StringBuilder ids_list = new StringBuilder();
        for(int i = 0; i < user_ids.size(); i++) {
            if(i < user_ids.size() - 1) {
                ids_list.append(String.format("%s,", user_ids.get(i)));
            } else {
                ids_list.append(user_ids.get(i));
            }
        }
        ovk.sendAPIMethod("Users.get", String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen,interests,music,movies,tv,books,city", ids_list), "peers");
    }

    public void get(OvkAPIWrapper ovk, ArrayList<Integer> user_ids) {
        StringBuilder ids_list = new StringBuilder();
        for(int i = 0; i < user_ids.size(); i++) {
            if(i < user_ids.size() - 1) {
                ids_list.append(String.format("%s,", user_ids.get(i)));
            } else {
                ids_list.append(user_ids.get(i));
            }
        }
        ovk.sendAPIMethod("Users.get", String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen,interests,music,movies,tv,books,city", ids_list.toString()));
    }

    public ArrayList<User> getList() {
        return users;
    }

    public void search(OvkAPIWrapper ovk, String query) {
        ovk.sendAPIMethod("Users.search", String.format("q=%s&count=50&fields=verified,sex,has_photo,photo_200,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen,interests,music,movies,tv,books,city", URLEncoder.encode(query)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(users);
        parcel.writeParcelable(user, i);
    }
}
