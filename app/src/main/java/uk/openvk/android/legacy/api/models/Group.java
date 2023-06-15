package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
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
    public String description;
    public String site;
    public ArrayList<User> members;

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
                if(group.has("description") && !group.isNull("description")) {
                    description = group.getString("description");
                }
                if(group.has("site") && !group.isNull("site")) {
                    site = group.getString("site");
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
            downloadManager.downloadOnePhotoToCache(avatar_msize_url, String.format("avatar_%s", id),
                    "group_avatars");
        } else if(quality.equals("high")) {
            if(avatar_hsize_url.length() == 0) {
                avatar_hsize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_hsize_url, String.format("avatar_%s", id),
                    "group_avatars");
        } else if(quality.equals("original")) {
            if(avatar_osize_url.length() == 0) {
                avatar_osize_url = avatar_msize_url;
            }
            downloadManager.downloadOnePhotoToCache(avatar_osize_url, String.format("avatar_%s", id),
                    "group_avatars");
        }
    }

    public void join(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Groups.join", String.format("group_id=%s", id));
    }

    public void leave(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Groups.leave", String.format("group_id=%s", id));
    }

    public void getMembers(OvkAPIWrapper ovk, int count, String where) {
        ovk.sendAPIMethod("Groups.getMembers", String.format("group_id=%s&fields=verified,online,photo_100," +
                "photo_200_orig,photo_200,last_seen&count=%s", id, count), where);
    }

    public void parseMembers(String response, DownloadManager downloadManager, boolean downloadPhoto) {
        try {
            this.members.clear();
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            if(json != null) {
                members_count = json.getInt("count");
                JSONArray members = json.getJSONArray("items");
                ArrayList<PhotoAttachment> avatars;
                avatars = new ArrayList<PhotoAttachment>();
                for (int i = 0; i < members.length(); i++) {
                    User member = new User();
                    JSONObject user = members.getJSONObject(i);
                    member.first_name = user.getString("name").split(" ")[0];
                    if(user.getString("name").split(" ").length > 1) {
                        if(user.getString("name").split(" ")[1] != null) {
                            member.last_name = user.getString("name").split(" ")[1];
                        } else {
                            member.last_name = "";
                        }
                    }
                    member.id = user.getLong("id");
                    member.avatar_url = user.getString("photo_100");
                    PhotoAttachment photoAttachment = new PhotoAttachment();
                    if(member.avatar_url != null && member.avatar_url.length() > 0) {
                        photoAttachment.url = member.avatar_url;
                        photoAttachment.filename = String.format("avatar_%s", member.id);
                        avatars.add(photoAttachment);
                    }
                    this.members.add(member);
                }
                if (downloadPhoto) {
                    downloadManager.downloadPhotosToCache(avatars, "group_members_avatars");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
