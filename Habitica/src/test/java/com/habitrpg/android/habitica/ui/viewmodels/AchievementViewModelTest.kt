package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.realm.RealmList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class AchievementViewModelTest :
    WordSpec({
        val userRepository = mockk<UserRepository>()
        val inventoryRepository = mockk<InventoryRepository>()
        val mainUserViewModel = mockk<MainUserViewModel>()

        fun achievement(
            key: String,
            category: String,
            earned: Boolean,
        ) = Achievement().apply {
            this.key = key
            this.category = category
            this.earned = earned
        }

        "itemSizeForType" should {
            "return 1 for type 1 and 3 otherwise" {
                every { userRepository.getAchievements() } returns flowOf(emptyList())
                every { userRepository.getQuestAchievements() } returns flowOf(emptyList())
                val viewModel = AchievementViewModel(userRepository, inventoryRepository, mainUserViewModel)
                viewModel.itemSizeForType(1) shouldBe 1
                viewModel.itemSizeForType(0) shouldBe 3
            }
        }

        "achievements" should {
            "group achievements by category and append quests and challenges" {
                val streak = achievement("streak", "basic", true)
                val perfect = achievement("perfect", "basic", false)
                val spring = achievement("spring", "seasonal", true)
                val questAchievement = QuestAchievement().apply { questKey = "dilatory" }
                every { userRepository.getAchievements() } returns flowOf(listOf(streak, perfect, spring))
                every { userRepository.getQuestAchievements() } returns flowOf(listOf(questAchievement))
                every { inventoryRepository.getQuestContent(listOf("dilatory")) } returns
                    flowOf(listOf(QuestContent().apply { key = "dilatory"; text = "Dilatory" }))
                every { mainUserViewModel.user } returns MutableLiveData(User().apply { challengeAchievements = RealmList("Won") })

                val entries = AchievementViewModel(userRepository, inventoryRepository, mainUserViewModel).achievements.first()

                entries shouldBe
                    listOf(
                        "basic" to 1,
                        streak,
                        perfect,
                        "seasonal" to 1,
                        spring,
                        "Quests completed" to 1,
                        questAchievement,
                        "Challenges won" to 1,
                        "Won",
                    )
                questAchievement.title shouldBe "Dilatory"
            }

            "leave out challenges if none were won" {
                every { userRepository.getAchievements() } returns flowOf(emptyList())
                every { userRepository.getQuestAchievements() } returns flowOf(emptyList())
                every { inventoryRepository.getQuestContent(emptyList<String>()) } returns flowOf(emptyList())
                every { mainUserViewModel.user } returns MutableLiveData(User())

                AchievementViewModel(userRepository, inventoryRepository, mainUserViewModel).achievements.first() shouldBe
                    listOf("Quests completed" to 0)
            }
        }
    })
