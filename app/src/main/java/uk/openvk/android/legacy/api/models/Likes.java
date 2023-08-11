package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Likes implements Parcelable {
    private JSONParser jsonParser;
    public long owner_id;
    public long item_id;
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

    public void add(OvkAPIWrapper wrapper, long owner_id, long post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        wrapper.sendAPIMethod("Likes.add", String.format("type=post&owner_id=%s&item_id=%s",
                owner_id, post_id));
    }

    public void delete(OvkAPIWrapper wrapper, long owner_id, long post_id, int position) {
        this.owner_id = owner_id;
        this.item_id = post_id;
        this.position = position;
        wrapper.sendAPIMethod("Likes.delete", String.format("type=post&owner_id=%s&item_id=%s",
                owner_id, post_id));
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
        parcel.writeLong(owner_id);
        parcel.writeLong(item_id);
        parcel.writeInt(count);
        parcel.writeInt(position);
    }
}
