package uk.openvk.android.legacy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.models.WallPostSource;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.models.WallPost;
import uk.openvk.android.legacy.api.models.RepostInfo;

/**
 * Created by Dmitry on 28.09.2022.
 */
public class Newsfeed implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<WallPost> items;
    private ArrayList<PhotoAttachment> photos_msize;
    private ArrayList<PhotoAttachment> photos_hsize;
    private ArrayList<PhotoAttachment> photos_osize;

    public long next_from;
    public Newsfeed(String response, DownloadManager downloadManager, String quality, Context ctx) {
        jsonParser = new JSONParser();
        if(items == null) {
            parse(ctx, downloadManager, response, quality, true);
        }
    }

    public Newsfeed() {
        jsonParser = new JSONParser();
    }

    protected Newsfeed(Parcel in) {
        items = in.createTypedArrayList(WallPost.CREATOR);
    }

    public static final Creator<Newsfeed> CREATOR = new Creator<Newsfeed>() {
        @Override
        public Newsfeed createFromParcel(Parcel in) {
            return new Newsfeed(in);
        }

        @Override
        public Newsfeed[] newArray(int size) {
            return new Newsfeed[size];
        }
    };

    public void parse(Context ctx, DownloadManager downloadManager, String response, String quality, boolean clear) {
        if(clear) {
            items = new ArrayList<WallPost>();
            photos_osize = new ArrayList<PhotoAttachment>();
            photos_hsize = new ArrayList<PhotoAttachment>();
            photos_msize = new ArrayList<PhotoAttachment>();
        }
        ArrayList<PhotoAttachment> avatars = new ArrayList<PhotoAttachment>();
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject newsfeed = json.getJSONObject("response");
                next_from = newsfeed.getLong("next_from");
                JSONArray items = newsfeed.getJSONArray("items");
                for(int i = 0; i < items.length(); i++) {
                    JSONObject post = items.getJSONObject(i);
                    JSONObject comments = post.getJSONObject("comments");
                    JSONObject likes = post.getJSONObject("likes");
                    JSONObject reposts = post.getJSONObject("reposts");
                    JSONArray attachments = post.getJSONArray("attachments");
                    long owner_id = post.getLong("owner_id");
                    long post_id = post.getLong("id");
                    long author_id = post.getLong("from_id");
                    long dt_sec = post.getLong("date");
                    String original_author_name = "";
                    String original_author_avatar_url = "";
                    String author_name = "";
                    String owner_name = "";
                    String avatar_url = "";
                    String owner_avatar_url = "";
                    String author_avatar_url = "";
                    String content = post.getString("text");
                    boolean verified_author = false;
                    boolean isLiked = false;
                    if(likes.getInt("user_likes") > 0) {
                        isLiked = true;
                    } else {
                        isLiked = false;
                    }
                    PostCounters counters = new PostCounters(likes.getInt("count"), comments.getInt("count"), reposts.getInt("count"), isLiked, false);

                    ArrayList<Attachment> attachments_list = createAttachmentsList(owner_id, post_id, quality, attachments);

                    WallPost item = new WallPost(String.format("(Unknown author: %s)", author_id), dt_sec, null, content, counters, "", attachments_list, owner_id, post_id, ctx);
                    if(post.has("post_source") && !post.isNull("post_source")) {
                        if(post.getJSONObject("post_source").getString("type").equals("api")) {
                            item.post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"), post.getJSONObject("post_source").getString("platform"));
                        } else {
                            item.post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"), null);
                        }
                    }
                    if(post.getJSONArray("copy_history").length() > 0) {
                        JSONObject repost = post.getJSONArray("copy_history").getJSONObject(0);
                        WallPost repost_item = new WallPost(String.format("(Unknown author: %s)", repost.getLong("from_id")), repost.getLong("date"), null, repost.getString("text"), null, "",
                                null, repost.getLong("owner_id"), repost.getLong("id"), ctx);
                        RepostInfo repostInfo = new RepostInfo(String.format("(Unknown author: %s)", repost.getLong("from_id")), repost.getLong("date"), ctx);
                        repostInfo.newsfeed_item = repost_item;
                        item.repost = repostInfo;
                        JSONArray repost_attachments = repost.getJSONArray("attachments");
                        attachments_list = createAttachmentsList(owner_id, post_id, quality, repost_attachments);
                        repost_item.attachments = attachments_list;
                    }
                    item.author_id = author_id;
                    if(author_id > 0) {
                        if(newsfeed.has("profiles")) {
                            JSONArray profiles = newsfeed.getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (profile.getLong("id") == author_id) {
                                    author_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                    author_avatar_url = profile.getString("photo_100");
                                    if(profile.has("verified")) {
                                        if (profile.getInt("verified") == 1) {
                                            verified_author = true;
                                        } else {
                                            verified_author = false;
                                        }
                                    }
                                } else if (profile.getInt("id") == owner_id) {
                                    owner_name = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                    owner_avatar_url = profile.getString("photo_100");
                                    if(profile.has("verified")) {
                                        if (profile.getInt("verified") == 1) {
                                            verified_author = true;
                                        } else {
                                            verified_author = false;
                                        }
                                    }
                                }
                            }
                            if(author_avatar_url.length() > 0)
                            avatar_url = author_avatar_url;
                        }
                        if(owner_id < 0) {
                            if(newsfeed.has("groups")) {
                                JSONArray groups = newsfeed.getJSONArray("groups");
                                for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                                    JSONObject group = groups.getJSONObject(groups_index);
                                    if (-group.getLong("id") == owner_id) {
                                        owner_name = group.getString("name");
                                        avatar_url = group.getString("photo_100");
                                        if(group.has("verified")) {
                                            if (group.getInt("verified") == 1) {
                                                verified_author = true;
                                            } else {
                                                verified_author = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(author_id == owner_id) {
                            item.name = author_name;
                        } else if(owner_name.length() > 0) {
                            item.name = ctx.getResources().getString(R.string.on_wall, author_name, owner_name);
                        } else {
                            item.name = author_name;
                        }
                    } else {
                        if(newsfeed.has("groups") && newsfeed.has("profiles")) {
                            JSONArray groups = newsfeed.getJSONArray("groups");
                            JSONArray profiles = newsfeed.getJSONArray("profiles");
                            for (int groups_index = 0; groups_index < groups.length(); groups_index++) {
                                JSONObject group = groups.getJSONObject(groups_index);
                                if (-group.getInt("id") == author_id) {
                                    item.name = group.getString("name");
                                    avatar_url = group.getString("photo_100");
                                    if(group.has("verified")) {
                                        if (group.getInt("verified") == 1) {
                                            verified_author = true;
                                        } else {
                                            verified_author = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item.verified_author = verified_author;
                    PhotoAttachment avatar = new PhotoAttachment();
                    avatar.url = avatar_url;
                    avatar.filename = String.format("avatar_%s", author_id);
                    avatars.add(avatar);
                    this.items.add(item);
                }
                if(quality.equals("medium")) {
                    downloadManager.downloadPhotosToCache(photos_msize, "newsfeed_photo_attachments");
                } else if(quality.equals("high")) {
                    downloadManager.downloadPhotosToCache(photos_hsize, "newsfeed_photo_attachments");
                } else if(quality.equals("original")) {
                    downloadManager.downloadPhotosToCache(photos_osize, "newsfeed_photo_attachments");
                }
                downloadManager.downloadPhotosToCache(avatars, "newsfeed_avatars");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper ovk, int count) {
        ovk.sendAPIMethod("Newsfeed.get", String.format("count=%s&extended=1", count));
    }

    public void get(OvkAPIWrapper ovk, int count, long start_from) {
        ovk.sendAPIMethod("Newsfeed.get", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public void getGlobal(OvkAPIWrapper ovk, int count) {
        ovk.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&extended=1", count));
    }

    public void getGlobal(OvkAPIWrapper ovk, int count, long start_from) {
        ovk.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public ArrayList<Attachment> createAttachmentsList(long owner_id, long post_id, String quality, JSONArray attachments) {
        ArrayList<Attachment> attachments_list = new ArrayList<>();
        try {
            for (int attachments_index = 0; attachments_index < attachments.length(); attachments_index++) {
                String photo_medium_size;
                String photo_high_size;
                String photo_original_size;
                String attachment_status;
                JSONObject attachment = attachments.getJSONObject(attachments_index);
                if (attachment.getString("type").equals("photo")) {
                    JSONObject photo = attachment.getJSONObject("photo");
                    PhotoAttachment photoAttachment = new PhotoAttachment();
                    photoAttachment.id = photo.getLong("id");
                    JSONArray photo_sizes = photo.getJSONArray("sizes");
                    photo_medium_size = photo_sizes.getJSONObject(5).getString("url");
                    photo_high_size = photo_sizes.getJSONObject(8).getString("url");
                    photo_original_size = photo_sizes.getJSONObject(10).getString("url");
                    if(quality.equals("medium")) {
                        photoAttachment.url = photo_medium_size;
                    } else if(quality.equals("high")) {
                        photoAttachment.url = photo_high_size;
                    } else if(quality.equals("original")) {
                        photoAttachment.url = photo_original_size;
                    }
                    photoAttachment.original_url = photo_original_size;
                    photoAttachment.filename = String.format("newsfeed_attachment_o%sp%s", owner_id, post_id);
                    if (photo_medium_size.length() > 0 || photo_high_size.length() > 0) {
                        attachment_status = "loading";
                    } else {
                        attachment_status = "none";
                    }
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachment_obj.setContent(photoAttachment);
                    attachments_list.add(attachment_obj);
                    switch (quality) {
                        case "medium":
                            photos_msize.add(photoAttachment);
                            break;
                        case "high":
                            photos_hsize.add(photoAttachment);
                            break;
                        case "original":
                            photos_osize.add(photoAttachment);
                            break;
                    }
                } else if (attachment.getString("type").equals("poll")) {
                    JSONObject poll_attachment = attachment.getJSONObject("poll");
                    PollAttachment pollAttachment = new PollAttachment(poll_attachment.getString("question"), poll_attachment.getInt("id"), poll_attachment.getLong("end_date"), poll_attachment.getBoolean("multiple"), poll_attachment.getBoolean("can_vote"),
                            poll_attachment.getBoolean("anonymous"));
                    JSONArray answers = poll_attachment.getJSONArray("answers");
                    JSONArray votes = poll_attachment.getJSONArray("answer_ids");
                    if (votes.length() > 0) {
                        pollAttachment.user_votes = votes.length();
                    }
                    pollAttachment.votes = poll_attachment.getInt("votes");
                    for (int answers_index = 0; answers_index < answers.length(); answers_index++) {
                        JSONObject answer = answers.getJSONObject(answers_index);
                        PollAnswer pollAnswer = new PollAnswer(answer.getInt("id"), answer.getInt("rate"), answer.getInt("votes"), answer.getString("text"));
                        for (int votes_index = 0; votes_index < votes.length(); votes_index++) {
                            if (answer.getInt("id") == votes.getInt(votes_index)) {
                                pollAnswer.is_voted = true;
                            }
                        }
                        pollAttachment.answers.add(pollAnswer);
                    }
                    attachment_status = "done";
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachment_obj.setContent(pollAttachment);
                    attachments_list.add(attachment_obj);
                } else {
                    attachment_status = "not_supported";
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachments_list.add(attachment_obj);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return attachments_list;
    }

    public ArrayList<WallPost> getWallPosts() {
        return items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(items);
    }

}
