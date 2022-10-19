package uk.openvk.android.legacy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.models.Photo;
import uk.openvk.android.legacy.api.models.Poll;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.list_items.NewsfeedItem;
import uk.openvk.android.legacy.list_items.RepostInfo;

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

    protected Wall(Parcel in) {
        wallItems = in.createTypedArrayList(NewsfeedItem.CREATOR);
    }

    public static final Creator<Wall> CREATOR = new Creator<Wall>() {
        @Override
        public Wall createFromParcel(Parcel in) {
            return new Wall(in);
        }

        @Override
        public Wall[] newArray(int size) {
            return new Wall[size];
        }
    };

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
                    Poll poll = null;
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
                    String attachment_status = "";
                    String content = post.getString("text");
                    PostCounters counters = new PostCounters(likes.getInt("count"), comments.getInt("count"), reposts.getInt("count"), false, false);
                    if(attachments.length() == 1) {
                        JSONObject attachment = attachments.getJSONObject(0);
                        if(attachment.getString("type").equals("photo")) {
                            JSONObject photo = attachment.getJSONObject("photo");
                            JSONArray photo_sizes = photo.getJSONArray("sizes");
                            photo_medium_size = photo_sizes.getJSONObject(5).getString("url");
                            photo_high_size = photo_sizes.getJSONObject(10).getString("url");

                            if(photo_medium_size.length() > 0 || photo_high_size.length() > 0) {
                                attachment_status = "loading";
                            } else {
                                attachment_status = "none";
                            }
                        } else if(attachment.getString("type").equals("poll")) {
                            JSONObject poll_attachment = attachment.getJSONObject("poll");
                            poll = new Poll(poll_attachment.getString("question"), poll_attachment.getInt("id"), poll_attachment.getLong("end_date"), poll_attachment.getBoolean("multiple"), poll_attachment.getBoolean("can_vote"),
                                    poll_attachment.getBoolean("anonymous"));
                            JSONArray answers = poll_attachment.getJSONArray("answers");
                            JSONArray votes = poll_attachment.getJSONArray("answer_ids");
                            if(votes.length() > 0) {
                                poll.user_votes = votes.length();
                            }
                            poll.votes = poll_attachment.getInt("votes");
                            for(int answers_index = 0; answers_index < answers.length(); answers_index++) {
                                JSONObject answer = answers.getJSONObject(answers_index);
                                PollAnswer pollAnswer = new PollAnswer(answer.getInt("id"), answer.getInt("rate"), answer.getInt("votes"), answer.getString("text"));
                                for(int votes_index = 0; votes_index < votes.length(); votes_index++) {
                                    if(answer.getInt("id") == votes.getInt(votes_index)) {
                                        pollAnswer.is_voted = true;
                                    }
                                }
                                poll.answers.add(pollAnswer);
                            }
                            attachment_status = "poll";
                        } else {
                            attachment_status = "not_supported";
                        }
                    }
                    NewsfeedItem item = new NewsfeedItem(String.format("(Unknown author: %d)", author_id), dt_sec, null, content, counters, "",
                            photo_medium_size, photo_high_size, null, owner_id, post_id, ctx);
                    if(post.getJSONArray("copy_history").length() > 0) {
                        attachment_status = "none";
                        JSONObject repost = post.getJSONArray("copy_history").getJSONObject(0);
                        NewsfeedItem repost_item = new NewsfeedItem(String.format("(Unknown author: %d)", repost.getInt("from_id")), repost.getInt("date"), null, repost.getString("text"), null, "",
                                null, null, null, repost.getInt("owner_id"), repost.getInt("id"), ctx);
                        RepostInfo repostInfo = new RepostInfo(String.format("(Unknown author: %d)", repost.getInt("from_id")), repost.getInt("date"), ctx);
                        repostInfo.newsfeed_item = repost_item;
                        item.repost = repostInfo;
                    }
                    item.attachment_status = attachment_status;
                    item.author_id = author_id;
                    item.poll = poll;
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
                    photo_m.filename = String.format("wall_attachment_o%dp%d", owner_id, post_id);
                    photos_msize.add(photo_m);
                    Photo photo_h = new Photo();
                    photo_h.url = photo_high_size;
                    photo_h.filename = String.format("wall_attachment_o%dp%d", owner_id, post_id);
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
                                    if(profile.has("photo_100")) {
                                        comment.avatar_url = profile.getString("photo_100");
                                    }
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
        parcel.writeTypedList(wallItems);
    }
}
