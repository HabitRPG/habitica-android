package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import io.realm.RealmList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserTest {
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
            Assertions.assertEquals(5, user!!.petsFoundCount)
        }

    @get:Test
    val petsFoundCount_onNoPetCollectionAvailable_shouldReturnZero: Unit
        get() {
            Assertions.assertEquals(0, user!!.petsFoundCount)
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
            Assertions.assertEquals(5, user!!.mountsTamedCount)
        }

    @get:Test
    val mountsTamedCount_onNoMountCollectionAvailable_shouldReturnZero: Unit
        get() {
            Assertions.assertEquals(0, user!!.mountsTamedCount)
        }
}
