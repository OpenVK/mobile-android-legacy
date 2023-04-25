package uk.openvk.android.legacy.api;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.InstanceAdmin;
import uk.openvk.android.legacy.api.models.InstanceLink;
import uk.openvk.android.legacy.api.models.InstanceStatistics;
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

public class Ovk implements Parcelable {
    private JSONParser jsonParser;
    public String version;
    public InstanceStatistics instance_stats;
    public ArrayList<InstanceAdmin> instance_admins;
    public ArrayList<InstanceLink> instance_links;
    public Ovk() {
        jsonParser = new JSONParser();
    }

    protected Ovk(Parcel in) {
        version = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(version);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Ovk> CREATOR = new Creator<Ovk>() {
        @Override
        public Ovk createFromParcel(Parcel in) {
            return new Ovk(in);
        }

        @Override
        public Ovk[] newArray(int size) {
            return new Ovk[size];
        }
    };

    public void getVersion(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Ovk.version");
    }

    public void aboutInstance(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Ovk.aboutInstance", "fields=statistics,links,administrators");
    }

    public void parseVersion(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            version = json.getString("response");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseAboutInstance(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject statistics = json.getJSONObject("response").getJSONObject("statistics");
                int users_count = 0;
                int online_users_count = 0;
                int active_users_count = 0;
                int groups_count = 0;
                int wall_posts_count = 0;
                if(statistics.has("users_count")) users_count = statistics.getInt("users_count");
                if(statistics.has("online_users_count")) online_users_count = statistics.getInt("online_users_count");
                if(statistics.has("active_users_count")) active_users_count = statistics.getInt("active_users_count");
                if(statistics.has("groups_count")) groups_count = statistics.getInt("groups_count");
                if(statistics.has("wall_posts_count")) wall_posts_count = statistics.getInt("wall_posts_count");
                instance_stats = new InstanceStatistics(users_count, online_users_count, active_users_count, groups_count, wall_posts_count);
                JSONObject admins = json.getJSONObject("response").getJSONObject("administrators");
                JSONArray admin_items = json.getJSONObject("response").getJSONObject("administrators").getJSONArray("items");
                JSONArray links_items = json.getJSONObject("response").getJSONObject("links").getJSONArray("items");
                instance_admins = new ArrayList<InstanceAdmin>();
                for(int i = 0; i < admin_items.length(); i++) {
                    JSONObject admin = admin_items.getJSONObject(i);
                    instance_admins.add(new InstanceAdmin(admin.getString("first_name"), admin.getString("last_name"), admin.getInt("id")));
                }
                instance_links = new ArrayList<InstanceLink>();
                for(int i = 0; i < links_items.length(); i++) {
                    JSONObject link = links_items.getJSONObject(i);
                    instance_links.add(new InstanceLink(link.getString("name"), link.getString("url")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
