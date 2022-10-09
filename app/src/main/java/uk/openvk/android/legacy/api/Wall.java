package uk.openvk.android.legacy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.models.Photo;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.list_items.NewsItemCountersInfo;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

/**
 * Created by Dmitry on 28.09.2022.
 */
public class Wall implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<NewsfeedItem> wallItems;
    private ArrayList<Comment> comments;

    public Wall(String response, DownloadManager downloadManager, Context ctx) {
        jsonParser = new JSONParser();
        parse(ctx, downloadManager, response);
    }

    public Wall() {
        jsonParser = new JSONParser();
    }

    public void parse(Context ctx, DownloadManager downloadManager, String response) {
        wallItems = new ArrayList<NewsfeedItem>();
        ArrayList<Photo> photos_hsize = new ArrayList<Photo>();
        ArrayList<Photo> photos_msize = new ArrayList<Photo>();
        ArrayList<Photo> avatars = new ArrayList<Photo>();
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject newsfeed = json.getJSONObject("response");
                JSONArray items = newsfeed.getJSONArray("items");
                for(int i = 0; i < items.length(); i++) {
                    JSONObject post = items.getJSONObject(i);
                    JSONObject comments = post.getJSONObject("comments");
                    JSONObject likes = post.getJSONObject("likes");
                    JSONObject reposts = post.getJSONObject("reposts");
                    JSONArray attachments = post.getJSONArray("attachments");
                    String author_name = "";
                    String owner_name = "";
                    int owner_id = post.getInt("owner_id");
                    int author_id = post.getInt("from_id");
                    int post_id = post.getInt("id");
                    int dt_sec = post.getInt("date");
                    String photo_medium_size = "";
                    String photo_high_size = "";
                    String avatar_url = "";
                    String content = post.getString("text");
                    NewsItemCountersInfo counters = new NewsItemCountersInfo(likes.getInt("count"), comments.getInt("count"), reposts.getInt("count"), false, false);
                    if(attachments.length() == 1) {
                        JSONObject attachment = attachments.getJSONObject(0);
                        if(attachment.getString("type").equals("photo")) {
                            JSONObject photo = attachment.getJSONObject("photo");
                            JSONArray photo_sizes = photo.getJSONArray("sizes");
                            photo_medium_size = photo_sizes.getJSONObject(5).getString("url");
                            photo_high_size = photo_sizes.getJSONObject(10).getString("url");
                        }
                    }
                    NewsfeedItem item = new NewsfeedItem(String.format("(Unknown author: %d)", author_id), dt_sec, null, content, counters, "",
                            photo_medium_size, photo_high_size, owner_id, post_id, ctx);
                    item.author_id = author_id;
                    if(author_id > 0) {
                        if(newsfeed.has("profiles")) {
                            JSONArray profiles = newsfeed.getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (profile.getInt("id") == author_id) {
                                    author_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                    avatar_url = profile.getString("photo_50");
                                } else if (profile.getInt("id") == owner_id) {
                                    owner_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                    avatar_url = profile.getString("photo_50");
                                }
                            }
                        }
                        if(owner_id < 0) {
                            if(newsfeed.has("groups")) {
                                JSONArray groups = newsfeed.getJSONArray("groups");
                                for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                                    JSONObject group = groups.getJSONObject(groups_index);
                                    if (-group.getInt("id") == owner_id) {
                                        owner_name = group.getString("name");
                                        avatar_url = group.getString("photo_50");
                                    }
                                }
                            }
                        }
                        if(author_id == owner_id) {
                            item.name = author_name;
                        } else {
                            item.name = ctx.getResources().getString(R.string.on_wall, author_name, owner_name);
                        }
                    } else {
                        if(newsfeed.has("groups") && newsfeed.has("profiles")) {
                            JSONArray groups = newsfeed.getJSONArray("groups");
                            JSONArray profiles = newsfeed.getJSONArray("profiles");
                            for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                                JSONObject group = groups.getJSONObject(groups_index);
                                if (-group.getInt("id") == author_id) {
                                    item.name = group.getString("name");
                                    avatar_url = group.getString("photo_50");
                                }
                            }
                        }
                    }
                    Photo photo_m = new Photo();
                    photo_m.url = photo_medium_size;
                    photo_m.filename = String.format("wall_attachment_%d", post_id);
                    photos_msize.add(photo_m);
                    Photo photo_h = new Photo();
                    photo_h.url = photo_high_size;
                    photo_h.filename = String.format("wall_attachment_%d", post_id);
                    photos_hsize.add(photo_h);
                    Photo avatar = new Photo();
                    avatar.url = avatar_url;
                    avatar.filename = String.format("avatar_%d", author_id);
                    avatars.add(avatar);
                    wallItems.add(item);
                }
                downloadManager.downloadPhotosToCache(photos_msize, "wall_photo_attachments");
                downloadManager.downloadPhotosToCache(avatars, "wall_avatars");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Comment> parseComments(Context ctx, DownloadManager downloadManager, String response) {
        comments = new ArrayList<Comment>();
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if (json != null) {
                JSONObject comments = json.getJSONObject("response");
                JSONArray items = comments.getJSONArray("items");
                ArrayList<Photo> avatars = new ArrayList<>();
                for(int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String text = item.getString("text");
                    int author_id = item.getInt("from_id");
                    int date = item.getInt("date");
                    Comment comment = new Comment();
                    comment.id = author_id;
                    comment.text = text;
                    comment.author = String.format("(Unknown author: %d)", author_id);
                    comment.date = date;
                    Photo photo = new Photo();
                    photo.url = "";
                    photo.filename = "";
                    if(author_id > 0) {
                        if(comments.has("profiles")) {
                            JSONArray profiles = comments.getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (profile.getInt("id") == author_id) {
                                    comment.author = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                    comment.avatar_url = profile.getString("photo_100");
                                    photo.url = comment.avatar_url;
                                    photo.filename = String.format("avatar_%d", author_id);
                                }
                            }
                        }
                    } else {
                        if(comments.has("groups")) {
                            JSONArray groups = comments.getJSONArray("groups");
                            for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                                JSONObject group = groups.getJSONObject(groups_index);
                                if (group.getInt("id") == author_id) {
                                    comment.author = group.getString("name");
                                    comment.avatar_url = group.getString("photo_100");
                                    photo.url = comment.avatar_url;
                                    photo.filename = String.format("avatar_%d", author_id);
                                }
                            }
                        }
                    }
                    avatars.add(photo);
                    this.comments.add(comment);
                }
                downloadManager.downloadPhotosToCache(avatars, "comment_avatars");
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public void get(OvkAPIWrapper ovk, int owner_id, int count) {
        ovk.sendAPIMethod("Wall.get", String.format("owner_id=%d&count=%d&extended=1", owner_id, count));
    }

    public ArrayList<NewsfeedItem> getWallItems() {
        return wallItems;
    }

    public void post(OvkAPIWrapper ovk, int owner_id, String post) {
        ovk.sendAPIMethod("Wall.post", String.format("owner_id=%d&message=%s", owner_id, URLEncoder.encode(post)));
    }

    public void getComments(OvkAPIWrapper ovk, int owner_id, int post_id) {
        ovk.sendAPIMethod("Wall.getComments", String.format("owner_id=%d&post_id=%d&extended=1&count=50", owner_id, post_id));
    }

    public void createComment(OvkAPIWrapper ovk, int owner_id, int post_id, String text) {
        ovk.sendAPIMethod("Wall.createComment", String.format("owner_id=%d&post_id=%d&message=%s", owner_id, post_id, URLEncoder.encode(text)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
