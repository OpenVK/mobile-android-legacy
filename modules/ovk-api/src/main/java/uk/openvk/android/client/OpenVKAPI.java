/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;

import uk.openvk.android.client.entities.Account;
import uk.openvk.android.client.entities.Ovk;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.client.models.Audios;
import uk.openvk.android.client.models.Friends;
import uk.openvk.android.client.models.Groups;
import uk.openvk.android.client.models.Likes;
import uk.openvk.android.client.models.Messages;
import uk.openvk.android.client.models.Newsfeed;
import uk.openvk.android.client.models.Notes;
import uk.openvk.android.client.models.Photos;
import uk.openvk.android.client.models.Users;
import uk.openvk.android.client.models.Videos;
import uk.openvk.android.client.models.Wall;
import uk.openvk.android.client.wrappers.DownloadManager;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;
import uk.openvk.android.client.wrappers.UploadManager;

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
    public Audios audios;

    public static final String TAG = "OVK-API";
    public static final String DLM_TAG = "OVK-DLM";
    public static final String ULM_TAG = "OVK-ULM";
    public static final String LP_TAG = "OVK-LP";

    public OpenVKAPI(Context ctx, HashMap<String, Object> client_info,
                     android.os.Handler handler) {
        wrapper = new OvkAPIWrapper(ctx, client_info, handler);
        wrapper.requireHTTPS((boolean) client_info.get("useHTTPS"));
        wrapper.setProxyConnection(
                (boolean) client_info.get("useProxy"),
                (String) client_info.get("proxyType"),
                (String) client_info.get("proxyAddress")
        );
        if(client_info.containsKey("server")) {
            wrapper.setServer((String) client_info.get("server"));
            wrapper.setAccessToken((String) client_info.get("accessToken"));
        }
        dlman = new DownloadManager(ctx, client_info, handler);
        if(client_info.containsKey("server")) {
            dlman.setInstance((String) client_info.get("server"));
        }
        dlman.setProxyConnection(
                (boolean) client_info.get("useProxy"),
                (String) client_info.get("proxyType"),
                (String) client_info.get("proxyAddress")
        );
        dlman.setForceCaching((boolean) client_info.get("forcedCaching"));
        ulman = new UploadManager(ctx, client_info, handler);
        ulman.setProxyConnection(
                (boolean) client_info.get("useProxy"),
                (String) client_info.get("proxyAddress")
        );
        if(client_info.containsKey("server")) {
            ulman.setInstance((String) client_info.get("server"));
        }
        ulman.setForceCaching((boolean) client_info.get("forcedCaching"));
        account = new Account(ctx);
        if(client_info.containsKey("server") && client_info.containsKey("accessToken")) {
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
        audios = new Audios();
        ovk = new Ovk();
    }
}
