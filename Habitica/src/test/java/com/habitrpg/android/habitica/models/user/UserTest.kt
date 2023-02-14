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
        user.items = Items()
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

    "onboardingAchievements" should {
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
            user.onboardingAchievements[2].earned = true
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

    "petsFoundCount" should {
        "return sum of all pets" {
            val pets = RealmList<OwnedPet>()
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            user.items!!.pets = pets
            user.petsFoundCount shouldBe 5
        }

        "return zero on no pets collection" {
            user.items?.pets shouldBe null
            user.petsFoundCount shouldBe 0
        }
    }

    "mountsFoundCount" should {
        "return sum of all pets" {
            val mounts = RealmList<OwnedMount>()
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            user.items!!.mounts = mounts
            user.mountsTamedCount shouldBe 5
        }

        "return zero on no pets collection" {
            user.items?.mounts shouldBe null
            user.mountsTamedCount shouldBe 0
        }
    }

    "hasPermission" should {
        beforeEach {
            user.permissions = Permissions()
        }
        "return true for admin access" {
            user.permissions?.fullAccess = true
            user.hasPermission(Permission.USER_SUPPORT) shouldBe true
            user.hasPermission(Permission.MODERATOR) shouldBe true
        }

        "return true if has matching access" {
            user.permissions?.userSupport = true
            user.hasPermission(Permission.USER_SUPPORT) shouldBe true
        }

        "return false if does not have matching access" {
            user.permissions?.moderator = false
            user.hasPermission(Permission.MODERATOR) shouldBe false
        }
    }
})
