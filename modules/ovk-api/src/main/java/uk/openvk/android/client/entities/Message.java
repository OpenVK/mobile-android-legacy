/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.attachments.Attachment;
import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.wrappers.JSONParser;

public class Message extends LazyEntity {
    public boolean isIncoming;
    public String timestamp;
    public long timestamp_long;
    public String text;
    public boolean sending;
    public LazyEntity author;
    public ChatAction action;
    private JSONParser parser;
    public boolean isError;
    public ArrayList<Attachment> attachments;

    public Message(int type) {
        super(type);
    }

    @SuppressLint("SimpleDateFormat")
    public Message(long id, boolean incoming, long timestamp, String text) {
        super(LazyEntity.REAL_ENTITY);
        this.id = id;
        isIncoming = incoming;
        this.text = text;
        timestamp_long = timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(timestamp));
        this.timestamp = new SimpleDateFormat("HH:mm").format(dt);
    }

    public Message() {
        super(LazyEntity.SLEEPING_ENTITY);
    }

    public void getSendedId(String response) {
        parser = new JSONParser();
        try {
            JSONObject json = parser.parseJSON(response);
            if (json != null) {
                id = json.getLong("response");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseAction(JSONObject actionJson) {
        try {
            switch (actionJson.getString("type")) {
                case "chat_create":
                    action = new ChatAction(ChatAction.ACTION_CHAT_CREATE);
                    break;
                case "chat_photo_update":
                    action = new ChatAction(ChatAction.ACTION_CHAT_PHOTO_UPDATE);
                    break;
                case "chat_invite_user_by_link":
                    action = new ChatAction(ChatAction.ACTION_INVITE_USER_BY_LINK);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<Attachment> createAttachmentsList(JSONArray attachmentsJson, String photoQuality) {

        ArrayList<Attachment> attachments = new ArrayList<>();

        try {
            for (int attachments_index = 0; attachments_index < attachmentsJson.length(); attachments_index++) {
                String photo_original_size;
                String attachment_status;
                JSONObject attachment = attachmentsJson.getJSONObject(attachments_index);
                switch (attachment.getString("type")) {
                    case "photo": {
                        JSONObject photo = attachment.getJSONObject("photo");
                        Photo photoAttachment = new Photo();
                        photoAttachment.id = photo.getLong("id");
                        JSONArray photo_sizes = photo.getJSONArray("sizes");

                        photoAttachment.size = new int[2];

                        JSONObject originalSizeObj = photo_sizes.getJSONObject(10);
                        JSONObject highSizeObj = photo_sizes.getJSONObject(8);
                        JSONObject mediumSizeObj = photo_sizes.getJSONObject(5);
                        JSONObject lowSizeObj = photo_sizes.getJSONObject(2);

                        switch (photoQuality) {
                            case "original":
                                if (!originalSizeObj.isNull("width") && originalSizeObj.getInt("width") > 0) {
                                    photoAttachment.url = originalSizeObj.getString("url");
                                    photoAttachment.size[0] = mediumSizeObj.getInt("width");
                                    photoAttachment.size[1] = mediumSizeObj.getInt("height");
                                    break;
                                }
                            case "high":
                                if (!highSizeObj.isNull("width") && highSizeObj.getInt("width") > 0) {
                                    photoAttachment.url = highSizeObj.getString("url");
                                    photoAttachment.size[0] = highSizeObj.getInt("width");
                                    photoAttachment.size[1] = highSizeObj.getInt("height");
                                    break;
                                }
                            case "medium":
                                if (!mediumSizeObj.isNull("width") && mediumSizeObj.getInt("width") > 0) {
                                    photoAttachment.url = mediumSizeObj.getString("url");
                                    photoAttachment.size[0] = mediumSizeObj.getInt("width");
                                    photoAttachment.size[1] = mediumSizeObj.getInt("height");
                                    break;
                                }
                            case "low":
                                if (!lowSizeObj.isNull("width") && lowSizeObj.getInt("width") > 0) {
                                    photoAttachment.url = lowSizeObj.getString("url");
                                    photoAttachment.size[0] = lowSizeObj.getInt("width");
                                    photoAttachment.size[1] = lowSizeObj.getInt("height");
                                    break;
                                }
                                break;
                        }
                        photoAttachment.filename =
                                String.format("conversation_p%sm%sp%s", photoAttachment.id, id, author.id);
                        photoAttachment.original_url = originalSizeObj.getString("url");
                        attachments.add(photoAttachment);
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
                        attachments.add(videoAttachment);
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
                            Poll.PollAnswer pollAnswer =
                                    new Poll.PollAnswer(answer.getInt("id"), answer.getInt("rate"),
                                            answer.getInt("votes"), answer.getString("text"));
                            for (int votes_index = 0; votes_index < votes.length(); votes_index++) {
                                if (answer.getInt("id") == votes.getInt(votes_index)) {
                                    pollAnswer.is_voted = true;
                                }
                            }
                            poll.answers.add(pollAnswer);
                        }
                        poll.status = "done";
                        attachments.add(poll);
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
                        attachments.add(audio);
                        break;
                    }
                    default: {
                        attachment_status = "not_supported";
                        Attachment attachment_obj = new Attachment(attachment.getString("type"));
                        attachment_obj.status = attachment_status;
                        attachments.add(attachment_obj);
                        break;
                    }
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        } finally {
            entityType = LazyEntity.REAL_ENTITY;
        }
        return attachments;
    }
}
