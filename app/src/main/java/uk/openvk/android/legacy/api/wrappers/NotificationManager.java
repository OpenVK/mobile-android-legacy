package uk.openvk.android.legacy.api.wrappers;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.longpoll_api.MessageEvent;
import uk.openvk.android.legacy.ui.core.activities.ConversationActivity;

/** OPENVK LEGACY LICENSE NOTIFICATION
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
    private NotificationChannel notifChannel;
    private Context ctx;
    public boolean ledIndicate;
    public boolean vibrate;
    public boolean playSound;

    public NotificationManager(Context ctx, boolean ledIndicate, boolean vibrate, boolean playSound, String ringtone_url) {
        this.ctx = ctx;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifMan = ctx.getSystemService(android.app.NotificationManager.class);
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            notifChannel = new NotificationChannel("lp_updates", "LongPoll Updates", importance);
            notifChannel.enableLights(ledIndicate);
            notifChannel.enableVibration(vibrate);
            if(playSound) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                if(!ringtone_url.equals("content://settings/system/notification_sound")) {
                    notifChannel.setSound(Uri.parse(ringtone_url), audioAttributes);
                } else {
                    notifChannel.setSound(null, null);
                }
            }
            notifMan.createNotificationChannel(notifChannel);
        } else {
            notifMan = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
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
                Notification notification = createNotification(notifMan, R.drawable.ic_stat_notify,
                        msg_author, msg_event.msg_text);
                notification.contentIntent = createConversationIntent(msg_event.peer_id, msg_author);
                notifMan.notify(notification_id, notification);
            }
        }
    }

    public boolean isRepeat(String last_longpoll_response, String response) {
        try {
            if (last_longpoll_response.equals(response)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public Notification createNotification(android.app.NotificationManager notifMan, int icon,
                                           String title, String description) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder =
                    new Notification.Builder(ctx)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description)
                            .setChannelId("lp_updates");
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

    public PendingIntent createConversationIntent(int peer_id, String title) {
        Intent notificationIntent = new Intent(ctx, ConversationActivity.class);
        notificationIntent.putExtra("peer_id", peer_id);
        notificationIntent.putExtra("conv_title", title);
        notificationIntent.putExtra("online", 1);
        return PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
    }
}
