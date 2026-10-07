package uk.openvk.android.client.entities;

import uk.openvk.android.client.base.LazyEntity;

public class LongPollUpdate extends LazyEntity {

    private final int       eventType;
    public final long      msgId;
    private final long      minorId;
    public final long       peerId;
    private final long      timestamp;
    public final String     text;

    // Source: https://github.com/danyadev/longpoll-doc

    public LongPollUpdate(
            int eventType, long msgId, long minorId,
            long peerId, long timestamp,
            String text
    ) {
        super(REAL_ENTITY);
        this.eventType  = eventType;
        this.msgId      = msgId;
        this.minorId    = minorId;
        this.peerId     = peerId;
        this.timestamp  = timestamp;
        this.text       = text;
    }
}
