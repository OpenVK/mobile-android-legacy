/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.client.longpoll.LongPollWrapper;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.utils.SecureCredentialsStorage;

public class LongPollService extends Service {
    private String lp_server;
    private String key;
    private int ts;
    private Handler handler;
    private OvkAPIWrapper ovk_api;
    private LongPollWrapper lpW;
    private Context ctx;
    private String access_token;
    private boolean use_https = false;

    public LongPollService() {
    }

    public LongPollService(Context ctx, Handler handler,
                           HashMap<String, Object> client_info) {
        this.handler = handler;
        this.ctx = ctx;
        this.access_token = access_token;
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
        return super.onStartCommand(intent, flags, startId);
    }

    public void run(String instance, String lp_server, String key, int ts, boolean use_https,
                    boolean legacy_client) {
        this.use_https = use_https;
        HashMap<String, Object> client_info = SecureCredentialsStorage.generateClientInfo();
        if(lpW == null) {
            lpW = new LongPollWrapper(ctx, client_info);
        }
        ovk_api = new OvkAPIWrapper(ctx, client_info, handler);
        ovk_api.setServer(instance);
        ovk_api.setAccessToken(access_token);
        if(BuildConfig.BUILD_TYPE.equals("release")) ovk_api.log(false);
        runLongPull(lp_server, key, ts, use_https);
    }

    private void runLongPull(String lp_server, String key, int ts, boolean use_https) {
        if(BuildConfig.BUILD_TYPE.equals("release")) lpW.log(false);
        lpW.updateCounters(ovk_api);
        lpW.keepUptime(ovk_api);
        if(lp_server != null && key != null) {
            lpW.longPoll(lp_server, key, ts);
        }
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

    public void setProxyConnection(boolean useProxy, String proxy_address) {
        if(lpW == null) {
            lpW = new LongPollWrapper(ctx, SecureCredentialsStorage.generateClientInfo());
        }
        lpW.setProxyConnection(useProxy, proxy_address);
    }
}
