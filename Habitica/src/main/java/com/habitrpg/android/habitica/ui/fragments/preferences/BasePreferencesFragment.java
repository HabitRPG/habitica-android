package com.habitrpg.android.habitica.ui.fragments.preferences;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.habitrpg.android.habitica.R;

public abstract class BasePreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_fragment, rootKey);
        setupPreferences();
    }

    protected abstract void setupPreferences();

}
