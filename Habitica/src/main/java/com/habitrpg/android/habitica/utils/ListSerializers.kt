package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.SpriteSubstitutionContext
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.QuestCollect
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import com.habitrpg.android.habitica.models.tasks.GroupAssignedDetails
import com.habitrpg.android.habitica.models.user.OwnedEquipment
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.shared.habitica.serialization.JsonTreeSerializer
import com.habitrpg.shared.habitica.serialization.bool
import com.habitrpg.shared.habitica.serialization.decode
import com.habitrpg.shared.habitica.serialization.int
import com.habitrpg.shared.habitica.serialization.obj
import com.habitrpg.shared.habitica.serialization.string
import io.realm.RealmList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/** Reads a JSON object like `{"Wolf-Base": 5}` into a list with one entry per key. */
abstract class KeyedListSerializer<T>(
    serialName: String,
) : JsonTreeSerializer<RealmList<T>>(serialName) {
    /** Returns null to skip the entry. */
    abstract fun Json.item(
        key: String,
        value: JsonElement,
    ): T?

    override fun Json.read(element: JsonElement): RealmList<T> {
        val list = RealmList<T>()
        (element as? JsonObject)?.forEach { (key, value) -> item(key, value)?.let { list.add(it) } }
        return list
    }
}

object OwnedItemListSerializer : KeyedListSerializer<OwnedItem>("OwnedItems") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = (value as? JsonPrimitive)?.takeUnless { it is JsonNull }?.let {
        OwnedItem().apply {
            this.key = key
            numberOwned = it.doubleOrNull?.toInt() ?: 0
        }
    }
}

object OwnedPetListSerializer : KeyedListSerializer<OwnedPet>("OwnedPets") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = OwnedPet().apply {
        this.key = key
        trained = (value as? JsonPrimitive)?.doubleOrNull?.toInt() ?: 0
    }
}

object OwnedMountListSerializer : KeyedListSerializer<OwnedMount>("OwnedMounts") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = OwnedMount().apply {
        this.key = key
        owned = (value as? JsonPrimitive)?.booleanOrNull ?: false
    }
}

object OwnedEquipmentListSerializer : KeyedListSerializer<OwnedEquipment>("OwnedEquipment") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = (value as? JsonPrimitive)?.takeUnless { it is JsonNull }?.let {
        OwnedEquipment().apply {
            this.key = key
            owned = it.booleanOrNull ?: false
        }
    }
}

object QuestCollectListSerializer : KeyedListSerializer<QuestCollect>("QuestCollects") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = QuestCollect().apply {
        this.key = key
        count = (value as JsonObject).int("count") ?: 0
        text = value.string("text")
    }
}

object SpriteSubstitutionListSerializer : KeyedListSerializer<SpriteSubstitutionContext>("SpriteSubstitutions") {
    override fun Json.item(
        key: String,
        value: JsonElement,
    ) = SpriteSubstitutionContext()
        .apply {
            this.key = key
            (value as JsonObject).forEach { (from, to) -> substitutions[from] = (to as JsonPrimitive).content }
        }.takeIf { it.substitutions.isNotEmpty() }
}

/** `{"common": {"habits": true}, "android": {...}}` */
object TutorialStepListSerializer : JsonTreeSerializer<RealmList<TutorialStep>>("TutorialSteps") {
    override fun Json.read(element: JsonElement): RealmList<TutorialStep> {
        val steps = RealmList<TutorialStep>()
        for (group in listOf("common", "android")) {
            (element as JsonObject).obj(group)?.forEach { (key, value) ->
                steps.add(
                    TutorialStep().apply {
                        tutorialGroup = group
                        identifier = key
                        wasCompleted = (value as? JsonPrimitive)?.booleanOrNull ?: false
                    },
                )
            }
        }
        return steps
    }
}

/** Either an array or an object keyed by item key. */
object EquipmentListSerializer : JsonTreeSerializer<RealmList<Equipment>>("EquipmentList") {
    override fun Json.read(element: JsonElement): RealmList<Equipment> {
        val values = if (element is JsonObject) element.values else element as JsonArray
        return RealmList<Equipment>().apply { values.forEach { decode<Equipment>(it)?.let(::add) } }
    }
}

/** Repeated drops are merged into one item with a count. */
object QuestDropItemListSerializer : JsonTreeSerializer<RealmList<QuestDropItem>>("QuestDropItems") {
    override fun Json.read(element: JsonElement): RealmList<QuestDropItem> {
        val items = RealmList<QuestDropItem>()
        for (entry in element as JsonArray) {
            val item = decode<QuestDropItem>(entry) ?: continue
            val existing = items.firstOrNull { it.key == item.key }
            if (existing != null) {
                existing.count += 1
            } else {
                item.count = 1
                items.add(item)
            }
        }
        return items
    }
}

/** Either an array or an object keyed by the assigned user's ID. */
object AssignedDetailsListSerializer : JsonTreeSerializer<RealmList<GroupAssignedDetails>>("AssignedDetails") {
    override fun Json.read(element: JsonElement): RealmList<GroupAssignedDetails> {
        val list = RealmList<GroupAssignedDetails>()
        when (element) {
            is JsonArray -> element.forEach { decode<GroupAssignedDetails>(it)?.let(list::add) }
            is JsonObject ->
                element.forEach { (userID, value) ->
                    decode<GroupAssignedDetails>(value)?.apply { assignedUserID = userID }?.let(list::add)
                }
            else -> {}
        }
        return list
    }
}

/** `{"basic": {"achievements": {"streak": {...}}}}`, flattened with the category on each achievement. */
object AchievementListSerializer : JsonTreeSerializer<List<Achievement>>("Achievements") {
    override fun Json.read(element: JsonElement): List<Achievement> =
        (element as JsonObject).flatMap { (category, value) ->
            (value as JsonObject).obj("achievements").orEmpty().map { (key, achievementElement) ->
                val obj = achievementElement as JsonObject
                Achievement().apply {
                    this.key = key
                    this.category = category
                    earned = obj.bool("earned") ?: false
                    title = obj.string("title") ?: ""
                    text = obj.string("text") ?: ""
                    icon = obj.string("icon") ?: ""
                    index = obj.int("index") ?: 0
                    optionalCount = obj.int("optionalCount") ?: 0
                }
            }
        }
}
