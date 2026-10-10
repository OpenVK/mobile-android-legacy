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

package uk.openvk.android.legacy.core.activities.base;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

import uk.openvk.android.client.models.LongPollServer;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.interfaces.OvkAPIListeners;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.receivers.AudioPlayerReceiver;
import uk.openvk.android.legacy.receivers.OvkAPIReceiver;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.services.LongPollService;
import uk.openvk.android.legacy.utils.SecureCredentialsStorage;

import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_PLAYER_CONTROL;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_PLAYLIST;

@SuppressLint("Registered")
public class NetworkFragmentActivity extends TranslucentFragmentActivity
        implements AudioPlayerService.AudioPlayerListener {
    protected String mServer;
    protected String mState;
    protected OpenVKAPI mOpenVK;
    protected SharedPreferences mGlobalPrefs;
    protected SharedPreferences mInstancePrefs;
    public Handler handler;
    public OvkAPIReceiver mApiReceiver;
    private String mSessionId;
    private boolean mIsBoundAP;
    protected HashMap<String, Object> mClientInfo;

    public LongPollServer longPollServer;

    private Intent audioPlayerIntent;
    private AudioPlayerReceiver audioPlayerReceiver;
    private AudioPlayerService audioPlayerService;

    public boolean isBoundLPS;
    private Intent longPollIntent;
    private LongPollService longPollService;

    private ServiceConnection audioPlayerConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            audioPlayerService.removeListener(NetworkFragmentActivity.this);
            unbindAudioPlayer();

            audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mIsBoundAP = true;
            AudioPlayerService.AudioPlayerBinder mLocalBinder =
                    (AudioPlayerService.AudioPlayerBinder) service;
            audioPlayerService = mLocalBinder.getService();
            audioPlayerService.addListener(NetworkFragmentActivity.this);
        }
    };

    private ServiceConnection longPollConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindLongPollService();
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            isBoundLPS = true;
            LongPollService.LongPollBinder mLocalBinder =
                    (LongPollService.LongPollBinder) service;

            longPollService = mLocalBinder.getService();
        }
    };

    protected Fragment selectedFragment;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlobalPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mInstancePrefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();

        if(mInstancePrefs == null) {
            mInstancePrefs = getSharedPreferences(
                    String.format(
                            "instance_a%s_%s",
                            mGlobalPrefs.getString("current_instance", ""),
                            mGlobalPrefs.getLong("current_uid", 0)
                    ), 0
            );
        }

        handler = new Handler(Looper.myLooper());
        mClientInfo = SecureCredentialsStorage.generateClientInfo(this);
        mOpenVK = new OpenVKAPI(this, mClientInfo, handler);
        generateSessionId();

        OvkAPIListeners apiListeners = new OvkAPIListeners();
        setAPIListeners(apiListeners);
        registerReceivers();
    }

    public void registerReceivers() {
        mApiReceiver = new OvkAPIReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mApiReceiver,
                new IntentFilter("uk.openvk.android.client.DATA_RECEIVE")
        );
        audioPlayerReceiver = new AudioPlayerReceiver(this);
        IntentFilter intentFilter = new IntentFilter(ACTION_PLAYER_CONTROL);
        intentFilter.addAction(ACTION_UPDATE_PLAYLIST);
        intentFilter.addAction(ACTION_UPDATE_CURRENT_TRACKPOS);
        registerReceiver(audioPlayerReceiver, intentFilter);

        ((OvkApplication) getApplicationContext()).registerLongPollReceiver(mOpenVK);
    }

    private void setAPIListeners(final OvkAPIListeners listeners) {
        listeners.from = String.format("%s_%s", getLocalClassName(), getSessionId());
        listeners.successListener = new OvkAPIListeners.OnAPISuccessListener() {
            @Override
            public void onAPISuccess(final Context ctx, int msg_code, final Bundle data) {
                if(BuildConfig.DEBUG) {
                    if(msg_code > 0)
                        Log.d(OvkApplication.APP_TAG,
                                String.format(
                                        "Handling API message %s in %s",
                                        msg_code,
                                        listeners.from
                                )
                        );
                    else if(data != null)
                        Log.d(OvkApplication.APP_TAG,
                                String.format(
                                        "Handling API message %s in %s (method: \"%s\")",
                                        msg_code,
                                        listeners.from,
                                        data.getString("method")
                                )
                        );
                }

                if(msg_code == HandlerMessages.PARSE_JSON) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setAction("uk.openvk.android.client.DATA_RECEIVE");
                            data.putString("address",
                                    String.format("%s_%s", getLocalClassName(), getSessionId())
                            );
                            intent.putExtras(data);
                            LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
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
                if(BuildConfig.DEBUG)
                    Log.d(OvkApplication.APP_TAG,
                            String.format(
                                    "Handling API message %s in %s",
                                    msg_code,
                                    listeners.from
                            )
                    );
                receiveState(msg_code, data);
            }
        };
        listeners.processListener = new OvkAPIListeners.OnAPIProcessListener() {
            @Override
            public void onAPIProcess(Context ctx, Bundle data, long value, long length) {
                if(BuildConfig.DEBUG)
                    Log.d(OvkApplication.APP_TAG,
                            String.format(
                                    "Handling API message %s in %s",
                                    HandlerMessages.UPLOAD_PROGRESS,
                                    getLocalClassName()
                            )
                    );
                receiveState(HandlerMessages.UPLOAD_PROGRESS, data);
            }
        };
        mOpenVK.wrapper.setAPIListeners(listeners);
        mOpenVK.dlman.setAPIListeners(listeners);
        mOpenVK.ulman.setAPIListeners(listeners);
    }

    private String generateSessionId() {
        // Generating Activity Session ID for phantom receiving API data fix
        Random rand = new Random();
        int max_rand = 0xFFFF;
        byte[] bytes = ByteBuffer.allocate(4).putInt(rand.nextInt(max_rand)).array();
        while(Global.bytesToHex(bytes).equals(mSessionId)) {
            generateSessionId();
        }
        this.mSessionId = Global.bytesToHex(bytes);
        return mSessionId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void receiveState(int message, Bundle data) {

    }

    public SharedPreferences.Editor getGlobalPreferencesEditor() {
        return mGlobalPrefs.edit();
    }

    public SharedPreferences.Editor getInstancePreferenceEditor() {
        return mInstancePrefs.edit();
    }

    public void bindLongPollService() {
        isBoundLPS = true;

        if(longPollIntent == null) {
            longPollIntent = new Intent(getApplicationContext(), LongPollService.class);
            longPollIntent.putExtra("action", "LONGPOLL_START");
            longPollIntent.putExtra("lp_server", longPollServer.address);
            longPollIntent.putExtra("lp_key", longPollServer.key);
            longPollIntent.putExtra("lp_timestamp", longPollServer.ts);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getApplicationContext().startForegroundService(longPollIntent);
        else
            getApplicationContext().startService(longPollIntent);

        bindService(longPollIntent, longPollConnection, BIND_AUTO_CREATE);
    }

    public boolean checkIsBoundAudioPlayer() {
        return mIsBoundAP;
    }

    public void bindAudioPlayer() {
        mIsBoundAP = true;

        if(audioPlayerIntent == null) {
            audioPlayerIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
            if (!mIsBoundAP) {
                Log.d(OvkApplication.APP_TAG, "Creating AudioPlayerService intent");
                audioPlayerIntent.putExtra("action", "PLAYER_CREATE");
            } else {
                audioPlayerIntent.putExtra("action", "PLAYER_GET_CURRENT_POSITION");
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getApplicationContext().startForegroundService(audioPlayerIntent);
        else
            getApplicationContext().startService(audioPlayerIntent);

        bindService(audioPlayerIntent, audioPlayerConnection, BIND_AUTO_CREATE);
    }

    public void unbindAudioPlayer() {
        if(audioPlayerService != null) {
            if(!audioPlayerService.isPlaying()) {
                if(audioPlayerReceiver != null)
                    unregisterReceiver(audioPlayerReceiver);
                unbindService(audioPlayerConnection);
                getApplicationContext().stopService(audioPlayerIntent);
                mIsBoundAP = false;

                if (this instanceof AppActivity) {
                    AppActivity activity = ((AppActivity) this);
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                        activity.getNotificationManager().clearAudioPlayerNotification();
                }
            }
        }
        mIsBoundAP = false;
    }

    private void unbindLongPollService() {
        if(longPollService != null) {
            unbindService(longPollConnection);
            getApplicationContext().stopService(longPollIntent);
            isBoundLPS = false;
        }
    }

    public void setAudioPlayerState(int position, long owner_id, int status) {
        String action;
        switch (status) {
            case AudioPlayerService.STATUS_STARTING:
                action = "PLAYER_START";
                break;
            case AudioPlayerService.STATUS_PLAYING:
                action = "PLAYER_PLAY";
                break;
            case AudioPlayerService.STATUS_PAUSED:
                action = "PLAYER_PAUSE";
                break;
            default:
                action = "PLAYER_STOP";
                break;
        }

        audioPlayerIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
        audioPlayerIntent.putExtra("action", action);

        if(status == AudioPlayerService.STATUS_STARTING) {
            audioPlayerIntent.putExtra("owner_id", owner_id);
            audioPlayerIntent.putExtra("position", position);
        }

        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");

        startService(audioPlayerIntent);
        bindAudioPlayer();
    }

    public AudioPlayerService getAudioPlayerService() {
        return audioPlayerService;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mApiReceiver);
        super.onDestroy();
    }

    @Override
    public void onChangeAudioPlayerStatus(String action, int status, int track_pos, Bundle data) {

    }

    @Override
    public void onReceiveCurrentTrackPosition(int track_pos, int status) {

    }

    @Override
    public void onUpdateSeekbarPosition(int position, int duration, double buffer_length) {

    }

    @Override
    public void onAudioPlayerError(int what, int extra, int current_track_pos) {
        try {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.audio_play_error),
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Fragment getSelectedFragment() {
        return selectedFragment;
    }

    public OpenVKAPI getOpenVKAPI() {
        return mOpenVK;
    }

    public void setOpenVKAPI(OpenVKAPI ovkApi) {
        this.mOpenVK = ovkApi;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent classical bottom menu opening when clicking Menu button on Android 2.x devices
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }
}
