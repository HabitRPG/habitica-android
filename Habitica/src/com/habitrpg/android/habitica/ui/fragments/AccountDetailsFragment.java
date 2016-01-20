package com.habitrpg.android.habitica.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.habitrpg.android.habitica.R;

import java.util.Arrays;
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

        String[] accountDetailsPreferences = {
                context.getResources().getString(R.string.SP_username),
                context.getResources().getString(R.string.SP_email),
                context.getResources().getString(R.string.SP_APIToken),
                context.getResources().getString(R.string.SP_userID)
        };

        for(Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet() ){
            String key = preference.getKey();
            if (Arrays.asList(accountDetailsPreferences).contains(key)) {
               findPreference(key).setSummary(preference.getValue().toString());
            }
        }
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipMan.setPrimaryClip(ClipData.newPlainText(preference.getKey(), preference.getSummary()));
        Toast.makeText(getActivity(), "Copied " + preference.getKey() + " to clipboard.", Toast.LENGTH_SHORT).show();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
