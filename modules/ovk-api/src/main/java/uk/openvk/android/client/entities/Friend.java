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

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.JSONParser;

public class Friend implements Parcelable {
    public String first_name;
    public String last_name;
    public Long id;
    public boolean verified;
    public boolean online;
    public Bitmap avatar;
    public String avatar_url;
    private JSONParser jsonParser;
    public boolean from_mobile;

    public Friend(JSONObject user) {
        parse(user);
    }

    public Friend(String response, int position) {
        parse(response, position);
    }

    public Friend(String first_name, String last_name, long id, String status, String city,
                  String screen_name, String avatar_url, int friends_status, int ls_date, String birthdate,
                  String interests, String movies, String music, String tv, String books, boolean verified) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.verified = verified;
        jsonParser = new JSONParser();
    }

    protected Friend(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        id = in.readLong();
        verified = in.readByte() != 0;
        online = in.readByte() != 0;
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        avatar_url = in.readString();
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public Friend() {

    }

    public void parse(JSONObject user) {
        try {
            if(user != null) {
                first_name = user.getString("first_name");
                last_name = user.getString("last_name");
                id = user.getLong("id");
                avatar_url = "";
                if (user.has("verified")) {
                    verified = user.getInt("verified") == 1;
                } else {
                    verified = false;
                }
                online = user.has("online") && user.getInt("online") == 1;
                if (user.has("last_seen") && !user.isNull("last_seen")) {
                    if (user.getJSONObject("last_seen").getInt("platform") != 7) {
                        from_mobile = true;
                    }
                }
                if (user.has("photo_50")) {
                    avatar_url = user.getString("photo_50");
                } else if (user.has("photo_100")) {
                    avatar_url = user.getString("photo_100");
                } else if (user.has("photo_200_orig")) {
                    avatar_url = user.getString("photo_200_orig");
                } else if (user.has("photo_200")) {
                    avatar_url = user.getString("photo_200");
                } else if (user.has("photo_400")) {
                    avatar_url = user.getString("photo_400");
                } else if (user.has("photo_400_orig")) {
                    avatar_url = user.getString("photo_400_orig");
                } else if (user.has("photo_max")) {
                    avatar_url = user.getString("photo_max");
                } else if (user.has("photo_max_orig")) {
                    avatar_url = user.getString("photo_max_orig");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void parse(String response, int position) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONArray("items");
            if(users != null) {
                for (int i = 0; i < users.length(); i++) {
                    if(i == position) {
                        JSONObject user = (JSONObject) users.get(i);
                        first_name = user.getString("first_name");
                        last_name = user.getString("last_name");
                        id = user.getLong("id");
                        verified = user.getInt("verified") == 1;
                        online = user.getInt("online") == 1;
                        if (user.has("photo_50")) {
                            avatar_url = user.getString("photo_50");
                        } else if (user.has("photo_100")) {
                            avatar_url = user.getString("photo_100");
                        } else if (user.has("photo_200_orig")) {
                            avatar_url = user.getString("photo_200_orig");
                        } else if (user.has("photo_200")) {
                            avatar_url = user.getString("photo_200");
                        } else if (user.has("photo_400")) {
                            avatar_url = user.getString("photo_400");
                        } else if (user.has("photo_400_orig")) {
                            avatar_url = user.getString("photo_400_orig");
                        } else if (user.has("photo_max")) {
                            avatar_url = user.getString("photo_max");
                        } else if (user.has("photo_max_orig")) {
                            avatar_url = user.getString("photo_max_orig");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatar(DownloadManager downloadManager) {
        downloadManager.downloadOnePhotoToCache(avatar_url, String.format("avatar_%s", id), "friend_avatars");
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
        parcel.writeByte((byte) (verified ? 1 : 0));
        parcel.writeByte((byte) (online ? 1 : 0));
        parcel.writeParcelable(avatar, i);
        parcel.writeString(avatar_url);
    }
}
