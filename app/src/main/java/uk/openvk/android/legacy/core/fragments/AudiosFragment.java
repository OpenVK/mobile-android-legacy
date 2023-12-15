package uk.openvk.android.legacy.core.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.AudioPlayerActivity;
import uk.openvk.android.legacy.databases.AudioCacheDB;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.listeners.AudioPlayerListener;
import uk.openvk.android.legacy.receivers.AudioPlayerReceiver;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.ui.list.adapters.AudiosListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;

import static android.content.Context.BIND_AUTO_CREATE;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_PLAYER_CONTROL;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_CURRENT_TRACKPOS;
import static uk.openvk.android.legacy.services.AudioPlayerService.ACTION_UPDATE_PLAYLIST;
import static uk.openvk.android.legacy.services.AudioPlayerService.STATUS_STARTING_FROM_WALL;

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

public class AudiosFragment extends Fragment implements AudioPlayerService.AudioPlayerListener {
    private RecyclerView audiosView;
    private Account account;
    private View view;
    private String instance;
    private ArrayList<Audio> audios;
    private AudiosListAdapter audiosAdapter;
    private Context parent;
    private MediaPlayer mediaPlayer;
    public boolean isBoundAP;
    private AudioPlayerReceiver audioPlayerReceiver;
    private AudioPlayerService audioPlayerService;
    private int currentTrackPos;
    private Intent serviceIntent;
    private ServiceConnection audioPlayerConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            audioPlayerService.removeListener(AudiosFragment.this);
            isBoundAP = false;
            audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            isBoundAP = true;
            AudioPlayerService.AudioPlayerBinder mLocalBinder =
                    (AudioPlayerService.AudioPlayerBinder) service;
            ((AudioPlayerService.AudioPlayerBinder) service).setAudioPlayerListener(AudiosFragment.this);
            audioPlayerService = mLocalBinder.getService();
            audioPlayerService.addListener(AudiosFragment.this);
        }
    };

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
        AudioCacheDB.fillDatabase(parent, audios, true);
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
        }
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        parent.getApplicationContext().startService(serviceIntent);
        parent.getApplicationContext().bindService(serviceIntent, audioPlayerConnection, BIND_AUTO_CREATE);
    }

    public void setScrollingPositions(Context ctx, boolean b) {
    }

    public void receivePlayerStatus(String action, int status, int track_position, Bundle data) {
        if(audios != null && audios.size() > 0) {
            audiosAdapter.setTrackState(track_position, status);
            if (parent instanceof AppActivity) {
                AppActivity activity = ((AppActivity) parent);
                if (status == AudioPlayerService.STATUS_STARTING) {
                    activity.notifMan.createAudioPlayerChannel();
                }
                if (status != AudioPlayerService.STATUS_STOPPED) {
                    activity.notifMan.buildAudioPlayerNotification(
                            getContext(), audios, track_position
                    );
                    showBottomPlayer(audios.get(track_position));
                } else {
                    audiosAdapter.setTrackState(track_position, 0);
                    activity.notifMan.clearAudioPlayerNotification();
                    if (view != null) {
                        view.findViewById(R.id.audio_player_bar).setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void showBottomPlayer(final AudiosListAdapter.Holder holder, final Audio track) {
        LinearLayout bottom_player_view = view.findViewById(R.id.audio_player_bar);
        bottom_player_view.setVisibility(View.VISIBLE);
        TextView title_tv = bottom_player_view.findViewById(R.id.audio_panel_title);
        TextView artist_tv = bottom_player_view.findViewById(R.id.audio_panel_artist);
        final ImageView cover_view = bottom_player_view.findViewById(R.id.audio_panel_cover);
        final ImageView play_btn = bottom_player_view.findViewById(R.id.audio_panel_play);
        title_tv.setText(track.title);
        artist_tv.setText(track.artist);
        title_tv.setSelected(true);
        artist_tv.setSelected(true);
        bottom_player_view.findViewById(R.id.audio_panel_prev).setVisibility(View.GONE);
        bottom_player_view.findViewById(R.id.audio_panel_next).setVisibility(View.GONE);
        cover_view.setImageDrawable(
                getResources().getDrawable(R.drawable.aplayer_cover_placeholder)
        );
        if(track.status == 0 || track.status == 3) {
            play_btn.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_audio_panel_play)
            );
        } else if(track.status == 2) {
            play_btn.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_audio_panel_pause)
            );
        }
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomPlayer(holder, track);
                holder.playAudioTrack(audiosAdapter.getCurrentTrackPosition());
            }
        });
        bottom_player_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AudioPlayerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showBottomPlayer(Audio track) {
        LinearLayout bottom_player_view = view.findViewById(R.id.audio_player_bar);
        bottom_player_view.setVisibility(View.VISIBLE);
        TextView title_tv = bottom_player_view.findViewById(R.id.audio_panel_title);
        TextView artist_tv = bottom_player_view.findViewById(R.id.audio_panel_artist);
        final ImageView cover_view = bottom_player_view.findViewById(R.id.audio_panel_cover);
        final ImageView play_btn = bottom_player_view.findViewById(R.id.audio_panel_play);
        title_tv.setText(track.title);
        artist_tv.setText(track.artist);
    }

    public void updateCurrentTrackPosition(int track_pos, int status) {
        audiosAdapter.setTrackState(audiosAdapter.getCurrentTrackPosition(), status);
    }

    @Override
    public void onDestroy() {
        if(audioPlayerReceiver != null) {
            parent.unregisterReceiver(audioPlayerReceiver);
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
        Audio track = audios.get(currentTrackPos);
        Toast.makeText(
                getContext(),
                getResources().getString(R.string.audio_play_error),
                Toast.LENGTH_LONG).show();
    }
}
