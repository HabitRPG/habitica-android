package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.WorldStateEvent
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.EquipmentSet
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.social.CategoryOption
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.shared.habitica.serialization.JsonTreeSerializer
import com.habitrpg.shared.habitica.serialization.array
import com.habitrpg.shared.habitica.serialization.bool
import com.habitrpg.shared.habitica.serialization.decode
import com.habitrpg.shared.habitica.serialization.double
import com.habitrpg.shared.habitica.serialization.int
import com.habitrpg.shared.habitica.serialization.obj
import com.habitrpg.shared.habitica.serialization.string
import io.realm.RealmList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import java.text.SimpleDateFormat
import java.util.Calendar

object ContentSerializer : JsonTreeSerializer<ContentResult>("ContentResult") {
    override fun Json.read(element: JsonElement): ContentResult {
        val result = ContentResult()
        val obj = element as JsonObject
        result.potion = decode(obj["potion"])
        result.armoire = decode(obj["armoire"])
        result.gear = decode(obj["gear"])
        obj.obj("quests")?.values?.forEach { quest -> decode<QuestContent>(quest)?.let { result.quests.add(it) } }
        // Generated serializers set fields directly, the key setter also passes the key on to drop, colors and boss
        result.quests.forEach { it.key = it.key }
        val eggs = obj.obj("eggs") ?: JsonObject(emptyMap())
        eggs.values.forEach { egg -> decode<Egg>(egg)?.let { result.eggs.add(it) } }
        obj.obj("food")?.values?.forEach { food -> decode<Food>(food)?.let { result.food.add(it) } }
        val potions = obj.obj("hatchingPotions") ?: JsonObject(emptyMap())
        potions.values.forEach { potion -> decode<HatchingPotion>(potion)?.let { result.hatchingPotions.add(it) } }
        obj.obj("petInfo")?.values?.forEach {
            val petObject = it as JsonObject
            result.pets.add(
                Pet().apply {
                    animal = petObject.string("egg") ?: ""
                    color = petObject.string("potion") ?: ""
                    key = petObject.string("key") ?: ""
                    val egg = eggs.obj(animal)?.string("text") ?: ""
                    val potion = potions.obj(color)?.string("text") ?: ""
                    text = if (eggs.containsKey(animal) && potions.containsKey(color)) "$potion $egg" else petObject.string("text") ?: ""
                    type = petObject.string("type") ?: ""
                    if (type == "premium") premium = true
                },
            )
        }
        obj.obj("mountInfo")?.values?.forEach {
            val mountObject = it as JsonObject
            result.mounts.add(
                Mount().apply {
                    animal = mountObject.string("egg") ?: ""
                    color = mountObject.string("potion") ?: ""
                    key = mountObject.string("key") ?: ""
                    val egg = eggs.obj(animal)?.string("mountText") ?: ""
                    val potion = potions.obj(color)?.string("text") ?: ""
                    text = if (eggs.containsKey(animal) && potions.containsKey(color)) "$potion $egg" else mountObject.string("text") ?: ""
                    type = mountObject.string("type") ?: ""
                    if (type == "premium") premium = true
                },
            )
        }
        obj.obj("spells")?.forEach { (habitClass, skills) ->
            (skills as JsonObject).values.forEach {
                val skillObject = it as JsonObject
                result.spells.add(
                    Skill().apply {
                        key = skillObject.string("key") ?: ""
                        text = skillObject.string("text") ?: ""
                        notes = skillObject.string("notes") ?: ""
                        target = skillObject.string("target") ?: ""
                        this.habitClass = habitClass
                        mana = skillObject.int("mana") ?: 0
                        skillObject.int("lvl")?.let { lvl = it }
                    },
                )
            }
        }
        obj.array("categoryOptions")?.forEach {
            val option = it as JsonObject
            result.categoryOptions.add(
                CategoryOption().apply {
                    label = option.string("label") ?: ""
                    key = option.string("key") ?: ""
                },
            )
        }
        obj.obj("special")?.values?.forEach { item -> decode<SpecialItem>(item)?.let { result.special.add(it) } }
        obj.obj("mystery")?.values?.forEach { set -> decode<EquipmentSet>(set)?.let { result.mystery.add(it) } }
        obj.obj("appearances")?.let { result.appearances = customizations(it) }
        obj.obj("backgrounds")?.let { result.backgrounds = customizations(it) }
        result.backgrounds.add(
            Customization().apply {
                customizationSet = "incentiveBackgrounds"
                customizationSetName = "Login Incentive"
                identifier = ""
                price = 0
                type = "background"
            },
        )
        obj.obj("faq")?.array("questions")?.forEachIndexed { index, it ->
            val question = it as JsonObject
            result.faq.add(
                FAQArticle().apply {
                    position = index
                    this.question = question.string("question")
                    answer = question.string("android") ?: question.string("web")
                },
            )
        }
        return result
    }

