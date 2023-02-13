package com.habitrpg.android.habitica.models.members

import com.habitrpg.android.habitica.models.user.Stats
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class MemberTest : WordSpec({
    val member = Member()
    beforeEach {
        member.preferences = MemberPreferences()
        member.stats = Stats()
    }
    "hasClass" should {
        "false if classes are disabled" {
            member.preferences?.disableClasses = true
            member.hasClass shouldBe false
        }

        "false if class is empty" {
            member.hasClass shouldBe false
        }

        "false if no class was chosen" {
            member.flags?.classSelected = false
            member.hasClass shouldBe false
        }

        "false if class was selected but then disabled" {
            member.flags?.classSelected = true
            member.preferences?.disableClasses = true
            member.hasClass shouldBe false
        }

        "false if user is below level 10" {
            member.flags?.classSelected = true
            member.stats?.habitClass = Stats.ROGUE
            member.stats?.lvl = 9
            member.hasClass shouldBe false
        }

        "true if class was selected and not disabled" {
            member.flags?.classSelected = true
            member.stats?.habitClass = Stats.ROGUE
            member.stats?.lvl = 10
            member.hasClass shouldBe true
        }
    }
})
