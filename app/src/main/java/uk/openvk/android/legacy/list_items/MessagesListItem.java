package uk.openvk.android.legacy.list_items;

public class MessagesListItem {
    public boolean isIncoming;
    public boolean isError;
    public String timestamp;
    public String text;
    public MessagesListItem(boolean incoming, boolean error, int _timestamp, String _text) {
        isIncoming = incoming;
        isError = error;
        text = _text;
    }
}
