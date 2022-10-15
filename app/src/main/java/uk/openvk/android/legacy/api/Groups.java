package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.Photo;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 09.10.2022.
 */

public class Groups implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<Group> groups;


    public Groups() {
        jsonParser = new JSONParser();
        groups = new ArrayList<Group>();
    }

    public Groups(String response) {
        jsonParser = new JSONParser();
        groups = new ArrayList<Group>();
        parse(response);
    }

    protected Groups(Parcel in) {
        groups = in.createTypedArrayList(Group.CREATOR);
    }

    public static final Creator<Groups> CREATOR = new Creator<Groups>() {
        @Override
        public Groups createFromParcel(Parcel in) {
            return new Groups(in);
        }

        @Override
        public Groups[] newArray(int size) {
            return new Groups[size];
        }
    };

    public void parse(String response) {
        try {
            groups = new ArrayList<Group>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray groups = json.getJSONArray("response");
            if(this.groups.size() > 0) {
                this.groups.clear();
            }
            for (int i = 0; i < groups.length(); i++) {
                Group group = new Group(groups.getJSONObject(i));
                this.groups.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSearch(String response) {
        try {
            groups = new ArrayList<Group>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray groups = json.getJSONObject("response").getJSONArray("items");
            if(this.groups.size() > 0) {
                this.groups.clear();
            }
            for (int i = 0; i < groups.length(); i++) {
                Group group = new Group(groups.getJSONObject(i));
                this.groups.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Group> getList() {
        return groups;
    }

    public void search(OvkAPIWrapper ovk, String query) {
        ovk.sendAPIMethod("Groups.search", String.format("q=%s&count=50", query));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(groups);
    }

    public void getGroupByID(OvkAPIWrapper ovk, int id) {
        ovk.sendAPIMethod("Groups.getById", String.format("group_id=%d&fields=verified,photo_100,is_member,members_count", id));
    }

    public void getGroups(OvkAPIWrapper ovk, int user_id) {
        ovk.sendAPIMethod("Groups.get", String.format("user_id=%d&fields=verified,photo_200,is_member,members_count&extended=1", user_id));
    }

    public void parse(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        try {
            this.groups.clear();
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            JSONArray groups = json.getJSONArray("items");
            ArrayList<Photo> avatars;
            avatars = new ArrayList<Photo>();
            for (int i = 0; i < groups.length(); i++) {
                Group group = new Group(groups.getJSONObject(i));
                Photo photo = new Photo();
                photo.url = group.avatar_url;
                photo.filename = String.format("avatar_%d", group.id);
                avatars.add(photo);
                this.groups.add(group);
            }
            if(downloadPhoto) {
                downloadManager.downloadPhotosToCache(avatars, "group_avatars");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
