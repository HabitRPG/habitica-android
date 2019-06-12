package com.habitrpg.android.habitica.ui.fragments.preferences

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.prefs.TimePreference
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity
import com.habitrpg.android.habitica.ui.activities.FixCharacterValuesActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

class PreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var contentRepository: ContentRepository
    @Inject
    lateinit var soundManager: SoundManager
    @Inject
    lateinit  var pushNotificationManager: PushNotificationManager
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var apiClient: ApiClient

    private var timePreference: TimePreference? = null
    private var pushNotificationsPreference: PreferenceScreen? = null
    private var classSelectionPreference: Preference? = null
    private var serverUrlPreference: ListPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)

    }

    override fun setupPreferences() {
        timePreference = findPreference("reminder_time") as? TimePreference
        val useReminder = preferenceManager.sharedPreferences.getBoolean("use_reminder", false)
        timePreference?.isEnabled = useReminder


        pushNotificationsPreference = findPreference("pushNotifications") as? PreferenceScreen
        val userPushNotifications = preferenceManager.sharedPreferences.getBoolean("usePushNotifications", true)
        pushNotificationsPreference?.isEnabled = userPushNotifications


        classSelectionPreference = findPreference("choose_class")
        classSelectionPreference?.isVisible = false

        serverUrlPreference = findPreference("server_url") as? ListPreference
        serverUrlPreference?.isVisible = false
        serverUrlPreference?.summary = preferenceManager.sharedPreferences.getString("server_url", "")
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when(preference.key) {
            "logout" -> {
                context?.let { HabiticaBaseApplication.logout(it) }
                activity?.finish()
            }
            "choose_class" -> {
                val bundle = Bundle()
                bundle.putBoolean("isInitialSelection", user?.flags?.classSelected == false)

                val intent = Intent(activity, ClassSelectionActivity::class.java)
                intent.putExtras(bundle)

                if (user?.flags?.classSelected == true && user?.preferences?.disableClasses == false) {
                    context?.let { context ->
                        val builder = AlertDialog.Builder(context)
                                .setMessage(getString(R.string.change_class_confirmation))
                                .setNegativeButton(getString(R.string.dialog_go_back)) { dialog, _ -> dialog.dismiss() }
                                .setPositiveButton(getString(R.string.change_class)) { _, _ -> startActivityForResult(intent, MainActivity.SELECT_CLASS_RESULT) }
                        val alert = builder.create()
                        alert.show()
                    }
                } else {
                    startActivityForResult(intent, MainActivity.SELECT_CLASS_RESULT)
                }
                return true
            }
            "reload_content" -> {
                @Suppress("DEPRECATION")
                val dialog = ProgressDialog.show(context, context?.getString(R.string.reloading_content), null, true)
                contentRepository.retrieveContent(context,true).subscribe({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }) { throwable ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    RxErrorHandler.reportError(throwable)
                }
            }
            "fixCharacterValues" -> {
                val intent = Intent(activity, FixCharacterValuesActivity::class.java)
                activity?.startActivity(intent)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }


    @SuppressLint("ObsoleteSdkInt")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "use_reminder" -> {
                val useReminder = sharedPreferences.getBoolean(key, false)
                timePreference?.isEnabled = useReminder
                if (useReminder) {
                    TaskAlarmManager.scheduleDailyReminder(context)
                } else {
                    TaskAlarmManager.removeDailyReminder(context)
                }
            }
            "reminder_time" -> {
                TaskAlarmManager.removeDailyReminder(context)
                TaskAlarmManager.scheduleDailyReminder(context)
            }
            "usePushNotifications" -> {
                val userPushNotifications = sharedPreferences.getBoolean(key, false)
                pushNotificationsPreference?.isEnabled = userPushNotifications
                if (userPushNotifications) {
                    pushNotificationManager.addPushDeviceUsingStoredToken()
                } else {
                    pushNotificationManager.removePushDeviceUsingStoredToken()
                }
            }
            "cds_time" -> {
                val timeval = sharedPreferences.getString("cds_time", "00:00")
                val pieces = timeval?.split(":".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                if (pieces != null) {
                    val hour = Integer.parseInt(pieces[0])
                    userRepository.changeCustomDayStart(hour).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
            "language" -> {
                val languageHelper = LanguageHelper(sharedPreferences.getString(key, "en"))

                Locale.setDefault(languageHelper.locale)
                val configuration = Configuration()
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    @Suppress("Deprecation")
                    configuration.locale = languageHelper.locale
                } else {
                    configuration.setLocale(languageHelper.locale)
                }
                @Suppress("DEPRECATION")
                activity?.resources?.updateConfiguration(configuration, activity?.resources?.displayMetrics)
                userRepository.updateLanguage(user, languageHelper.languageCode)
                        .flatMap<ContentResult> { contentRepository.retrieveContent(context,true) }
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, MainActivity::class.java)
                    this.startActivity(intent)
                    activity?.finishAffinity()
                }
            }
            "audioTheme" -> {
                val newAudioTheme = sharedPreferences.getString(key, "off")
                if (newAudioTheme != null) {
                    compositeSubscription.add(userRepository.updateUser(user, "preferences.sound", newAudioTheme)
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
                    soundManager.soundTheme = newAudioTheme
                    soundManager.preloadAllFiles()
                }
            }
            "dailyDueDefaultView" -> userRepository.updateUser(user, "preferences.dailyDueDefaultView", sharedPreferences.getBoolean(key, false))
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            "server_url" -> {
                apiClient.updateServerUrl(sharedPreferences.getString(key, ""))
                findPreference(key).summary = sharedPreferences.getString(key, "")
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is TimePreference) {
            if (preference.getKey() == "cds_time") {
                if (fragmentManager?.findFragmentByTag(DayStartPreferenceDialogFragment.TAG) == null) {
                    fragmentManager?.let {
                        DayStartPreferenceDialogFragment.newInstance(this, preference.getKey())
                                .show(it, DayStartPreferenceDialogFragment.TAG)
                    }
                }
            } else {
                if (fragmentManager?.findFragmentByTag(TimePreferenceDialogFragment.TAG) == null) {
                    fragmentManager?.let {
                        TimePreferenceDialogFragment.newInstance(this, preference.getKey())
                                .show(it, TimePreferenceDialogFragment.TAG)
                    }
                }
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun setUser(user: User?) {
        super.setUser(user)
        if (10 <= user?.stats?.lvl ?: 0) {
            if (user?.flags?.classSelected == true) {
                if (user.preferences?.disableClasses == true) {
                    classSelectionPreference?.title = getString(R.string.enable_class)
                } else {
                    classSelectionPreference?.title = getString(R.string.change_class)
                    classSelectionPreference?.summary = getString(R.string.change_class_description)
                }
                classSelectionPreference?.isVisible = true
            } else {
                classSelectionPreference?.title = getString(R.string.enable_class)
                classSelectionPreference?.isVisible = true
            }
        }
        val cdsTimePreference = findPreference("cds_time") as? TimePreference
        cdsTimePreference?.text = user?.preferences?.dayStart.toString() + ":00"
        findPreference("dailyDueDefaultView").setDefaultValue(user?.preferences?.dailyDueDefaultView)
        val languagePreference = findPreference("language") as? ListPreference
        languagePreference?.value = user?.preferences?.language
        languagePreference?.summary = languagePreference?.entry
        val audioThemePreference = findPreference("audioTheme") as? ListPreference
        audioThemePreference?.value = user?.preferences?.sound
        audioThemePreference?.summary = audioThemePreference?.entry

        val preference = findPreference("authentication")
        if (user?.flags?.isVerifiedUsername == true) {
            preference.layoutResource = R.layout.preference_child_summary
            preference.summary = context?.getString(R.string.authentication_summary)
        } else {
            preference.layoutResource = R.layout.preference_child_summary_error
            preference.summary = context?.getString(R.string.username_not_confirmed)
        }

        if (user?.contributor?.admin == true) {
            serverUrlPreference?.isVisible = true
        }
    }
}
