package com.habitrpg.android.habitica.ui.fragments.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.LanguageHelper;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.prefs.TimePreference;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

public class PreferencesFragment extends BasePreferencesFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    public ApiClient apiClient;
    @Inject
    public SoundManager soundManager;
    @Inject
    UserRepository userRepository;
    private Context context;
    private TimePreference timePreference;
    private PreferenceScreen pushNotificationsPreference;
    private Preference classSelectionPreference;
    private User user;
    @Inject
    PushNotificationManager pushNotificationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HabiticaBaseApplication.getComponent().inject(this);
        context = getActivity();

        String userID = getPreferenceManager().getSharedPreferences().getString(context.getString(R.string.SP_userID), null);
        if (userID != null) {
            userRepository.getUser(userID).subscribe(PreferencesFragment.this::setUser, throwable -> {});
        }
    }

    @Override
    protected void setupPreferences() {
        timePreference = (TimePreference) findPreference("reminder_time");
        boolean useReminder = getPreferenceManager().getSharedPreferences().getBoolean("use_reminder", false);
        timePreference.setEnabled(useReminder);


        pushNotificationsPreference = (PreferenceScreen) findPreference("pushNotifications");
        boolean userPushNotifications = getPreferenceManager().getSharedPreferences().getBoolean("usePushNotifications", true);
        pushNotificationsPreference.setEnabled(userPushNotifications);


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

            if (user.getFlags().getClassSelected() && !user.getPreferences().getDisableClasses()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.change_class_confirmation))
                        .setNegativeButton(getString(R.string.dialog_go_back), (dialog, which) -> dialog.dismiss())
                        .setPositiveButton(getString(R.string.change_class), (dialog, which) -> startActivityForResult(intent, MainActivity.SELECT_CLASS_RESULT));
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                startActivityForResult(intent, MainActivity.SELECT_CLASS_RESULT);
            }
            return true;
        } else if (preference.getKey().equals("reload_content")) {
            apiClient.getContent().subscribe(contentResult -> {}, throwable -> {});
        }
        return super.onPreferenceTreeClick(preference);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "use_reminder":
                boolean use_reminder = sharedPreferences.getBoolean(key, false);
                timePreference.setEnabled(use_reminder);
                if (use_reminder) {
                    TaskAlarmManager.scheduleDailyReminder(context);
                } else {
                    TaskAlarmManager.removeDailyReminder(context);
                }
                break;
            case "reminder_time":
                TaskAlarmManager.removeDailyReminder(context);
                TaskAlarmManager.scheduleDailyReminder(context);
                break;
            case "usePushNotifications":
                boolean userPushNotifications = sharedPreferences.getBoolean(key, false);
                pushNotificationsPreference.setEnabled(userPushNotifications);
                if (userPushNotifications) {
                    pushNotificationManager.addPushDeviceUsingStoredToken();
                } else {
                    pushNotificationManager.removePushDeviceUsingStoredToken();
                }
                break;
            case "cds_time":
                String timeval = sharedPreferences.getString("cds_time", "00:00");
                String[] pieces = timeval.split(":");
                int hour = Integer.parseInt(pieces[0]);
                Map<String, Object> updateObject = new HashMap<>();
                updateObject.put("dayStart", hour);
                apiClient.changeCustomDayStart(updateObject)
                        .subscribe(user -> {
                        }, throwable -> {
                        });
                break;
            case "language": {
                LanguageHelper languageHelper = new LanguageHelper(sharedPreferences.getString(key, "en"));

                Locale.setDefault(languageHelper.getLocale());
                Configuration configuration = new Configuration();
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    configuration.locale = languageHelper.getLocale();
                } else {
                    configuration.setLocale(languageHelper.getLocale());
                }
                getActivity().getResources().updateConfiguration(configuration,
                        getActivity().getResources().getDisplayMetrics());
                userRepository.updateUser(user, "preferences.language", languageHelper.getLanguageCode())
                        .subscribe(habitRPGUser -> {
                        }, throwable -> {
                        });

                Preferences preferences = user.getPreferences();
                preferences.setLanguage(languageHelper.getLanguageCode());
                apiClient.setLanguageCode(preferences.getLanguage());
                apiClient.getContent()
                        .subscribe(contentResult -> {
                        }, throwable -> {
                        });

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    this.startActivity(intent);
                    getActivity().finishAffinity();
                }

                break;
            }
            case "audioTheme": {
                String newAudioTheme = sharedPreferences.getString(key, "off");
                userRepository.updateUser(user, "preferences.sound", newAudioTheme)
                        .subscribe(habitRPGUser -> {
                        }, throwable -> {
                        });

                Preferences preferences = user.getPreferences();
                preferences.setSound(newAudioTheme);

                soundManager.setSoundTheme(newAudioTheme);

                soundManager.preloadAllFiles();
                break;
            }
            case "dailyDueDefaultView":
                userRepository.updateUser(user, "preferences.dailyDueDefaultView", sharedPreferences.getBoolean(key, false))
                        .subscribe(habitRPGUser -> {
                        }, throwable -> {
                        });
                break;
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof TimePreference) {
            if (preference.getKey().equals("cds_time")) {
                if (getFragmentManager().findFragmentByTag(DayStartPreferenceDialogFragment.TAG) == null) {
                    DayStartPreferenceDialogFragment.newInstance(this, preference.getKey())
                            .show(getFragmentManager(), DayStartPreferenceDialogFragment.TAG);
                }
            } else {
                if (getFragmentManager().findFragmentByTag(TimePreferenceDialogFragment.TAG) == null) {
                    TimePreferenceDialogFragment.newInstance(this, preference.getKey())
                            .show(getFragmentManager(), TimePreferenceDialogFragment.TAG);
                }
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getFlags() != null && user.getStats() != null) {
            if (user.getStats().getLvl() >= 10) {
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
        }
        if (user != null && user.getPreferences() != null) {
            TimePreference cdsTimePreference = (TimePreference) findPreference("cds_time");
            cdsTimePreference.setText(user.getPreferences().getDayStart() + ":00");
            findPreference("dailyDueDefaultView").setDefaultValue(user.getPreferences().getDailyDueDefaultView());
        }
    }
}
