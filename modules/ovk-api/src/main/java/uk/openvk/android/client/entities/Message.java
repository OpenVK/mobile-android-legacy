/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.client.entities;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.wrappers.JSONParser;

public class Message extends LazyEntity {
    public boolean isIncoming;
    public String timestamp;
    public long timestamp_long;
    public String text;
    public boolean sending;
    public LazyEntity author;
    public ChatAction action;
    private JSONParser parser;
    public boolean isError;

    public Message(int type) {
        super(type);
    }

    @SuppressLint("SimpleDateFormat")
    public Message(long id, boolean incoming, long timestamp, String text) {
        super(LazyEntity.REAL_ENTITY);
        this.id = id;
        isIncoming = incoming;
        this.text = text;
        timestamp_long = timestamp;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(timestamp));
        this.timestamp = new SimpleDateFormat("HH:mm").format(dt);
    }

    public Message() {
        super(LazyEntity.SLEEPING_ENTITY);
    }

    public void getSendedId(String response) {
        parser = new JSONParser();
        try {
            JSONObject json = parser.parseJSON(response);
            if (json != null) {
                id = json.getLong("response");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseAction(JSONObject actionJson) {
        try {
            switch (actionJson.getString("type")) {
                case "chat_create":
                    action = new ChatAction(ChatAction.ACTION_CHAT_CREATE);
                    break;
                case "chat_photo_update":
                    action = new ChatAction(ChatAction.ACTION_CHAT_PHOTO_UPDATE);
                    break;
                case "chat_invite_user_by_link":
                    action = new ChatAction(ChatAction.ACTION_INVITE_USER_BY_LINK);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
