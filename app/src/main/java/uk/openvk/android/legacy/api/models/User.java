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
public class User {
    public String first_name;
    public String last_name;
    public int id;
    public boolean verified;
    public boolean online;
    public int ls_date;
    public String status;
    public String city;
    public String birthdate;
    public String screen_name;
    public Bitmap avatar;
    public String avatar_url;
    public int friends_status;
    public String interests;
    public String movies;
    public String music;
    public String tv;
    public String books;
    private JSONParser jsonParser;

    public User(JSONObject user) {
        parse(user);
    }

    public User(String response, int position) {
        parse(response, position);
    }

    public User(String first_name, String last_name, int id, String status, String city, String screen_name, String avatar_url, int friends_status, int ls_date, String birthdate,
                String interests, String movies, String music, String tv, String books, boolean verified) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.verified = verified;
        this.status = status;
        this.city = city;
        this.birthdate = birthdate;
        this.screen_name = screen_name;
        this.avatar_url = avatar_url;
        this.friends_status = friends_status;
        this.ls_date = ls_date;
        this.interests = interests;
        this.movies = movies;
        this.music = music;
        this.tv = tv;
        this.books = books;
        jsonParser = new JSONParser();
    }

    public User() {
        jsonParser = new JSONParser();
    }

    public void parse(JSONObject user) {
        try {
            if(user != null) {
                first_name = user.getString("first_name");
                last_name = user.getString("last_name");
                id = user.getInt("id");
                if(user.has("last_seen") && !user.isNull("last_seen")) {
                    ls_date = user.getJSONObject("last_seen").getInt("time");
                }
                if(!user.isNull("status")) {
                    status = user.getString("status");
                } else {
                    status = "";
                }
                //screen_name = user.getString("screen_name");
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
                friends_status = user.getInt("friend_status");
                if(!user.isNull("interests")) {
                    interests = user.getString("interests");
                } else {
                    interests = "";
                }
                if(!user.isNull("movies")) {
                    movies = user.getString("movies");
                } else {
                    movies = "";
                }
                if(!user.isNull("music")) {
                    music = user.getString("music");
                } else {
                    music = "";
                }
                if(!user.isNull("tv")) {
                    tv = user.getString("tv");
                } else {
                    tv = "";
                }
                if(!user.isNull("books")) {
                    books = user.getString("books");
                } else {
                    books = "";
                }
                //birthdate = user.getString("bdate");
                if(!user.isNull("city")) {
                    city = user.getString("city");
                } else {
                    city = "";
                }
                //birthdate = user.getString("bdate");
                if(!user.isNull("city")) {
                    city = user.getString("city");
                }
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void parse(String response, int position) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray users = json.getJSONArray("items");
            if (users != null) {
                for (int i = 0; i < users.length(); i++) {
                    if (i == position) {
                        JSONObject user = (JSONObject) users.get(i);
                        first_name = user.getString("first_name");
                        last_name = user.getString("last_name");
                        id = user.getInt("id");
                        status = user.getString("status");
                        screen_name = user.getString("screen_name");
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
                        friends_status = user.getInt("friend_status");
                        if (!user.isNull("interests")) {
                            interests = user.getString("interests");
                        } else {
                            interests = "";
                        }
                        if (!user.isNull("movies")) {
                            movies = user.getString("movies");
                        } else {
                            movies = "";
                        }
                        if (!user.isNull("music")) {
                            music = user.getString("music");
                        } else {
                            music = "";
                        }
                        if (!user.isNull("tv")) {
                            tv = user.getString("tv");
                        } else {
                            tv = "";
                        }
                        if (!user.isNull("books")) {
                            books = user.getString("books");
                        } else {
                            books = "";
                        }
                        //birthdate = user.getString("bdate");
                        if (!user.isNull("city")) {
                            city = user.getString("city");
                        } else {
                            city = "";
                        }
                        if (user.getInt("verified") == 1) {
                            verified = true;
                        } else {
                            verified = false;
                        }
                        if (user.getInt("online") == 1) {
                            online = true;
                        } else {
                            online = false;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatar(DownloadManager downloadManager) {
        downloadManager.downloadOnePhotoToCache(avatar_url, String.format("avatar_%d", id), "profile_avatars");
    }
}
