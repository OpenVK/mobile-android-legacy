package uk.openvk.android.legacy.list_items;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ConversationsListItem {
    public String title;
    public int peer_id;
    public int online;
    public Bitmap avatar;
    public Bitmap lastMsgAuthAvatar;
    public String lastMsgText;
    public String lastMsgTimestamp;

    public ConversationsListItem(String dm_title, Bitmap dm_avatar, Bitmap lastMsgAuthorsAvatar, String lastMessageText, int lastMessageTimestamp, int conv_id, int _online) {
        title = dm_title;
        peer_id = conv_id;
        avatar = dm_avatar;
        lastMsgAuthAvatar = lastMsgAuthorsAvatar;
        lastMsgText = lastMessageText;
        online = _online;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(lastMessageTimestamp));
        if((System.currentTimeMillis() - (lastMessageTimestamp * 1000)) < 86400000) {
            lastMsgTimestamp = new SimpleDateFormat("HH:mm").format(dt);
        } else {
            lastMsgTimestamp = new SimpleDateFormat("dd.MM HH:mm").format(dt);
        }
    }
}
