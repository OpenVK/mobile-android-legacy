package uk.openvk.android.legacy.list_items;

import android.graphics.Bitmap;

public class ConversationsListItem {
    public String title;
    public int peer_id;
    public int online;
    public Bitmap avatar;
    public Bitmap lastMsgAuthAvatar;
    public String lastMsgText;

    public ConversationsListItem(String dm_title, Bitmap dm_avatar, Bitmap lastMsgAuthorsAvatar, String lastMessageText, int conv_id, int _online) {
        title = dm_title;
        peer_id = conv_id;
        avatar = dm_avatar;
        lastMsgAuthAvatar = lastMsgAuthorsAvatar;
        lastMsgText = lastMessageText;
        online = _online;
    }
}
