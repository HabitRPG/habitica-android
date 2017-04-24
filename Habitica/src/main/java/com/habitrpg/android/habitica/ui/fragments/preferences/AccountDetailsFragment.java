package com.habitrpg.android.habitica.ui.fragments.preferences;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.widget.Toast;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.helpers.QrCodeManager;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.SubscriptionPlan;
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class AccountDetailsFragment extends BasePreferencesFragment {

    @Inject
    UserRepository userRepository;

    private QrCodeManager qrCodeManager;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HabiticaBaseApplication.getComponent().inject(this);

        String userID = getPreferenceManager().getSharedPreferences().getString(getContext().getString(R.string.SP_userID), null);
        if (userID != null) {
            userRepository.getUser(userID).subscribe(this::setUser, ReactiveErrorHandler.handleEmptyError());
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    protected void setupPreferences() {
        for (Map.Entry<String, ?> preference : getPreferenceScreen().getSharedPreferences().getAll().entrySet()) {
            String key = preference.getKey();
            if (getAccountDetailsPreferences().contains(key) && preference.getValue() != null) {
                findPreference(key).setSummary(preference.getValue().toString());
            }
        }

        qrCodeManager = new QrCodeManager(userRepository, this.getContext());
    }

    protected List<String> getAccountDetailsPreferences() {
        return Arrays.asList(getString(R.string.SP_username), getString(R.string.SP_email),
                getString(R.string.SP_APIToken), getString(R.string.SP_userID), getString(R.string.SP_user_qr_code));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("SP_user_qr_code")) {
            qrCodeManager.showDialogue();
        } else if (preference.getKey().equals("subscription_status")) {
            if (user != null && user.getPurchased() != null && user.getPurchased().getPlan() != null) {
                SubscriptionPlan plan = user.getPurchased().getPlan();
                if (plan.isActive()) {
                    showSubscriptionStatusDialog();
                    return super.onPreferenceTreeClick(preference);
                }
            }
            EventBus.getDefault().post(new OpenGemPurchaseFragmentCommand());
        } else {
            ClipboardManager clipMan = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipMan.setPrimaryClip(ClipData.newPlainText(preference.getKey(), preference.getSummary()));
            Toast.makeText(getActivity(), "Copied " + preference.getKey() + " to clipboard.", Toast.LENGTH_SHORT).show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void showSubscriptionStatusDialog() {
        SubscriptionDetailsView view = new SubscriptionDetailsView(getContext());
        view.setPlan(user.getPurchased().getPlan());
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(R.string.subscription_status)
                .setPositiveButton(R.string.close, (dialogInterface, i) -> dialogInterface.dismiss()).create();
        dialog.show();
    }
}
