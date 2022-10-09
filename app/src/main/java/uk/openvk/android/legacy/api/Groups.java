package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.User;
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
    }

    public Groups(String response) {
        jsonParser = new JSONParser();
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
}
