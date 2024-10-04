package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityWebViewBinding

class WebViewActivity: BaseActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun getLayoutResId(): Int {
        return R.layout.activity_web_view
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(findViewById(R.id.toolbar))
        title = ""

        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        binding.webView.loadUrl(intent.getStringExtra("url") ?: "")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
