package uk.openvk.android.legacy.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.list_items.NewsItemCountersInfo;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

/**
 * Created by Dmitry on 28.09.2022.
 */
public class Newsfeed {
    private JSONParser jsonParser;
    private ArrayList<NewsfeedItem> newsfeedItems;

    public Newsfeed(String response, Context ctx) {
        jsonParser = new JSONParser();
        parse(ctx, response);
    }

    public Newsfeed() {
        jsonParser = new JSONParser();
    }

    public void parse(Context ctx, String response) {
        newsfeedItems = new ArrayList<NewsfeedItem>();
        ArrayList<String> photo_hsize = new ArrayList<String>();
        ArrayList<String> photo_msize = new ArrayList<String>();
        ArrayList<String> avatars = new ArrayList<String>();
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
                    int owner_id = post.getInt("owner_id");
                    int post_id = post.getInt("id");
                    int author_id = post.getInt("from_id");
                    int dt_sec = post.getInt("date");
                    String author_name = "";
                    String owner_name = "";
                    String avatar_url = "";
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
                        if(newsfeed.has("profiles")) {
                            JSONArray profiles = newsfeed.getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (profile.getInt("id") == author_id) {
                                    author_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                } else if (profile.getInt("id") == owner_id) {
                                    owner_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
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
                    photo_hsize.add(photo_high_size);
                    photo_msize.add(photo_medium_size);
                    avatars.add(avatar_url);
                    newsfeedItems.add(item);
                }
                DownloadManager downloadManager = new DownloadManager(ctx, true);
                downloadManager.downloadPhotosToCache(photo_msize, "newsfeed_item_photo");
                downloadManager.downloadPhotosToCache(photo_msize, "newsfeed_item_photo");
                downloadManager.downloadPhotosToCache(avatars, "newsfeed_item_avatar");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper ovk, int count) {
        ovk.sendAPIMethod("Newsfeed.get", String.format("count=%d&extended=1", count));
    }

    public ArrayList<NewsfeedItem> getNewsfeedItems() {
        return newsfeedItems;
    }
}
