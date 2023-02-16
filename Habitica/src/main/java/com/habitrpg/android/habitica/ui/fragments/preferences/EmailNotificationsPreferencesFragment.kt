package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.user.User

class EmailNotificationsPreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var isInitialSet: Boolean = true
    private var isSettingUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun setupPreferences() { /* no-on */ }

    override fun setUser(user: User?) {
        super.setUser(user)
        isSettingUser = !isInitialSet
        updatePreference("preference_email_you_won_challenge", user?.preferences?.emailNotifications?.wonChallenge)
        updatePreference("preference_email_received_a_private_message", user?.preferences?.emailNotifications?.newPM)
        updatePreference("preference_email_gifted_gems", user?.preferences?.emailNotifications?.giftedGems)
        updatePreference("preference_email_gifted_subscription", user?.preferences?.emailNotifications?.giftedSubscription)
        updatePreference("preference_email_invited_to_party", user?.preferences?.emailNotifications?.invitedParty)
        updatePreference("preference_email_invited_to_guild", user?.preferences?.emailNotifications?.invitedGuild)
        updatePreference("preference_email_your_quest_has_begun", user?.preferences?.emailNotifications?.questStarted)
        updatePreference("preference_email_invited_to_quest", user?.preferences?.emailNotifications?.invitedQuest)
        updatePreference("preference_email_important_announcements", user?.preferences?.emailNotifications?.majorUpdates)
        updatePreference("preference_email_kicked_group", user?.preferences?.emailNotifications?.kickedGroup)
        updatePreference("preference_email_onboarding", user?.preferences?.emailNotifications?.onboarding)
        updatePreference("preference_email_subscription_reminders", user?.preferences?.emailNotifications?.subscriptionReminders)
        isSettingUser = false
        isInitialSet = false
    }

    private fun updatePreference(key: String, isChecked: Boolean?) {
        val preference = (findPreference(key) as? CheckBoxPreference)
        preference?.isChecked = isChecked == true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (isSettingUser) {
            return
        }
        val pathKey = when (key) {
            "preference_email_you_won_challenge" -> "wonChallenge"
            "preference_email_received_a_private_message" -> "newPM"
            "preference_email_gifted_gems" -> "giftedGems"
            "preference_email_gifted_subscription" -> "giftedSubscription"
            "preference_email_invited_to_party" -> "invitedParty"
            "preference_email_invited_to_guild" -> "invitedGuild"
            "preference_email_your_quest_has_begun" -> "questStarted"
            "preference_email_invited_to_quest" -> "invitedQuest"
            "preference_email_important_announcements" -> "majorUpdates"
            "preference_email_kicked_group" -> "kickedGroup"
            "preference_email_onboarding" -> "onboarding"
            "preference_email_subscription_reminders" -> "subscriptionReminders"
            else -> null
        }
        if (pathKey != null) {
            lifecycleScope.launchCatching {
                userRepository.updateUser(
                    "preferences.emailNotifications.$pathKey",
                    sharedPreferences.getBoolean(key, false)
                )
            }
        }
    }
}
