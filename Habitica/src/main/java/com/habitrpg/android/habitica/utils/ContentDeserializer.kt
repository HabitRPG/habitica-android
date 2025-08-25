package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.ContentGear
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.EquipmentSet
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.social.CategoryOption
import io.realm.RealmList
import java.lang.reflect.Type

class ContentDeserializer : JsonDeserializer<ContentResult> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ContentResult {
        val deserializeTrace = FirebasePerformance.getInstance().newTrace("ContentDeserialize")
        deserializeTrace.start()
        val result = ContentResult()
        val obj = json.asJsonObject

        result.potion = context.deserialize(obj.get("potion"), Equipment::class.java)
        result.armoire = context.deserialize(obj.get("armoire"), Equipment::class.java)
        result.gear = context.deserialize(obj.get("gear"), ContentGear::class.java)

        for (entry in obj.get("quests").asJsonObject.entrySet()) {
            result.quests.add(context.deserialize(entry.value, QuestContent::class.java))
            result.quests.forEach { it.key = it.key }
        }
        for (entry in obj.get("eggs").asJsonObject.entrySet()) {
            result.eggs.add(context.deserialize(entry.value, Egg::class.java))
        }
        for (entry in obj.get("food").asJsonObject.entrySet()) {
            result.food.add(context.deserialize(entry.value, Food::class.java))
        }
        for (entry in obj.get("hatchingPotions").asJsonObject.entrySet()) {
            result.hatchingPotions.add(context.deserialize(entry.value, HatchingPotion::class.java))
        }

        val pets = obj.getAsJsonObject("petInfo")
        for (key in pets.keySet()) {
            val pet = Pet()
            val petObj = pets.getAsJsonObject(key)
            pet.animal = petObj.getAsString("egg")
            pet.color = petObj.getAsString("potion")
            pet.key = petObj.getAsString("key")
            pet.text = petObj.getAsString("text")
            pet.type = petObj.getAsString("type")
            if (pet.type == "premium") {
                pet.premium = true
            }
            result.pets.add(pet)
        }

        val mounts = obj.getAsJsonObject("mountInfo")
        for (key in mounts.keySet()) {
            val mount = Mount()
            val mountObj = mounts.getAsJsonObject(key)
            mount.animal = mountObj.getAsString("egg")
            mount.color = mountObj.getAsString("potion")
            mount.key = mountObj.getAsString("key")
            mount.text = mountObj.getAsString("text")
            mount.type = mountObj.getAsString("type")
            if (mount.type == "premium") {
                mount.premium = true
            }
            result.mounts.add(mount)
        }
        for ((classname, value) in obj.getAsJsonObject("spells").entrySet()) {
            val classObject = value.asJsonObject

            for ((_, value1) in classObject.entrySet()) {
                val skillObject = value1.asJsonObject
                val skill = Skill()
                skill.key = skillObject.get("key").asString
                skill.text = skillObject.get("text").asString
                skill.notes = skillObject.get("notes").asString
                skill.key = skillObject.get("key").asString
                skill.target = skillObject.get("target").asString
                skill.habitClass = classname
                skill.mana = skillObject.get("mana").asInt

                val lvlElement = skillObject.get("lvl")

                if (lvlElement != null) {
                    skill.lvl = lvlElement.asInt
                }

                result.spells.add(skill)
            }
        }

        if (obj.has("categoryOptions")) {
            obj.getAsJsonArray("categoryOptions").forEach { elem ->
                val o     = elem.asJsonObject
                val label = o.get("label").asString
                val key   = o.get("key").asString
                result.categoryOptions.add(CategoryOption().also {
                    it.label = label
                    it.key = key
                })
            }
        }

        if (obj.has("special")) {
            for (entry in obj.get("special").asJsonObject.entrySet()) {
                result.special.add(context.deserialize(entry.value, SpecialItem::class.java))
            }
        }

        if (obj.has("mystery")) {
            for (entry in obj.get("mystery").asJsonObject.entrySet()) {
                result.mystery.add(context.deserialize(entry.value, EquipmentSet::class.java))
            }
        }

        result.appearances =
            context.deserialize(
                obj.get("appearances"),
                object : TypeToken<RealmList<Customization>>() {}.type
            )
        result.backgrounds =
            context.deserialize(
                obj.get("backgrounds"),
                object : TypeToken<RealmList<Customization>>() {}.type
            )
        val noBackground = Customization()
        noBackground.customizationSet = "incentiveBackgrounds"
        noBackground.customizationSetName = "Login Incentive"
        noBackground.identifier = ""
        noBackground.price = 0
        noBackground.type = "background"
        result.backgrounds.add(noBackground)

        result.faq =
            context.deserialize(obj.get("faq"), object : TypeToken<RealmList<FAQArticle>>() {}.type)
        deserializeTrace.stop()
        return result
    }
}
