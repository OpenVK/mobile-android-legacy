package uk.openvk.android.legacy.api.models;

/**
 * Created by Dmitry on 27.09.2022.
 */
public class LongPollServer {
    public String address;
    public String key;
    public int ts;

    public LongPollServer() {

    }

    public LongPollServer(String address, String key, int ts) {
        this.address = address;
        this.key = key;
        this.ts = ts;
    }
}
