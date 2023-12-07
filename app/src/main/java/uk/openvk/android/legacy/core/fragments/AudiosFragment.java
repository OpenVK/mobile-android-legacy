package uk.openvk.android.legacy.core.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.listeners.AudioPlayerListener;
import uk.openvk.android.legacy.receivers.AudioPlayerReceiver;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.ui.list.adapters.AudiosListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.utils.NotificationManager;

import static android.content.Context.BIND_AUTO_CREATE;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_PLAYER_CONTROL;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_PLAYLIST;

/*  Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

public class AudiosFragment extends Fragment implements AudioPlayerListener {
    private RecyclerView audiosView;
    private Account account;
    private View view;
    private String instance;
    private ArrayList<Audio> audios;
    private AudiosListAdapter audiosAdapter;
    private Context parent;
    private MediaPlayer mediaPlayer;
    private boolean isBoundAP;
    private AudioPlayerReceiver audioPlayerReceiver;
    private ServiceConnection audioPlayerConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            isBoundAP = false;
            audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            isBoundAP = true;
            AudioPlayerService.AudioPlayerBinder mLocalBinder =
                    (AudioPlayerService.AudioPlayerBinder) service;
            OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
            ((AudioPlayerService.AudioPlayerBinder) service).setAudioPlayerListener(AudiosFragment.this);
            app.audioPlayerService = mLocalBinder.getService();
        }
    };
    private AudioPlayerService audioPlayerService;
    private int currentTrackPos;
    private Intent serviceIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_audios, container, false);
        audiosView = view.findViewById(R.id.audios_listview);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Audio> audios) {
        this.parent = ctx;
        this.audios = audios;
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        audioPlayerReceiver = new AudioPlayerReceiver(getContext());
        IntentFilter intentFilter = new IntentFilter(ACTION_PLAYER_CONTROL);
        intentFilter.addAction(ACTION_UPDATE_PLAYLIST);
        intentFilter.addAction(ACTION_UPDATE_CURRENT_TRACKPOS);
        parent.registerReceiver(audioPlayerReceiver, intentFilter);
        if(app.audioPlayerService == null) {
            app.audioPlayerService = new AudioPlayerService();
        }
        startAudioPlayerService();
        if (audiosAdapter == null) {
            LinearLayout bottom_player_view = view.findViewById(R.id.audio_player_bar);
            audiosAdapter = new AudiosListAdapter(ctx, bottom_player_view, audios);
            if(app.isTablet && app.swdp >= 760) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(glm);
            } else if(app.isTablet && app.swdp >= 600) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(glm);
            } else {
                LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(llm);
            }
            audiosView.setAdapter(audiosAdapter);
        } else {
            audiosAdapter.notifyDataSetChanged();
        }
    }

    public void startAudioPlayerService() {
        serviceIntent = new Intent(getContext().getApplicationContext(), AudioPlayerService.class);
        if(!isBoundAP) {
            OvkApplication app = ((OvkApplication) getContext().getApplicationContext());
            Log.d(OvkApplication.APP_TAG, "Creating AudioPlayerService intent");
            serviceIntent.putExtra("action", "PLAYER_CREATE");
        } else {
            serviceIntent.putExtra("action", "PLAYER_GET_CURRENT_POSITION");
        }
        parent.getApplicationContext().startService(serviceIntent);
        parent.getApplicationContext().bindService(serviceIntent, audioPlayerConnection, BIND_AUTO_CREATE);
    }

    public void setAudioPlayerState(int position, int status) {
        String action = "";
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
        serviceIntent = new Intent(parent.getApplicationContext(), AudioPlayerService.class);
        serviceIntent.putExtra("action", action);
        if(status == AudioPlayerService.STATUS_STARTING) {
            serviceIntent.putExtra("position", position);
            serviceIntent.putParcelableArrayListExtra("playlist", audios);
        }
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        parent.getApplicationContext().startService(serviceIntent);
        parent.getApplicationContext().bindService(serviceIntent, audioPlayerConnection, BIND_AUTO_CREATE);
    }

    public void setScrollingPositions(Context ctx, boolean b) {
    }

    @Override
    public void onStartAudioPlayer(AudioPlayerService service) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        app.audioPlayerService = service;
    }

    public void receivePlayerStatus(String action, int status, int track_position, Bundle data) {
        audiosAdapter.setTrackState(track_position, status);
        if(status == AudioPlayerService.STATUS_STARTING) {
            if (parent instanceof AppActivity) {
                AppActivity activity = ((AppActivity) parent);
                activity.notifMan.createAudioPlayerChannel();
                activity.notifMan.buildAudioPlayerNotification(getContext(), audios, track_position, true, false);
            }
        }
    }

    @Override
    public void onDestroy() {
        if(audioPlayerReceiver != null) {
            parent.getApplicationContext().unregisterReceiver(audioPlayerReceiver);
        }
        if(audioPlayerService != null) {
            parent.getApplicationContext().unbindService(audioPlayerConnection);
            parent.getApplicationContext().stopService(serviceIntent);
            isBoundAP = false;
            if (parent instanceof AppActivity) {
                AppActivity activity = ((AppActivity) parent);
                activity.notifMan.clearAudioPlayerNotification();
            }
        }
        super.onDestroy();
    }

    public void updateCurrentTrackPosition(int track_pos, int status) {
        audiosAdapter.setTrackState(audiosAdapter.getCurrentTrackPosition(), status);
    }
}
