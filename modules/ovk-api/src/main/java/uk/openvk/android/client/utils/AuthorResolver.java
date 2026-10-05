package uk.openvk.android.client.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;

public class AuthorResolver {

    public static LazyEntity resolveAuthorFromJSON(JSONObject collection, long id) {
        LazyEntity author = null;
        try {
            if(collection.has("groups") && id < 0) {
                JSONArray groups = collection.getJSONArray("groups");
                for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                    JSONObject group = groups.getJSONObject(groups_index);
                    if (-group.getLong("id") == id) {
                        author = parseGroupFromEntity(group, -group.getLong("id"));
                    }
                }
            } else if(collection.has("profiles") && id > 0 && id < 200000000){
                JSONArray profiles = collection.getJSONArray("profiles");
                for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                    JSONObject profile = profiles.getJSONObject(profiles_index);
                    if (profile.getLong("id") == id) {
                        author = parseProfileFromEntity(profile, profile.getLong("id"));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return author;
    }

    private static LazyEntity parseGroupFromEntity(JSONObject group, long owner_id) {
        LazyEntity groupEntity = new Group();
        groupEntity.id = owner_id;

        try {
            ((Group) groupEntity).name = group.getString("name");
            ((Group) groupEntity).avatar_url = group.getString("photo_50");
            if (group.get("verified") instanceof Integer) {
                ((Group) groupEntity).verified = group.getInt("verified") == 1;
            } else {
                ((Group) groupEntity).verified = group.getBoolean("verified");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupEntity;
    }

    private static LazyEntity parseProfileFromEntity(JSONObject profile, long user_id) {
        LazyEntity user = new User();
        user.id = user_id;
        try {
            ((User) user).first_name = profile.getString("first_name");
            ((User) user).last_name = profile.getString("last_name");
            ((User) user).avatar_url = profile.getString("photo_50");
            if (profile.has("verified")) {
                if (profile.get("verified") instanceof Integer) {
                    ((User) user).verified = profile.getInt("verified") == 1;
                } else {
                    ((User) user).verified = profile.getBoolean("verified");
                }
            }
            if (profile.has("sex"))
                ((User) user).sex = profile.getInt("sex");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return user;
    }
}
