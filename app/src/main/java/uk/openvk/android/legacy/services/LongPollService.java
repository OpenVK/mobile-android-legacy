package uk.openvk.android.legacy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import uk.openvk.android.legacy.api.wrappers.LongPollWrapper;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class LongPollService extends Service {
    private String lp_server;
    private String key;
    private int ts;
    private OvkAPIWrapper ovk_api;
    private LongPollWrapper lpW;
    private Context ctx;
    private String access_token;

    public LongPollService(Context ctx, String access_token) {
        this.ctx = ctx;
        this.access_token = access_token;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("OpenVK Legacy", "Starting LongPoll Service...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("OpenVK Legacy", String.format("Getting LPS start ID: %d", startId));
        return super.onStartCommand(intent, flags, startId);
    }

    public void run(String instance, String lp_server, String key, int ts, boolean use_https) {
        ovk_api = new OvkAPIWrapper(ctx, use_https);
        ovk_api.setServer(instance);
        ovk_api.setAccessToken(access_token);
        ovk_api.sendAPIMethod("Account.setOnline");
        runLongPull(lp_server, key, ts, use_https);
    }

    private void runLongPull(String lp_server, String key, int ts, boolean use_https) {
        lpW = new LongPollWrapper(ctx, use_https);
        lpW.longPoll(lp_server, key, ts);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("OpenVK Legacy", "Stopping LongPoll Service...");
    }
}
