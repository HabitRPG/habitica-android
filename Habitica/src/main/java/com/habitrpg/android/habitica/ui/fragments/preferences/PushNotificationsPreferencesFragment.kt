package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import org.unifiedpush.android.connector.UnifiedPush
import javax.inject.Inject
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.data.ApiClient

@AndroidEntryPoint
class PushNotificationsPreferencesFragment :
    BasePreferencesFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var isInitialSet: Boolean = true
    private var isSettingUser: Boolean = false
    private var unifiedPushPreference: ListPreference? = null
    private var unifiedPushTestPreference: Preference? = null

    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    @Inject
    lateinit var apiClient: ApiClient

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        updateUnifiedPushPreference()
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun setupPreferences() {
        unifiedPushPreference = findPreference("preference_unified_push_provider") as? ListPreference
        unifiedPushPreference?.setOnPreferenceChangeListener { preference, newValue ->
            val packageName = (newValue as? String).orEmpty()
            handleUnifiedPushSelection(packageName)
            val listPreference = preference as? ListPreference
            val index = listPreference?.entryValues?.indexOf(packageName) ?: -1
            if (index >= 0 && listPreference?.entries?.size.orZero() > index) {
                listPreference?.summary = listPreference?.entries?.get(index)
            }
            true
        }
        unifiedPushTestPreference = findPreference("preference_unified_push_test")
        unifiedPushTestPreference?.setOnPreferenceClickListener {
            triggerUnifiedPushTest()
            true
        }
        updateUnifiedPushPreference()
    }

    override fun setUser(user: User?) {
        super.setUser(user)
        isSettingUser = !isInitialSet
        updatePreference(
            "preference_push_you_won_challenge",
            user?.preferences?.pushNotifications?.wonChallenge
        )
        updatePreference(
            "preference_push_received_a_private_message",
            user?.preferences?.pushNotifications?.newPM
        )
        updatePreference(
            "preference_push_gifted_gems",
            user?.preferences?.pushNotifications?.giftedGems
        )
        updatePreference(
            "preference_push_gifted_subscription",
            user?.preferences?.pushNotifications?.giftedSubscription
        )
        updatePreference(
            "preference_push_invited_to_party",
            user?.preferences?.pushNotifications?.invitedParty
        )
        updatePreference(
            "preference_push_invited_to_guild",
            user?.preferences?.pushNotifications?.invitedGuild
        )
        updatePreference(
            "preference_push_your_quest_has_begun",
            user?.preferences?.pushNotifications?.questStarted
        )
        updatePreference(
            "preference_push_invited_to_quest",
            user?.preferences?.pushNotifications?.invitedQuest
        )
        updatePreference(
            "preference_push_important_announcements",
            user?.preferences?.pushNotifications?.majorUpdates
        )
        updatePreference(
            "preference_push_party_activity",
            user?.preferences?.pushNotifications?.partyActivity
        )
        updatePreference(
            "preference_push_party_mention",
            user?.preferences?.pushNotifications?.mentionParty
        )
        updatePreference(
            "preference_push_joined_guild_mention",
            user?.preferences?.pushNotifications?.mentionJoinedGuild
        )
        updatePreference(
            "preference_push_content_release",
            user?.preferences?.pushNotifications?.contentRelease
        )
        isSettingUser = false
        isInitialSet = false
    }

    private fun updatePreference(
        key: String,
        isChecked: Boolean?
    ) {
        val preference = (findPreference(key) as? CheckBoxPreference)
        preference?.isChecked = isChecked == true
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?
    ) {
        if (isSettingUser) {
            return
        }
        val pathKey =
            when (key) {
                "preference_push_you_won_challenge" -> "wonChallenge"
                "preference_push_received_a_private_message" -> "newPM"
                "preference_push_gifted_gems" -> "giftedGems"
                "preference_push_gifted_subscription" -> "giftedSubscription"
                "preference_push_invited_to_party" -> "invitedParty"
                "preference_push_invited_to_guild" -> "invitedGuild"
                "preference_push_your_quest_has_begun" -> "questStarted"
                "preference_push_invited_to_quest" -> "invitedQuest"
                "preference_push_important_announcements" -> "majorUpdates"
                "preference_push_party_activity" -> "partyActivity"
                "preference_push_party_mention" -> "mentionParty"
                "preference_push_joined_guild_mention" -> "mentionJoinedGuild"
                "preference_push_unjoined_guild_mention" -> "mentionUnjoinedGuild"
                "preference_push_content_release" -> "contentRelease"
                else -> null
            }
        if (pathKey != null) {
            lifecycleScope.launchCatching {
                userRepository.updateUser(
                    "preferences.pushNotifications.$pathKey",
                    sharedPreferences.getBoolean(key, false)
                )
            }
        }
    }

    private fun handleUnifiedPushSelection(packageName: String) {
        val context = context ?: return
        if (packageName.isEmpty()) {
            UnifiedPush.removeDistributor(context)
            pushNotificationManager.unregisterUnifiedPushEndpoint()
        } else {
            UnifiedPush.saveDistributor(context, packageName)
            UnifiedPush.register(context)
            pushNotificationManager.ensureUnifiedPushRegistration()
        }
        updateUnifiedPushPreference()
    }

    private fun updateUnifiedPushPreference() {
        val preference = unifiedPushPreference ?: return
        val context = context ?: return
        val distributors = UnifiedPush.getDistributors(context)
        if (distributors.isEmpty()) {
            preference.isEnabled = false
            preference.summary = getString(R.string.unified_push_provider_unavailable)
            preference.entries = arrayOf(getString(R.string.unified_push_provider_disabled))
            preference.entryValues = arrayOf("")
            preference.value = ""
            unifiedPushTestPreference?.apply {
                isVisible = false
                isEnabled = false
                summary = getString(R.string.unified_push_test_unavailable)
            }
            return
        }

        preference.isEnabled = true
        val pm = context.packageManager
        val entries = mutableListOf(getString(R.string.unified_push_provider_disabled))
        val values = mutableListOf("")

        distributors.distinct().forEach { packageName ->
            val label = resolveAppLabel(pm, packageName)
            entries.add(label)
            values.add(packageName)
        }

        preference.entries = entries.toTypedArray()
        preference.entryValues = values.toTypedArray()

        val savedDistributor = UnifiedPush.getSavedDistributor(context)
        val resolvedValue = savedDistributor?.takeIf { values.contains(it) } ?: ""
        preference.value = resolvedValue

        val selectedIndex = values.indexOf(resolvedValue).takeIf { it >= 0 } ?: 0
        preference.summary = entries[selectedIndex]

        unifiedPushTestPreference?.apply {
            isVisible = true
            isEnabled = true
            summary = getString(R.string.unified_push_test_summary)
        }
    }

    private fun resolveAppLabel(pm: PackageManager, packageName: String): String {
        return runCatching {
            val applicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(applicationInfo).toString()
        }.getOrDefault(packageName)
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun triggerUnifiedPushTest() {
        val context = context ?: return
        val preference = unifiedPushTestPreference ?: return
        preference.isEnabled = false
        lifecycleScope.launchCatching({
            preference.isEnabled = true
            Toast.makeText(context, getString(R.string.unified_push_test_error), Toast.LENGTH_LONG).show()
        }) {
            try {
                pushNotificationManager.ensureUnifiedPushRegistration()
                apiClient.sendUnifiedPushTest()
                Toast.makeText(context, getString(R.string.unified_push_test_success), Toast.LENGTH_SHORT).show()
            } finally {
                preference.isEnabled = true
            }
        }
    }
}
