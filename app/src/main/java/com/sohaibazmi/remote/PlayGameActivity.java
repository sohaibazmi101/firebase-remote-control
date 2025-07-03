package com.sohaibazmi.remote;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        WebView webView = findViewById(R.id.gameWebView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String gameUrl = getIntent().getStringExtra("game_url");
        if (gameUrl != null) {
            webView.loadUrl(gameUrl);
        }
    }
}
