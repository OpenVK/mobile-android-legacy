/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.LongPollUpdate;
import uk.openvk.android.client.models.LongPollServer;
import uk.openvk.android.client.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.utils.NotificationManager;

public class LongPollReceiver extends BroadcastReceiver {

    static final String TAG = "LPReceiver";

    private static LongPollReceiver receiver;
    private OpenVKAPI ovk_api;

    public LongPollReceiver(OpenVKAPI ovk_api) {
        this.ovk_api = ovk_api;
        receiver = this;
    }

    public LongPollReceiver() {
        receiver = this;
    }

    public static LongPollReceiver getInstance() {
        if(receiver == null) {
            receiver = new LongPollReceiver(null);
        }
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);

        NotificationManager notifMan = ((OvkApplication) context.getApplicationContext()).notifMan;

        if(intent.getExtras() != null) {
            Bundle data = intent.getExtras();

            if (!data.containsKey("response"))
                return;

            String response = data.getString("response");

            if (ovk_api != null) {
                LongPollServer server = ovk_api.messages.getLongPollServer();
                ArrayList<LongPollUpdate> updates = server.parseUpdates(response);

                for (int i = 0; i < updates.size(); i++) {
                    notifMan.createNewMessageNotification(updates.get(i));
                }
            }
        }
    }

}
