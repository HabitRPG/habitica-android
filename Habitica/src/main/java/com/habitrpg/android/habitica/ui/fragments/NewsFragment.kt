package com.habitrpg.android.habitica.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentNewsBinding
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import kotlinx.coroutines.launch

class NewsFragment : BaseMainFragment<FragmentNewsBinding>() {

    override var binding: FragmentNewsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewsBinding {
        return FragmentNewsBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private val webviewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url?.contains("/static/new-stuff") == true) {
                view?.loadUrl(url)
            } else if (url != null) {
                    MainNavigationController.navigate(url)
            }
            return true
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.url?.path == "/static/new-stuff") {
                view?.loadUrl(request.url.toString())
            } else {
                request?.url?.let { MainNavigationController.navigate(it) }
            }
            return true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = context?.getString(R.string.base_url)
        val webSettings = binding?.newsWebview?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        binding?.newsWebview?.webViewClient = webviewClient
        binding?.newsWebview?.webChromeClient = object : WebChromeClient() {
        }
        binding?.newsWebview?.loadUrl("$address/static/new-stuff")
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser("flags.newStuff", false)
        }
    }
}
