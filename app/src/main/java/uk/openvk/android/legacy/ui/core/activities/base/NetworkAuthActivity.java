package uk.openvk.android.legacy.ui.core.activities.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.interfaces.OvkAPIListeners;
import uk.openvk.android.legacy.receivers.OvkAPIReceiver;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 **/

@SuppressLint("Registered")
public class NetworkAuthActivity extends TranslucentAuthActivity {
    public OpenVKAPI ovk_api;
    public SharedPreferences global_prefs;
    public SharedPreferences instance_prefs;
    public SharedPreferences.Editor global_prefs_editor;
    public SharedPreferences.Editor instance_prefs_editor;
    public Handler handler;
    public OvkAPIReceiver receiver;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
        global_prefs_editor = global_prefs.edit();
        instance_prefs_editor = instance_prefs.edit();
        handler = new Handler(Looper.myLooper());
        ovk_api = new OpenVKAPI(this, global_prefs, null, handler);
        OvkAPIListeners apiListeners = new OvkAPIListeners();
        setAPIListeners(apiListeners);
        registerAPIDataReceiver();
    }

    public void registerAPIDataReceiver() {
        receiver = new OvkAPIReceiver(this);
        this.registerReceiver(receiver, new IntentFilter(
                "uk.openvk.android.legacy.API_DATA_RECEIVE"));
    }

    public void setAPIListeners(final OvkAPIListeners listeners) {
        listeners.from = getLocalClassName();
        listeners.successListener = new OvkAPIListeners.OnAPISuccessListener() {
            @Override
            public void onAPISuccess(final Context ctx, int msg_code, final Bundle data) {
                if(!BuildConfig.BUILD_TYPE.equals("release"))
                    Log.d(OvkApplication.APP_TAG,
                            String.format(
                                    "Handling API message %s in %s",
                                    msg_code,
                                    listeners.from
                            )
                    );
                if(msg_code == HandlerMessages.PARSE_JSON) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setAction("uk.openvk.android.legacy.API_DATA_RECEIVE");
                            data.putString("address", getLocalClassName());
                            intent.putExtras(data);
                            sendBroadcast(intent);
                        }
                    }).start();
                } else {
                    receiveState(msg_code, data);
                }
            }
        };
        listeners.failListener = new OvkAPIListeners.OnAPIFailListener() {
            @Override
            public void onAPIFailed(Context ctx, int msg_code, final Bundle data) {
                if(!BuildConfig.BUILD_TYPE.equals("release"))
                    Log.d(OvkApplication.APP_TAG,
                            String.format(
                                    "Handling API message %s in %s",
                                    msg_code,
                                    getClass().getCanonicalName()
                            )
                    );
                receiveState(msg_code, data);
            }
        };
        ovk_api.wrapper.setAPIListeners(listeners);
    }

    public void receiveState(int message, Bundle data) {

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
