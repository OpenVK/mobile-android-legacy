/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;

import uk.openvk.android.client.interfaces.OvkAPIListeners;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.client.longpoll.LongPollWrapper;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.utils.NotificationManager;
import uk.openvk.android.legacy.utils.SecureCredentialsStorage;

import static uk.openvk.android.legacy.OvkApplication.APP_TAG;

public class LongPollService extends Service {
    private Handler             handler;
    private OvkAPIWrapper       ovk_api;
    private LongPollWrapper     lpW;
    private Context             ctx;
    private LongPollBinder      binder;
    private Notification        notification;
    private NotificationManager notifManager;

    public LongPollService() {

    }

    public LongPollService(Context ctx, Handler handler,
                           HashMap<String, Object> client_info) {
        this.handler = handler;
        this.ctx = ctx;
        lpW = new LongPollWrapper(ctx, client_info);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(OvkApplication.LP_TAG, "Starting LongPoll Service...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(OvkApplication.LP_TAG, String.format("Getting LPS start ID: %s", startId));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (intent == null) {
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            } else {
                if(notification == null) {
                    notifManager = new NotificationManager(this);
                    notification = notifManager.createLongPollNotification(
                            R.drawable.ic_stat_notify, "service_notifs",
                            getResources().getString(R.string.longpoll_notification_title),
                            getResources().getString(R.string.longpoll_notification_subtitle)
                    );
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        startForeground(0x6200, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                    else
                        startForeground(0x6200, notification);
                }
            }
        }

        Bundle data = intent.getExtras();

        if(data == null || data.isEmpty())
            return START_NOT_STICKY;

        String action           = data.containsKey("action") ?
                                        data.getString("action") : "";

        String lpServer         = data.containsKey("lp_server") ?
                                        data.getString("lp_server") : "";

        String lpKey            = data.containsKey("lp_key") ?
                                        data.getString("lp_key") : "";

        int lpTimestamp         = data.getInt("lp_timestamp");


        HashMap<String, Object> clientInfo = SecureCredentialsStorage.generateClientInfo(
                getApplicationContext()
        );

        if(action == null)
            return START_NOT_STICKY;

        switch (action) {
            case "LONGPOLL_START":
                initService(lpServer, lpKey, lpTimestamp, clientInfo);
                break;
        }

        return START_STICKY;
    }

    private void initService(String lp_server, String key, int ts, HashMap<String, Object> clientInfo) {

        if(lpW == null)
            lpW = new LongPollWrapper(getApplicationContext(), clientInfo);

        OvkAPIListeners listeners = new OvkAPIListeners();

        listeners.successListener = new OvkAPIListeners.OnAPISuccessListener() {
            @Override
            public void onAPISuccess(final Context ctx, int msg_code, final Bundle data) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction("uk.openvk.android.client.LONGPOLL_RECEIVE");
                        intent.putExtras(data);
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
                    }
                }).start();
            }
        };

        lpW.setAPIListeners(listeners);

        ovk_api = new OvkAPIWrapper(
                this, SecureCredentialsStorage.generateClientInfo(this), handler
        );
        runLongPull(lp_server, key, ts);
    }

    @SuppressWarnings("ConstantConditions")
    private void runLongPull(String lp_server, String key, int ts) {
        if(!BuildConfig.VERSION_NAME.endsWith("-d"))
            lpW.log(false);

        lpW.updateCounters(ovk_api);
        lpW.keepUptime(ovk_api);

        if(lp_server != null && key != null)
            lpW.longPoll(lp_server, key, ts, false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(OvkApplication.LP_TAG, "Stopping LongPoll Service...");
    }

    public void setProxyConnection(boolean useProxy, String proxyAddress) {
        if(lpW == null) {
            lpW = new LongPollWrapper(
                    ctx, SecureCredentialsStorage.generateClientInfo(getBaseContext())
            );
        }
        lpW.setProxyConnection(useProxy, proxyAddress);
    }

    public class LongPollBinder extends Binder {
        public LongPollService getService() {
            return LongPollService.this;
        }
    }
}
