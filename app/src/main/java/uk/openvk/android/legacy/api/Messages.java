package uk.openvk.android.legacy.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Messages {
    private JSONParser jsonParser;
    private ArrayList<Conversation> conversations;
    private LongPollServer longPollServer;

    public Messages() {
        jsonParser = new JSONParser();
    }

    public void getConversations(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Messages.getConversations", "count=30&extended=1");
    }

    public ArrayList<Conversation> parseConversationsList(String response) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                JSONArray items = json.getJSONObject("response").getJSONArray("items");
                conversations = new ArrayList<Conversation>();
                for(int i = 0; i < items.length(); i++) {
                    JSONObject conv = items.getJSONObject(i).getJSONObject("conversation");
                    JSONObject last_msg = items.getJSONObject(i).getJSONObject("last_message");
                    int peer_id = conv.getJSONObject("peer").getInt("id");
                    Conversation conversation = new Conversation();
                    conversation.peer_id = peer_id;
                    if(peer_id > 0) {
                        if(json.getJSONObject("response").has("profiles")) {
                            JSONArray profiles = json.getJSONObject("response").getJSONArray("profiles");
                            for (int profiles_index = 0; profiles_index < profiles.length(); profiles_index++) {
                                JSONObject profile = profiles.getJSONObject(profiles_index);
                                if(peer_id == profile.getInt("id")) {
                                    conversation.title = String.format("%s %s", profile.getString("first_name"), profile.getString("last_name"));
                                }
                            }
                        }
                    } else {
                        if (json.getJSONObject("response").has("groups")) {
                            JSONArray profiles = json.getJSONObject("response").getJSONArray("groups");
                            for (int groups_index = 0; groups_index < profiles.length(); groups_index++) {
                                JSONObject group = profiles.getJSONObject(groups_index);
                                if (peer_id == -group.getInt("id")) {
                                    conversation.title = String.format("%s", group.getString("name"));
                                }
                            }
                        }
                    }
                    conversation.lastMsgTime = last_msg.getInt("date");
                    conversation.lastMsgText = last_msg.getString("text");
                    conversations.add(conversation);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return conversations;
    }

    public void getLongPollServer(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Messages.getLongPollServer");
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
}
