package uk.openvk.android.legacy.list_items;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MessagesListItem {
    public boolean isIncoming;
    public boolean isError;
    public String timestamp;
    public int timestamp_int;
    public String text;

    public MessagesListItem(boolean incoming, boolean error, int _timestamp, String _text, Context ctx) {
        isIncoming = incoming;
        isError = error;
        text = _text;
        timestamp_int = _timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(_timestamp));
        if((System.currentTimeMillis() - (_timestamp * 1000)) < 86400000) {
            timestamp = new SimpleDateFormat("HH:mm").format(dt);
        } else {
            timestamp = new SimpleDateFormat("dd.MM HH:mm").format(dt);
        }
    }
}
