package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFragment extends BaseMainFragment {

    @BindView(R.id.news_webview)
    WebView newsWebview;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_news, container, false);

        unbinder = ButterKnife.bind(this, view);
        String address = BuildConfig.DEBUG ? BuildConfig.BASE_URL : getContext().getString(R.string.base_url);

        newsWebview.loadUrl(address + "/static/new-stuff");

        return view;
    }

    @Override
    public void injectFragment(AppComponent component) {
        component.inject(this);
    }


    @Override
    public String customTitle() {
        return getString(R.string.sidebar_news);
    }
}
