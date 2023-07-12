package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.prefs.TimePreference
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.PrefsActivity
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.android.habitica.ui.views.preferences.PauseResumeDamageView
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PreferencesFragment : BasePreferencesFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var contentRepository : ContentRepository

    @Inject
    lateinit var soundManager : SoundManager

    @Inject
    lateinit var pushNotificationManager : PushNotificationManager

    @Inject
    lateinit var configManager : AppConfigManager

    @Inject
    lateinit var apiClient : ApiClient

    private var timePreference : TimePreference? = null
    private var pushNotificationsPreference : PreferenceScreen? = null
    private var emailNotificationsPreference : PreferenceScreen? = null
    private var classSelectionPreference : Preference? = null
    private var serverUrlPreference : ListPreference? = null
    private var taskListPreference : ListPreference? = null

    private val classSelectionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.retrieveUser(true, true)
            }
        }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.itemAnimator = null

        lifecycleScope.launchCatching {
            userRepository.retrieveTeamPlans()
        }
    }

    override fun setupPreferences() {
        timePreference = findPreference("reminder_time") as? TimePreference
        val useReminder = preferenceManager.sharedPreferences?.getBoolean("use_reminder", false)
        timePreference?.isEnabled = useReminder ?: false

        classSelectionPreference = findPreference("choose_class")

        val weekdayPreference = findPreference("FirstDayOfTheWeek") as? ListPreference
        weekdayPreference?.summary = weekdayPreference?.entry

        serverUrlPreference = findPreference("server_url") as? ListPreference
        serverUrlPreference?.isVisible = false
        serverUrlPreference?.summary =
            preferenceManager.sharedPreferences?.getString("server_url", "")

        val themePreference = findPreference("theme_name") as? ListPreference
        themePreference?.summary = themePreference?.entry ?: "Default"
        val themeModePreference = findPreference("theme_mode") as? ListPreference
        themeModePreference?.summary = themeModePreference?.entry ?: "Follow System"

        val launchScreenPreference = findPreference("launch_screen") as? ListPreference
        launchScreenPreference?.summary = launchScreenPreference?.entry ?: "Habits"

        val taskDisplayPreference = findPreference("task_display") as? ListPreference
        if (configManager.enableTaskDisplayMode()) {
            taskDisplayPreference?.isVisible = true
            taskDisplayPreference?.summary = taskDisplayPreference?.entry
        } else {
            taskDisplayPreference?.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onPreferenceTreeClick(preference : Preference) : Boolean {
        when (preference.key) {
            "logout" -> {
                logout()
            }

            "pause_damage" -> {
                showAsBottomSheet {dismiss ->
                    PauseResumeDamageView(user?.preferences?.sleep ?: true, {
                        lifecycleScope.launchCatching {
                            user?.let { it -> userRepository.sleep(it) }
                            dismiss()
                        }
                    })
                }
            }

            "choose_class" -> {
                val isPlayerOptedOutOfClass = user?.preferences?.disableClasses ?: false
                val isClassSelected =  user?.flags?.classSelected ?: false
                val bundle = Bundle()
                bundle.putBoolean("isInitialSelection", isClassSelected)
                val intent = Intent(activity, ClassSelectionActivity::class.java)
                intent.putExtras(bundle)

                if (isClassSelected && !isPlayerOptedOutOfClass) {
                    if ((user?.gemCount ?: 0) >= 3) {
                        context?.let { context ->
                            val dialog = HabiticaAlertDialog(context)
                            dialog.setTitle(R.string.change_class_confirmation)
                            dialog.setMessage(R.string.change_class_message)
                            dialog.addButton(R.string.change_class,
                                isPrimary = true,
                                isDestructive = true
                            ) { _, _ ->
                                classSelectionResult.launch(
                                    intent
                                )
                            }
                            dialog.addButton(R.string.close, false)
                            dialog.enqueue()
                        }
                    } else {
                        activity?.let { activity ->
                            val dialog = InsufficientGemsDialog(activity, 3)
                            dialog.show()
                        }
                    }
                } else {
                    // This will handle initial class selection (If a player has not already selected a class),
                    // if a player started changing a class and force closes the app previously,
                    // and if a player previously opted out of class selection.
                    classSelectionResult.launch(intent)
                }
                return true
            }

            "reload_content" -> {
                (activity as? SnackbarActivity)?.showSnackbar(
                    content = context?.getString(R.string.reloading_content)
                )
                reloadContent(true)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun reloadContent(withConfirmation : Boolean) {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            contentRepository.retrieveContent(true)
            if (withConfirmation) {
                (activity as? SnackbarActivity)?.showSnackbar(
                    content = context?.getString(R.string.reloaded_content),
                    displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                )
            }
        }
    }

    private fun logout() {
        context?.let { context ->
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.are_you_sure)
            dialog.addButton(R.string.logout, true) { _, _ ->
                HabiticaBaseApplication.logout(context)
                activity?.finish()
            }
            dialog.addCancelButton()
            dialog.show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val usePushPreference = findPreference("usePushNotifications") as? CheckBoxPreference
            usePushPreference?.isChecked = true
            pushNotificationManager.addPushDeviceUsingStoredToken()
        } else {
            // If user denies notification settings originally - they must manually enable it through notification settings.
            val alert = context?.let { HabiticaAlertDialog(it) }
            alert?.setTitle(R.string.push_notification_system_settings_title)
            alert?.setMessage(R.string.push_notification_system_settings_description)
            alert?.addButton(R.string.open_settings, true, false) { _, _ ->
                val notifSettingIntent : Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context?.applicationContext?.packageName)
                startActivity(notifSettingIntent)
            }
            alert?.addButton(R.string.cancel, false) { _, _ ->
                alert.dismiss()
            }
            alert?.show()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences : SharedPreferences, key : String?) {
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
                val notifPermissionEnabled : Boolean =
                    pushNotificationManager.notificationPermissionEnabled()
                val usePushNotifications = sharedPreferences.getBoolean(key, true)
                pushNotificationsPreference?.isEnabled = usePushNotifications
                lifecycleScope.launchCatching {
                    userRepository.updateUser(
                        "preferences.pushNotifications.unsubscribeFromAll",
                        !usePushNotifications
                    )
                }
                if (usePushNotifications) {
                    if (!notifPermissionEnabled && Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        pushNotificationManager.addPushDeviceUsingStoredToken()
                    }
                } else {
                    lifecycleScope.launchCatching {
                        pushNotificationManager.removePushDeviceUsingStoredToken()
                    }
                }
            }

            "useEmails" -> {
                val useEmailNotifications = sharedPreferences.getBoolean(key, true)
                emailNotificationsPreference?.isEnabled = useEmailNotifications
                lifecycleScope.launchCatching {
                    userRepository.updateUser(
                        "preferences.emailNotifications.unsubscribeFromAll",
                        !useEmailNotifications
                    )
                }
            }

            "cds_time" -> {
                val timeval = sharedPreferences.getString("cds_time", "0") ?: "0"
                val hour = Integer.parseInt(timeval)
                lifecycleScope.launchCatching {
                    userRepository.changeCustomDayStart(hour)
                }
                val preference = findPreference<ListPreference>(key)
                preference?.summary = preference?.entry
            }

            "language" -> {
                val languageHelper = LanguageHelper(sharedPreferences.getString(key, "en"))

                Locale.setDefault(languageHelper.locale)
                val configuration = Configuration()
                configuration.setLocale(languageHelper.locale)
                @Suppress("DEPRECATION")
                activity?.resources?.updateConfiguration(
                    configuration,
                    activity?.resources?.displayMetrics
                )

                if (user?.preferences?.language == languageHelper.languageCode) {
                    return
                }
                lifecycleScope.launchCatching {
                    userRepository.updateLanguage(languageHelper.languageCode ?: "en")
                    reloadContent(false)
                }
                val intent = Intent(activity, MainActivity::class.java)
                this.startActivity(intent)
                activity?.finishAffinity()
            }

            "audioTheme" -> {
                val newAudioTheme = sharedPreferences.getString(key, "off")
                if (newAudioTheme != null) {
                    lifecycleScope.launchCatching {
                        userRepository.updateUser("preferences.sound", newAudioTheme)
                    }
                    soundManager.soundTheme = newAudioTheme
                    soundManager.preloadAllFiles()
                }
            }

            "theme_name" -> {
                val activity = activity as? PrefsActivity ?: return
                activity.reload()
            }

            "theme_mode" -> {
                val activity = activity as? PrefsActivity ?: return
                activity.reload()
            }

            "server_url" -> {
                apiClient.updateServerUrl(sharedPreferences.getString(key, ""))
                findPreference<Preference>(key)?.summary = sharedPreferences.getString(key, "")
            }

            "task_display" -> {
                val preference = findPreference<ListPreference>(key)
                preference?.summary = preference?.entry
            }

            "FirstDayOfTheWeek" -> {
                val preference = findPreference<ListPreference>(key)
                preference?.summary = preference?.entry
            }

            "disablePMs" -> {
                val isDisabled = sharedPreferences.getBoolean("disablePMs", false)
                if (user?.inbox?.optOut != isDisabled) {
                    lifecycleScope.launchCatching {
                        userRepository.updateUser("inbox.optOut", isDisabled)
                    }
                }
            }

            "launch_screen" -> {
                val preference = findPreference<ListPreference>(key)
                preference?.summary = preference?.entry ?: "Habits"
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference : Preference) {
        if (preference is TimePreference) {
            if (parentFragmentManager.findFragmentByTag(TimePreferenceDialogFragment.TAG) == null) {
                TimePreferenceDialogFragment.newInstance(this, preference.getKey())
                    .show(parentFragmentManager, TimePreferenceDialogFragment.TAG)
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun setUser(user : User?) {
        super.setUser(user)

        val pauseDamagePreference = findPreference<Preference>("pause_damage")
        pauseDamagePreference?.title = getString(
            if (user?.preferences?.sleep == true) {
                R.string.resume_damage
            } else {
                R.string.pause_damage
            }
        )
        pauseDamagePreference?.summary = getString(
            if (user?.preferences?.sleep == true) {
                R.string.resume_damage_summary
            } else {
                R.string.pause_damage_summary
            }
        )

        if (10 <= (user?.stats?.lvl ?: 0)) {
            if (user?.flags?.classSelected == true) {
                if (user.preferences?.disableClasses == true) {
                    classSelectionPreference?.title = getString(R.string.enable_class)
                    classSelectionPreference?.summary = getString(R.string.enable_class_description)
                } else {
                    classSelectionPreference?.title = getString(R.string.change_class)
                    classSelectionPreference?.summary = getString(R.string.change_class_description)
                }
            } else {
                classSelectionPreference?.title = getString(R.string.enable_class)
            }
            classSelectionPreference?.isVisible = true
        } else {
            classSelectionPreference?.isVisible = false
        }
        val cdsTimePreference = findPreference("cds_time") as? ListPreference
        cdsTimePreference?.value = user?.preferences?.dayStart.toString()
        cdsTimePreference?.summary = cdsTimePreference?.entry
        val languagePreference = findPreference("language") as? ListPreference
        languagePreference?.value = user?.preferences?.language
        languagePreference?.summary = languagePreference?.entry
        val audioThemePreference = findPreference("audioTheme") as? ListPreference
        audioThemePreference?.value = user?.preferences?.sound
        audioThemePreference?.summary = audioThemePreference?.entry

        val preference = findPreference<Preference>("authentication")
        if (user?.flags?.verifiedUsername == true) {
            preference?.layoutResource = R.layout.preference_child_summary
            preference?.summary = context?.getString(R.string.authentication_summary)
        } else {
            preference?.layoutResource = R.layout.preference_child_summary_error
            preference?.summary = context?.getString(R.string.username_not_confirmed)
        }

        if (user?.party?.id?.isNotBlank() != true) {
            val launchScreenPreference = findPreference<ListPreference>("launch_screen")
            launchScreenPreference?.entries =
                resources.getStringArray(R.array.launch_screen_types).dropLast(1).toTypedArray()
            launchScreenPreference?.entryValues =
                resources.getStringArray(R.array.launch_screen_values).dropLast(1).toTypedArray()
        }

        val disablePMsPreference = findPreference("disablePMs") as? CheckBoxPreference
        val inbox = user?.inbox
        disablePMsPreference?.isChecked = inbox?.optOut ?: true

        val notifPermissionEnabled : Boolean =
            pushNotificationManager.notificationPermissionEnabled()
        val usePushPreference = findPreference("usePushNotifications") as? CheckBoxPreference
        pushNotificationsPreference = findPreference("pushNotifications") as? PreferenceScreen
        val usePushNotifications =
            !(user?.preferences?.pushNotifications?.unsubscribeFromAll ?: false)
        pushNotificationsPreference?.isEnabled = usePushNotifications
        usePushPreference?.isChecked = (usePushNotifications && notifPermissionEnabled)
        if (!notifPermissionEnabled) {
            usePushPreference?.summary =
                getString(R.string.push_notification_system_settings_description)
        } else {
            usePushPreference?.summary = ""
        }
        val useEmailPreference = findPreference("useEmails") as? CheckBoxPreference
        emailNotificationsPreference = findPreference("emailNotifications") as? PreferenceScreen
        val useEmailNotifications =
            !(user?.preferences?.emailNotifications?.unsubscribeFromAll ?: false)
        emailNotificationsPreference?.isEnabled = useEmailNotifications
        useEmailPreference?.isChecked = useEmailNotifications

        lifecycleScope.launch {
            val teams = userRepository.getTeamPlans().firstOrNull() ?: return@launch
            val context = context ?: return@launch
            val groupCategory = findPreference<PreferenceCategory>("groups_category")
            val footer = groupCategory?.findPreference<Preference>("groups_footer")
            footer?.order = 9999
            groupCategory?.removeAll()
            if (teams.isEmpty()) {
                groupCategory?.isVisible = false
            } else {
                groupCategory?.isVisible = true
                for (team in teams) {
                    val newPreference = CheckBoxPreference(context)
                    newPreference.layoutResource = R.layout.preference_child_summary
                    newPreference.title = getString(R.string.copy_shared_tasks)
                    newPreference.summary = team.summary
                    newPreference.key = "copy_tasks-${team.id}"
                    newPreference.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { _, newValue ->
                            val currentIds =
                                user?.preferences?.tasks?.mirrorGroupTasks?.toMutableList()
                                    ?: mutableListOf()
                            if (newValue == true && !currentIds.contains(team.id)) {
                                currentIds.add(team.id)
                            } else if (newValue == false && currentIds.contains(team.id)) {
                                currentIds.remove(team.id)
                            }
                            lifecycleScope.launchCatching {
                                userRepository.updateUser(
                                    "preferences.tasks.mirrorGroupTasks",
                                    currentIds
                                )
                            }
                            true
                        }
                    groupCategory?.addPreference(newPreference)
                    newPreference.isChecked =
                        user?.preferences?.tasks?.mirrorGroupTasks?.contains(team.id) == true
                }
            }
            if (footer != null) {
                groupCategory.addPreference(footer)
            }
        }

        if (configManager.testingLevel() == AppTestingLevel.STAFF || BuildConfig.DEBUG) {
            serverUrlPreference?.isVisible = true
            taskListPreference?.isVisible = true
        }
    }
}
