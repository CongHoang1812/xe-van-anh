package com.example.authenticateuserandpass.ui.webview

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val webView = findViewById<WebView>(R.id.webview)

        // Cho phép chạy JavaScript
        webView.settings.javaScriptEnabled = true

        // Đảm bảo mở link trong WebView thay vì Chrome
        webView.webViewClient = WebViewClient()
        var url = intent.getStringExtra("webview")
        // Load trang web
        webView.loadUrl(url!!)
    }
}