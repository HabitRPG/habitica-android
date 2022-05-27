package com.habitrpg.android.habitica.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedObject
import com.habitrpg.android.habitica.models.user.OwnedPet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class StableViewModel(private val application: Application?, private val itemType: String?): BaseViewModel() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override fun inject(component: UserComponent) {
        component.inject(this)
    }


    private val _items: MutableLiveData<List<Any>> = MutableLiveData()
    val items: LiveData<List<Any>> = _items
    private val _eggs: MutableLiveData<Map<String, Egg>> = MutableLiveData()
    val eggs: LiveData<Map<String, Egg>> = _eggs
    private val _ownedItems: MutableLiveData<Map<String, OwnedItem>> = MutableLiveData()
    val ownedItems: LiveData<Map<String, OwnedItem>> = _ownedItems
    private val _mounts: MutableLiveData<List<Mount>> = MutableLiveData()
    val mounts: LiveData<List<Mount>> = _mounts
    private val _ownedMounts: MutableLiveData<Map<String, OwnedMount>> = MutableLiveData()
    val ownedMounts: LiveData<Map<String, OwnedMount>> = _ownedMounts

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
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

            disposable.add(
                inventoryRepository.getItems(Egg::class.java)
                    .map {
                        val eggMap = mutableMapOf<String, Egg>()
                        it.forEach { egg ->
                            eggMap[egg.key] = egg as Egg
                        }
                        eggMap
                    }
                    .subscribe(
                        {
                            _eggs.value = it
                        },
                        RxErrorHandler.handleEmptyError()
                    )
            )
            disposable.add(inventoryRepository.getOwnedItems(true).subscribe({ _ownedItems.value = it }, RxErrorHandler.handleEmptyError()))
            _mounts.value = if ("pets" == itemType) {
                inventoryRepository.getMounts().firstOrNull() ?: emptyList()
            } else {
                animals.map { it as Mount }
            }
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
            val lastIdentifier = if (lastAnimal.animal.isNotEmpty()) lastAnimal.animal else lastAnimal.key
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
                val title = if (itemType == "pets") {
                    application?.getString(R.string.pet_category, animal.getTranslatedType(application))
                } else {
                    application?.getString(R.string.mount_category, animal.getTranslatedType(application))
                }
                val section = StableSection(animal.type, title ?: "")
                items.add(section)
                lastSection = section
            }
            val isOwned = when (itemType) {
                "pets" -> {
                    val ownedPet = ownedAnimals[animal.key] as? OwnedPet
                    ownedPet?.trained ?: 0 > 0
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
        items.removeAll { it is StableSection && (it.key as? String) == "special" && it.ownedCount == 0 }
        return items
    }
}

class StableViewModelFactory(
    private val application: Application?,
    private val itemType: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StableViewModel(application, itemType) as T
    }
}
