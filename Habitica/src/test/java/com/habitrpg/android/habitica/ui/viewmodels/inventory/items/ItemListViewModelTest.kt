package com.habitrpg.android.habitica.ui.viewmodels.inventory.items

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ItemListViewModelTest :
    WordSpec({
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        val inventoryRepository = mockk<InventoryRepository>()
        val socialRepository = mockk<SocialRepository>()
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val reviewManager = mockk<ReviewManager>(relaxed = true)
        val hatchPetUseCase = mockk<HatchPetUseCase>()
        val feedPetUseCase = mockk<FeedPetUseCase>()
        val userRepository = mockk<UserRepository>()
        val mainUserViewModel = mockk<MainUserViewModel>()

        every { mainUserViewModel.user } returns mockk(relaxed = true)
        every { userRepository.getSkills(any()) } returns flowOf(emptyList())
        every { userRepository.getSpecialItems(any()) } returns flowOf(emptyList())
        every { inventoryRepository.getPets() } returns flowOf(emptyList())
        every { inventoryRepository.getOwnedPets() } returns flowOf(emptyList())

        fun makeViewModel(itemType: String = "eggs") =
            ItemListViewModel(
                SavedStateHandle(mapOf(ItemRecyclerFragment.ARG_ITEM_TYPE to itemType)),
                inventoryRepository,
                socialRepository,
                sharedPreferences,
                reviewManager,
                hatchPetUseCase,
                feedPetUseCase,
                userRepository,
                mainUserViewModel,
            )

        beforeEach { Dispatchers.setMain(testDispatcher) }
        afterEach { clearMocks(inventoryRepository, userRepository, socialRepository, answers = false) }

        "ownedItems" should {
            "load the owned items for the item type from the fragment arguments" {
                val owned = listOf(OwnedItem().apply { key = "Saddle" })
                every { inventoryRepository.getOwnedItems("food") } returns flowOf(owned)

                val result = makeViewModel("food").ownedItems.first()

                result shouldBe owned
                verify(exactly = 1) { inventoryRepository.getOwnedItems("food") }
            }
        }

        "items" should {
            "load eggs and key them by their item key" {
                val owned = listOf(OwnedItem().apply { key = "egg_Wolf" }, OwnedItem().apply { key = "egg_Cactus" })
                every { inventoryRepository.getOwnedItems("eggs") } returns flowOf(owned)
                val wolfEgg = Egg().apply { key = "egg_Wolf" }
                val cactusEgg = Egg().apply { key = "egg_Cactus" }
                every { inventoryRepository.getItems(Egg::class.java, any()) } returns flowOf(listOf(wolfEgg, cactusEgg))

                val viewModel = makeViewModel()
                val result = viewModel.items.first()

                result shouldBe mapOf("egg_Wolf" to wolfEgg, "egg_Cactus" to cactusEgg)
            }

            "look up content using the class for the item type" {
                val owned = listOf(OwnedItem().apply { key = "Saddle" })
                every { inventoryRepository.getOwnedItems("food") } returns flowOf(owned)
                val saddle = Food().apply { key = "Saddle" }
                every { inventoryRepository.getItems(Food::class.java, arrayOf("Saddle")) } returns flowOf(listOf(saddle))

                val result = makeViewModel("food").items.first()

                result shouldBe mapOf("Saddle" to saddle)
            }
        }

        "hatchPet" should {
            "still hatch after the view model's scope was cancelled" {
                val potion = HatchingPotion().apply { key = "Base" }
                val egg = Egg().apply { key = "Wolf" }
                val activity = mockk<Activity>()
                coEvery { hatchPetUseCase.callInteractor(any()) } returns null
                every { mainUserViewModel.user.value } returns null

                val viewModel = makeViewModel()
                // mirrors ItemDialogFragment, which dismisses itself (clearing its view model) before hatching
                viewModel.viewModelScope.cancel()
                viewModel.hatchPet(potion, egg, activity)

                coVerify(exactly = 1) {
                    hatchPetUseCase.callInteractor(match { it.egg == egg && it.potion == potion })
                }
            }
        }

        "equip" should {
            "delegate to the inventory repository" {
                coEvery { inventoryRepository.equip("weapon", "sword") } returns null
                makeViewModel().equip("weapon", "sword")
                coVerify(exactly = 1) { inventoryRepository.equip("weapon", "sword") }
            }
        }

        "sellItem" should {
            "delegate to the inventory repository" {
                val ownedItem = OwnedItem().apply { key = "egg_Wolf" }
                coEvery { inventoryRepository.sellItem(ownedItem) } returns null
                makeViewModel().sellItem(ownedItem)
                coVerify(exactly = 1) { inventoryRepository.sellItem(ownedItem) }
            }
        }

        "inviteToQuest" should {
            "delegate to the inventory repository" {
                val quest = QuestContent()
                coEvery { inventoryRepository.inviteToQuest(quest) } returns null
                makeViewModel().inviteToQuest(quest)
                coVerify(exactly = 1) { inventoryRepository.inviteToQuest(quest) }
            }
        }

        "openMysteryItem" should {
            "delegate to the inventory repository and return its result" {
                val user = User()
                val equipment = Equipment()
                coEvery { inventoryRepository.openMysteryItem(user) } returns equipment

                val result = makeViewModel().openMysteryItem(user)

                result shouldBe equipment
            }
        }

        "useSkill" should {
            "delegate to the user repository" {
                coEvery { userRepository.useSkill("fireball", "target1", "member1") } returns null
                makeViewModel().useSkill("fireball", "target1", "member1")
                coVerify(exactly = 1) { userRepository.useSkill("fireball", "target1", "member1") }
            }
        }
    })
