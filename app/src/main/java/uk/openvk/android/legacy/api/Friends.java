package uk.openvk.android.legacy.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.api.models.Photo;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Friends {
    private JSONParser jsonParser;
    private ArrayList<Friend> friends;
    private DownloadManager downloadManager;

    public Friends() {
        jsonParser = new JSONParser();
        friends = new ArrayList<Friend>();
    }

    public Friends(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        jsonParser = new JSONParser();
        parse(response, downloadManager, downloadPhoto);
    }

    public void parse(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        try {
            this.friends.clear();
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            JSONArray users = json.getJSONArray("items");
            ArrayList<Photo> avatars;
            avatars = new ArrayList<Photo>();
            for (int i = 0; i < users.length(); i++) {
                Friend friend = new Friend(users.getJSONObject(i));
                Photo photo = new Photo();
                photo.url = friend.avatar_url;
                photo.filename = String.format("avatar_%d", friend.id);
                avatars.add(photo);
                this.friends.add(friend);
            }
            if(downloadPhoto) {
                downloadManager.downloadPhotosToCache(avatars, "friend_avatars");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper ovk, int user_id, String where) {
        ovk.sendAPIMethod("Friends.get", String.format("user_id=%d&fields=verified,online,photo_100", user_id), where);
    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }
}
