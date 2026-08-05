package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.user.OwnedCustomization
import com.habitrpg.android.habitica.models.user.Purchases
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import java.lang.reflect.Type

class PurchasedDeserializer : JsonDeserializer<Purchases?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Purchases {
        val obj = json.getAsJsonObject()
        val purchases = Purchases()

        for (type in listOf("background", "shirt", "skin")) {
            if (!obj.has(type)) {
                continue
            }
            for (entry in obj.get(type).getAsJsonObject().entrySet()) {
                purchases.customizations?.add(
                    this.parseCustomization(
                        type,
                        null,
                        entry.key,
                        entry.value.getAsBoolean()
                    )
                )
            }
        }
        if (obj.has("hair")) {
            for (categoryEntry in obj.get("hair").getAsJsonObject().entrySet()) {
                for (entry in categoryEntry.value.getAsJsonObject().entrySet()) {
                    purchases.customizations?.add(
                        this.parseCustomization(
                            "hair",
                            categoryEntry.key,
                            entry.key,
                            entry.value.getAsBoolean()
                        )
                    )
                }
            }
        }

        purchases.plan = context.deserialize<SubscriptionPlan?>(
            obj.get("plan"),
            SubscriptionPlan::class.java
        )

        return purchases
    }

    private fun parseCustomization(
        type: String?,
        category: String?,
        key: String?,
        wasPurchased: Boolean
    ): OwnedCustomization {
        val customization = OwnedCustomization()
        customization.key = key
        customization.type = type
        if (category != null) {
            customization.category = category
        }
        customization.purchased = wasPurchased

        return customization
    }
}
