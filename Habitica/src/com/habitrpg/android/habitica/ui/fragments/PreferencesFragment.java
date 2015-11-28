package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;

/**
 * Created by franzejr on 28/11/15.
 */
public class PreferencesFragment extends PreferenceFragment {
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        addPreferencesFromResource(R.xml.preferences_fragment);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("logout")) {
            HabiticaApplication.logout(context);
        }else if(preference.getKey().equals("accountDetails")) {
            openAccountDetailsFragment();
        }
        return false;
    }

    private void openAccountDetailsFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AccountDetailsFragment())
                .commit();
    }
}
