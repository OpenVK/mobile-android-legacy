package uk.openvk.android.legacy.api.models;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Message {
    public boolean isIncoming;
    public boolean isError;
    public String timestamp;
    public int timestamp_int;
    public String text;

    public Message(boolean incoming, boolean error, int _timestamp, String _text, Context ctx) {
        isIncoming = incoming;
        isError = error;
        text = _text;
        timestamp_int = _timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(_timestamp));
        timestamp = new SimpleDateFormat("HH:mm").format(dt);
    }
}
