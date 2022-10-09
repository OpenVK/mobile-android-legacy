package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Likes implements Parcelable {
    private JSONParser jsonParser;
    public int owner_id;
    public int item_id;
    public int count;
    public int position;

    public Likes() {
        jsonParser = new JSONParser();
    }

    protected Likes(Parcel in) {
        owner_id = in.readInt();
        item_id = in.readInt();
        count = in.readInt();
        position = in.readInt();
    }

    public static final Creator<Likes> CREATOR = new Creator<Likes>() {
        @Override
        public Likes createFromParcel(Parcel in) {
            return new Likes(in);
        }

        @Override
        public Likes[] newArray(int size) {
            return new Likes[size];
        }
    };

    public void add(OvkAPIWrapper ovk, int owner_id, int post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        ovk.sendAPIMethod("Likes.add", String.format("type=post&owner_id=%d&item_id=%d", owner_id, post_id));
    }

    public void delete(OvkAPIWrapper ovk, int owner_id, int post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        ovk.sendAPIMethod("Likes.delete", String.format("type=post&owner_id=%d&item_id=%d", owner_id, post_id));
    }

    public void parse(String response) {
        JSONObject json = jsonParser.parseJSON(response);
        try {
            if(json != null) {
                JSONObject result = json.getJSONObject("response");
                count = result.getInt("likes");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(owner_id);
        parcel.writeInt(item_id);
        parcel.writeInt(count);
        parcel.writeInt(position);
    }
}
