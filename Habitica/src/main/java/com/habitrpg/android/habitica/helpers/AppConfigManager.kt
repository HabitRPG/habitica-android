package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.preference.PreferenceManager
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
import com.habitrpg.shared.habitica.serialization.habiticaJson
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.util.Date
import javax.inject.Provider
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AppConfigManager(
    private val contentRepository: Provider<ContentRepository>,
    private val sharedPreferences: SharedPreferences,
) : com.habitrpg.common.habitica.helpers.AppConfigManager(),
    Clearable {
    private var worldState: WorldState? = null

    private val config = MutableStateFlow(loadStoredConfig())

    @Volatile
    private var configVersion: String? =
        sharedPreferences.getString(CONFIG_VERSION_STORAGE_KEY, null).takeIf { config.value.isNotEmpty() }

    @Volatile
    private var latestServerVersion: String? = null
    private var lastVersionFetch: Long? = null

    @Volatile
    private var lastFetchInSession: Long? = null
    private var fetchJob: Job? = null

    @Synchronized
    override fun clear() {
        fetchJob?.cancel()
        fetchJob = null
        lastFetchInSession = null
        lastVersionFetch = null
        configVersion = null
        latestServerVersion = null
        config.value = JsonObject(emptyMap())
    }

    @Synchronized
    fun onServerConfigVersion(version: String) {
        latestServerVersion = version
        if (version == configVersion || fetchJob?.isActive == true) return
        val now = System.currentTimeMillis()
        if (lastVersionFetch?.let { now - it < VERSION_REFETCH_INTERVAL } == true) return
        lastVersionFetch = now
        fetchConfig(forced = true)
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

    fun <T> value(key: ClientConfigKey<T>): T = decode(config.value, key)

    fun <T> valueFlow(key: ClientConfigKey<T>): Flow<T> = config.map { decode(it, key) }.distinctUntilChanged()

    private fun <T> decode(
        config: JsonObject,
        key: ClientConfigKey<T>,
    ): T {
        val raw = config[key.key]
        if (raw == null || raw is JsonNull) return key.defaultValue
        return try {
            val element =
                if (raw is JsonPrimitive && raw.isString && key.serializer.descriptor.kind != PrimitiveKind.STRING) {
                    habiticaJson.parseToJsonElement(raw.content)
                } else {
                    raw
                }
            habiticaJson.decodeFromJsonElement(key.serializer, element) ?: key.defaultValue
        } catch (_: IllegalArgumentException) {
            key.defaultValue
        }
    }

    @Synchronized
    fun fetchConfig(forced: Boolean = false) {
        if (!forced) {
            val lastFetch = lastFetchInSession
            if (lastFetch != null && System.currentTimeMillis() - lastFetch < CONFIG_REFRESH_INTERVAL) return
            if (fetchJob?.isActive == true) return
        }
        fetchJob?.cancel()
        fetchJob =
            MainScope().launchCatching {
                contentRepository.get().retrieveClientConfig()?.let { updateConfig(it) }
            }
    }

    @VisibleForTesting
    internal fun updateConfig(newConfig: JsonObject) {
        val now = System.currentTimeMillis()
        config.value = newConfig
        lastFetchInSession = now
        // The client-config response reported its version before its data got here
        configVersion = latestServerVersion
        sharedPreferences.edit {
            putString(CONFIG_STORAGE_KEY, newConfig.toString())
            putString(CONFIG_VERSION_STORAGE_KEY, configVersion)
        }
    }

    // Use the config from the last launch until a fresh one is loaded
    private fun loadStoredConfig(): JsonObject =
        try {
            sharedPreferences.getString(CONFIG_STORAGE_KEY, null)?.let { habiticaJson.parseToJsonElement(it).jsonObject }
        } catch (_: RuntimeException) {
            null
        } ?: JsonObject(emptyMap())

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
        private const val CONFIG_VERSION_STORAGE_KEY = "clientConfigVersion"
        private val CONFIG_REFRESH_INTERVAL = 24.hours.inWholeMilliseconds
        private val VERSION_REFETCH_INTERVAL = 1.minutes.inWholeMilliseconds
    }
}
