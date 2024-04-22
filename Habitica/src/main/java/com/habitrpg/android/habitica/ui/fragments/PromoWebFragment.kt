package com.habitrpg.android.habitica.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import com.habitrpg.android.habitica.databinding.FragmentNewsBinding
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromoWebFragment : BaseMainFragment<FragmentNewsBinding>() {
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentNewsBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentNewsBinding {
        return FragmentNewsBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val webSettings = binding?.newsWebview?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        binding?.newsWebview?.webChromeClient =
            object : WebChromeClient() {
            }
        arguments?.let {
            val args = PromoWebFragmentArgs.fromBundle(it)
            var url = args.url
            url = url.replace("USER_ID", userViewModel.userID)
            binding?.newsWebview?.loadUrl(url)
        }
    }
}
