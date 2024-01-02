package uk.openvk.android.legacy.api.entities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.wrappers.JSONParser;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

    private long dt_sec;
    public long post_id;
    public long author_id;
    public long owner_id;
    public String name;
    public String info;
    public String text;
    public RepostInfo repost;
    private int type;
    public Bitmap avatar;
    private String avatar_url;
    public PostCounters counters;
    public boolean verified_author;
    public boolean is_explicit;
    public ArrayList<Attachment> attachments;
    public WallPostSource post_source;
    private String json_str;
    public boolean contains_repost;
    public Date dt;

    @SuppressLint("SimpleDateFormat")
    public WallPost(String author, long dt_sec, RepostInfo repostInfo, String post_text,
                    PostCounters nICI, String avatar_url, ArrayList<Attachment> attachments,
                    long o_id, long p_id, Context ctx) {
        name = author;
        this.dt_sec = dt_sec;
        dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        info = Global.formatDateTime(ctx, dt_sec);
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        this.avatar_url = avatar_url;
        owner_id = o_id;
        post_id = p_id;
        this.attachments = attachments;
        contains_repost = repost != null && repost.newsfeed_item != null;
    }

    public WallPost() {

    }

    public WallPost(int type) {
        this.type = type;
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
            if(post.has("is_explicit")) {
                is_explicit = post.getBoolean("is_explicit");
            }
            createAttachmentsList(owner_id, post_id, attachments);
            dt_sec = post.getLong("date");
            dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
            info = Global.formatDateTime(ctx, dt_sec);
            text = post.getString("text");
            boolean isLiked = false;
            boolean verified_author = false;
            if(likes != null && reposts != null && comments != null) {
                isLiked = likes.getInt("user_likes") > 0;
                counters = new PostCounters(likes.getInt("count"), comments.getInt("count"),
                        reposts.getInt("count"), isLiked, false);
            } else {
                counters = new PostCounters(
                        0, 0, 0, false, false
                );
            }
            if(post.has("post_source") && !post.isNull("post_source")) {
                if(post.getJSONObject("post_source").getString("type").equals("api")) {
                    post_source = new WallPostSource(post.getJSONObject("post_source").getString("type"),
                            post.getJSONObject("post_source").getString("platform"));
                } else {
                    post_source = new WallPostSource(
                            post.getJSONObject("post_source").getString("type"), null);
                }
            }
            if(post.getJSONArray("copy_history").length() > 0) {
                JSONObject repost = post.getJSONArray("copy_history").getJSONObject(0);
                WallPost repost_item = new WallPost(String.format("(Unknown author: %s)",
                        repost.getInt("from_id")),
                        repost.getInt("date"), null, repost.getString("text"),
                        null, "",
                        null, repost.getInt("owner_id"), repost.getInt("id"), ctx);
                repost_item.setJSONString(repost.toString());
                RepostInfo repostInfo = new RepostInfo(String.format("(Unknown author: %s)",
                        repost.getInt("from_id")),
                        repost.getInt("date"), ctx);
                repostInfo.newsfeed_item = repost_item;
                this.repost = repostInfo;
                JSONArray repost_attachments = repost.getJSONArray("attachments");
            }
            contains_repost = repost != null && repost.newsfeed_item != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setExplicit(boolean value) {
        this.is_explicit = value;
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
                switch (attachment.getString("type")) {
                    case "photo": {
                        JSONObject photo = attachment.getJSONObject("photo");
                        Photo photoAttachment = new Photo();
                        photoAttachment.id = photo.getLong("id");
                        JSONArray photo_sizes = photo.getJSONArray("sizes");
                        photo_medium_size = photo_sizes.getJSONObject(5).getString("url");
                        photo_high_size = photo_sizes.getJSONObject(8).getString("url");
                        photo_original_size = photo_sizes.getJSONObject(10).getString("url");
                        photoAttachment.filename = String.format("wall_o%sp%s", owner_id, post_id);
                        photoAttachment.original_url = photo_original_size;
                        this.attachments.add(photoAttachment);
                        break;
                    }
                    case "video": {
                        JSONObject video = attachment.getJSONObject("video");
                        Video videoAttachment = new Video(video);
                        videoAttachment.id = video.getLong("id");
                        videoAttachment.title = video.getString("title");
                        VideoFiles files = new VideoFiles();
                        if (video.has("files") && !video.isNull("files")) {
                            JSONObject videoFiles = video.getJSONObject("files");
                            if (videoFiles.has("mp4_144")) {
                                files.mp4_144 = videoFiles.getString("mp4_144");
                            }
                            if (videoFiles.has("mp4_240")) {
                                files.mp4_240 = videoFiles.getString("mp4_240");
                            }
                            if (videoFiles.has("mp4_360")) {
                                files.mp4_360 = videoFiles.getString("mp4_360");
                            }
                            if (videoFiles.has("mp4_480")) {
                                files.mp4_480 = videoFiles.getString("mp4_480");
                            }
                            if (videoFiles.has("mp4_720")) {
                                files.mp4_720 = videoFiles.getString("mp4_720");
                            }
                            if (videoFiles.has("mp4_1080")) {
                                files.mp4_1080 = videoFiles.getString("mp4_1080");
                            }
                            if (videoFiles.has("ogv_480")) {
                                files.ogv_480 = videoFiles.getString("ogv_480");
                            }
                        }
                        videoAttachment.files = files;
                        if (video.has("image")) {
                            JSONArray thumb_array = video.getJSONArray("image");
                            videoAttachment.url_thumb = thumb_array.getJSONObject(0).getString("url");
                        }
                        videoAttachment.duration = video.getInt("duration");
                        this.attachments.add(videoAttachment);
                        break;
                    }
                    case "poll": {
                        JSONObject poll_attachment = attachment.getJSONObject("poll");
                        Poll poll = new Poll(
                                poll_attachment.getString("question"),
                                poll_attachment.getInt("id"),
                                poll_attachment.getLong("end_date"),
                                poll_attachment.getBoolean("multiple"),
                                poll_attachment.getBoolean("can_vote"),
                                poll_attachment.getBoolean("anonymous")
                        );
                        JSONArray answers = poll_attachment.getJSONArray("answers");
                        JSONArray votes = poll_attachment.getJSONArray("answer_ids");
                        if (votes.length() > 0) {
                            poll.user_votes = votes.length();
                        }
                        poll.votes = poll_attachment.getInt("votes");
                        for (int answers_index = 0; answers_index < answers.length(); answers_index++) {
                            JSONObject answer = answers.getJSONObject(answers_index);
                            PollAnswer pollAnswer = new PollAnswer(answer.getInt("id"), answer.getInt("rate"),
                                    answer.getInt("votes"), answer.getString("text"));
                            for (int votes_index = 0; votes_index < votes.length(); votes_index++) {
                                if (answer.getInt("id") == votes.getInt(votes_index)) {
                                    pollAnswer.is_voted = true;
                                }
                            }
                            poll.answers.add(pollAnswer);
                        }
                        poll.status = "done";
                        this.attachments.add(poll);
                        break;
                    }
                    case "audio": {
                        Audio audio = new Audio();
                        JSONObject audio_attachment = attachment.getJSONObject("audio");
                        audio.id = audio_attachment.getLong("aid");
                        audio.unique_id = audio_attachment.getString("unique_id");
                        audio.owner_id = audio_attachment.getLong("owner_id");
                        audio.artist = audio_attachment.getString("artist");
                        audio.title = audio_attachment.getString("title");
                        audio.album = audio_attachment.getString("album");
                        audio.lyrics = audio_attachment.getLong("lyrics");
                        audio.url = audio_attachment.getString("url");
                        audio.setDuration(audio_attachment.getInt("duration"));
                        this.attachments.add(audio);
                        break;
                    }
                    default: {
                        attachment_status = "not_supported";
                        Attachment attachment_obj = new Attachment(attachment.getString("type"));
                        attachment_obj.status = attachment_status;
                        this.attachments.add(attachment_obj);
                        break;
                    }
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

    @SuppressLint("SimpleDateFormat")
    public void convertSQLiteToEntity(Cursor cursor, Context ctx) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        post_id = values.getAsInteger("post_id");
        owner_id = values.getAsInteger("owner_id");
        author_id = values.getAsInteger("author_id");
        text = values.getAsString("text");
        dt = new Date(values.getAsLong("time"));
        info = Global.formatDateTime(ctx, TimeUnit.MILLISECONDS.toSeconds(values.getAsLong("time")));
        counters = new PostCounters();
        counters.likes = values.getAsInteger("likes");
        counters.reposts = values.getAsInteger("reposts");
        counters.comments = values.getAsInteger("comments");
        name = values.getAsString("author_name");
        avatar_url = values.getAsString("avatar_url");
        if(values.getAsString("attachments") != null)
            deserializeAttachments(values.getAsString("attachments"), this);
        else
            attachments = new ArrayList<>();
        contains_repost = values.getAsBoolean("contains_repost");
        if (contains_repost) {
            repost = new RepostInfo( values.getAsString("repost_author_name"),
                    values.getAsLong("repost_original_time"), ctx);
            repost.newsfeed_item = new WallPost();
            repost.newsfeed_item.post_id = values.getAsInteger("repost_original_id");
            repost.newsfeed_item.author_id = values.getAsInteger("repost_author_id");
            repost.newsfeed_item.name = values.getAsString("repost_author_name");
            repost.newsfeed_item.attachments = new ArrayList<>();
            if(values.getAsString("repost_attachments") != null)
                deserializeAttachments(values.getAsString("repost_attachments"), repost.newsfeed_item);
            repost.newsfeed_item.text = values.getAsString("repost_text");
            repost.newsfeed_item.avatar_url = values.getAsString("repost_avatar_url");
        }
    }

    public void convertEntityToSQLite(SQLiteDatabase database, String from) {
        ContentValues values = new ContentValues();
        values.put("post_id", post_id);
        values.put("author_id", author_id);
        values.put("owner_id", owner_id);
        values.put("author_name", name);
        values.put("text", text);
        values.put("time", dt.getTime());
        values.put("avatar_url", avatar_url);
        values.put("likes", counters.likes);
        values.put("comments", counters.comments);
        values.put("reposts", counters.reposts);
        values.put("contains_repost", contains_repost);
        if(attachments.size() > 0) {
            String attachments_json = serializeAttachments(this, attachments);
            if(attachments_json != null) {
                values.put("attachments", attachments_json);
            }
        }
        if(contains_repost) {
            values.put("repost_original_id", repost.newsfeed_item.post_id);
            values.put("repost_author_id", repost.newsfeed_item.author_id);
            values.put("repost_owner_id", repost.newsfeed_item.owner_id);
            values.put("repost_author_name", repost.newsfeed_item.name);
            values.put("repost_text", repost.newsfeed_item.text);
            if(repost.newsfeed_item.attachments.size() > 0) {
                String attachments_json =
                        serializeAttachments(repost.newsfeed_item, repost.newsfeed_item.attachments);
                if(attachments_json != null) {
                    values.put("repost_attachments", attachments_json);
                }
            }
            values.put("repost_avatar_url", repost.newsfeed_item.avatar_url);
            values.put("repost_original_time", repost.newsfeed_item.dt.getTime());
        }
        database.insert(from, null, values);
    }

    private String serializeAttachments(WallPost post, ArrayList<Attachment> attachments) {
        if (attachments.size() == 0) {
            return null;
        }
        try {
            JSONArray json_attachments = new JSONArray();
            for (Attachment att : post.attachments) {
                JSONObject json_attach = new JSONObject();
                att.serialize(json_attach);
                json_attachments.put(json_attach);
            }
            return json_attachments.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void deserializeAttachments(String attach_blob, WallPost post) {
        if (attach_blob != null) {
            try {
                JSONArray attachments = new JSONArray(attach_blob);
                post.attachments = new ArrayList<>();
                post.post_source = new WallPostSource();
                int count = attachments.length();
                Attachment attachment = null;
                for (int i = 0; i < count; i++) {
                    switch (attachments.getJSONObject(i).getString("type")) {
                        case "photo":
                            attachment = new Photo();
                            attachment.deserialize(attachments.getJSONObject(i).toString());
                            break;
                        case "video":
                            attachment = new Video();
                            attachment.deserialize(attachments.getJSONObject(i).toString());
                            break;
                        case "poll":
                            attachment = new Poll();
                            attachment.deserialize(attachments.getJSONObject(i).toString());
                            break;
                        case "note":
                            attachment = new Note();
                            attachment.deserialize(attachments.getJSONObject(i).toString());
                            break;
                        case "audio":
                            attachment = new Audio();
                            attachment.deserialize(attachments.getJSONObject(i).toString());
                            break;
                    }

                    post.attachments.add(attachment);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
