package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.extensions.inflate
import kotlinx.android.synthetic.main.fragment_news.*


class NewsFragment : BaseMainFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = container?.inflate(R.layout.fragment_news)
        hideToolbar()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = if (BuildConfig.DEBUG) BuildConfig.BASE_URL else context?.getString(R.string.base_url)
        val webSettings = newsWebview.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        newsWebview.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                return super.onConsoleMessage(consoleMessage)
            }
        }
        newsWebview.loadUrl("$address/static/new-stuff")
    }

    override fun onDestroyView() {
        showToolbar()
        super.onDestroyView()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }


}
