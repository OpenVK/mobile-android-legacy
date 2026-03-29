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

package uk.openvk.android.client.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.LongPollServer;
import uk.openvk.android.client.entities.Message;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.JSONParser;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;

public class Messages {
    private JSONParser jsonParser;
    private ArrayList<Conversation> conversations;
    private LongPollServer longPollServer;

    public Messages() {
        jsonParser = new JSONParser();
    }

    public void getConversations(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Messages.getConversations", "count=30&extended=1&fields=photo_100");
    }

    public ArrayList<Conversation> parseConversationsList(String response, DownloadManager downloadManager) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                JSONArray items = json.getJSONObject("response").getJSONArray("items");
                if(conversations == null)
                    conversations = new ArrayList<>();
                ArrayList<Photo> avatars = new ArrayList<>();
                for(int i = 0; i < items.length(); i++) {
                    Conversation conv = parseConversation(items.getJSONObject(i));
                    if(conv == null)
                        continue;
                    if (conv.peer_id > 0 && conv.peer_type.equals("user")) {
                        if (json.getJSONObject("response").has("profiles")) {
                            JSONArray profiles = json.getJSONObject("response").getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if (conv.peer_id == profile.getInt("id")) {
                                    conv.title = String.format("%s %s", profile.getString("first_name"),
                                            profile.getString("last_name"));
                                    conv.avatar_url = "";
                                    if (profile.has("photo_100")) {
                                        conv.avatar_url = profile.getString("photo_100");

                                        Photo avatar = new Photo();
                                        avatar.url = conv.avatar_url;
                                        avatar.filename = String.format("avatar_%s", conv.peer_id);
                                        avatars.add(avatar);
                                    }
                                }
                            }
                        }
                    } else if (conv.peer_type.equals("group")) {
                        if (json.getJSONObject("response").has("groups")) {
                            JSONArray profiles = json.getJSONObject("response").getJSONArray("groups");
                            for (int groups_index = 0; groups_index < profiles.length(); groups_index++) {
                                JSONObject group = profiles.getJSONObject(groups_index);
                                if (conv.peer_id == -group.getInt("id")) {
                                    conv.title = String.format("%s", group.getString("name"));
                                    if (group.has("photo_100")) {
                                        conv.avatar_url = group.getString("photo_100");

                                        Photo avatar = new Photo();
                                        avatar.url = conv.avatar_url;
                                        avatar.filename = String.format("avatar_%s", conv.peer_id);
                                        avatars.add(avatar);
                                    }
                                }
                            }
                        }
                    }

                }
                downloadManager.downloadPhotosToCache(avatars, "conversations_avatars");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return conversations;
    }

    private Conversation parseConversation(JSONObject jsonObj) {
        try {
            JSONObject jsonConv = null;
            if(jsonObj.has("conversation"))
                jsonConv = jsonObj.getJSONObject("conversation");
            else
                jsonConv = jsonObj;
            JSONObject last_msg = null;
            if(jsonObj.has("last_message"))
                last_msg = jsonObj.getJSONObject("last_message");
            int peer_id = jsonConv.getJSONObject("peer").getInt("id");
            Conversation conversation = new Conversation();
            conversation.peer_id = peer_id;
            conversation.peer_type = jsonConv.getJSONObject("peer").getString("type");
            Photo photoAttachment = new Photo();
            photoAttachment.url = "";
            photoAttachment.filename = "";

            if(last_msg != null) {
                conversation.lastMsgTime = last_msg.getInt("date");
                conversation.lastMsgText = last_msg.getString("text");
                conversation.lastMsgAuthorId = last_msg.getInt("from_id");
            }
            try { // handle floating crash
                conversations.add(conversation);
            } catch (ArrayIndexOutOfBoundsException ignored) {
                Log.e(OpenVKAPI.TAG, "WTF? The length itself in an array must not " +
                        "be overestimated.");
            }
            return conversation;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public void getLongPollServer(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Messages.getLongPollServer");
    }

    public ArrayList<Message> parseConversationHistory(Context ctx, String args, String response) {
        String argsArray[] = args.split("&");
        for (String argument : argsArray) {
            String key = argument.split("=")[0];
            String value = argument.split("=")[1];

            if(key.equals("peer_id")) {
                long peer_id = Long.parseLong(value);
                Conversation conv = searchConversation(peer_id);

                if(conv != null)
                    return conv.parseHistory(ctx, response);
            }
        }
        return null;
    }

    public Conversation searchConversation(long peer_id) {
        if(conversations == null)
            return null;

        for (Conversation conversation : conversations) {
            if(conversation.peer_id == peer_id) {
                return conversation;
            }
        }

        return null;
    }

    public LongPollServer parseLongPollServer(String response) {
        longPollServer = new LongPollServer();
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                JSONObject lp_server = json.getJSONObject("response");
                longPollServer.address = lp_server.getString("server");
                longPollServer.key = lp_server.getString("key");
                longPollServer.ts = lp_server.getInt("ts");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return longPollServer;
    }

    public void delete(OvkAPIWrapper wrapper, long id) {
        wrapper.sendAPIMethod("Messages.delete", String.format("message_ids=%s", id));
    }

    public ArrayList<Conversation> getConversations() {
        return conversations;
    }

    public void getConversationById(OvkAPIWrapper wrapper, long peer_id) {
        wrapper.sendAPIMethod("Messages.getConversationsById", String.format("peer_ids=%s", peer_id));
    }
}
