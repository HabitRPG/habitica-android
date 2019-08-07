package com.habitrpg.android.habitica.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_news.*


class NewsFragment : BaseMainFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_news)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = if (BuildConfig.DEBUG) BuildConfig.BASE_URL else context?.getString(R.string.base_url)
        val webSettings = newsWebview.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        newsWebview.webChromeClient = object : WebChromeClient() {
        }
        newsWebview.loadUrl("$address/static/new-stuff")
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        compositeSubscription.add(userRepository.updateUser(user, "flags.newStuff", false).subscribeWithErrorHandler(Consumer {}))
    }
}
