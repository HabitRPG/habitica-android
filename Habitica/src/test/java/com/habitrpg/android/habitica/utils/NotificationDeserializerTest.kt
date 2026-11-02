package com.habitrpg.android.habitica.utils

import com.habitrpg.shared.habitica.models.Notification
import com.habitrpg.shared.habitica.models.notifications.UnallocatedPointsData
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class NotificationDeserializerTest :
    SerializerSpec({

        "deserialize" should {
            "read typed notification data" {
                val json = """{"id": "n1", "type": "UNALLOCATED_STATS_POINTS", "seen": true, "data": {"points": 3}}"""
                val notification = parse<Notification>(json)
                notification.id shouldBe "n1"
                notification.seen shouldBe true
                notification.data.shouldBeInstanceOf<UnallocatedPointsData>().points shouldBe 3
            }

            "skip data for unknown types" {
                val notification = parse<Notification>("""{"type": "UNKNOWN", "data": {"points": 3}}""")
                notification.data.shouldBeNull()
                notification.seen.shouldBeNull()
            }

            "keep the notification when its data has an unexpected shape" {
                val notification = parse<Notification>("""{"id": "n1", "type": "UNALLOCATED_STATS_POINTS", "data": {"points": "many"}}""")
                notification.id shouldBe "n1"
                notification.data.shouldBeNull()
            }
        }
    })
