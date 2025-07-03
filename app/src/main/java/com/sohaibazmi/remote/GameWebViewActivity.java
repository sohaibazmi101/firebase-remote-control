package com.sohaibazmi.remote;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class GameWebViewActivity extends AppCompatActivity {

    private WebView gameWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_webview);

        String gameUrl = getIntent().getStringExtra("url");

        gameWebView = findViewById(R.id.gameWebView);
        WebSettings webSettings = gameWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        gameWebView.setWebViewClient(new WebViewClient());
        gameWebView.loadUrl(gameUrl);
    }

    @Override
    public void onBackPressed() {
        if (gameWebView.canGoBack()) {
            gameWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
