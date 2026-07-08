package uk.openvk.android.legacy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import uk.openvk.android.legacy.core.activities.settings.DebugMenuActivity;

public class DebugMenuReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, DebugMenuActivity.class);
        context.startActivity(i);
    }
}
