package com.habitrpg.android.habitica.ui.viewmodels.inventory.items

import android.content.SharedPreferences
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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

        fun makeViewModel() =
            ItemListViewModel(
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

        "items" should {
            "load eggs by default and key them by their item key" {
                val owned = listOf(OwnedItem().apply { key = "egg_Wolf" }, OwnedItem().apply { key = "egg_Cactus" })
                every { inventoryRepository.getOwnedItems("") } returns flowOf(owned)
                val wolfEgg = Egg().apply { key = "egg_Wolf" }
                val cactusEgg = Egg().apply { key = "egg_Cactus" }
                every { inventoryRepository.getItems(Egg::class.java, any()) } returns flowOf(listOf(wolfEgg, cactusEgg))

                val viewModel = makeViewModel()
                val result = viewModel.items.first()

                result shouldBe mapOf("egg_Wolf" to wolfEgg, "egg_Cactus" to cactusEgg)
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
