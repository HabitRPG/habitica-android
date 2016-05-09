package com.habitrpg.android.habitica.ui.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.callbacks.MergeUserCallback;
import com.habitrpg.android.habitica.prefs.TimePreference;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PreferencesFragment extends BasePreferencesFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, Callback<HabitRPGUser> {

    private Context context;
    private TimePreference timePreference;
    private Preference classSelectionPreference;
    private HabitRPGUser user;
    private APIHelper apiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiHelper = HabiticaApplication.ApiHelper;

        context = getActivity();

        String userID = getPreferenceManager().getSharedPreferences().getString(context.getString(R.string.SP_userID), null);
        if (userID != null) {
            new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(userID)).async().querySingle(userTransactionListener);
        }

    }

    @Override
    protected void setupPreferences() {
        timePreference = (TimePreference) findPreference("reminder_time");
        boolean useReminder = getPreferenceManager().getSharedPreferences().getBoolean("use_reminder", false);
        timePreference.setEnabled(useReminder);

        classSelectionPreference = findPreference("choose_class");
        classSelectionPreference.setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            PreferencesFragment.this.setUser(habitRPGUser);
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
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("logout")) {
            HabiticaApplication.logout(context);
            getActivity().finish();
        } else if (preference.getKey().equals("choose_class")) {
            Bundle bundle = new Bundle();
            bundle.putString("size", user.getPreferences().getSize());
            bundle.putString("skin", user.getPreferences().getSkin());
            bundle.putString("shirt", user.getPreferences().getShirt());
            bundle.putInt("hairBangs", user.getPreferences().getHair().getBangs());
            bundle.putInt("hairBase", user.getPreferences().getHair().getBase());
            bundle.putString("hairColor", user.getPreferences().getHair().getColor());
            bundle.putInt("hairMustache", user.getPreferences().getHair().getMustache());
            bundle.putInt("hairBeard", user.getPreferences().getHair().getBeard());
            if (!user.getFlags().getClassSelected()) {
                bundle.putBoolean("isInitialSelection", true);
            } else {
                bundle.putBoolean("isInitialSelection", false);
            }

            Intent intent = new Intent(getActivity(), ClassSelectionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, MainActivity.SELECT_CLASS_RESULT);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void scheduleNotifications() {

        String timeval = getPreferenceManager().getSharedPreferences().getString("reminder_time", "19:00");

        String[] pieces = timeval.split(":");
        int hour = Integer.parseInt(pieces[0]);
        int minute = Integer.parseInt(pieces[1]);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        long trigger_time = cal.getTimeInMillis();

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.CHECK_DAILIES, false);

        if (PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE) == null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger_time, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void removeNotifications() {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent displayIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        alarmManager.cancel(displayIntent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("use_reminder")) {
            boolean use_reminder = sharedPreferences.getBoolean(key, false);
            timePreference.setEnabled(use_reminder);
            if (use_reminder) {
                scheduleNotifications();
            } else {
                removeNotifications();
            }
        } else if (key.equals("reminder_time")) {
            removeNotifications();
            scheduleNotifications();
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof TimePreference) {
            if (getFragmentManager().findFragmentByTag(TimePreferenceDialogFragment.TAG) == null) {
                TimePreferenceDialogFragment.newInstance(this, preference.getKey())
                        .show(getFragmentManager(), TimePreferenceDialogFragment.TAG);
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    public void setUser(HabitRPGUser user) {
        this.user = user;
        if (user.getFlags().getClassSelected()) {
            if (user.getPreferences().getDisableClasses()) {
                classSelectionPreference.setTitle(getString(R.string.enable_class));
            } else {
                classSelectionPreference.setTitle(getString(R.string.change_class));
                classSelectionPreference.setSummary(getString(R.string.change_class_description));
            }
            classSelectionPreference.setVisible(true);
        } else {
            classSelectionPreference.setTitle(getString(R.string.enable_class));
            classSelectionPreference.setVisible(true);
        }
    }

    @Override
    public void success(HabitRPGUser habitRPGUser, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }
}
