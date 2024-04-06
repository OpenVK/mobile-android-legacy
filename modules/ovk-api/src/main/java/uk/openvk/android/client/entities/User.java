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

import uk.openvk.android.client.counters.UserCounters;
import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.JSONParser;

public class User implements Parcelable {
    public String first_name;
    public String last_name;
    public long id;
    public boolean verified;
    public boolean online;
    public long ls_date;
    public int ls_platform;
    public String status;
    public String city;
    public String birthdate;
    public String screen_name;
    public Bitmap avatar;
    public String avatar_msize_url;
    public String avatar_hsize_url;
    public String avatar_osize_url;
    public long avatar_id;
    public int friends_status;
    public String interests;
    public String movies;
    public String music;
    public String tv;
    public String books;
    public String deactivated;
    private JSONParser jsonParser;
    public int sex;
    public String avatar_url;
    public String ban_reason;
    public UserCounters counters;

    public User(JSONObject user) {
        parse(user);
    }

    public User(String response, int position) {
        parse(response, position);
    }

    public User() {
        jsonParser = new JSONParser();
    }

    protected User(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        id = in.readLong();
        verified = in.readByte() != 0;
        online = in.readByte() != 0;
        ls_date = in.readLong();
        status = in.readString();
        city = in.readString();
        birthdate = in.readString();
        screen_name = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        avatar_msize_url = in.readString();
        friends_status = in.readInt();
        interests = in.readString();
        movies = in.readString();
        music = in.readString();
        tv = in.readString();
        books = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void parse(JSONObject user) {
        try {
            if(user != null) {
                avatar_msize_url = "";
                avatar_hsize_url = "";
                avatar_osize_url = "";
                first_name = user.getString("first_name");
                last_name = user.getString("last_name");
                id = user.getInt("id");
                if(user.has("last_seen") && !user.isNull("last_seen")) {
                    ls_date = user.getJSONObject("last_seen").getLong("time");
                    ls_platform = user.getJSONObject("last_seen").getInt("platform");
                }
                if(!user.isNull("status")) {
                    status = user.getString("status");
                } else {
                    status = "";
                }
                //screen_name = user.getString("screen_name");
                if (user.has("photo_50")) {
                    avatar_msize_url = user.getString("photo_50");
                } if (user.has("photo_100")) {
                    avatar_msize_url = user.getString("photo_100");
                } if (user.has("photo_200")) {
                    avatar_msize_url = user.getString("photo_200");
                } if (user.has("photo_200_orig")) {
                    avatar_msize_url = user.getString("photo_200_orig");
                    avatar_url = avatar_msize_url;
                } if (user.has("photo_400")) {
                    avatar_hsize_url = user.getString("photo_400");
                } if (user.has("photo_400_orig")) {
                    avatar_hsize_url = user.getString("photo_400_orig");
                    avatar_url = avatar_hsize_url;
                } if (user.has("photo_max")) {
                    avatar_osize_url = user.getString("photo_max");
                } if (user.has("photo_max_orig")) {
                    avatar_osize_url = user.getString("photo_max_orig");
                    avatar_url = avatar_osize_url;
                }

                if(user.has("counters")) {
                    JSONObject counters_obj = user.getJSONObject("counters");
                    counters = new UserCounters(
                            counters_obj.getLong("friends_count"),
                            counters_obj.getLong("photos_count"),
                            counters_obj.getLong("videos_count"),
                            counters_obj.getLong("audios_count"),
                            counters_obj.getLong("notes_count")
                    );
                }

                if(user.has("deactivated")) {
                    deactivated = user.getString("deactivated");
                    if(deactivated.equals("banned") && user.isNull("ban_reason")) {
                        ban_reason = user.getString("ban_reason");
                    } else {
                        ban_reason = "";
                    }
                } else {
                    friends_status = user.getInt("friend_status");
                    interests = !user.isNull("interests") ? user.getString("interests") : "";
                    movies = !user.isNull("movies") ? user.getString("movies") : "";
                    music = !user.isNull("music") ? user.getString("music") : "";
                    tv = !user.isNull("tv") ? user.getString("tv") : "";
                    books = !user.isNull("books") ? user.getString("books") : "";
                    //birthdate = user.getString("bdate");
                    city = !user.isNull("city") ? user.getString("city") : "";
                    //birthdate = user.getString("bdate");
                    if (!user.isNull("city")) {
                        city = user.getString("city");
                    }
                    verified = user.getInt("verified") == 1;
                    online = user.getInt("online") == 1;
                    if(user.has("sex")) {
                        sex = user.getInt("sex");
                    }
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
            if (users != null) {
                for (int i = 0; i < users.length(); i++) {
                    if (i == position) {
                        JSONObject user = (JSONObject) users.get(i);
                        first_name = user.getString("first_name");
                        last_name = user.getString("last_name");
                        id = user.getInt("id");
                        status = user.getString("status");
                        screen_name = user.getString("screen_name");
                        if (user.has("photo_50")) {
                            avatar_msize_url = user.getString("photo_50");
                        } if (user.has("photo_100")) {
                            avatar_msize_url = user.getString("photo_100");
                        } if (user.has("photo_200_orig")) {
                            avatar_msize_url = user.getString("photo_200_orig");
                            avatar_url = avatar_msize_url;
                        } if (user.has("photo_200")) {
                            avatar_msize_url = user.getString("photo_200");
                        } if (user.has("photo_400")) {
                            avatar_hsize_url = user.getString("photo_400");
                        } if (user.has("photo_400_orig")) {
                            avatar_hsize_url = user.getString("photo_400_orig");
                            avatar_url = avatar_hsize_url;
                        } if (user.has("photo_max")) {
                            avatar_osize_url = user.getString("photo_max");
                        } if (user.has("photo_max_orig")) {
                            avatar_osize_url = user.getString("photo_max_orig");
                            avatar_url = avatar_osize_url;
                        }

                        if(user.has("counters")) {
                            JSONObject counters_obj = user.getJSONObject("counters");
                            counters = new UserCounters(
                                    counters_obj.getLong("friends_count"),
                                    counters_obj.getLong("photos_count"),
                                    counters_obj.getLong("videos_count"),
                                    counters_obj.getLong("audios_count"),
                                    counters_obj.getLong("notes_count")
                            );
                        }

                        friends_status = user.getInt("friend_status");
                        interests = !user.isNull("interests") ? user.getString("interests") : "";
                        movies = !user.isNull("movies") ? user.getString("movies") : "";
                        music = !user.isNull("music") ? user.getString("music") : "";
                        tv = !user.isNull("tv") ? user.getString("tv") : "";
                        books = !user.isNull("books") ? user.getString("books") : "";
                        //birthdate = user.getString("bdate");
                        city = !user.isNull("city") ? user.getString("city") : "";
                        verified = user.getInt("verified") == 1;
                        online = user.getInt("online") == 1;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatar(DownloadManager downloadManager, String quality) {
        switch (quality) {
            case "medium":
                downloadManager.downloadOnePhotoToCache(avatar_msize_url, String.format("avatar_%s", id),
                        "profile_avatars");
                break;
            case "high":
                if (avatar_hsize_url.length() == 0) {
                    avatar_hsize_url = avatar_msize_url;
                }
                downloadManager.downloadOnePhotoToCache(avatar_hsize_url, String.format("avatar_%s", id),
                        "profile_avatars");
                break;
            case "original":
                if (avatar_osize_url.length() == 0) {
                    avatar_osize_url = avatar_msize_url;
                }
                downloadManager.downloadOnePhotoToCache(avatar_osize_url, String.format("avatar_%s", id),
                        "profile_avatars");
                break;
        }
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
        parcel.writeLong(ls_date);
        parcel.writeString(status);
        parcel.writeString(city);
        parcel.writeString(birthdate);
        parcel.writeString(screen_name);
        parcel.writeParcelable(avatar, i);
        parcel.writeString(avatar_msize_url);
        parcel.writeString(avatar_hsize_url);
        parcel.writeString(avatar_osize_url);
        parcel.writeInt(friends_status);
        parcel.writeString(interests);
        parcel.writeString(movies);
        parcel.writeString(music);
        parcel.writeString(tv);
        parcel.writeString(books);
    }

    public void downloadAvatar(DownloadManager downloadManager, String quality, String where) {
        if(quality.equals("medium")) {
            downloadManager.downloadOnePhotoToCache(avatar_msize_url, String.format("avatar_%s", id), where);
        } else if(quality.equals("high")) {
            if(avatar_hsize_url.length() == 0) {
                avatar_hsize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_hsize_url, String.format("avatar_%s", id), where);
        } else if(quality.equals("original")) {
            if(avatar_osize_url.length() == 0) {
                avatar_osize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_osize_url, String.format("avatar_%s", id), where);
        }
    }
}
