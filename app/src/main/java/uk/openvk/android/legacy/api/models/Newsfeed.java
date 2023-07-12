package uk.openvk.android.legacy.api.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.CommonAttachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.entities.VideoFiles;
import uk.openvk.android.legacy.api.entities.WallPostSource;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.api.entities.RepostInfo;

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

public class Newsfeed implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<WallPost> items;
    private ArrayList<PhotoAttachment> photos_lsize;
    private ArrayList<PhotoAttachment> photos_msize;
    private ArrayList<PhotoAttachment> photos_hsize;
    private ArrayList<PhotoAttachment> photos_osize;

    public long next_from;
    private DownloadManager dlm;

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
        this.dlm = downloadManager;
        if(clear) {
            items = new ArrayList<WallPost>();
            photos_osize = new ArrayList<>();
            photos_hsize = new ArrayList<>();
            photos_msize = new ArrayList<>();
            photos_lsize = new ArrayList<>();
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
                    isLiked = likes.getInt("user_likes") > 0;
                    PostCounters counters = new PostCounters(likes.getInt("count"), comments.getInt("count"),
                            reposts.getInt("count"), isLiked, false);

                    ArrayList<Attachment> attachments_list = createAttachmentsList(owner_id, post_id,
                            quality, attachments);

                    WallPost item = new WallPost(String.format("(Unknown author: %s)", author_id), dt_sec,
                            null, content, counters, "", attachments_list, owner_id, post_id, ctx);
                    item.setJSONString(post.toString());
                    if(post.has("post_source") && !post.isNull("post_source")) {
                        if(post.getJSONObject("post_source").getString("type").equals("api")) {
                            item.post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"),
                                    post.getJSONObject("post_source").getString("platform"));
                        } else {
                            item.post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"), null);
                        }
                    }
                    if(post.getJSONArray("copy_history").length() > 0) {
                        JSONObject repost = post.getJSONArray("copy_history").getJSONObject(0);
                        WallPost repost_item = new WallPost(String.format("(Unknown author: %s)",
                                repost.getLong("from_id")), repost.getLong("date"), null, repost.getString("text"), null, "",
                                null, repost.getLong("owner_id"), repost.getLong("id"), ctx);
                        repost_item.setJSONString(repost.toString());
                        RepostInfo repostInfo = new RepostInfo(String.format("(Unknown author: %s)",
                                repost.getLong("from_id")), repost.getLong("date"), ctx);
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
                                    author_name = String.format("%s %s", profile.getString("first_name"),
                                            profile.getString("last_name"));
                                    author_avatar_url = profile.getString("photo_100");
                                    if(profile.has("verified")) {
                                        if(profile.get("verified") instanceof Integer) {
                                            verified_author = profile.getInt("verified") == 1;
                                        } else {
                                            verified_author = profile.getBoolean("verified");
                                        }
                                    }
                                } else if (profile.getInt("id") == owner_id) {
                                    owner_name = String.format("%s %s", profile.getString("first_name"),
                                            profile.getString("last_name"));
                                    owner_avatar_url = profile.getString("photo_100");
                                    if(profile.has("verified")) {
                                        if(profile.get("verified") instanceof Integer) {
                                            verified_author = profile.getInt("verified") == 1;
                                        } else {
                                            verified_author = profile.getBoolean("verified");
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
                                            if(group.get("verified") instanceof Integer) {
                                                verified_author = group.getInt("verified") == 1;
                                            } else {
                                                verified_author = group.getBoolean("verified");
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
                                        if(group.get("verified") instanceof Integer) {
                                            verified_author = group.getInt("verified") == 1;
                                        } else {
                                            verified_author = group.getBoolean("verified");
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
                switch (quality) {
                    case "low":
                        downloadManager.downloadPhotosToCache(photos_lsize, "newsfeed_photo_attachments");
                        break;
                    case "medium":
                        downloadManager.downloadPhotosToCache(photos_msize, "newsfeed_photo_attachments");
                        break;
                    case "high":
                        downloadManager.downloadPhotosToCache(photos_hsize, "newsfeed_photo_attachments");
                        break;
                    case "original":
                        downloadManager.downloadPhotosToCache(photos_osize, "newsfeed_photo_attachments");
                        break;
                }
                downloadManager.downloadPhotosToCache(avatars, "newsfeed_avatars");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void get(OvkAPIWrapper wrapper, int count) {
        wrapper.sendAPIMethod("Newsfeed.get", String.format("count=%s&extended=1", count));
    }

    public void get(OvkAPIWrapper wrapper, int count, long start_from) {
        wrapper.sendAPIMethod("Newsfeed.get", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public void getGlobal(OvkAPIWrapper wrapper, int count) {
        wrapper.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&extended=1", count));
    }

    public void getGlobal(OvkAPIWrapper wrapper, int count, long start_from) {
        wrapper.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public ArrayList<Attachment> createAttachmentsList(long owner_id, long post_id, String quality, JSONArray attachments) {
        ArrayList<Attachment> attachments_list = new ArrayList<>();
        try {
            for (int attachments_index = 0; attachments_index < attachments.length(); attachments_index++) {
                String photo_low_size;
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
                    photo_low_size = photo_sizes.getJSONObject(2).getString("url");
                    photo_medium_size = photo_sizes.getJSONObject(5).getString("url");
                    photo_high_size = photo_sizes.getJSONObject(8).getString("url");
                    photo_original_size = photo_sizes.getJSONObject(10).getString("url");
                    photoAttachment.size = new int[2];
                    switch (quality) {
                        case "low":
                            photoAttachment.url = photo_low_size;
                            if(!photo_sizes.getJSONObject(2).isNull("width")) {
                                photoAttachment.size[0] = photo_sizes.getJSONObject(2).getInt("width");
                            } else {
                                photoAttachment.size[0] = 384;
                            }
                            if(!photo_sizes.getJSONObject(2).isNull("height")) {
                                photoAttachment.size[1] = photo_sizes.getJSONObject(2).getInt("height");
                            } else {
                                photoAttachment.size[1] = 288;
                            }
                            break;
                        case "medium":
                            photoAttachment.url = photo_medium_size;
                            if(!photo_sizes.getJSONObject(5).isNull("width")) {
                                photoAttachment.size[0] = photo_sizes.getJSONObject(5).getInt("width");
                            } else {
                                photoAttachment.size[0] = 512;
                            }
                            if(!photo_sizes.getJSONObject(5).isNull("height")) {
                                photoAttachment.size[1] = photo_sizes.getJSONObject(5).getInt("height");
                            } else {
                                photoAttachment.size[1] = 384;
                            }
                            break;
                        case "high":
                            if(photo_high_size != null && photo_high_size.length() > 0) {
                                photoAttachment.url = photo_high_size;
                            } else {
                                photoAttachment.url = photo_medium_size;
                            }
                            if(!photo_sizes.getJSONObject(8).isNull("width")) {
                                photoAttachment.size[0] = photo_sizes.getJSONObject(8).getInt("width");
                            } else {
                                photoAttachment.size[0] = 1024;
                            }
                            if(!photo_sizes.getJSONObject(8).isNull("height")) {
                                photoAttachment.size[1] = photo_sizes.getJSONObject(8).getInt("height");
                            } else {
                                photoAttachment.size[1] = 768;
                            }
                            break;
                        case "original":
                            if(photo_original_size != null && photo_original_size.length() > 0) {
                                photoAttachment.url = photo_original_size;
                            } else if(photo_high_size != null && photo_high_size.length() > 0) {
                                photoAttachment.url = photo_high_size;
                            } else {
                                photoAttachment.url = photo_medium_size;
                            }
                            if(!photo_sizes.getJSONObject(8).isNull("width")) {
                                photoAttachment.size[0] = photo_sizes.getJSONObject(8).getInt("width");
                            } else {
                                photoAttachment.size[0] = 2560;
                            }
                            if(!photo_sizes.getJSONObject(8).isNull("height")) {
                                photoAttachment.size[1] = photo_sizes.getJSONObject(8).getInt("height");
                            } else {
                                photoAttachment.size[1] = 1920;
                            }
                            break;
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
                        case "low":
                            photos_lsize.add(photoAttachment);
                            break;
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
                } else if (attachment.getString("type").equals("video")) {
                    JSONObject video = attachment.getJSONObject("video");
                    VideoAttachment videoAttachment = new VideoAttachment();
                    videoAttachment.id = video.getLong("id");
                    videoAttachment.title = video.getString("title");
                    VideoFiles files = new VideoFiles();
                    if(video.has("files")) {
                        JSONObject videoFiles = video.getJSONObject("files");
                        if(videoFiles.has("mp4_144")) {
                            files.mp4_144 = videoFiles.getString("mp4_144");
                        } if(videoFiles.has("mp4_240")) {
                            files.mp4_240 = videoFiles.getString("mp4_240");
                        } if(videoFiles.has("mp4_360")) {
                            files.mp4_360 = videoFiles.getString("mp4_360");
                        } if(videoFiles.has("mp4_480")) {
                            files.mp4_480 = videoFiles.getString("mp4_480");
                        } if(videoFiles.has("mp4_720")) {
                            files.mp4_720 = videoFiles.getString("mp4_720");
                        } if(videoFiles.has("mp4_1080")) {
                            files.mp4_1080 = videoFiles.getString("mp4_1080");
                        } if(videoFiles.has("ogv_480")) {
                            files.ogv_480 = videoFiles.getString("ogv_480");
                        }
                    }
                    videoAttachment.files = files;
                    if(video.has("image")) {
                        JSONArray thumb_array = video.getJSONArray("image");
                        videoAttachment.url_thumb = thumb_array.getJSONObject(0).getString("url");
                        dlm.downloadOnePhotoToCache(videoAttachment.url_thumb, String.format("thumbnail_%so%s",
                                video.getLong("id"), owner_id), "video_thumbnails");
                    }
                    videoAttachment.duration = video.getInt("duration");
                    attachment_status = "done";
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachment_obj.setContent(videoAttachment);
                    attachments_list.add(attachment_obj);
                } else if (attachment.getString("type").equals("poll")) {
                    JSONObject poll_attachment = attachment.getJSONObject("poll");
                    PollAttachment pollAttachment = new PollAttachment(poll_attachment.getString("question"),
                            poll_attachment.getInt("id"),
                            poll_attachment.getLong("end_date"), poll_attachment.getBoolean("multiple"),
                            poll_attachment.getBoolean("can_vote"),
                            poll_attachment.getBoolean("anonymous"));
                    JSONArray answers = poll_attachment.getJSONArray("answers");
                    JSONArray votes = poll_attachment.getJSONArray("answer_ids");
                    if (votes.length() > 0) {
                        pollAttachment.user_votes = votes.length();
                    }
                    pollAttachment.votes = poll_attachment.getInt("votes");
                    for (int answers_index = 0; answers_index < answers.length(); answers_index++) {
                        JSONObject answer = answers.getJSONObject(answers_index);
                        PollAnswer pollAnswer = new PollAnswer(answer.getInt("id"), answer.getInt("rate"),
                                answer.getInt("votes"), answer.getString("text"));
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
                } else if (attachment.getString("type").equals("note")) {
                    CommonAttachment commonAttachment = new CommonAttachment(
                            attachment.getString("title"), attachment.getString("text"));
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = "done";
                    attachment_obj.setContent(commonAttachment);
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
