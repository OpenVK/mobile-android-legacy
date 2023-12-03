package uk.openvk.android.legacy.utils;

import android.content.Context;
import android.content.Intent;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import uk.openvk.android.legacy.ui.core.activities.CrashReporterActivity;

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

 /**
    <p>About our usage of ACRA in application see
            <a href="https://github.com/openvk/mobile-android-legacy/issues/153">issue #153</a>.
    </p>

 */
public class ACRACustomSender implements ReportSender {
    @Override
    public void send(Context context, CrashReportData errorContent) {
        Intent intent = new Intent(context.getApplicationContext(), CrashReporterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(intent);
    }
}
