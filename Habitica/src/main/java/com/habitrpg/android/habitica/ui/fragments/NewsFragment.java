package com.habitrpg.android.habitica.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFragment extends BaseMainFragment {

    private View view;

    @BindView(R.id.news_webview)
    WebView newsWebview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_news, container, false);

        ButterKnife.bind(this, view);

        newsWebview.loadUrl(BuildConfig.BASE_URL + "/static/new-stuff");

        return view;
    }

}
