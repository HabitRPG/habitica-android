package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.functions.Consumer

class PushNotificationsPreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var isInitialSet: Boolean = true
    private var isSettingUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.component?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun setupPreferences() {

    }

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
            "preference_push_you_won_challenge" -> "wonChallenge"
            "preference_push_received_a_private_message" -> "newPM"
            "preference_push_gifted_gems" -> "giftedGems"
            "preference_push_gifted_subscription" -> "giftedSubscription"
            "preference_push_invited_to_party" -> "invitedParty"
            "preference_push_invited_to_guild" -> "invitedGuild"
            "preference_push_your_quest_has_begun" -> "questStarted"
            "preference_push_invited_to_quest" -> "invitedQuest"
            "preference_push_important_announcements" -> "majorUpdates"
            else -> null
        }
        if (pathKey != null) {
            compositeSubscription.add(userRepository.updateUser(user, "preferences.pushNotifications.$pathKey", sharedPreferences.getBoolean(key, false)).subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()))
        }
    }
}