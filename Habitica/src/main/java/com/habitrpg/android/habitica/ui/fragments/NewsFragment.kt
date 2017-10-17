package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent

import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment : BaseMainFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater?.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = if (BuildConfig.DEBUG) BuildConfig.BASE_URL else context.getString(R.string.base_url)
        newsWebview.loadUrl(address + "/static/new-stuff")
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }


    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_news)
    }
}
