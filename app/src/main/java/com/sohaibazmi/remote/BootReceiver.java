package com.sohaibazmi.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("REMOTE_CMD", "🔁 Boot completed, restarting service...");
            context.startForegroundService(new Intent(context, RemoteService.class));
        }
    }
}
