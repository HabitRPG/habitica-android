package com.habitrpg.android.habitica.prefs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PrefsActivity extends PreferenceActivity {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     *
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (mHasHeaders != null && mLoadHeaders != null) {
            try {
                return (Boolean) mHasHeaders.invoke(this);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle aSavedState) {
        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(aSavedState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.app_prefs_cat1);
            addPreferencesFromResource(R.xml.app_prefs_cat2);
            this.findPreference(this.getString(R.string.SP_last_seen_version)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(PrefsActivity.this);
                    //_ChangelogDialog.show( getSupportFragmentManager());
                    return false;
                }
            });


        } else {
            if (this.getActionBar() != null) {
                this.getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this, new Object[]{R.xml.pref_headers, aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
                    "xml", anAct.getPackageName());
            addPreferencesFromResource(thePrefRes);
        }
    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
