package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.getHabiticaPromotionFromKey
import java.util.*

class AppConfigManager {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun shopSpriteSuffix(): String {
        return remoteConfig.getString("shopSpriteSuffix")
    }

    fun maxChatLength(): Long {
        return remoteConfig.getLong("maxChatLength")
    }

    fun spriteSubstitutions(): Map<String, Map<String, String>> {
        val type = object : TypeToken<Map<String, Map<String, String>>>() {}.type
        return Gson().fromJson(remoteConfig.getString("spriteSubstitutions"), type)
    }

    fun supportEmail(): String {
        return remoteConfig.getString("supportEmail")
    }

    fun enableUsernameAutocomplete(): Boolean {
        return remoteConfig.getBoolean("enableUsernameAutocomplete")
    }

    fun enableLocalChanges(): Boolean {
        return remoteConfig.getBoolean("enableLocalChanges")
    }

    fun lastVersionNumber(): String {
        return remoteConfig.getString("lastVersionNumber")
    }

    fun lastVersionCode(): Long {
        return remoteConfig.getLong("lastVersionCode")
    }

    fun noPartyLinkPartyGuild(): Boolean {
        return remoteConfig.getBoolean("noPartyLinkPartyGuild")
    }

    fun testingLevel(): AppTestingLevel {
        return AppTestingLevel.valueOf(BuildConfig.TESTING_LEVEL.toUpperCase(Locale.US))
    }

    fun enableLocalTaskScoring(): Boolean {
        return remoteConfig.getBoolean("enableLocalTaskScoring")
    }

    fun flipAddTaskBehaviour(): Boolean {
        return remoteConfig.getBoolean("flipAddTaskBehaviour")
    }

    fun insufficientGemPurchase(): Boolean {
        return remoteConfig.getBoolean("insufficientGemPurchase")
    }

    fun insufficientGemPurchaseAdjust(): Boolean {
        return remoteConfig.getBoolean("insufficientGemPurchaseAdjust")
    }

    fun showSubscriptionBanner(): Boolean {
        return remoteConfig.getBoolean("showSubscriptionBanner")
    }

    fun minimumPasswordLength(): Long {
        return remoteConfig.getLong("minimumPasswordLength")
    }

    fun enableTaskDisplayMode(): Boolean {
        return remoteConfig.getBoolean("enableTaskDisplayMode")
    }

    fun feedbackURL(): String {
        return remoteConfig.getString("feedbackURL")
    }

    fun taskDisplayMode(context: Context): String {
        return if (remoteConfig.getBoolean("enableTaskDisplayMode")) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString("task_display", "standard") ?: "standard"
        } else {
            "standard"
        }
    }

    fun enableAdventureGuide(): Boolean {
        return remoteConfig.getBoolean("enableAdventureGuide")
    }

    fun activePromo(context: Context): HabiticaPromotion? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = preferences.getString("currentEvent", null)
        if (key?.isNotBlank() == true) {
            val startDateLong = preferences.getLong("currentEventStartDate", 0)
            val startDate = if (startDateLong > 0) Date(startDateLong) else null
            val endDateLong = preferences.getLong("currentEventEndDate", 0)
            val endDate = if (endDateLong > 0) Date(endDateLong) else null
            return getHabiticaPromotionFromKey(preferences.getString("currentEventPromo", null) ?: key, startDate, endDate)
        }
        return null
    }

    fun knownIssues(): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return Gson().fromJson(remoteConfig.getString("knownIssues"), type)
    }

    fun enableTeamBoards(): Boolean {
        return remoteConfig.getBoolean("enableTeamBoards")
    }
}
