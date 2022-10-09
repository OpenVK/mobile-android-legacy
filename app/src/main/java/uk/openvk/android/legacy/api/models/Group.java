package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/**
 * Created by Dmitry on 09.10.2022.
 */

public class Group implements Parcelable {
    public String name;
    public int id;
    public boolean verified;
    private JSONParser jsonParser;
    public String screen_name;
    public Bitmap avatar;

    public Group() {
        jsonParser = new JSONParser();
    }

    public Group(JSONObject group) {
        parse(group);
    }

    public void parse(JSONObject group) {
        try {
            if (group != null) {
                name = group.getString("name");
                id = group.getInt("id");
                if(group.has("screen_name") && !group.isNull("screen_name")) {
                    screen_name = group.getString("screen_name");
                }
                if(group.has("verified")) {
                    if (group.getInt("verified") == 1) {
                        verified = true;
                    } else {
                        verified = false;
                    }
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Group(Parcel in) {
        name = in.readString();
        id = in.readInt();
        verified = in.readByte() != 0;
        screen_name = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(id);
        parcel.writeByte((byte) (verified ? 1 : 0));
        parcel.writeString(screen_name);
        parcel.writeParcelable(avatar, i);
    }
}
