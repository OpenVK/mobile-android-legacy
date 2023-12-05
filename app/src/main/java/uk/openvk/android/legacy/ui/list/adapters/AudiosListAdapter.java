package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
    private MediaPlayer mp;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Audio> objects;
    public boolean opened_sliding_menu;
    private int currentTrackPos;

    public AudiosListAdapter(Context context, LinearLayout bottom_player_view, MediaPlayer mp, ArrayList<Audio> items) {
        ctx = context;
        objects = items;
        this.mp = mp;
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
            } else if(item.status == 3) {
                ((ImageView) view.findViewById(R.id.audio_play_icon))
                        .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_pause));
                view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.audio_play_icon).setVisibility(View.GONE);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Audio item = getItem(position);
                    Log.d(OvkApplication.APP_TAG, String.format("Audio track status: %s", item.status));
                    if(item.status == 0 || item.status == 3) {
                        playAudioTrack(position);
                        currentTrackPos = position;
                        item.status = 2;
                    } else if(item.status == 2) {
                        pauseAudioTrack();
                        item.status = 3;
                    }
                    objects.set(position, item);
                    showBottomPlayer();
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void showBottomPlayer() {
            bottom_player_view.setVisibility(View.VISIBLE);
            TextView title_tv = bottom_player_view.findViewById(R.id.audio_panel_title);
            TextView artist_tv = bottom_player_view.findViewById(R.id.audio_panel_artist);
            final ImageView cover_view = bottom_player_view.findViewById(R.id.audio_panel_cover);
            final ImageView play_btn = bottom_player_view.findViewById(R.id.audio_panel_play);
            Audio track = getItem(currentTrackPos);
            title_tv.setText(track.title);
            artist_tv.setText(track.artist);
            bottom_player_view.findViewById(R.id.audio_panel_prev).setVisibility(View.GONE);
            bottom_player_view.findViewById(R.id.audio_panel_next).setVisibility(View.GONE);
            cover_view.setImageDrawable(
                    ctx.getResources().getDrawable(R.drawable.aplayer_cover_placeholder)
            );
            if(track.status == 0 || track.status == 3) {
                play_btn.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.ic_audio_panel_play)
                );
            } else if(track.status == 2) {
                play_btn.setImageDrawable(
                    ctx.getResources().getDrawable(R.drawable.ic_audio_panel_pause)
                );
            }
            play_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = currentTrackPos;
                    Audio item = getItem(position);
                    if(item.status == 0 || item.status == 3) {
                        playAudioTrack(position);
                        item.status = 2;
                    } else if(item.status == 2) {
                        pauseAudioTrack();
                        item.status = 3;
                    }
                    objects.set(position, item);
                    showBottomPlayer();
                    notifyItemChanged(currentTrackPos);
                }
            });
        }

        private void pauseAudioTrack() {
            mp.pause();
            ((ImageView) view.findViewById(R.id.audio_play_icon))
                    .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_pause));
            view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
        }

        private void playAudioTrack(final int position) {
            try {
                Audio track = getItem(position);
                final Audio track2 = track;
                if(mp != null && track.status == 0) {
                    mp.release();
                    mp = new MediaPlayer();
                    clearCurrentTrackPos();
                }
                ((ImageView) view.findViewById(R.id.audio_play_icon))
                        .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_play));
                view.findViewById(R.id.audio_play_icon).setVisibility(View.VISIBLE);
                if(track.status == 0) {
                    track.status = 1;
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    view.findViewById(R.id.audio_progress).setVisibility(View.VISIBLE);
                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            view.findViewById(R.id.audio_progress).setVisibility(View.GONE);
                            track2.status = 2;
                            objects.set(position, track2);
                        }
                    });
                    mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                            if (percent == 100) {
                                view.findViewById(R.id.audio_saved_icon).setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mediaPlayer.getDuration() > 0) {
                                track2.status = 0;
                                ((ImageView) view.findViewById(R.id.audio_play_icon))
                                        .setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_audio_pause));
                                objects.set(position, track2);
                                if(currentTrackPos < objects.size() - 1) {
                                    playAudioTrack(currentTrackPos++);
                                    showBottomPlayer();
                                }
                            }
                        }
                    });
                    mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                            track2.status = 0;
                            (view.findViewById(R.id.audio_play_icon)).setVisibility(View.GONE);
                            objects.set(position, track2);
                            return false;
                        }
                    });
                    mp.setDataSource(track.url);
                    mp.prepare();
                }
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void clearCurrentTrackPos() {
            Audio currentTrack = getItem(currentTrackPos);
            currentTrack.status = 0;
            objects.set(currentTrackPos, currentTrack);
            notifyItemChanged(currentTrackPos);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public int getCurrentTrackPosition() {
        return currentTrackPos;
    }

}

