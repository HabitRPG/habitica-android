package com.habitrpg.android.habitica.ui.fragments.preferences;

import com.habitrpg.android.habitica.R;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AccountDetailsFragment extends BasePreferencesFragment {

    @Override
    protected void setupPreferences() {
        for (Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet()) {
            String key = preference.getKey();
            if (getAccountDetailsPreferences().contains(key) && preference.getValue() != null) {
                findPreference(key).setSummary(preference.getValue().toString());
            }
        }

        displayQRCode();
    }

    protected void displayQRCode() {
        Bitmap myBitmap = QRCode.from(getString(R.string.SP_userID)).bitmap();
        ImageView myImage = (ImageView) getActivity().findViewById(R.id.imageView);
        myImage.setVisibility(View.VISIBLE);
        myImage.setImageBitmap(myBitmap);
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
        return super.onPreferenceTreeClick(preference);
    }
}
