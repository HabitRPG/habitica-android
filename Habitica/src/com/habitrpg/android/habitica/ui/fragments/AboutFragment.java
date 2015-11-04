package com.habitrpg.android.habitica.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habitrpg.android.habitica.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by franzejr on 04/11/15.
 */
public class AboutFragment extends Fragment {

    private String androidSourceCodeLink = "https://github.com/HabitRPG/habitrpg-android/";
    private String twitterLink = "https://twitter.com/habitica";

    @OnClick(R.id.sourceCodeLink)
    public void openSourceCodePage(){
        openBrowserLink(androidSourceCodeLink);
    }

    @OnClick(R.id.twitter)
    public void openTwitterPage(){
        openBrowserLink(twitterLink);
    }

    @OnClick(R.id.giveUsFeedback)
    public void sendEmailAction(){
        sendEmail();
    }


    @OnClick(R.id.googlePlayStoreButton)
    public void openGooglePlay(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.example.android"));
        startActivity(intent);
    }

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (view == null)
            view = inflater.inflate(R.layout.fragment_about, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
    }

    private void openBrowserLink(String url){
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mobile@habitica.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Habitica Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "MESSAGE");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