    /** Appearances are grouped by type (and hair by category), backgrounds by their monthly set. */
    private fun customizations(obj: JsonObject): RealmList<Customization> {
        val customizations = RealmList<Customization>()
        if (obj.containsKey("shirt")) {
            for (type in listOf("shirt", "skin", "chair")) {
                obj.obj(type)?.forEach { (key, value) -> customizations.add(appearance(type, null, key, value as JsonObject)) }
            }
            obj.obj("hair")?.forEach { (category, values) ->
                (values as JsonObject).forEach { (key, value) ->
                    customizations.add(appearance("hair", category, key, value as JsonObject))
                }
            }
        } else {
            obj.entries.forEachIndexed { setIndex, (setName, backgrounds) ->
                (backgrounds as JsonObject).forEach { (key, value) ->
                    customizations.add(background(setName, setIndex, key, value as JsonObject))
                }
            }
        }
        return customizations
    }

    private fun appearance(
        type: String,
        category: String?,
        key: String,
        entry: JsonObject,
    ) = Customization().apply {
        identifier = key
        this.type = type
        category?.let { this.category = it }
        entry.int("price")?.let { price = it }
        entry.obj("set")?.let { setInfo ->
            customizationSet = setInfo.string("key")
            setInfo.int("setPrice")?.let { setPrice = it }
            setInfo.string("text")?.let { customizationSetName = it }
            val format = SimpleDateFormat("yyyy-MM-dd")
            try {
                setInfo.string("availableFrom")?.let { availableFrom = format.parse(it) }
                setInfo.string("availableUntil")?.let { availableUntil = format.parse(it) }
            } catch (e: Exception) {
                ExceptionHandler.reportError(e)
            }
        }
    }

    private fun background(
        setName: String,
        setIndex: Int,
        key: String,
        entry: JsonObject,
    ) = Customization().apply {
        customizationSet = setName
        type = "background"
        identifier = key
        when (setName) {
            "eventBackgrounds" -> {
                customizationSetName = "EVENT BACKGROUNDS"
                price = 0
                setPrice = 0
                isBuyable = false
            }

            "incentiveBackgrounds" -> {
                customizationSetName = "PLAIN BACKGROUND SET"
                price = 0
                setPrice = 0
                isBuyable = false
            }

            "timeTravelBackgrounds" -> {
                customizationSetName = "STEAMPUNK BACKGROUNDS"
                price = 1
                setPrice = 0
                isBuyable = false
            }

            else -> {
                // Set names look like "backgrounds082014"
                val month = monthName(setName.substring(11, 13).toInt() - 1)
                customizationSetName = "SET ${setIndex + 1}: $month ${setName.substring(13, 17)}"
                customizationSet = setName.substring(13, 17) + "." + setName.substring(11, 13)
                price = 7
                setPrice = 15
            }
        }
        text = entry.string("text") ?: ""
        notes = entry.string("notes") ?: ""
    }

    private fun monthName(month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        return SimpleDateFormat("MMMM").format(calendar.time).uppercase()
    }
}

object WorldStateSerializer : JsonTreeSerializer<WorldState>("WorldState") {
    override fun Json.read(element: JsonElement): WorldState {
        val state = WorldState()
        val obj = element as? JsonObject ?: return state
        obj.obj("worldBoss")?.let { worldBoss ->
            worldBoss.bool("active")?.let { state.worldBossActive = it }
            worldBoss.string("key")?.let { state.worldBossKey = it }
            worldBoss.obj("progress")?.let { progress ->
                state.progress =
                    QuestProgress().apply {
                        progress.double("hp")?.let { hp = it }
                        progress.double("rage")?.let { rage = it }
                    }
            }
            worldBoss.obj("extra")?.obj("worldDmg")?.forEach { (key, value) ->
                state.rageStrikes?.add(QuestRageStrike(key, (value as? JsonPrimitive)?.booleanOrNull ?: false))
            }
        }
        state.npcImageSuffix = obj.string("npcImageSuffix") ?: ""
        try {
            obj.obj("currentEvent")?.let { event ->
                state.currentEvent = decode<WorldStateEvent>(event)
                obj.array("currentEventList")?.forEach { listEvent -> decode<WorldStateEvent>(listEvent)?.let { state.events.add(it) } }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return state
    }
}
