package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.social.UserParty
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import java.util.UUID

class UserTest : WordSpec({
    val user = User()
    beforeEach {
        user.preferences = Preferences()
        user.stats = Stats()
        user.flags = Flags()
        user.party = UserParty()
        user.purchased = Purchases()
        user.purchased?.plan = SubscriptionPlan()
    }
    "hasClass" should {
        "false if classes are disabled" {
            user.preferences?.disableClasses = true
            user.hasClass shouldBe false
        }

        "false if class is empty" {
            user.hasClass shouldBe false
        }

        "false if no class was chosen" {
            user.flags?.classSelected = false
            user.hasClass shouldBe false
        }

        "false if class was selected but then disabled" {
            user.flags?.classSelected = true
            user.preferences?.disableClasses = true
            user.hasClass shouldBe false
        }

        "false if user is below level 10" {
            user.flags?.classSelected = true
            user.stats?.habitClass = Stats.ROGUE
            user.stats?.lvl = 9
            user.hasClass shouldBe false
        }

        "true if class was selected and not disabled" {
            user.flags?.classSelected = true
            user.stats?.habitClass = Stats.ROGUE
            user.stats?.lvl = 10
            user.hasClass shouldBe true
        }
    }

    "Onboarding Achievements" should {
        beforeEach {
            user.achievements = RealmList()
            for (key in User.ONBOARDING_ACHIEVEMENT_KEYS) {
                user.achievements.add(UserAchievement(key, false))
            }
        }

        "hasCompletedOnboarding should be true if all onboarding achievements are completed" {
            user.onboardingAchievements.forEach { it.earned = true }
            user.hasCompletedOnboarding shouldBe true
        }

        "hasCompletedOnboarding should be false if not all onboarding achievements are completed" {
            user.onboardingAchievements.get(2).earned = true
            user.hasCompletedOnboarding shouldBe false
        }
    }

    "hasParty" should {
        "true if user has valid party" {
            user.party?.id = UUID.randomUUID().toString()
            user.hasParty shouldBe true
        }

        "false if user has no party data" {
            user.party = null
            user.hasParty shouldBe false
        }

        "false if user has invalid party" {
            user.hasParty shouldBe false
        }
    }
})
