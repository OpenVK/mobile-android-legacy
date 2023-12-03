package uk.openvk.android.legacy.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.logging.Handler;

import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Ovk;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.api.models.Friends;
import uk.openvk.android.legacy.api.models.Groups;
import uk.openvk.android.legacy.api.models.Likes;
import uk.openvk.android.legacy.api.models.Messages;
import uk.openvk.android.legacy.api.models.Newsfeed;
import uk.openvk.android.legacy.api.models.Notes;
import uk.openvk.android.legacy.api.models.Photos;
import uk.openvk.android.legacy.api.models.Users;
import uk.openvk.android.legacy.api.models.Videos;
import uk.openvk.android.legacy.api.models.Wall;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.api.wrappers.UploadManager;

/* OPENVK LEGACY LICENSE NOTIFICATION
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
 * Source code: https://github.com/openvk/mobile-android-legacy */

public class OpenVKAPI {
    public Account account;
    public Newsfeed newsfeed;
    public Messages messages;
    public Users users;
    public Groups groups;
    public Friends friends;
    public Wall wall;
    public User user;
    public Likes likes;
    public Notes notes;
    public Photos photos;
    public Videos videos;
    public Ovk ovk;
    public OvkAPIWrapper wrapper;
    public DownloadManager dlman;
    public UploadManager ulman;

    public OpenVKAPI(Context ctx, SharedPreferences global_prefs, SharedPreferences instance_prefs,
                     android.os.Handler handler) {
        wrapper = new OvkAPIWrapper(ctx, global_prefs.getBoolean("useHTTPS", true),
                global_prefs.getBoolean("legacyHttpClient", false), handler);
        wrapper.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        if(instance_prefs != null && instance_prefs.contains("server")) {
            wrapper.setServer(instance_prefs.getString("server", ""));
            wrapper.setAccessToken(instance_prefs.getString("access_token", ""));
        }
        dlman = new DownloadManager(ctx, global_prefs.getBoolean("useHTTPS", true),
                global_prefs.getBoolean("legacyHttpClient", false), handler);
        if(instance_prefs != null && instance_prefs.contains("server")) {
            dlman.setInstance(instance_prefs.getString("server", ""));
        }
        dlman.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        dlman.setForceCaching(global_prefs.getBoolean("forcedCaching", true));
        ulman = new UploadManager(ctx, global_prefs.getBoolean("useHTTPS", true),
                global_prefs.getBoolean("legacyHttpClient", false), handler);
        ulman.setProxyConnection(global_prefs.getBoolean("useProxy", false),
                global_prefs.getString("proxy_address", ""));
        if(instance_prefs != null && instance_prefs.contains("server")) {
            ulman.setInstance(instance_prefs.getString("server", ""));
        }
        ulman.setForceCaching(global_prefs.getBoolean("forcedCaching", true));
        account = new Account(ctx);
        if(instance_prefs != null && instance_prefs.contains("server")) {
            account.getProfileInfo(wrapper);
        }
        newsfeed = new Newsfeed();
        user = new User();
        likes = new Likes();
        messages = new Messages();
        users = new Users();
        friends = new Friends();
        groups = new Groups();
        wall = new Wall();
        notes = new Notes();
        photos = new Photos();
        videos = new Videos();
        ovk = new Ovk();
    }
}
