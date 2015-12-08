package com.habitrpg.android.habitica.ui.fragments;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.NotificationPublisher;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.prefs.TimePreference;

import java.util.Calendar;

/**
 * Created by franzejr on 28/11/15.
 */
public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private TimePreference timePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        addPreferencesFromResource(R.xml.preferences_fragment);
        timePreference = (TimePreference) findPreference("reminder_time");
        boolean use_reminder = getPreferenceManager().getSharedPreferences().getBoolean("use_reminder", false);
        timePreference.setEnabled(use_reminder);
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("logout")) {
            HabiticaApplication.logout(context);
            getActivity().finish();
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


    private void scheduleNotifications() {

        String timeval = getPreferenceManager().getSharedPreferences().getString("reminder_time", "19:00");

        if (timeval != null) {
            String[] pieces = timeval.split(":");
            int hour = Integer.parseInt(pieces[0]);
            int minute = Integer.parseInt(pieces[1]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.MINUTE, minute);
            long trigger_time = cal.getTimeInMillis();

            Intent notificationIntent = new Intent(context, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, getNotification());
            notificationIntent.putExtra(NotificationPublisher.CHECK_DAILIES, true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger_time, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void removeNotifications() {
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent displayIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        alarmManager.cancel(displayIntent);
    }

    @TargetApi(21)
    private Notification getNotification() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        Notification notification;
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(this.getString(R.string.reminder_title));
        builder.setSmallIcon(R.drawable.ic_gryphon);
        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(context.getResources().getColor(R.color.brand_300));
        }

        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN){
            return builder.build();
        } else{
            return builder.getNotification();
        }
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
}
