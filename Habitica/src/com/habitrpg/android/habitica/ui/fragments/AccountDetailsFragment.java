package com.habitrpg.android.habitica.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.widget.Toast;

import com.habitrpg.android.habitica.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by franzejr on 28/11/15.
 */
public class AccountDetailsFragment extends BasePreferencesFragment {

    @Override
    protected void setupPreferences() {
        for (Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet()) {
            String key = preference.getKey();
            if (getAccountDetailsPreferences().contains(key)) {
                findPreference(key).setSummary(preference.getValue().toString());
            }
        }
    }

    protected List<String> getAccountDetailsPreferences() {
        return Arrays.asList(getString(R.string.SP_username), getString(R.string.SP_email),
                getString(R.string.SP_APIToken), getString(R.string.SP_userID));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipMan.setPrimaryClip(ClipData.newPlainText(preference.getKey(), preference.getSummary()));
        Toast.makeText(getActivity(), "Copied " + preference.getKey() + " to clipboard.", Toast.LENGTH_SHORT).show();
        return true;
    }
}
