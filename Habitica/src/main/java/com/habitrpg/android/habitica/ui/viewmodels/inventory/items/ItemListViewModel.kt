package com.habitrpg.android.habitica.ui.viewmodels.inventory.items

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val socialRepository: SocialRepository,
    private val sharedPreferences: SharedPreferences,
    private val reviewManager: ReviewManager,
    private val hatchPetUseCase: HatchPetUseCase,
    userRepository: UserRepository,
                        userViewModel: MainUserViewModel
) : BaseViewModel(userRepository, userViewModel) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val specialSkills = userViewModel.user.asFlow()
        .filterNotNull()
        .flatMapLatest { user ->
            userRepository
                .getSkills(user)
                .combine(userRepository.getSpecialItems(user)) { skills, items ->
                    val allEntries = mutableListOf<Skill>()
                    for (skill in skills) {
                        allEntries.add(skill)
                    }
                    for (item in items) {
                        allEntries.add(item)
                    }
                    return@combine allEntries
                }
        }

    val itemType: MutableStateFlow<String> = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val ownedItems = itemType.flatMapLatest {
        inventoryRepository
            .getOwnedItems(it)
    }

    val items = ownedItems.map { items -> items.mapNotNull { it.key } }
        .map {
            val itemClass: Class<out Item> =
                when (itemType.value) {
                    "eggs" -> Egg::class.java
                    "hatchingPotions" -> HatchingPotion::class.java
                    "food" -> Food::class.java
                    "quests" -> QuestContent::class.java
                    "special" -> SpecialItem::class.java
                    else -> Egg::class.java
                }
            inventoryRepository.getItems(itemClass, it.toTypedArray()).firstOrNull()
        }.map {
            val itemMap = mutableMapOf<String, Item>()
            for (item in it ?: emptyList()) {
                itemMap[item.key] = item
            }
            return@map itemMap
        }

    val pets = inventoryRepository.getPets()
    val ownedPets = inventoryRepository.getOwnedPets().map { ownedPets ->
        val petMap = mutableMapOf<String, OwnedPet>()
        ownedPets.forEach { petMap[it.key ?: ""] = it }
        return@map petMap
    }

    override fun onCleared() {
        inventoryRepository.close()
        socialRepository.close()
        super.onCleared()
    }

    fun inviteToQuest(quest: QuestContent) {
        viewModelScope.launchCatching {
            inventoryRepository.inviteToQuest(quest)
            MainNavigationController.navigate(R.id.partyFragment)
        }
    }

    suspend fun openMysteryItem(user: User?): Equipment? {
        return inventoryRepository.openMysteryItem(user)
    }

    fun equip(type: String, key: String) {
        viewModelScope.launchCatching {
            inventoryRepository.equip(type, key)
        }
    }

    fun hatchPet(potion: HatchingPotion, egg: Egg, activity: Activity) {
        viewModelScope.launchCatching {
            hatchPetUseCase.callInteractor(
                HatchPetUseCase.RequestValues(
                    potion,
                    egg,
                    activity,
                ),
            )

            var hatchCount = sharedPreferences.getInt("pets_hatched", 0)
            hatchCount += 1
            sharedPreferences.edit {
                putInt("pets_hatched", hatchCount)
            }
            userViewModel.user.value?.let { user ->
                val parentActivity = activity as? MainActivity
                val totalCheckIns = user.loginIncentives

                if (parentActivity != null) {
                    reviewManager.requestReview(parentActivity, totalCheckIns)
                }
            }
        }
    }

    fun createParty(user: User, partyName: String) {
        viewModelScope.launchCatching {
            socialRepository.createGroup(
                partyName,
                "",
                user.id,
                "party",
                "",
                false,
            )
            val user = userRepository.retrieveUser(false, true)
            if (user?.hasParty == true) {
                val party = socialRepository.retrieveGroup("party")
                socialRepository.retrievePartyMembers(party?.id ?: "", true)
                MainNavigationController.navigate(
                    R.id.partyFragment,
                    bundleOf(Pair("partyID", user.party?.id)),
                )
            }
        }
    }

    suspend fun useSkill(skillKey: String, target: String?, memberID: String) {
        userRepository.useSkill(skillKey, target, memberID)
    }

    fun sellItem(ownedItem: OwnedItem) {
        viewModelScope.launchCatching {
            inventoryRepository.sellItem(ownedItem)
        }
    }
}
