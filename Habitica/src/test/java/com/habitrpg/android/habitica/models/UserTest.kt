package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.kotest.matchers.shouldBe
import io.realm.RealmList

class UserTest : BaseAnnotationTestCase() {
    private var user: User? = null
    @BeforeEach
    fun setup() {
        user = User()
        val items = Items()
        user!!.items = items
    }

    @get:Test
    val petsFoundCount_shouldReturnSumOfAllPetEntries: Unit
        get() {
            val pets = RealmList<OwnedPet>()
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            pets.add(OwnedPet())
            user!!.items!!.pets = pets
            user?.petsFoundCount shouldBe 5
        }

    @get:Test
    val petsFoundCount_onNoPetCollectionAvailable_shouldReturnZero: Unit
        get() {
            user?.petsFoundCount shouldBe 0
        }

    @get:Test
    val mountsTamedCount_shouldReturnSumOfAllMountEntries: Unit
        get() {
            val mounts = RealmList<OwnedMount>()
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            mounts.add(OwnedMount())
            user!!.items!!.mounts = mounts
            user?.mountsTamedCount shouldBe 5
        }

    @get:Test
    val mountsTamedCount_onNoMountCollectionAvailable_shouldReturnZero: Unit
        get() {
            user?.mountsTamedCount shouldBe 0
        }
}
