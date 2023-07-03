package uk.openvk.android.legacy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LongPollReceiver extends BroadcastReceiver {

    static final String TAG = "LPReceiver";

    private static LongPollReceiver receiver;
    private static int networkType = -1;
    private Context ctx;

    public LongPollReceiver(Context ctx) {
        this.ctx = ctx;
        receiver = this;
    }

    public LongPollReceiver() {
        receiver = this;
    }

    public static LongPollReceiver getInstance() {
        if(receiver == null) {
            receiver = new LongPollReceiver(null);
        }
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);
    }

}
