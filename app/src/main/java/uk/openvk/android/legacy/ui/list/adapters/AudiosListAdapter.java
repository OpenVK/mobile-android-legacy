package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.services.AudioPlayerService;

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

public class AudiosListAdapter extends RecyclerView.Adapter<AudiosListAdapter.Holder> {
    private LinearLayout bottom_player_view;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Audio> objects;
    public boolean opened_sliding_menu;
    private int currentTrackPos;

    public AudiosListAdapter(Context context, LinearLayout bottom_player_view, ArrayList<Audio> items) {
        ctx = context;
        objects = items;
        this.bottom_player_view = bottom_player_view;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Audio getItem(int position) {
        return objects.get(position);
    }

    @Override
    public AudiosListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudiosListAdapter.Holder(
                LayoutInflater.from(ctx).inflate(R.layout.list_item_audio, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    Audio getAudio(int position) {
        return (getItem(position));
    }

    public void setTrackState(int current_track_position, int status) {
        Audio audio = objects.get(currentTrackPos);
        audio.status = 0;
        objects.set(currentTrackPos, audio);
        audio = objects.get(current_track_position);
        int track_status;
        switch (status) {
            case AudioPlayerService.STATUS_STARTING:
                track_status = 1;
                break;
            case AudioPlayerService.STATUS_PLAYING:
                track_status = 2;
                break;
            case AudioPlayerService.STATUS_PAUSED:
                track_status = 3;
                break;
            default:
                track_status = 0;
        }
        audio.status = track_status;
        objects.set(current_track_position, audio);

        notifyItemChanged(currentTrackPos, false);
        currentTrackPos = current_track_position;
    }

    public int getCurrentTrackPosition() {
        return currentTrackPos;
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_subtext;
        public View view;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_name = (view.findViewById(R.id.audio_title));
            item_subtext = (view.findViewById(R.id.audio_artist));
        }

        void bind(final int position) {
            final Audio item = getItem(position);
            item_name.setText(item.title);
            item_subtext.setText(item.artist);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx instanceof AppActivity) {
                        ((AppActivity) ctx).hideSelectedItemBackground();
                    }
                }
            });

            view.findViewById(R.id.audio_progress).setVisibility(View.GONE);
            view.findViewById(R.id.audio_saved_icon).setVisibility(View.GONE);
            if(item.status == 1 || item.status == 2) {
                view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
                showBottomPlayer(item);
            } else if(item.status == 3) {
                ((ImageView) view.findViewById(R.id.audio_play_icon))
                        .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_pause));
                view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
                showBottomPlayer(item);
            } else {
                view.findViewById(R.id.audio_play_icon).setVisibility(View.GONE);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTrackState(currentTrackPos, AudioPlayerService.STATUS_STOPPED);
                    Audio item = getItem(position);
                    Log.d(OvkApplication.APP_TAG, String.format("Audio track status: %s", item.status));
                    if(item.status == 0 || item.status == 3) {
                        currentTrackPos = position;
                    }
                    playAudioTrack(position);
                    showBottomPlayer(item);
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void showBottomPlayer(Audio track) {
            if(ctx instanceof AppActivity) {
                AppActivity activity = ((AppActivity) ctx);
                activity.audiosFragment.showBottomPlayer(this, track);
            }
        }

        private void pauseAudioTrack() {
            ((ImageView) view.findViewById(R.id.audio_play_icon))
                    .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_pause));
            view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
        }

        public void playAudioTrack(final int position) {
            Audio track = getItem(position);
            final Audio track2 = track;
            ((ImageView) view.findViewById(R.id.audio_play_icon))
                    .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_play));
            view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
            view.findViewById(R.id.audio_progress).setVisibility(View.VISIBLE);
            if(ctx instanceof AppActivity) {
                AppActivity activity = ((AppActivity) ctx);
                switch (track.status) {
                    case 0:
                        activity.audiosFragment.setAudioPlayerState(position, AudioPlayerService.STATUS_STARTING);
                        break;
                    case 2:
                        activity.audiosFragment.setAudioPlayerState(position, AudioPlayerService.STATUS_PAUSED);
                        break;
                    case 3:
                        activity.audiosFragment.setAudioPlayerState(position, AudioPlayerService.STATUS_PLAYING);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

}

