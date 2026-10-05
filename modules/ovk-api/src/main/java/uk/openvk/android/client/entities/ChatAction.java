package uk.openvk.android.client.entities;

public class ChatAction {
    public static final int ACTION_CHAT_CREATE = 0x0001;
    public static final int ACTION_INVITE_USER_BY_LINK = 0x0002;
    public static final int ACTION_CHAT_PHOTO_UPDATE = 0x0003;
    public final int type;

    public ChatAction(int type) {
        this.type = type;
    }
}
