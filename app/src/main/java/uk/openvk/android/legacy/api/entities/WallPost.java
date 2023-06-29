package uk.openvk.android.legacy.api.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.wrappers.JSONParser;

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

public class WallPost implements Parcelable {

    private String avatar_url;
    public Bitmap avatar;
    public String name;
    public RepostInfo repost;
    public String info;
    public String text;
    public long owner_id;
    public long post_id;
    public PostCounters counters;
    public long author_id;
    public boolean verified_author;
    public ArrayList<Attachment> attachments;
    public WallPostSource post_source;
    private String json_str;

    @SuppressLint("SimpleDateFormat")
    public WallPost(String author, long dt_sec, RepostInfo repostInfo, String post_text,
                    PostCounters nICI, String avatar_url, ArrayList<Attachment> attachments,
                    long o_id, long p_id, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
        dt_midnight.setHours(0);
        dt_midnight.setMinutes(0);
        dt_midnight.setSeconds(0);
        if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 86400000) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.today_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < (86400000 * 2)) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 31536000000L) {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt), ctx.getResources().getString(R.string.date_at),
                    new SimpleDateFormat("HH:mm").format(dt));
        } else {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt), ctx.getResources().getString(R.string.date_at),
                    new SimpleDateFormat("HH:mm").format(dt));
        }
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        this.avatar_url = avatar_url;
        owner_id = o_id;
        post_id = p_id;
        this.attachments = attachments;
    }

    public WallPost() {

    }

    @SuppressLint("SimpleDateFormat")
    public WallPost(String json_str, Context ctx) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject post = jsonParser.parseJSON(json_str);
            JSONObject comments = null;
            JSONObject likes = null;
            JSONObject reposts = null;
            try {
                comments = post.getJSONObject("comments");
                likes = post.getJSONObject("likes");
                reposts = post.getJSONObject("reposts");
            } catch (Exception ignore) {

            }
            JSONArray attachments = post.getJSONArray("attachments");
            owner_id = post.getLong("owner_id");
            post_id = post.getLong("id");
            author_id = post.getLong("from_id");
            createAttachmentsList(owner_id, post_id, attachments);
            long dt_sec = post.getLong("date");
            Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            dt_midnight.setHours(0);
            dt_midnight.setMinutes(0);
            dt_midnight.setSeconds(0);
            setJSONString(post.toString());
            if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 86400000) {
                info = String.format("%s %s", ctx.getResources().getString(R.string.today_at),
                        new SimpleDateFormat("HH:mm").format(dt));
            } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < (86400000 * 2)) {
                info = String.format("%s %s", ctx.getResources().getString(R.string.yesterday_at),
                        new SimpleDateFormat("HH:mm").format(dt));
            } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 31536000000L) {
                info = String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt),
                        ctx.getResources().getString(R.string.date_at),
                        new SimpleDateFormat("HH:mm").format(dt));
            } else {
                info = String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt),
                        ctx.getResources().getString(R.string.date_at),
                        new SimpleDateFormat("HH:mm").format(dt));
            }
            String avatar_url = "";
            String owner_avatar_url = "";
            String author_avatar_url = "";
            text = post.getString("text");
            boolean isLiked = false;
            boolean verified_author = false;
            if(likes != null && reposts != null && comments != null) {
                isLiked = likes.getInt("user_likes") > 0;
                counters = new PostCounters(likes.getInt("count"), comments.getInt("count"),
                        reposts.getInt("count"), isLiked, false);
            } else {
                counters = new PostCounters(0, 0, 0, false, false);
            }
            if(post.has("post_source") && !post.isNull("post_source")) {
                if(post.getJSONObject("post_source").getString("type").equals("api")) {
                    post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"),
                            post.getJSONObject("post_source").getString("platform"));
                } else {
                    post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"), null);
                }
            }
            if(post.getJSONArray("copy_history").length() > 0) {
                JSONObject repost = post.getJSONArray("copy_history").getJSONObject(0);
                WallPost repost_item = new WallPost(String.format("(Unknown author: %s)",
                        repost.getInt("from_id")),
                        repost.getInt("date"), null, repost.getString("text"), null, "",
                        null, repost.getInt("owner_id"), repost.getInt("id"), ctx);
                RepostInfo repostInfo = new RepostInfo(String.format("(Unknown author: %s)",
                        repost.getInt("from_id")),
                        repost.getInt("date"), ctx);
                repostInfo.newsfeed_item = repost_item;
                this.repost = repostInfo;
                JSONArray repost_attachments = repost.getJSONArray("attachments");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Attachment> createAttachmentsList(
            long owner_id, long post_id, JSONArray attachments) {
        this.attachments = new ArrayList<>();
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
                    photoAttachment.filename = String.format("wall_o%sp%s", owner_id, post_id);
                    photoAttachment.original_url = photo_original_size;
                    if (photo_medium_size.length() > 0 || photo_high_size.length() > 0) {
                        attachment_status = "loading";
                    } else {
                        attachment_status = "none";
                    }
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachment_obj.setContent(photoAttachment);
                    this.attachments.add(attachment_obj);
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
                    }
                    videoAttachment.duration = video.getInt("duration");
                    attachment_status = "done";
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    attachment_obj.setContent(videoAttachment);
                    this.attachments.add(attachment_obj);
                } else if (attachment.getString("type").equals("poll")) {
                    JSONObject poll_attachment = attachment.getJSONObject("poll");
                    PollAttachment pollAttachment = new PollAttachment(poll_attachment.getString("question"),
                            poll_attachment.getInt("id"), poll_attachment.getLong("end_date"),
                            poll_attachment.getBoolean("multiple"),
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
                    this.attachments.add(attachment_obj);
                } else {
                    attachment_status = "not_supported";
                    Attachment attachment_obj = new Attachment(attachment.getString("type"));
                    attachment_obj.status = attachment_status;
                    this.attachments.add(attachment_obj);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        if(this.attachments == null) {
            Log.e(OvkApplication.API_TAG, "Oops!");
        }
        return this.attachments;
    }

    public WallPost(Parcel in) {
        avatar_url = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        info = in.readString();
        text = in.readString();
        owner_id = in.readLong();
        post_id = in.readLong();
        author_id = in.readInt();
    }

    public void setJSONString(String json) {
        this.json_str = json;
    }

    public String getJSONString() {
        return json_str;
    }

    public static final Creator<WallPost> CREATOR = new Creator<WallPost>() {
        @Override
        public WallPost createFromParcel(Parcel in) {
            return new WallPost(in);
        }

        @Override
        public WallPost[] newArray(int size) {
            return new WallPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(avatar_url);
        parcel.writeParcelable(avatar, i);
        parcel.writeString(name);
        parcel.writeString(info);
        parcel.writeString(text);
        parcel.writeLong(owner_id);
        parcel.writeLong(post_id);
        parcel.writeLong(author_id);
    }
}
