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

package uk.openvk.android.legacy.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;

import uk.openvk.android.client.entities.Authorization;
import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.PhotoAlbum;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.AudioPlayerActivity;
import uk.openvk.android.legacy.core.activities.AuthActivity;
import uk.openvk.android.legacy.core.activities.ConversationActivity;
import uk.openvk.android.legacy.core.activities.GroupMembersActivity;
import uk.openvk.android.legacy.core.activities.NewPostActivity;
import uk.openvk.android.legacy.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.core.activities.WallPostActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkAuthActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.activities.intents.FriendsIntentActivity;
import uk.openvk.android.legacy.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.core.activities.intents.NotesIntentActivity;
import uk.openvk.android.legacy.core.activities.PhotoAlbumActivity;
import uk.openvk.android.legacy.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.core.activities.intents.VideosIntentActivity;

public class OvkAPIReceiver extends BroadcastReceiver {
    private Activity activity;
    public OvkAPIReceiver(Activity _activity) {
        activity = _activity;
    }

    public OvkAPIReceiver() {

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle data = intent.getExtras();
        if(data != null && data.containsKey("address")
                && data.getString("address").startsWith(activity.getLocalClassName())) {
            if (activity instanceof NetworkAuthActivity) {
                final NetworkAuthActivity netAuthActivity = (NetworkAuthActivity) activity;
                OpenVKAPI ovk_api = netAuthActivity.ovk_api;
                final Message msg = parseJSONData(ovk_api.wrapper, netAuthActivity.handler, data);
                ovk_api.wrapper.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(BuildConfig.DEBUG) {
                            Log.d(OpenVKAPI.TAG,
                                    String.format("Handling message %s in %s", msg.what, activity.getLocalClassName())
                            );
                        }
                        netAuthActivity.receiveState(msg.what, data);
                    }
                });
            } else if (activity instanceof NetworkFragmentActivity) {
                final NetworkFragmentActivity netFragmActivity = (NetworkFragmentActivity) activity;
                OpenVKAPI ovk_api = netFragmActivity.ovk_api;
                final Message msg = parseJSONData(ovk_api.wrapper, netFragmActivity.handler, data);
                ovk_api.wrapper.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(BuildConfig.DEBUG) {
                            Log.d(OpenVKAPI.TAG,
                                    String.format("Handling message %s in %s", msg.what, activity.getLocalClassName())
                            );
                        }
                        netFragmActivity.receiveState(msg.what, data);
                    }
                });
            } else if (activity instanceof NetworkActivity) {
                final NetworkActivity netActivity = (NetworkActivity) activity;
                OpenVKAPI ovk_api = netActivity.ovk_api;
                final Message msg = parseJSONData(ovk_api.wrapper, netActivity.handler, data);
                ovk_api.wrapper.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(BuildConfig.DEBUG) {
                            Log.d(OpenVKAPI.TAG,
                                    String.format("Handling message %s in %s", msg.what, activity.getLocalClassName())
                            );
                        }
                        netActivity.receiveState(msg.what, data);
                    }
                });
            }
        }
    }

    public Message parseJSONData(OvkAPIWrapper wrapper, Handler handler, Bundle data) {
        Message msg = new Message();
        String method = data.getString("method");
        String args = data.getString("args");
        String where = data.getString("where");
        msg.setData(data);
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        DownloadManager downloadManager = new DownloadManager(activity,
                wrapper.getClientInfo(), handler);

        downloadManager.setInstance(((OvkApplication) activity
                .getApplicationContext()).getCurrentInstance());
        if(activity instanceof NetworkFragmentActivity || activity instanceof NetworkActivity) {
            OpenVKAPI ovk_api;
            if(activity instanceof NetworkActivity) {
                NetworkActivity net_a = (NetworkActivity) activity;
                ovk_api = net_a.ovk_api;
            } else {
                NetworkFragmentActivity net_a = (NetworkFragmentActivity) activity;
                ovk_api = net_a.ovk_api;
            }

            assert method != null;
            switch (method) {
                case "Account.getProfileInfo":
                    ovk_api.account.parse(data.getString("response"), wrapper);
                    msg.what = HandlerMessages.ACCOUNT_PROFILE_INFO;
                    break;
                case "Account.getCounters":
                    ovk_api.account.parseCounters(data.getString("response"));
                    msg.what = HandlerMessages.ACCOUNT_COUNTERS;
                    break;
                case "Newsfeed.get":
                    if (where != null && where.equals("more_news")) {
                        msg.what = HandlerMessages.NEWSFEED_GET_MORE;
                        ovk_api.newsfeed.parse(activity,
                                downloadManager, data.getString("response"),
                                global_prefs.getString("photos_quality", ""), false);
                    } else {
                        msg.what = HandlerMessages.NEWSFEED_GET;
                        ovk_api.newsfeed.parse(activity,
                                downloadManager, data.getString("response"),
                                global_prefs.getString("photos_quality", ""), true);
                    }
                    break;
                case "Newsfeed.getGlobal":
                    if (where != null && where.equals("more_news")) {
                        msg.what = HandlerMessages.NEWSFEED_GET_MORE_GLOBAL;
                        ovk_api.newsfeed.parse(activity,
                                downloadManager, data.getString("response"),
                                global_prefs.getString("photos_quality", ""), false);
                    } else {
                        msg.what = HandlerMessages.NEWSFEED_GET_GLOBAL;
                        ovk_api.newsfeed.parse(activity,
                                downloadManager, data.getString("response"),
                                global_prefs.getString("photos_quality", ""), true);
                    }
                    break;
                case "Likes.add":
                    ovk_api.likes.parse(data.getString("response"));
                    msg.what = HandlerMessages.LIKES_ADD;
                    break;
                case "Likes.delete":
                    ovk_api.likes.parse(data.getString("response"));
                    msg.what = HandlerMessages.LIKES_DELETE;
                    break;
                case "Users.search":
                    ovk_api.users.parseSearch(data.getString("response"),
                            ovk_api.dlman);
                    msg.what = HandlerMessages.USERS_SEARCH;
                    break;
                case "Users.get":
                    ovk_api.users.parse(data.getString("response"));
                    msg.what = HandlerMessages.USERS_GET;
                    break;
                case "Friends.get":
                    if (args != null && args.contains("offset")) {
                        msg.what = HandlerMessages.FRIENDS_GET_MORE;
                        ovk_api.friends.parse(data.getString("response"),
                                downloadManager, true, false);
                    } else {
                        assert where != null;
                        if(where.equals("profile_counter")) {
                            msg.what = HandlerMessages.FRIENDS_GET_ALT;
                            ovk_api.friends.parse(data.getString("response"),
                                    downloadManager, false, true);
                        } else {
                            msg.what = HandlerMessages.FRIENDS_GET;
                            ovk_api.friends.parse(data.getString("response"),
                                    downloadManager, true, true);
                        }
                    }
                    break;
                case "Friends.getRequests":
                    msg.what = HandlerMessages.FRIENDS_REQUESTS;
                    ovk_api.friends.parseRequests(data.getString("response"),
                            downloadManager, true);
                    break;
                case "Photos.getAlbums":
                    msg.what = HandlerMessages.PHOTOS_GETALBUMS;
                    if (args != null && args.contains("offset")) {
                        ovk_api.photos.parseAlbums(data.getString("response"),
                                downloadManager, false);
                    } else {
                        assert where != null;
                        ovk_api.photos.parseAlbums(data.getString("response"),
                                downloadManager, true);
                    }
                    break;
                case "Video.get":
                    msg.what = HandlerMessages.VIDEOS_GET;
                    ovk_api.videos.parse(downloadManager, data.getString("response"));
                    break;
                case "Audio.get":
                    msg.what = HandlerMessages.AUDIOS_GET;
                    ovk_api.audios.parseAudioTracks(data.getString("response"), true);
                    break;
                case "Wall.get":
                    if(where != null && where.equals("more_wall_posts")) {
                        ovk_api.wall.parse(activity, downloadManager,
                                global_prefs.getString("photos_quality", ""),
                                data.getString("response"), false, true);
                        msg.what = HandlerMessages.WALL_GET_MORE;
                    } else {
                        ovk_api.wall.parse(activity, downloadManager,
                                global_prefs.getString("photos_quality", ""),
                                data.getString("response"), true, true);
                        msg.what = HandlerMessages.WALL_GET;
                    }
                    break;
                case "Messages.getConversations":
                    ovk_api.messages.parseConversationsList(
                            data.getString("response"),
                            downloadManager);
                    msg.what = HandlerMessages.MESSAGES_CONVERSATIONS;
                    break;
                case "Groups.get":
                    if (args != null && args.contains("offset")) {
                        msg.what = HandlerMessages.GROUPS_GET_MORE;
                        ovk_api.groups.parse(data.getString("response"),
                                downloadManager,
                                global_prefs.getString("photos_quality", ""),
                                true, false);
                    } else {
                        msg.what = HandlerMessages.GROUPS_GET;
                        ovk_api.groups.parse(data.getString("response"),
                                downloadManager,
                                global_prefs.getString("photos_quality", ""),
                                true, true);
                    }
                    break;
                case "Ovk.version":
                    msg.what = HandlerMessages.OVK_VERSION;
                    ovk_api.ovk.parseVersion(data.getString("response"));
                    break;
                case "Ovk.aboutInstance":
                    msg.what = HandlerMessages.OVK_ABOUTINSTANCE;
                    ovk_api.ovk.parseAboutInstance(data.getString("response"));
                    break;
                case "Notes.get":
                    msg.what = HandlerMessages.NOTES_GET;
                    ovk_api.notes.parse(data.getString("response"));
                    break;
                case "Groups.search":
                    ovk_api.groups.parseSearch(data.getString("response"),
                            ovk_api.dlman);
                    msg.what = HandlerMessages.GROUPS_SEARCH;
                    break;
                case "Audio.getLyrics":
                    msg.what = HandlerMessages.AUDIOS_GET_LYRICS;
                    ovk_api.audios.parseLyrics(data.getString("response"));
                    break;
            }
        }

        if (activity instanceof AuthActivity) {
            AuthActivity auth_a = (AuthActivity) activity;
            assert method != null;
            switch (method) {
                case "Account.getProfileInfo":
                    try {
                        auth_a.auth = new Authorization(data.getString("response"));
                        msg.what = HandlerMessages.ACCOUNT_PROFILE_INFO;
                    } catch (JSONException e) {
                        msg.what = HandlerMessages.INTERNAL_ERROR;
                    } catch (IllegalAccessException e) {
                        msg.what = HandlerMessages.INVALID_USERNAME_OR_PASSWORD;
                    }
                    break;
            }
        } else if (activity instanceof NetworkFragmentActivity) {
            NetworkFragmentActivity net_a = (NetworkFragmentActivity) activity;
            downloadManager = net_a.ovk_api.dlman;
            assert method != null;
            if(activity instanceof AppActivity) {
                AppActivity app_a = ((AppActivity) activity);
                switch (method) {
                    case "Messages.getLongPollServer":
                        app_a.longPollServer = app_a.ovk_api.messages
                                .parseLongPollServer(data.getString("response"));
                        msg.what = HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER;
                        break;
                }
            } else if(activity instanceof NotesIntentActivity) {
                if(method.equals("Notes.get")) {
                    msg.what = HandlerMessages.NOTES_GET;
                    net_a.ovk_api.notes.parse(data.getString("response"));
                }
            }
        } else if (activity instanceof NetworkActivity) {
            NetworkActivity net_a = (NetworkActivity) activity;
            assert method != null;
            if(activity instanceof ConversationActivity) {
                ConversationActivity conv_a = ((ConversationActivity) activity);
                switch (method) {
                    case "Messages.getHistory":
                        conv_a.history = conv_a.conversation.parseHistory(activity, data.getString("response"));
                        msg.what = HandlerMessages.MESSAGES_GET_HISTORY;
                        break;
                    case "Messages.send":
                        msg.what = HandlerMessages.MESSAGES_SEND;
                        break;
                    case "Messages.delete":
                        msg.what = HandlerMessages.MESSAGES_DELETE;
                        break;
                }
            } else if(activity instanceof NewPostActivity) {
                if(method.equals("Wall.post")) {
                    msg.what = HandlerMessages.WALL_POST;
                } else if(method.startsWith("Photos.get") && method.endsWith("Server")) {
                    net_a.ovk_api.photos.parseUploadServer(data.getString("response"), method);
                    msg.what = HandlerMessages.PHOTOS_UPLOAD_SERVER;
                } else if(method.startsWith("Photos.save")) {
                    msg.what = HandlerMessages.PHOTOS_SAVE;
                    net_a.ovk_api.photos.parseOnePhoto(data.getString("response"));
                }
            } else if(activity instanceof PhotoAlbumActivity) {
                PhotoAlbumActivity album_a = ((PhotoAlbumActivity) activity);
                downloadManager = album_a.ovk_api.dlman;
                switch (method) {
                    case "Photos.getAlbums":
                        msg.what = HandlerMessages.PHOTOS_GETALBUMS;
                        if (args != null && args.contains("offset")) {
                            album_a.ovk_api.photos.parseAlbums(data.getString("response"),
                                    downloadManager, false);
                        } else {
                            assert where != null;
                            album_a.ovk_api.photos.parseAlbums(data.getString("response"),
                                    downloadManager, true);
                        }
                        break;
                    case "Photos.get":
                        msg.what = HandlerMessages.PHOTOS_GET;
                        album_a.ovk_api.photos.parse(
                                data.getString("response"),
                                new PhotoAlbum(Long.parseLong(album_a.ids[1]), Long.parseLong(album_a.ids[0])),
                                downloadManager
                        );
                        break;
                }
            }
        } else if(activity instanceof GroupMembersActivity) {
            GroupMembersActivity group_members_a = ((GroupMembersActivity) activity);
            downloadManager = group_members_a.ovk_api.dlman;
            assert method != null;
            switch (method) {
                case "Groups.getMembers":
                    group_members_a.group.members = new ArrayList<>();
                    group_members_a.group.parseMembers(data.getString("response"),
                            downloadManager, true);
                    msg.what = HandlerMessages.GROUP_MEMBERS;
                    break;
            }
        }
        return msg;
    }
}
