package com.habitrpg.android.habitica.ui.fragments.preferences;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.helpers.QrCodeManager;
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionDetailsView;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.SubscriptionPlan;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AccountDetailsFragment extends BasePreferencesFragment {

    private QrCodeManager qrCodeManager;
    private HabitRPGUser user;
    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            AccountDetailsFragment.this.setUser(habitRPGUser);
        }

        @Override
        public boolean onReady(BaseTransaction<HabitRPGUser> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<HabitRPGUser> baseTransaction, HabitRPGUser habitRPGUser) {
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userID = getPreferenceManager().getSharedPreferences().getString(getContext().getString(R.string.SP_userID), null);
        if (userID != null) {
            new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(userID)).async().querySingle(userTransactionListener);
        }
    }

    public void setUser(HabitRPGUser user) {
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

        qrCodeManager = new QrCodeManager(this.getContext());
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
                .setPositiveButton(R.string.close, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).create();
        dialog.show();
    }
}
