package com.habitrpg.android.habitica.models.user

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.WordSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

@ExperimentalKotest
class SpecialItemsTest : WordSpec({
    data class SpecialItemsData(val snowBalls: Int, val seafoam: Int, val shinyseed: Int, val spookySparkles: Int)
    lateinit var items: SpecialItems
    beforeEach {
        items = SpecialItems()
    }
    "hasSpecialItems" should {
        "false if none are owned" {
            items.hasSpecialItems shouldBe false
        }
        "true if any are owned" {
            items.snowball = 4
            items.hasSpecialItems shouldBe true
        }
        withData(
            SpecialItemsData(1, 0, 0, 0),
            SpecialItemsData(0, 1, 0, 0),
            SpecialItemsData(0, 0, 1, 0),
            SpecialItemsData(0, 0, 0, 1),
            SpecialItemsData(1, 3, 4, 8)
        ) { (snowballs, seafoam, shinyseeds, spookySparkles) ->
            items.snowball = snowballs
            items.seafoam = seafoam
            items.shinySeed = shinyseeds
            items.spookySparkles = spookySparkles
            items.hasSpecialItems shouldBe true
        }
    }
})
