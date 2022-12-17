package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;

import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 09.10.2022.
 */

public class Group implements Parcelable {
    public String name;
    public long id;
    public boolean verified;
    private JSONParser jsonParser;
    public String screen_name;
    public Bitmap avatar;
    public String avatar_msize_url;
    public String avatar_hsize_url;
    public String avatar_osize_url;
    public long members_count;
    public int is_member;

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
                id = group.getLong("id");
                if(group.has("is_member")) {
                    is_member = group.getInt("is_member");
                }
                avatar_msize_url = "";
                avatar_hsize_url = "";
                avatar_osize_url = "";
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
                    avatar_msize_url = group.getString("photo_50");
                } if (group.has("photo_100")) {
                    avatar_msize_url = group.getString("photo_100");
                } if (group.has("photo_200")) {
                    avatar_msize_url = group.getString("photo_200");
                } if (group.has("photo_200_orig")) {
                    avatar_msize_url = group.getString("photo_200_orig");
                } if (group.has("photo_400")) {
                    avatar_hsize_url = group.getString("photo_400");
                } if (group.has("photo_400_orig")) {
                    avatar_hsize_url = group.getString("photo_400_orig");
                } if (group.has("photo_max")) {
                    avatar_osize_url = group.getString("photo_max");
                } if (group.has("photo_max_orig")) {
                    avatar_osize_url = group.getString("photo_max_orig");
                }
                if(group.has("members_count")) {
                    members_count = group.getLong("members_count");
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Group(Parcel in) {
        name = in.readString();
        id = in.readLong();
        verified = in.readByte() != 0;
        screen_name = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        members_count = in.readLong();
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
        parcel.writeLong(id);
        parcel.writeByte((byte) (verified ? 1 : 0));
        parcel.writeString(screen_name);
        parcel.writeParcelable(avatar, i);
        parcel.writeString(avatar_msize_url);
        parcel.writeString(avatar_hsize_url);
        parcel.writeString(avatar_osize_url);
        parcel.writeLong(members_count);
    }


    public void downloadAvatar(DownloadManager downloadManager, String quality) {
        if(quality.equals("medium")) {
            downloadManager.downloadOnePhotoToCache(avatar_msize_url, String.format("avatar_%d", id), "group_avatars");
        } else if(quality.equals("high")) {
            if(avatar_hsize_url.length() == 0) {
                avatar_hsize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_hsize_url, String.format("avatar_%d", id), "group_avatars");
        } else if(quality.equals("original")) {
            if(avatar_osize_url.length() == 0) {
                avatar_osize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_osize_url, String.format("avatar_%d", id), "group_avatars");
        }
    }

    public void join(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Groups.join", String.format("group_id=%d", id));
    }

    public void leave(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Groups.leave", String.format("group_id=%d", id));
    }
}
