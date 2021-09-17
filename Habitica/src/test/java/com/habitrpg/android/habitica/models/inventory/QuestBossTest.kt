package com.habitrpg.android.habitica.models.inventory

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class QuestBossTest : StringSpec({
    val boss = QuestBoss()
    beforeEach {
        boss.rage = QuestBossRage()
    }
    "returns false for 0" {
        boss.rage?.value = 0.0
        boss.hasRage shouldBe false
    }
    "returns true for more than 0" {
        boss.rage?.value = 1000.0
        boss.hasRage shouldBe true
    }
    "returns false for no value" {
        boss.rage = null
        boss.hasRage shouldBe false
    }
})
