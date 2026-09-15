package com.habitrpg.android.habitica.ui.viewmodels

import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel

class AchievementViewModel @Inject constructor(
    userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository,
    userViewModel: MainUserViewModel
) : BaseViewModel(userRepository, userViewModel) {

    fun itemSizeForType(itemViewType: Int) = if (itemViewType == 1) {
        1
    } else {
        3
    }

    override fun onCleared() {
        inventoryRepository.close()
        super.onCleared()
    }

    val achievements = userRepository
        .getAchievements()
        .combine(userRepository.getQuestAchievements()) { achievements, questAchievements ->
            return@combine Pair(achievements, questAchievements)
        }.combine(
            userRepository
                .getQuestAchievements()
                .map { it.mapNotNull { achievement -> achievement.questKey } }
                .map { inventoryRepository.getQuestContent(it).firstOrNull() },
        ) { achievements, content ->
            Pair(achievements, content)
        }.map {
            val achievements = it.first.first
            val entries = mutableListOf<Any>()
            var lastCategory = ""
            achievements.forEach { achievement ->
                val categoryIdentifier = achievement.category ?: ""
                if (categoryIdentifier != lastCategory) {
                    val category =
                        Pair(
                            categoryIdentifier,
                            achievements.count { check ->
                                check.category == categoryIdentifier && check.earned
                            },
                        )
                    entries.add(category)
                    lastCategory = categoryIdentifier
                }
                entries.add(achievement)
            }
            val questAchievements = it.first.second
            entries.add(Pair("Quests completed", questAchievements.size))
            entries.addAll(
                questAchievements.map { achievement ->
                    val questContent = it.second?.firstOrNull { achievement.questKey == it.key }
                    achievement.title = questContent?.text
                    achievement
                },
            )

            val user = userViewModel.user.value
            val challengeAchievementCount = user?.challengeAchievements?.size ?: 0
            if (challengeAchievementCount > 0) {
                entries.add(Pair("Challenges won", challengeAchievementCount))
                user?.challengeAchievements?.let { it1 -> entries.addAll(it1) }
            }
            return@map entries
        }
}
