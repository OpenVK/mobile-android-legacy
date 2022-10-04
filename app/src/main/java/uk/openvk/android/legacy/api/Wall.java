package uk.openvk.android.legacy.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.list_items.NewsItemCountersInfo;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

/**
 * Created by Dmitry on 28.09.2022.
 */
public class Wall {
    private JSONParser jsonParser;
    private ArrayList<NewsfeedItem> wallItems;
    private ArrayList<Comment> comments;

    public Wall(String response, Context ctx) {
        jsonParser = new JSONParser();
        parse(ctx, response);
    }

    public Wall() {
        jsonParser = new JSONParser();
    }

    public void parse(Context ctx, String response) {
        wallItems = new ArrayList<NewsfeedItem>();
        ArrayList<String> photo_hsize = new ArrayList<String>();
        ArrayList<String> photo_msize = new ArrayList<String>();
        ArrayList<String> avatars = new ArrayList<String>();
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject newsfeed = json.getJSONObject("response");
                JSONArray items = newsfeed.getJSONArray("items");
                JSONArray profiles = newsfeed.getJSONArray("profiles");
                JSONArray groups = newsfeed.getJSONArray("groups");
                for(int i = 0; i < items.length(); i++) {
                    JSONObject post = items.getJSONObject(i);
                    JSONObject comments = post.getJSONObject("comments");
                    JSONObject likes = post.getJSONObject("likes");
                    JSONObject reposts = post.getJSONObject("reposts");
                    JSONArray attachments = post.getJSONArray("attachments");
                    int owner_id = post.getInt("owner_id");
                    int author_id = post.getInt("from_id");
                    int post_id = post.getInt("id");
                    int dt_sec = post.getInt("date");
                    String photo_medium_size = "";
                    String photo_high_size = "";
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
                    if(author_id > 0) {
                        for(int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                            JSONObject profile = profiles.getJSONObject(profiles_index);
                            if(profile.getInt("id") == author_id) {
                                item.name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                            }
                        }
                    } else {
                        for(int groups_index = 0; groups_index < groups.length(); groups_index++) {
                            JSONObject group = groups.getJSONObject(groups_index);
                            if(group.getInt("id") == author_id) {
                                item.name = group.getString("name");
                            }
                        }
                    }
                    photo_hsize.add(photo_high_size);
                    photo_msize.add(photo_medium_size);
                    wallItems.add(item);
                }
                DownloadManager downloadManager = new DownloadManager(ctx, true);
                downloadManager.downloadPhotosToCache(photo_msize, "wall_item_photo");
                downloadManager.downloadPhotosToCache(avatars, "wall_item_avatar");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Comment> parseComments(String response) {
        comments = new ArrayList<Comment>();
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if (json != null) {
                JSONObject comments = json.getJSONObject("response");
                JSONArray items = comments.getJSONArray("items");
                for(int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String text = item.getString("text");
                    int author_id = item.getInt("from_id");
                    int date = item.getInt("date");
                    Comment comment = new Comment();
                    comment.text = text;
                    comment.author = String.format("(Unknown author: %d)", author_id);
                    comment.date = date;
                    if(author_id > 0) {
                        if(comments.has("profiles")) {
                            JSONArray profiles = comments.getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (profile.getInt("id") == author_id) {
                                    comment.author = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
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
                                }
                            }
                        }
                    }
                    this.comments.add(comment);
                }
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
}
