package com.habitrpg.android.habitica.utils

import android.annotation.SuppressLint
import com.google.gson.*
import com.habitrpg.android.habitica.models.inventory.Customization
import io.realm.Realm
import io.realm.RealmList
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class CustomizationDeserializer : JsonDeserializer<List<Customization>> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Customization> {
        val jsonObject = json.asJsonObject
        val customizations = RealmList<Customization>()
        val realm = Realm.getDefaultInstance()

        val existingCustomizations = realm.copyFromRealm(realm.where(Customization::class.java).findAll())
        if (jsonObject.has("shirt")) {
            for (customization in existingCustomizations) {
                if (jsonObject.has(customization.type)) {
                    var nestedObject = jsonObject.get(customization.type).asJsonObject
                    if (customization.category != null) {
                        if (nestedObject.has(customization.category)) {
                            nestedObject = nestedObject.get(customization.category).asJsonObject
                        } else {
                            continue
                        }
                    }
                    if (nestedObject.has(customization.identifier)) {
                        customizations.add(this.parseCustomization(customization, customization.type, customization.category, customization.identifier, nestedObject.get(customization.identifier).asJsonObject))
                        nestedObject.remove(customization.identifier)
                    }
                }
            }

            for (type in listOf("shirt", "skin", "chair")) {
                for ((key, value) in jsonObject.get(type).asJsonObject.entrySet()) {
                    customizations.add(this.parseCustomization(null, type, null, key, value.asJsonObject))
                }
            }

            for ((key, value) in jsonObject.get("hair").asJsonObject.entrySet()) {
                for ((key1, value1) in value.asJsonObject.entrySet()) {
                    customizations.add(this.parseCustomization(null, "hair", key, key1, value1.asJsonObject))
                }
            }
        } else {
            for (customization in existingCustomizations) {
                if (jsonObject.has(customization.customizationSet)) {
                    val nestedObject = jsonObject.get(customization.customizationSet).asJsonObject
                    if (nestedObject.has(customization.identifier)) {
                        customizations.add(this.parseBackground(customization, customization.customizationSet ?: "", customization.identifier, nestedObject.get(customization.identifier).asJsonObject))
                        nestedObject.remove(customization.identifier)
                    }
                }
            }

            for ((key, value) in jsonObject.entrySet()) {
                for ((key1, value1) in value.asJsonObject.entrySet()) {
                    customizations.add(this.parseBackground(null, key, key1, value1.asJsonObject))
                }
            }
        }

        realm.close()

        return customizations
    }

    private fun parseCustomization(existingCustomizaion: Customization?, type: String?, category: String?, key: String?, entry: JsonObject): Customization {
        var customization = existingCustomizaion
        if (customization == null) {
            customization = Customization()
            customization.identifier = key
            customization.type = type
            if (category != null) {
                customization.category = category
            }
        }
        if (entry.has("price")) {
            customization.price = entry.get("price").asInt
        }

        if (entry.has("set")) {
            val setInfo = entry.get("set").asJsonObject
            customization.customizationSet = setInfo.get("key").asString
            if (setInfo.has("setPrice")) {
                customization.setPrice = setInfo.get("setPrice").asInt
            }
            if (setInfo.has("text")) {
                customization.customizationSetName = setInfo.get("text").asString
            }
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd")
            try {
                if (setInfo.has("availableFrom")) {
                    customization.availableFrom = format.parse(setInfo.get("availableFrom").asString)
                }
                if (setInfo.has("availableUntil")) {
                    customization.availableUntil = format.parse(setInfo.get("availableUntil").asString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return customization
    }

    private fun parseBackground(existingCustomization: Customization?, setName: String, key: String?, entry: JsonObject): Customization {
        var customization = existingCustomization

        if (customization == null) {
            customization = Customization()
            customization.customizationSet = setName
            val readableSetName = setName.substring(13, 17) + "." + setName.substring(11, 13)
            customization.customizationSetName = readableSetName
            customization.type = "background"
            customization.identifier = key
        }
        when (setName) {
            "incentiveBackgrounds" -> {
                customization.customizationSetName = "Login Incentive"
                customization.price = 0
                customization.setPrice = 0
                customization.isBuyable = false
            }
            "timeTravelBackgrounds" -> {
                customization.customizationSetName = "Time Travel Backgrounds"
                customization.price = 1
                customization.setPrice = 0
                customization.isBuyable = false
            }
            else -> {
                customization.price = 7
                customization.setPrice = 15
            }
        }

        customization.text = entry.get("text").asString
        customization.notes = entry.asJsonObject.get("notes").asString

        return customization
    }
}
