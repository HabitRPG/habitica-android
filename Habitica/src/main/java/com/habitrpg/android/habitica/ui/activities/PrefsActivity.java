package com.habitrpg.android.habitica.ui.activities;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.fragments.AccountDetailsFragment;
import com.habitrpg.android.habitica.ui.fragments.PreferencesFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;

public class PrefsActivity extends BaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_prefs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new PreferencesFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            onBackPressed();
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragment,
                                           PreferenceScreen preferenceScreen) {
        PreferenceFragmentCompat fragment = createNextPage(preferenceScreen);
        if (fragment != null) {
            Bundle arguments = new Bundle();
            arguments.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    private PreferenceFragmentCompat createNextPage(PreferenceScreen preferenceScreen) {
        PreferenceFragmentCompat fragment = null;
        if (preferenceScreen.getKey().equals("accountDetails")) {
            fragment = new AccountDetailsFragment();
        }
        return fragment;
    }

    // TODO:
    // This method should be moved to HabiticaApplication
    public static HostConfig fromContext(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        HostConfig config;
        String httpPort = BuildConfig.PORT;
        String address = BuildConfig.DEBUG ? BuildConfig.BASE_URL : ctx.getString(R.string.base_url);
        String api = prefs.getString(ctx.getString(R.string.SP_APIToken), null);
        String userID = prefs.getString(ctx.getString(R.string.SP_userID), null);
        config = new HostConfig(address, httpPort, api, userID);
        return config;
    }
}

