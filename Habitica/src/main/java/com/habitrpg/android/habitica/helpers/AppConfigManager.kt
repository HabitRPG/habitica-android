package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
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
import com.habitrpg.common.habitica.helpers.Clearable
import com.habitrpg.common.habitica.helpers.SpriteSubstitutionManager
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.Date
import javax.inject.Provider

class AppConfigManager(
    contentRepository: Provider<ContentRepository>,
    private val sharedPreferences: SharedPreferences,
) : com.habitrpg.common.habitica.helpers.AppConfigManager(),
    Clearable {
    private var worldState: WorldState? = null

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun clear() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    init {
        scope.launchCatching {
            contentRepository.get().getWorldState().collect {
                worldState = it

                worldState?.currentEvent?.spriteSubstitutions?.let { subs ->
                    if (subs.isNotEmpty()) {
                        val subMap = mutableMapOf<String, Map<String, String>>()
                        subs.forEach { sub ->
                            subMap[sub.key ?: ""] = sub.substitutions
                        }
                        SpriteSubstitutionManager.setSubstitutions(subMap)
                    } else {
                        SpriteSubstitutionManager.setSubstitutions(emptyMap())
                    }
                }
            }
        }
    }

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun shopSpriteSuffix(): String? = worldState?.findNpcImageSuffix()

    fun maxChatLength(): Long = remoteConfig.getLong("maxChatLength")

    fun supportEmail(): String = remoteConfig.getString("supportEmail")

    fun enableUsernameAutocomplete(): Boolean = remoteConfig.getBoolean("enableUsernameAutocomplete")

    fun enableLocalChanges(): Boolean = remoteConfig.getBoolean("enableLocalChanges")

    fun lastVersionNumber(): String = remoteConfig.getString("lastVersionNumber")

    fun lastVersionCode(): Long = remoteConfig.getLong("lastVersionCode")

    fun testingLevel(): AppTestingLevel = AppTestingLevel.valueOf(BuildConfig.TESTING_LEVEL.uppercase())

    fun enableLocalTaskScoring(): Boolean = remoteConfig.getBoolean("enableLocalTaskScoring")

    fun showSubscriptionBanner(): Boolean = remoteConfig.getBoolean("showSubscriptionBanner")

    fun enableTaskDisplayMode(): Boolean =
        remoteConfig.getBoolean("enableTaskDisplayMode") || testingLevel() == AppTestingLevel.STAFF || BuildConfig.DEBUG

    fun feedbackURL(): String = remoteConfig.getString("feedbackURL")

    fun surveyURL(): String = remoteConfig.getString("surveyURL")

    fun taskDisplayMode(context: Context): String =
        if (enableTaskDisplayMode()) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString("task_display", "standard") ?: "standard"
        } else {
            "standard"
        }

    fun activePromo(): HabiticaPromotion? {
        val prefsPromo = sharedPreferences.getString("active_promo", null)
        if (prefsPromo?.isNotBlank() == true) {
            return getHabiticaPromotionFromKey(prefsPromo, null, null)
        }
        if (BuildConfig.ACTIVE_PROMO.isNotBlank()) {
            return getHabiticaPromotionFromKey(BuildConfig.ACTIVE_PROMO, null, null)
        }
        var promo: HabiticaPromotion? = null
        if (worldState?.isValid == true) {
            val allEvents = worldState?.events?.toMutableList() ?: mutableListOf()
            allEvents.add(worldState?.currentEvent)
            for (event in allEvents) {
                if (event == null) return null
                val thisPromo =
                    getHabiticaPromotionFromKey(
                        event.promo ?: event.eventKey ?: "",
                        event.start,
                        event.end,
                    )
                if (thisPromo != null) {
                    promo = thisPromo
                }
            }
        }
        if (promo == null && remoteConfig.getString("activePromo").isNotBlank()) {
            promo = getHabiticaPromotionFromKey(remoteConfig.getString("activePromo"), null, null)
        }
        if (promo is HabiticaWebPromotion) {
            promo.url = surveyURL()
        }
        if (promo?.isActive == true) {
            return promo
        }
        return null
    }

    fun knownIssues(): List<Map<String, String>> {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        return Gson().fromJson(remoteConfig.getString("knownIssues"), type)
    }

    fun enableArmoireAds(): Boolean = remoteConfig.getBoolean("enableArmoireAds")

    fun hideChallenges(): Boolean = remoteConfig.getBoolean("hideChallenges")

    fun enableReviewPrompt(): Boolean = remoteConfig.getBoolean("enableReviewPrompt")

    fun reviewCheckingMinCount(): Long = remoteConfig.getLong("reviewCheckingMinCount")

    fun getBirthdayEvent(): WorldStateEvent? {
        val events =
            ((worldState?.events as? List<WorldStateEvent>) ?: listOf(worldState?.currentEvent))
        return events.firstOrNull { it?.eventKey == "birthday10" && it.end?.after(Date()) == true }
    }

    fun showAltDeathText(): Boolean = remoteConfig.getBoolean("showAltDeathText")
}
