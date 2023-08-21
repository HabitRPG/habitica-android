package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.CheckBoxPreference
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PushNotificationsPreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var isInitialSet: Boolean = true
    private var isSettingUser: Boolean = false

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
        updatePreference("preference_push_you_won_challenge", user?.preferences?.pushNotifications?.wonChallenge)
        updatePreference("preference_push_received_a_private_message", user?.preferences?.pushNotifications?.newPM)
        updatePreference("preference_push_gifted_gems", user?.preferences?.pushNotifications?.giftedGems)
        updatePreference("preference_push_gifted_subscription", user?.preferences?.pushNotifications?.giftedSubscription)
        updatePreference("preference_push_invited_to_party", user?.preferences?.pushNotifications?.invitedParty)
        updatePreference("preference_push_invited_to_guild", user?.preferences?.pushNotifications?.invitedGuild)
        updatePreference("preference_push_your_quest_has_begun", user?.preferences?.pushNotifications?.questStarted)
        updatePreference("preference_push_invited_to_quest", user?.preferences?.pushNotifications?.invitedQuest)
        updatePreference("preference_push_important_announcements", user?.preferences?.pushNotifications?.majorUpdates)
        updatePreference("preference_push_party_activity", user?.preferences?.pushNotifications?.partyActivity)
        updatePreference("preference_push_party_mention", user?.preferences?.pushNotifications?.mentionParty)
        updatePreference("preference_push_joined_guild_mention", user?.preferences?.pushNotifications?.mentionJoinedGuild)
        updatePreference("preference_push_unjoined_guild_mention", user?.preferences?.pushNotifications?.mentionUnjoinedGuild)
        isSettingUser = false
        isInitialSet = false
    }

    private fun updatePreference(key: String, isChecked: Boolean?) {
        val preference = (findPreference(key) as? CheckBoxPreference)
        preference?.isChecked = isChecked == true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (isSettingUser) {
            return
        }
        val pathKey = when (key) {
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
            else -> null
        }
        if (pathKey != null) {
            lifecycleScope.launchCatching {
                userRepository.updateUser("preferences.pushNotifications.$pathKey", sharedPreferences.getBoolean(key, false))
            }
        }
    }
}
