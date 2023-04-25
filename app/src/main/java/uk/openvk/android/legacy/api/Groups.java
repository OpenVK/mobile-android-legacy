package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.models.Group;
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

public class Groups implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<Group> groups;
    public int offset;

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
        ovk.sendAPIMethod("Groups.search", String.format("q=%s&count=50", URLEncoder.encode(query)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(groups);
    }

    public void getGroupByID(OvkAPIWrapper ovk, long id) {
        ovk.sendAPIMethod("Groups.getById", String.format("group_id=%s&fields=verified,photo_200,photo_400,photo_max_orig,is_member,members_count,site,description,contacts", id));
    }

    public void getGroups(OvkAPIWrapper ovk, long user_id, long count) {
        ovk.sendAPIMethod("Groups.get", String.format("user_id=%s&count=%s&fields=verified,photo_200,photo_400,photo_max_orig,is_member,members_count,site,description,contacts&extended=1", user_id, count));
    }

    public void getGroups(OvkAPIWrapper ovk, long user_id, int count, int offset) {
        ovk.sendAPIMethod("Groups.get", String.format("user_id=%s&count=%s&fields=verified,photo_200,photo_400,photo_max_orig,is_member,members_count,site,description,contacts&offset=%s&extended=1", user_id, count, offset), "more_groups");
    }

    public void parse(String response, DownloadManager downloadManager, String quality, boolean downloadPhoto, boolean clear) {
        try {
            if(clear) {
                this.groups.clear();
            }
            JSONObject json = jsonParser.parseJSON(response).getJSONObject("response");
            JSONArray groups = json.getJSONArray("items");
            ArrayList<PhotoAttachment> avatars;
            avatars = new ArrayList<PhotoAttachment>();
            for (int i = 0; i < groups.length(); i++) {
                Group group = new Group(groups.getJSONObject(i));
                PhotoAttachment photoAttachment = new PhotoAttachment();
                if(quality.equals("medium")) {
                    photoAttachment.url = group.avatar_msize_url;
                } else if(quality.equals("high")) {
                    photoAttachment.url = group.avatar_hsize_url;
                } else {
                    photoAttachment.url = group.avatar_osize_url;
                }

                if(photoAttachment.url.length() == 0) {
                    photoAttachment.url = group.avatar_msize_url;
                }
                photoAttachment.filename = String.format("avatar_%d", group.id);
                avatars.add(photoAttachment);
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
