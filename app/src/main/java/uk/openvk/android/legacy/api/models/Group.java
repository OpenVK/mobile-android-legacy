package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.DownloadManager;
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
    public String avatar_url;
    public int members_count;

    public Group() {
        jsonParser = new JSONParser();
    }

    public Group(JSONObject group) {
        jsonParser = new JSONParser();
        parse(group);
    }

    public void parse(JSONObject group) {
        try {
            if (group != null) {
                name = group.getString("name");
                id = group.getInt("id");
                avatar_url = "";
                if(group.has("screen_name") && !group.isNull("screen_name")) {
                    screen_name = group.getString("screen_name");
                }
                if(group.has("verified")) {
                    if (group.getInt("verified") == 1) {
                        verified = true;
                    } else {
                        verified = false;
                    }
                } else {
                    verified = false;
                }

                if (group.has("photo_50")) {
                    avatar_url = group.getString("photo_50");
                } else if (group.has("photo_100")) {
                    avatar_url = group.getString("photo_100");
                } else if (group.has("photo_200_orig")) {
                    avatar_url = group.getString("photo_200_orig");
                } else if (group.has("photo_200")) {
                    avatar_url = group.getString("photo_200");
                } else if (group.has("photo_400")) {
                    avatar_url = group.getString("photo_400");
                } else if (group.has("photo_400_orig")) {
                    avatar_url = group.getString("photo_400_orig");
                } else if (group.has("photo_max")) {
                    avatar_url = group.getString("photo_max");
                } else if (group.has("photo_max_orig")) {
                    avatar_url = group.getString("photo_max_orig");
                }
                if(group.has("members_count")) {
                    members_count = group.getInt("members_count");
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
        members_count = in.readInt();
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
        parcel.writeString(avatar_url);
        parcel.writeInt(members_count);
    }


    public void downloadAvatar(DownloadManager downloadManager) {
        downloadManager.downloadOnePhotoToCache(avatar_url, String.format("avatar_%d", id), "group_avatars");
    }


}
