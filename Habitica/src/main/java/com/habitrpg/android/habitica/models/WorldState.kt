package com.habitrpg.android.habitica.models

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import java.util.*

open class WorldState: RealmObject() {
    var worldBossKey: String = ""
    var worldBossActive: Boolean = false
    var progress: QuestProgress? = null
    var rageStrikes: RealmList<QuestRageStrike>? = null

    var npcImageSuffix: String? = null

    var currentEvent: WorldStateEvent? = null
    @SerializedName("currentEventList")
    var events: RealmList<WorldStateEvent> = RealmList()
}