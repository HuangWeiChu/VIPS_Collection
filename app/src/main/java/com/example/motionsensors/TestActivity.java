package com.example.motionsensors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TestActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String responseHtml = MainActivity.myResponse;
        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.loadData(responseHtml, "text/HTML", "UTF-8");
        setContentView(webView);
    }
}


