package com.habitrpg.android.habitica.helpers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

class ClientConfigKey<T> private constructor(
    val key: String,
    val defaultValue: T,
    internal val serializer: KSerializer<T>,
) {
    companion object {
        private inline fun <reified T> key(
            key: String,
            default: T,
        ) = ClientConfigKey(key, default, serializer<T>())

        val supportEmail = key("supportEmail", "admin@habitica.com")
        val feedbackURL = key("feedbackURL", "")
        val surveyURL = key("surveyURL", "")

        val maxChatLength = key("maxChatLength", 3000L)
        val lastVersionNumber = key("lastVersionNumber", "")
        val lastVersionCode = key("lastVersionCode", 0L)
        val knownIssues = key<List<Map<String, String>>>("knownIssues", emptyList())

        val enableUsernameAutocomplete = key("enableUsernameAutocomplete", false)
        val enableTaskDisplayMode = key("enableTaskDisplayMode", true)
        val showSubscriptionBanner = key("showSubscriptionBanner", false)
        val hideChallenges = key("hideChallenges", false)
        val reviewCheckingMinCount = key("reviewCheckingMinCount", 1L)
        val showAltDeathText = key("showAltDeathText", false)
    }
}
