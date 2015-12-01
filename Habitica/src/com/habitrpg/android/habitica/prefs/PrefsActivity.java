package com.habitrpg.android.habitica.prefs;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.PreferencesFragment;

public class PrefsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
        }
    }

    // TODO:
    // This method should be moved to HabiticaApplication
    public static HostConfig fromContext(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        HostConfig config;
        String httpPort = "80";
        String address = prefs.getString(ctx.getString(R.string.SP_address), ctx.getString(R.string.SP_address_default));
        if (address.contains("http://habitrpg.com")) {
            address = "https://habitrpg.com";
            prefs.edit().putString(ctx.getString(R.string.SP_address), address).commit();
        } else if (address.contains("http://beta.habitrpg.com")) {
            address = "https://beta.habitrpg.com/";
            prefs.edit().putString(ctx.getString(R.string.SP_address), address).commit();

        }
        if (address == null || address == "" || address.length() < 2) {
            config = null;
        } else {
            String api = prefs.getString(ctx.getString(R.string.SP_APIToken), null);
            String userID = prefs.getString(ctx.getString(R.string.SP_userID), null);
            config = new HostConfig(address, httpPort, api, userID);
        }
        return config;
    }
}

