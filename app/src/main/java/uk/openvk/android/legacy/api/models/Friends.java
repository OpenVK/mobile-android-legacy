package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
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

public class Friends implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<Friend> friends;
    public ArrayList<Friend> requests;
    private DownloadManager downloadManager;
    public int count;
    public int offset = 0;

    public Friends() {
        jsonParser = new JSONParser();
        friends = new ArrayList<Friend>();
        requests = new ArrayList<Friend>();
    }

    public Friends(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        jsonParser = new JSONParser();
        friends = new ArrayList<Friend>();
        requests = new ArrayList<Friend>();
        parse(response, downloadManager, downloadPhoto, true);
    }

    protected Friends(Parcel in) {
    }

    public static final Creator<Friends> CREATOR = new Creator<Friends>() {
        @Override
        public Friends createFromParcel(Parcel in) {
            return new Friends(in);
        }

        @Override
        public Friends[] newArray(int size) {
            return new Friends[size];
        }
    };

    public void parse(String response, DownloadManager downloadManager, boolean downloadPhoto, boolean clear) {
        try {
            if(clear) {
                this.friends.clear();
            }
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            if(json != null) {
                count = json.getInt("count");
                JSONArray users = json.getJSONArray("items");
                ArrayList<PhotoAttachment> avatars;
                avatars = new ArrayList<PhotoAttachment>();
                for (int i = 0; i < users.length(); i++) {
                    Friend friend = new Friend(users.getJSONObject(i));
                    PhotoAttachment photoAttachment = new PhotoAttachment();
                    photoAttachment.url = friend.avatar_url;
                    photoAttachment.filename = String.format("avatar_%s", friend.id);
                    avatars.add(photoAttachment);
                    this.friends.add(friend);
                }
                if (downloadPhoto) {
                    downloadManager.downloadPhotosToCache(avatars, "friend_avatars");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseRequests(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        try {
            this.requests.clear();
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            if(json != null) {
                count = json.getInt("count");
                JSONArray users = json.getJSONArray("items");
                ArrayList<PhotoAttachment> avatars;
                avatars = new ArrayList<PhotoAttachment>();
                for (int i = 0; i < users.length(); i++) {
                    Friend friend = new Friend(users.getJSONObject(i));
                    PhotoAttachment photoAttachment = new PhotoAttachment();
                    if(friend.avatar_url != null && friend.avatar_url.length() > 0) {
                        photoAttachment.url = friend.avatar_url;
                        photoAttachment.filename = String.format("avatar_%s", friend.id);
                        avatars.add(photoAttachment);
                    }
                    this.requests.add(friend);
                }
                if (downloadPhoto) {
                    downloadManager.downloadPhotosToCache(avatars, "friend_avatars");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper wrapper, long user_id, int count, String where) {
        wrapper.sendAPIMethod("Friends.get", String.format("user_id=%s&fields=verified,online,photo_100," +
                "photo_200_orig,photo_200,last_seen&count=%s", user_id, count), where);
    }

    public void get(OvkAPIWrapper wrapper, long user_id, int count, int offset) {
        this.offset++;
        wrapper.sendAPIMethod("Friends.get", String.format("user_id=%s&fields=verified,online,photo_100," +
                "photo_200_orig,photo_200,last_seen&count=%s&offset=%s", user_id, count, this.offset),
                "more_friends");
    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public void add(OvkAPIWrapper wrapper, long user_id) {
        wrapper.sendAPIMethod("Friends.add", String.format("user_id=%s", user_id));
    }

    public void delete(OvkAPIWrapper wrapper, long user_id) {
        wrapper.sendAPIMethod("Friends.delete", String.format("user_id=%s", user_id));
    }

    public void getRequests(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Friends.getRequests", String.format("fields=verified,online,photo_100,photo_200_orig,photo_200"));
    }
}
