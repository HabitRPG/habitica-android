package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class StableViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val userRepository = mockk<UserRepository>(relaxed = true)
        val mainUserViewModel = mockk<MainUserViewModel>()
        val inventoryRepository = mockk<InventoryRepository>(relaxed = true)

        every { mainUserViewModel.user } returns MutableLiveData()

        fun makeViewModel(itemType: String) =
            StableViewModel(SavedStateHandle(mapOf("CLASS_TYPE_KEY" to itemType)), userRepository, mainUserViewModel, inventoryRepository)

        fun pet(
            key: String,
            type: String = "drop",
        ) = Pet().apply {
            this.key = key
            this.type = type
        }

        fun ownedPet(
            key: String,
            trained: Int,
        ) = OwnedPet().apply {
            this.key = key
            this.trained = trained
        }

        beforeEach { Dispatchers.setMain(testDispatcher) }

        "items" should {
            "group pets by animal and count owned ones per section" {
                val wolfBase = pet("Wolf-Base")
                val foxBase = pet("Fox-Base")
                every { inventoryRepository.getPets() } returns flowOf(listOf(wolfBase, pet("Wolf-Red"), foxBase))
                every { inventoryRepository.getOwnedPets() } returns flowOf(listOf(ownedPet("Wolf-Base", 5), ownedPet("Fox-Base", 0)))

                val items = makeViewModel("pets").items.getOrAwaitValue()

                items[0] shouldBe "header"
                items[1].shouldBeInstanceOf<StableSection>().run {
                    key shouldBe "drop"
                    ownedCount shouldBe 1
                    totalCount shouldBe 3
                }
                items.drop(2) shouldBe listOf(wolfBase, foxBase)
                wolfBase.numberOwned shouldBe 1
                wolfBase.totalNumber shouldBe 2
                foxBase.totalNumber shouldBe 1
            }

            "hide special pets and their section if none are owned" {
                every { inventoryRepository.getPets() } returns flowOf(listOf(pet("Wolf-Base"), pet("Jackalope-RoyalPurple", "special")))
                every { inventoryRepository.getOwnedPets() } returns flowOf(emptyList())

                val items = makeViewModel("pets").items.getOrAwaitValue()

                items.filterIsInstance<StableSection>().map { it.key } shouldBe listOf("drop")
                items.filterIsInstance<Pet>().map { it.key } shouldBe listOf("Wolf-Base")
            }

            "count owned mounts" {
                val wolf = Mount().apply { key = "Wolf-Base"; animal = "Wolf"; type = "drop" }
                every { inventoryRepository.getMounts() } returns flowOf(listOf(wolf))
                every { inventoryRepository.getOwnedMounts() } returns flowOf(listOf(OwnedMount().apply { key = "Wolf-Base"; owned = true }))

                makeViewModel("mounts").items.getOrAwaitValue()

                wolf.numberOwned shouldBe 1
            }

            "only contain the header if there are no animals" {
                every { inventoryRepository.getPets() } returns flowOf(emptyList())
                every { inventoryRepository.getOwnedPets() } returns flowOf(emptyList())
                makeViewModel("pets").items.getOrAwaitValue() shouldBe emptyList()
            }
        }

        "ownedPets" should {
            "map owned pets by key" {
                val owned = ownedPet("Wolf-Base", 5)
                every { inventoryRepository.getPets() } returns flowOf(emptyList())
                every { inventoryRepository.getOwnedPets() } returns flowOf(listOf(owned))
                makeViewModel("pets").ownedPets.getOrAwaitValue() shouldBe mapOf("Wolf-Base" to owned)
            }
        }

        "eggs" should {
            "map eggs by key" {
                val egg = Egg().apply { key = "Wolf" }
                every { inventoryRepository.getItems(Egg::class.java) } returns flowOf(listOf(egg))
                makeViewModel("pets").eggs.getOrAwaitValue() shouldBe mapOf("Wolf" to egg)
            }
        }
    })
