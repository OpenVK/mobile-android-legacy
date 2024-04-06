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

package uk.openvk.android.client.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

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
                try {
                    this.users.add(user);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    Log.e(OpenVKAPI.TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSearch(String response, DownloadManager downloadManager) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONObject("response").getJSONArray("items");
            ArrayList<Photo> avatars;
            avatars = new ArrayList<>();
            if(this.users.size() > 0) {
                this.users.clear();
            }
            for (int i = 0; i < users.length(); i++) {
                User user = new User(users.getJSONObject(i));
                Photo photoAttachment = new Photo();
                photoAttachment.url = user.avatar_msize_url;
                photoAttachment.filename = String.format("avatar_%s", user.id);
                avatars.add(photoAttachment);
                try { // handle floating crash
                    this.users.add(user);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    Log.e(OpenVKAPI.TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
            if(downloadManager != null) {
                downloadManager.downloadPhotosToCache(avatars, "profile_avatars");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUser(OvkAPIWrapper wrapper, long user_id) {
        wrapper.sendAPIMethod("Users.get",
                String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200," +
                "photo_200_orig,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen," +
                                "interests,music,movies,tv,books,city,counters",
                user_id));
    }

    public void getAccountUser(OvkAPIWrapper wrapper, long user_id) {
        wrapper.sendAPIMethod("Users.get",
                String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200," +
                "photo_200_orig,photo_400,photo_max_orig,status,screen_name,friend_status,last_seen," +
                                "interests,music,movies,tv,books,city,counters",
                user_id), "account_user");
    }

    public void getPeerUsers(OvkAPIWrapper wrapper, ArrayList<Conversation> conversations) {
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
        wrapper.sendAPIMethod("Users.get",
                String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200," +
                "photo_400,photo_max_orig,status,screen_name,friend_status,last_seen," +
                                "interests,music,movies,tv,books,city,counters",
                ids_list), "peers");
    }

    public void get(OvkAPIWrapper wrapper, ArrayList<Long> user_ids) {
        StringBuilder ids_list = new StringBuilder();
        for(int i = 0; i < user_ids.size(); i++) {
            if(i < user_ids.size() - 1) {
                ids_list.append(String.format("%s,", user_ids.get(i)));
            } else {
                ids_list.append(user_ids.get(i));
            }
        }
        wrapper.sendAPIMethod("Users.get",
                String.format("user_ids=%s&fields=verified,sex,has_photo,photo_200," +
                "photo_400,photo_max_orig,status,screen_name,friend_status,last_seen," +
                                "interests,music,movies,tv,books,city,counters",
                ids_list.toString()));
    }

    public ArrayList<User> getList() {
        return users;
    }

    public void search(OvkAPIWrapper wrapper, String query) {
        wrapper.sendAPIMethod("Users.search",
                String.format("q=%s&count=50&fields=verified,sex,has_photo,photo_200," +
                "photo_400,photo_max_orig,status,screen_name,friend_status,last_seen," +
                                "interests,music,movies,tv,books,city,counters",
                URLEncoder.encode(query)));
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
