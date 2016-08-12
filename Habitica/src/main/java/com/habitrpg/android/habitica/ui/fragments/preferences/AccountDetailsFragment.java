package com.habitrpg.android.habitica.ui.fragments.preferences;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.QrCodeManager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AccountDetailsFragment extends BasePreferencesFragment {

    private QrCodeManager qrCodeManager;

    @Override
    protected void setupPreferences() {
        for (Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet()) {
            String key = preference.getKey();
            if (getAccountDetailsPreferences().contains(key) && preference.getValue() != null) {
                findPreference(key).setSummary(preference.getValue().toString());
            }
        }

        qrCodeManager = new QrCodeManager(this.getContext());
    }

    protected List<String> getAccountDetailsPreferences() {
        return Arrays.asList(getString(R.string.SP_username), getString(R.string.SP_email),
                getString(R.string.SP_APIToken), getString(R.string.SP_userID), getString(R.string.SP_user_qr_code));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        Log.v("keith",  preference.getKey());
        if (preference.getKey().equals(getString(R.string.SP_user_qr_code))) {
            qrCodeManager.showDialogue();
        }

        ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipMan.setPrimaryClip(ClipData.newPlainText(preference.getKey(), preference.getSummary()));
        Toast.makeText(getActivity(), "Copied " + preference.getKey() + " to clipboard.", Toast.LENGTH_SHORT).show();
        return super.onPreferenceTreeClick(preference);
    }
}
