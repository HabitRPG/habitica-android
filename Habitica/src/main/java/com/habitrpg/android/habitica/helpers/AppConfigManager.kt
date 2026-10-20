package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
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
import kotlinx.coroutines.MainScope
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Provider
import kotlin.time.Duration.Companion.hours

class AppConfigManager(
    private val contentRepository: Provider<ContentRepository>,
    private val sharedPreferences: SharedPreferences,
) : com.habitrpg.common.habitica.helpers.AppConfigManager(),
    Clearable {
    private var worldState: WorldState? = null
    private val gson = Gson()

    @Volatile
    private var config: JsonObject = loadStoredConfig()

    @Volatile
    private var lastFetchInSession: Long? = null
    private val isFetching = AtomicBoolean(false)

    override fun clear() {
    }

    init {
        MainScope().launchCatching {
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

    fun shopSpriteSuffix(): String? = worldState?.findNpcImageSuffix()

    fun <T> value(key: ClientConfigKey<T>): T {
        val raw = config.get(key.key)
        if (raw == null || raw.isJsonNull) return key.defaultValue
        return try {
            gson.fromJson<T>(raw, key.type) ?: key.defaultValue
        } catch (_: JsonParseException) {
            key.defaultValue
        }
    }

    fun fetchConfig(forced: Boolean = false) {
        val lastFetch = lastFetchInSession
        if (!forced && lastFetch != null && System.currentTimeMillis() - lastFetch < CONFIG_REFRESH_INTERVAL) return
        if (!isFetching.compareAndSet(false, true)) return
        MainScope().launchCatching {
            try {
                contentRepository.get().retrieveClientConfig()?.let { updateConfig(it) }
            } finally {
                isFetching.set(false)
            }
        }
    }

    @VisibleForTesting
    internal fun updateConfig(newConfig: JsonObject) {
        val now = System.currentTimeMillis()
        config = newConfig
        lastFetchInSession = now
        sharedPreferences.edit {
            putString(CONFIG_STORAGE_KEY, newConfig.toString())
            putLong(LAST_FETCH_STORAGE_KEY, now)
        }
    }

    // Use the config from the last launch until a fresh one is loaded
    private fun loadStoredConfig(): JsonObject =
        try {
            sharedPreferences.getString(CONFIG_STORAGE_KEY, null)?.let { JsonParser.parseString(it).asJsonObject }
        } catch (_: RuntimeException) {
            null
        } ?: JsonObject()

    fun testingLevel(): AppTestingLevel = AppTestingLevel.valueOf(BuildConfig.TESTING_LEVEL.uppercase())

    fun enableTaskDisplayMode(): Boolean =
        value(ClientConfigKey.enableTaskDisplayMode) || testingLevel() == AppTestingLevel.STAFF || BuildConfig.DEBUG

    fun taskDisplayMode(context: Context): String =
        if (enableTaskDisplayMode()) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString("task_display", "standard") ?: "standard"
        } else {
            "standard"
        }

    fun activePromo(): HabiticaPromotion? {
        val prefsPromo =
            if (BuildConfig.DEBUG || testingLevel() == AppTestingLevel.STAFF) {
                sharedPreferences.getString("active_promo", null)
            } else {
                null
            }
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
                if (event == null) continue
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
        if (promo is HabiticaWebPromotion) {
            promo.url = value(ClientConfigKey.surveyURL)
        }
        if (promo?.isActive == true) {
            return promo
        }
        return null
    }

    fun getBirthdayEvent(): WorldStateEvent? {
        val events =
            ((worldState?.events as? List<WorldStateEvent>) ?: listOf(worldState?.currentEvent))
        return events.firstOrNull { it?.eventKey == "birthday10" && it.end?.after(Date()) == true }
    }

    companion object {
        private const val CONFIG_STORAGE_KEY = "clientConfig"
        private const val LAST_FETCH_STORAGE_KEY = "clientConfigLastFetch"
        private val CONFIG_REFRESH_INTERVAL = 24.hours.inWholeMilliseconds
    }
}
