package com.habitrpg.android.habitica.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;

public class PrefsActivity extends AppCompatActivity {


    public static class SettingsFragment extends PreferenceFragment {
        private Context context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = this.getActivity();
            addPreferencesFromResource(R.xml.activity_preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getKey().equals("logout")) {
                HabiticaApplication.logout(context);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
        }

        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

