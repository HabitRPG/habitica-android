package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.common.habitica.models.Notification
import com.habitrpg.common.habitica.models.notifications.UnallocatedPointsData
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class NotificationDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read typed notification data" {
                val json = """{"id": "n1", "type": "UNALLOCATED_STATS_POINTS", "seen": true, "data": {"points": 3}}"""
                val notification = gson.fromJson(json, Notification::class.java)
                notification.id shouldBe "n1"
                notification.seen shouldBe true
                notification.data.shouldBeInstanceOf<UnallocatedPointsData>().points shouldBe 3
            }

            "skip data for unknown types" {
                val notification = gson.fromJson("""{"type": "UNKNOWN", "data": {"points": 3}}""", Notification::class.java)
                notification.data.shouldBeNull()
                notification.seen.shouldBeNull()
            }
        }
    })
