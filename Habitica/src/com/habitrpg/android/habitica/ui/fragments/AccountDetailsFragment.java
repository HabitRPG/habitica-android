package com.habitrpg.android.habitica.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;

import java.util.Map;

/**
 * Created by franzejr on 28/11/15.
 */
public class AccountDetailsFragment extends PreferenceFragment {
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        addPreferencesFromResource(R.xml.preferences_account_details);
        setupPreferences();
    }

    private void setupPreferences() {
        for(Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet() ){

            String usernamePreference = context.getResources().getString(R.string.SP_username);
            String emailPreference = context.getResources().getString(R.string.SP_email);
            String apiTokenPreference = context.getResources().getString(R.string.SP_APIToken);
            String userIdPreference = context.getResources().getString(R.string.SP_userID);

            if(preference.getKey().equals(usernamePreference)){
                findPreference(usernamePreference).setSummary(preference.getValue().toString());
            }else if(preference.getKey().equals(emailPreference)){
                findPreference(emailPreference).setSummary(preference.getValue().toString());
            }else if(preference.getKey().equals(apiTokenPreference)){
                findPreference(apiTokenPreference).setSummary(preference.getValue().toString());
            }else if(preference.getKey().equals(userIdPreference)){
                findPreference(userIdPreference).setSummary(preference.getValue().toString());
            }
        }
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("logout")) {
            HabiticaApplication.logout(context);
        }
        return false;
    }
}
