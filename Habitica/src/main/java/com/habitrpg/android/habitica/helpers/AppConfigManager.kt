package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.WorldStateEvent
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.HabiticaWebPromotion
import com.habitrpg.android.habitica.models.promotions.getHabiticaPromotionFromKey
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.MainScope
import java.util.Date

class AppConfigManager(contentRepository: ContentRepository?) : com.habitrpg.common.habitica.helpers.AppConfigManager() {

    private var worldState: WorldState? = null

    init {
        try {
            MainScope().launchCatching {
                contentRepository?.getWorldState()?.collect {
                    worldState = it
                }
            }
        } catch (_: java.lang.IllegalStateException) {
            // pass
        }
    }

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun shopSpriteSuffix(): String {
        return worldState?.npcImageSuffix ?: remoteConfig.getString("shopSpriteSuffix")
    }

    fun maxChatLength(): Long {
        return remoteConfig.getLong("maxChatLength")
    }

    override fun spriteSubstitutions(): Map<String, Map<String, String>> {
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
        return AppTestingLevel.valueOf(BuildConfig.TESTING_LEVEL.uppercase())
    }

    fun enableLocalTaskScoring(): Boolean {
        return remoteConfig.getBoolean("enableLocalTaskScoring")
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
        return remoteConfig.getBoolean("enableTaskDisplayMode") || testingLevel() == AppTestingLevel.STAFF || BuildConfig.DEBUG
    }

    fun feedbackURL(): String {
        return remoteConfig.getString("feedbackURL")
    }

    fun surveyURL(): String {
        return remoteConfig.getString("surveyURL")
    }

    fun taskDisplayMode(context: Context): String {
        return if (enableTaskDisplayMode()) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString("task_display", "standard") ?: "standard"
        } else {
            "standard"
        }
    }

    fun activePromo(): HabiticaPromotion? {
        var promo: HabiticaPromotion? = null
        for (event in worldState?.events ?: listOf(worldState?.currentEvent)) {
            if (event == null) return null
            val thisPromo = getHabiticaPromotionFromKey(event.promo ?: event.eventKey ?: "", event.start, event.end)
            if (thisPromo != null) {
                promo = thisPromo
            }
        }
        if (promo == null && remoteConfig.getString("activePromo").isNotBlank()) {
            promo = getHabiticaPromotionFromKey(remoteConfig.getString("activePromo"), null, null)
        }
        if (promo?.isActive != true) {
            return null
        }
        if (promo is HabiticaWebPromotion) {
            promo.url = surveyURL()
        }
        return promo
    }

    fun knownIssues(): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return Gson().fromJson(remoteConfig.getString("knownIssues"), type)
    }

    fun enableArmoireAds(): Boolean {
        return remoteConfig.getBoolean("enableArmoireAds")
    }

    fun enableFaintAds(): Boolean {
        return remoteConfig.getBoolean("enableFaintAds")
    }

    fun enableArmoireSubs(): Boolean {
        return remoteConfig.getBoolean("enableArmoireSubs")
    }

    fun enableFaintSubs(): Boolean {
        return remoteConfig.getBoolean("enableFaintSubs")
    }

    fun enableSpellAds(): Boolean {
        return remoteConfig.getBoolean("enableSpellAds")
    }

    fun hideChallenges(): Boolean {
        return remoteConfig.getBoolean("hideChallenges")
    }

    fun getBirthdayEvent(): WorldStateEvent? {
        val events = ((worldState?.events as? List<WorldStateEvent>) ?: listOf(worldState?.currentEvent))
        return events.firstOrNull { it?.eventKey == "birthday10" && it.end?.after(Date()) == true }
    }
}
