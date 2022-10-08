package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 30.09.2022.
 */
public class Friend {
    public String first_name;
    public String last_name;
    public int id;
    public boolean verified;
    public boolean online;
    public Bitmap avatar;
    public String avatar_url;
    private JSONParser jsonParser;

    public Friend(JSONObject user) {
        parse(user);
    }

    public Friend(String response, int position) {
        parse(response, position);
    }

    public Friend(String first_name, String last_name, int id, String status, String city, String screen_name, String avatar_url, int friends_status, int ls_date, String birthdate,
                  String interests, String movies, String music, String tv, String books, boolean verified) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.verified = verified;
        jsonParser = new JSONParser();
    }

    public void parse(JSONObject user) {
        try {
            if(user != null) {
                first_name = user.getString("first_name");
                last_name = user.getString("last_name");
                id = user.getInt("id");
                if(user.has("verified")) {
                    if (user.getInt("verified") == 1) {
                        verified = true;
                    } else {
                        verified = false;
                    }
                } else {
                    verified = false;
                }
                if(user.getInt("online") == 1) {
                    online = true;
                } else {
                    online = false;
                }
                if (user.has("photo_50")) {
                    avatar_url = user.getString("photo_50");
                } else if (user.has("photo_100")) {
                    avatar_url = user.getString("photo_100");
                } else if (user.has("photo_200_orig")) {
                    avatar_url = user.getString("photo_200_orig");
                } else if (user.has("photo_200")) {
                    avatar_url = user.getString("photo_200");
                } else if (user.has("photo_400")) {
                    avatar_url = user.getString("photo_400");
                } else if (user.has("photo_400_orig")) {
                    avatar_url = user.getString("photo_400_orig");
                } else if (user.has("photo_max")) {
                    avatar_url = user.getString("photo_max");
                } else if (user.has("photo_max_orig")) {
                    avatar_url = user.getString("photo_max_orig");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void parse(String response, int position) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONArray("items");
            if(users != null) {
                for (int i = 0; i < users.length(); i++) {
                    if(i == position) {
                        JSONObject user = (JSONObject) users.get(i);
                        first_name = user.getString("first_name");
                        last_name = user.getString("last_name");
                        id = user.getInt("id");
                        if(user.getInt("verified") == 1) {
                            verified = true;
                        } else {
                            verified = false;
                        }
                        if(user.getInt("online") == 1) {
                            online = true;
                        } else {
                            online = false;
                        }
                        if (user.has("photo_50")) {
                            avatar_url = user.getString("photo_50");
                        } else if (user.has("photo_100")) {
                            avatar_url = user.getString("photo_100");
                        } else if (user.has("photo_200_orig")) {
                            avatar_url = user.getString("photo_200_orig");
                        } else if (user.has("photo_200")) {
                            avatar_url = user.getString("photo_200");
                        } else if (user.has("photo_400")) {
                            avatar_url = user.getString("photo_400");
                        } else if (user.has("photo_400_orig")) {
                            avatar_url = user.getString("photo_400_orig");
                        } else if (user.has("photo_max")) {
                            avatar_url = user.getString("photo_max");
                        } else if (user.has("photo_max_orig")) {
                            avatar_url = user.getString("photo_max_orig");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatar(DownloadManager downloadManager) {
        downloadManager.downloadOnePhotoToCache(avatar_url, String.format("avatar_%d", id), "friend_avatars");
    }

}
