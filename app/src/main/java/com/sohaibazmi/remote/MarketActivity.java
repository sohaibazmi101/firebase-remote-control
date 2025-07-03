package com.sohaibazmi.remote;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MarketActivity extends AppCompatActivity {

    private WebView marketWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        marketWebView = findViewById(R.id.marketWebView);

        WebSettings webSettings = marketWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        marketWebView.setWebViewClient(new WebViewClient());

        // Load live market website
        marketWebView.loadUrl("https://www.moneycontrol.com/markets/indian-indices/");
    }

    @Override
    public void onBackPressed() {
        if (marketWebView.canGoBack()) {
            marketWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
