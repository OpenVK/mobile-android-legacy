package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

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

    public void get(OvkAPIWrapper ovk, long user_id, int count, String where) {
        ovk.sendAPIMethod("Friends.get", String.format("user_id=%s&fields=verified,online,photo_100,photo_200_orig,photo_200,last_seen&count=%s", user_id, count), where);
    }

    public void get(OvkAPIWrapper ovk, long user_id, int count, int offset) {
        this.offset++;
        ovk.sendAPIMethod("Friends.get", String.format("user_id=%s&fields=verified,online,photo_100,photo_200_orig,photo_200,last_seen&count=%s&offset=%s", user_id, count, this.offset), "more_friends");
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

    public void add(OvkAPIWrapper ovk, long user_id) {
        ovk.sendAPIMethod("Friends.add", String.format("user_id=%d", user_id));
    }

    public void delete(OvkAPIWrapper ovk, long user_id) {
        ovk.sendAPIMethod("Friends.delete", String.format("user_id=%d", user_id));
    }

    public void getRequests(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Friends.getRequests", String.format("fields=verified,online,photo_100,photo_200_orig,photo_200"));
    }
}
