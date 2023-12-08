package uk.openvk.android.legacy.utils;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.api.entities.Conversation;
import uk.openvk.android.legacy.api.longpoll.MessageEvent;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.AudioPlayerActivity;
import uk.openvk.android.legacy.core.activities.ConversationActivity;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.services.connections.AudioPlayerConnection;

import static android.content.Context.BIND_AUTO_CREATE;

/** Copyleft © 2022, 2023 OpenVK Team
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
 **/

public class NotificationManager {

    public String ringtone_url;
    private android.app.NotificationManager notifMan;
    private NotificationChannel longPollCh;
    private NotificationChannel audioPlayerCh;
    private Context ctx;
    public boolean ledIndicate;
    public boolean vibrate;
    public boolean playSound;
    private PendingIntent audioPlayerIntent;

    public NotificationManager(Context ctx, boolean ledIndicate, boolean vibrate, boolean playSound, String ringtone_url) {
        this.ctx = ctx;
        this.ledIndicate = ledIndicate;
        this.vibrate = vibrate;
        this.playSound = playSound;
        this.ringtone_url = ringtone_url;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifMan = ctx.getSystemService(android.app.NotificationManager.class);
        } else {
            notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void createLongPollChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifMan = ctx.getSystemService(android.app.NotificationManager.class);
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            longPollCh = new NotificationChannel("lp_updates", "LongPoll Updates", importance);
            longPollCh.enableLights(ledIndicate);
            longPollCh.enableVibration(vibrate);
            if(playSound) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                if(!ringtone_url.equals("content://settings/system/notification_sound")) {
                    longPollCh.setSound(Uri.parse(ringtone_url), audioAttributes);
                } else {
                    longPollCh.setSound(null, null);
                }
            }
            notifMan.createNotificationChannel(longPollCh);
        } else {
            notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void createAudioPlayerChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifMan = ctx.getSystemService(android.app.NotificationManager.class);
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            audioPlayerCh = new NotificationChannel("audio_player", "Audio Player", importance);
            notifMan.createNotificationChannel(audioPlayerCh);
        } else {
            notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        audioPlayerIntent = createAudioPlayerIntent(0, new ArrayList<Audio>());
    }

    public void buildDirectMsgNotification(Context ctx, ArrayList<Conversation> conversations,
                                           Bundle data, boolean notify, boolean is_repeat) {
        int notification_id = 0;
        MessageEvent msg_event = new MessageEvent(data.getString("response"));
        if(msg_event.peer_id > 0 && notify) {
            if (!is_repeat) {
                String msg_author = String.format("Unknown ID %s", msg_event.peer_id);
                if(conversations != null) {
                    for (int i = 0; i < conversations.size(); i++) {
                        if (conversations.get(i).peer_id == msg_event.peer_id) {
                            msg_author = conversations.get(i).title;
                        }
                    }
                }
                notification_id = notification_id + 1;
                String last_longpoll_response = data.getString("response");
                Notification notification = createLongPollNotification(notifMan, R.drawable.ic_stat_notify,
                        "lp_updates", msg_author, msg_event.msg_text);
                notification.contentIntent = createConversationIntent(msg_event.peer_id, msg_author);
                notifMan.notify(notification_id, notification);
            }
        }
    }

    public boolean isRepeat(String last_longpoll_response, String response) {
        try {
            return last_longpoll_response.equals(response);
        } catch (Exception ex) {
            return false;
        }
    }

    public void buildAudioPlayerNotification(Context ctx,
                                             ArrayList<Audio> audios, int current_pos) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifMan = ctx.getSystemService(android.app.NotificationManager.class);
        } else {
            notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        int notification_id = 2500;
        String track_title = "Unknown track";
        String track_artist = "Unknown artist";
        int currentTrackPosition = 0;
        Audio track = null;
        if(audios != null) {
            track = audios.get(current_pos);
            currentTrackPosition = current_pos;
        }
        notification_id = notification_id + 1;
        Notification notification = createAudioPlayerNotification(
                ctx, R.drawable.ic_stat_notify_play, "audio_player", track
        );
        notification.flags |= Notification.FLAG_NO_CLEAR;
        audioPlayerIntent = createAudioPlayerIntent(currentTrackPosition, audios);
        notification.contentIntent = audioPlayerIntent;
        notifMan.notify(notification_id, notification);
    }

    public Notification createLongPollNotification(android.app.NotificationManager notifMan, int icon,
                                           String channel_id, String title, String description) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder =
                    new Notification.Builder(ctx)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setChannelId(channel_id);
            notification = builder.build();
            Intent notificationIntent = new Intent(ctx, ConversationActivity.class);
            notification.contentIntent = PendingIntent.getActivity(ctx, 2, notificationIntent, 0);
        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description);

            notification = builder.build();

            if(ledIndicate) {
                notification.defaults = Notification.DEFAULT_LIGHTS;
            }
            if(vibrate) {
                notification.defaults = Notification.DEFAULT_VIBRATE;
            }

            if(playSound) {
                notification.defaults = Notification.DEFAULT_SOUND;
                if(ringtone_url.equals("content://settings/system/notification_sound")) {
                    MediaPlayer mp = MediaPlayer.create(ctx, R.raw.notify);
                    mp.start();
                } else {
                    notification.sound = Uri.parse(ringtone_url);
                }
            }
        }
        return notification;
    }

    public Notification createAudioPlayerNotification(Context ctx, int icon, String channel_id, Audio track) {
        Notification notification;
        RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(), R.layout.audio_notification);
        remoteViews.setTextViewText(R.id.title, track.title);
        remoteViews.setTextViewText(R.id.content, track.artist);
        PendingIntent playPendingIntent = setAudioPlayerControls(ctx, AudioPlayerService.STATUS_PLAYING);
        PendingIntent pausePendingIntent = setAudioPlayerControls(ctx, AudioPlayerService.STATUS_PAUSED);
        PendingIntent prevPendingIntent = setAudioPlayerControls(ctx, AudioPlayerService.STATUS_GOTO_PREVIOUS);
        PendingIntent nextPendingIntent = setAudioPlayerControls(ctx, AudioPlayerService.STATUS_GOTO_NEXT);
        switch (track.status) {
            case 3:
                remoteViews.setOnClickPendingIntent(R.id.playpause, playPendingIntent);
                remoteViews.setImageViewResource(R.id.playpause, R.drawable.ic_audio_panel_play);
                break;
            default:
                remoteViews.setOnClickPendingIntent(R.id.playpause, pausePendingIntent);
                remoteViews.setImageViewResource(R.id.playpause, R.drawable.ic_audio_panel_pause);
                break;
        }

        remoteViews.setOnClickPendingIntent(R.id.prev, prevPendingIntent);

        remoteViews.setOnClickPendingIntent(R.id.next, nextPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder =
                    new Notification.Builder(ctx)
                            .setSmallIcon(icon)
                            .setContent(remoteViews)
                            .setChannelId(channel_id);
            notification = builder.build();
            Intent notificationIntent = new Intent(ctx, AppActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notification.contentIntent = PendingIntent.getActivity(ctx, 2, notificationIntent, 0);
        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(icon)
                            .setContent(remoteViews);

            notification = builder.build();
        }
        return notification;
    }

    private PendingIntent setAudioPlayerControls(Context ctx, int status) {
        String action = "";
        int request_code = 0;
        switch (status) {
            case AudioPlayerService.STATUS_STARTING:
                action = "PLAYER_START";
                request_code = 0;
                break;
            case AudioPlayerService.STATUS_PLAYING:
                action = "PLAYER_PLAY";
                request_code = 1;
                break;
            case AudioPlayerService.STATUS_PAUSED:
                action = "PLAYER_PAUSE";
                request_code = 2;
                break;
            case AudioPlayerService.STATUS_GOTO_PREVIOUS:
                action = "PLAYER_PREVIOUS";
                request_code = 3;
                break;
            case AudioPlayerService.STATUS_GOTO_NEXT:
                action = "PLAYER_NEXT";
                request_code = 4;
                break;
            default:
                action = "PLAYER_STOP";
                request_code = 5;
                break;
        }
        Intent serviceIntent = new Intent(ctx.getApplicationContext(), AudioPlayerService.class);
        serviceIntent.putExtra("action", action);
        return PendingIntent.getService(
                ctx.getApplicationContext(), request_code, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public PendingIntent createConversationIntent(int peer_id, String title) {
        Intent notificationIntent = new Intent(ctx, ConversationActivity.class);
        notificationIntent.putExtra("peer_id", peer_id);
        notificationIntent.putExtra("conv_title", title);
        notificationIntent.putExtra("online", 1);
        return PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    }

    public PendingIntent createAudioPlayerIntent(int track_position, ArrayList<Audio> audios) {
        Intent notificationIntent = new Intent(ctx, AppActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    }

    public void clearAudioPlayerNotification() {
        notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifMan != null) {
            notifMan.cancel(2501);
        }
    }
}
