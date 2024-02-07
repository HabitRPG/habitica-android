package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedObject
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableRecyclerFragment
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class StableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val inventoryRepository: InventoryRepository
) : BaseViewModel(userRepository, userViewModel) {

    internal val itemType: String? = savedStateHandle[StableRecyclerFragment.ITEM_TYPE_KEY]

    private val _items: MutableLiveData<List<Any>> = MutableLiveData()
    val items: LiveData<List<Any>> = _items
    val eggs: LiveData<Map<String, Egg>> = inventoryRepository.getItems(Egg::class.java)
        .map {
            val eggMap = mutableMapOf<String, Egg>()
            it.forEach { egg ->
                eggMap[egg.key] = egg as Egg
            }
            eggMap
        }
        .asLiveData()
    val ownedItems: LiveData<Map<String, OwnedItem>> = inventoryRepository.getOwnedItems(true).asLiveData()
    val mounts: LiveData<List<Mount>> = inventoryRepository.getMounts().asLiveData()
    private val _ownedPets: MutableLiveData<Map<String, OwnedPet>> = MutableLiveData()
    val ownedPets: LiveData<Map<String, OwnedPet>> = _ownedPets
    private val _ownedMounts: MutableLiveData<Map<String, OwnedMount>> = MutableLiveData()
    val ownedMounts: LiveData<Map<String, OwnedMount>> = _ownedMounts

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launchCatching {
            val animals = if ("pets" == itemType) {
                inventoryRepository.getPets().firstOrNull()
            } else {
                inventoryRepository.getMounts().firstOrNull()
            } ?: emptyList()
            if ("pets" == itemType) {
                inventoryRepository.getOwnedPets()
            } else {
                inventoryRepository.getOwnedMounts()
            }.map {
                val animalMap = mutableMapOf<String, OwnedObject>()
                it.forEach { animal ->
                    val castedAnimal = animal as? OwnedObject ?: return@forEach
                    animalMap[castedAnimal.key ?: ""] = castedAnimal
                }
                animalMap
            }.collect {
                _items.value = mapAnimals(animals, it)
            }
        }
        viewModelScope.launchCatching {
            inventoryRepository.getOwnedPets().map { ownedPets ->
                val petMap = mutableMapOf<String, OwnedPet>()
                ownedPets.forEach { petMap[it.key ?: ""] = it }
                return@map petMap
            }.collect {
                _ownedPets.value = it
            }
        }
        viewModelScope.launchCatching {
            inventoryRepository.getOwnedMounts().map { ownedMounts ->
                val mountMap = mutableMapOf<String, OwnedMount>()
                ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                return@map mountMap
            }.collect {
                _ownedMounts.value = it
            }
        }
    }

    private fun mapAnimals(unsortedAnimals: List<Animal>, ownedAnimals: Map<String, OwnedObject>): ArrayList<Any> {
        val items = ArrayList<Any>()
        var lastAnimal: Animal = unsortedAnimals.firstOrNull() ?: return items
        var lastSection: StableSection? = null
        for (animal in unsortedAnimals) {
            val identifier = if (animal.animal.isNotEmpty() && (animal.type != "special" && animal.type != "wacky")) animal.animal else animal.key
            val lastIdentifier = lastAnimal.animal.ifEmpty { lastAnimal.key }
            if (animal.type == "premium") {
                if (!items.contains(lastAnimal)) {
                    items.add(lastAnimal)
                }
                lastAnimal = items.first { (it as? Animal)?.animal == animal.animal } as Animal
            } else if (identifier != lastIdentifier || animal === unsortedAnimals[unsortedAnimals.size - 1]) {
                if (!((lastAnimal.type == "special") && lastAnimal.numberOwned == 0) && !items.contains(lastAnimal)) {
                    items.add(lastAnimal)
                }
                lastAnimal = animal
            }

            if (animal.type != lastSection?.key && animal.type != "premium") {
                if (items.size > 0 && items[items.size - 1].javaClass == StableSection::class.java) {
                    items.removeAt(items.size - 1)
                }
                val section = StableSection(animal.type, itemType ?: "")
                items.add(section)
                lastSection = section
            }
            val isOwned = when (itemType) {
                "pets" -> {
                    val ownedPet = ownedAnimals[animal.key] as? OwnedPet
                    (ownedPet?.trained ?: 0) > 0
                }
                "mounts" -> {
                    val ownedMount = ownedAnimals[animal.key] as? OwnedMount
                    ownedMount?.owned == true
                }
                else -> false
            }
            lastAnimal.totalNumber += 1
            lastSection?.totalCount = (lastSection?.totalCount ?: 0) + 1
            if (isOwned) {
                lastAnimal.numberOwned += 1
                lastSection?.ownedCount = (lastSection?.ownedCount ?: 0) + 1
            }
        }
        if (!((lastAnimal.type == "premium" || lastAnimal.type == "special") && lastAnimal.numberOwned == 0)) {
            items.add(lastAnimal)
        }

        items.add(0, "header")
        items.removeAll { it is StableSection && it.key == "special" && it.ownedCount == 0 }
        return items
    }
}
