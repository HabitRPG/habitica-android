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
import com.habitrpg.android.habitica.databinding.FragmentNewsBinding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler

class NewsFragment : BaseMainFragment<FragmentNewsBinding>() {

    override var binding: FragmentNewsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewsBinding {
        return FragmentNewsBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = if (BuildConfig.DEBUG) BuildConfig.BASE_URL else context?.getString(R.string.base_url)
        val webSettings = binding?.newsWebview?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        binding?.newsWebview?.webChromeClient = object : WebChromeClient() {
        }
        binding?.newsWebview?.loadUrl("$address/static/new-stuff")
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        compositeSubscription.add(userRepository.updateUser("flags.newStuff", false).subscribeWithErrorHandler({}))
    }
}
