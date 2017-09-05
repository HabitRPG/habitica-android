package com.habitrpg.android.habitica.ui.fragments.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.ui.SpeechBubbleView;
import com.habitrpg.android.habitica.ui.fragments.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeFragment extends BaseFragment {

    @BindView(R.id.speech_bubble)
    SpeechBubbleView speechBubbleView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_welcome, container, false);

        unbinder = ButterKnife.bind(this, v);

        speechBubbleView.animateText(getContext().getString(R.string.welcome_text));

        return v;
    }

    @Override
    public void injectFragment(AppComponent component) {}
}
