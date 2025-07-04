package com.sohaibazmi.remote;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TelegramWorker extends Worker {

    public TelegramWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (RemoteService.isInternetAvailable(getApplicationContext())) {
            String deviceId = RemoteService.getDeviceId(getApplicationContext());
            String message = "Device Id: " + deviceId + " is active";
            RemoteService.sendTelegramText(message);
        }
        return Result.success();
    }
}
