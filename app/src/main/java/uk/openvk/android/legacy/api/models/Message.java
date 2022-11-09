package uk.openvk.android.legacy.api.models;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

public class Message {
    public long id;
    public boolean isIncoming;
    public boolean isError;
    public String timestamp;
    public int timestamp_int;
    public String text;
    public boolean sending;
    public long author_id;
    private JSONParser parser;

    public Message(long id, boolean incoming, boolean error, int _timestamp, String _text, Context ctx) {
        this.id = id;
        isIncoming = incoming;
        isError = error;
        text = _text;
        timestamp_int = _timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(_timestamp));
        timestamp = new SimpleDateFormat("HH:mm").format(dt);
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
}
