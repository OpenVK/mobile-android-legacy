package uk.openvk.android.legacy.api.interfaces;

import android.content.Context;
import android.os.Bundle;

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
 **/

public class OvkAPIListeners {
    public String from;

    public static interface OnAPIProcessListener {
        public void onAPIProcess(Context ctx, Bundle data, long value, long length);
    }

    public static interface OnAPISuccessListener {
        public void onAPISuccess(Context ctx, int msg_code, Bundle data);
    }

    public static interface OnAPIFailListener {
        public void onAPIFailed(Context ctx, int msg_code, Bundle data);
    }

    public OnAPIProcessListener processListener;
    public OnAPIFailListener failListener;
    public OnAPISuccessListener successListener;

    public OvkAPIListeners() {
        processListener = new OnAPIProcessListener() {
            @Override
            public void onAPIProcess(Context ctx, Bundle data, long value, long length) {

            }
        };
        failListener = new OnAPIFailListener() {
            @Override
            public void onAPIFailed(Context ctx, int http_code, Bundle data) {

            }
        };
        successListener = new OnAPISuccessListener() {
            @Override
            public void onAPISuccess(Context ctx, int http_code, Bundle data) {

            }
        };
    }
}

