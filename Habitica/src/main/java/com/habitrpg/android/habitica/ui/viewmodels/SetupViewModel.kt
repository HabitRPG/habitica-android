package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(val userRepository: UserRepository, val taskRepository: TaskRepository) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _selectedTaskCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedTaskCategories: StateFlow<Set<String>> = _selectedTaskCategories

    fun initializeUser(initialUser: User?) {
        _user.value = initialUser
    }

    fun equipCustomization(item: SetupCustomization) {
        val currentUser = _user.value ?: return
        when (item.category) {
            SetupCustomizationRepository.CATEGORY_BODY -> currentUser.preferences?.shirt = item.key
            SetupCustomizationRepository.CATEGORY_HAIR -> when (item.subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_COLOR -> currentUser.preferences?.hair?.color = item.key
                SetupCustomizationRepository.SUBCATEGORY_BANGS -> currentUser.preferences?.hair?.bangs = item.key.toIntOrNull() ?: 0
                SetupCustomizationRepository.SUBCATEGORY_PONYTAIL -> currentUser.preferences?.hair?.base = item.key.toIntOrNull() ?: 0
                else -> {}
            }
            SetupCustomizationRepository.CATEGORY_SKIN -> currentUser.preferences?.skin = item.key
            SetupCustomizationRepository.CATEGORY_EXTRAS -> when (item.subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> currentUser.preferences?.chair = item.key
                SetupCustomizationRepository.SUBCATEGORY_FLOWER -> currentUser.preferences?.hair?.flower = item.key.toIntOrNull() ?: 0
                SetupCustomizationRepository.SUBCATEGORY_GLASSES -> currentUser.items?.gear?.equipped?.eyeWear = item.key
                else -> {}
            }
            else -> {}
        }
        _user.value = currentUser
    }

    fun getActiveCustomization(category: String, subcategory: String): String {
        val currentUser = _user.value
        return when (category) {
            SetupCustomizationRepository.CATEGORY_BODY -> currentUser?.preferences?.shirt
            SetupCustomizationRepository.CATEGORY_HAIR -> when (subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_COLOR -> currentUser?.preferences?.hair?.color
                SetupCustomizationRepository.SUBCATEGORY_BANGS -> currentUser?.preferences?.hair?.bangs?.toString()
                SetupCustomizationRepository.SUBCATEGORY_PONYTAIL -> currentUser?.preferences?.hair?.base?.toString()
                else -> ""
            }
            SetupCustomizationRepository.CATEGORY_SKIN -> currentUser?.preferences?.skin
            SetupCustomizationRepository.CATEGORY_EXTRAS -> when (subcategory) {
                SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> currentUser?.preferences?.chair
                SetupCustomizationRepository.SUBCATEGORY_FLOWER -> currentUser?.preferences?.hair?.flower?.toString()
                SetupCustomizationRepository.SUBCATEGORY_GLASSES -> currentUser?.items?.gear?.equipped?.eyeWear
                else -> ""
            }
            else -> ""
        } ?: ""
    }

    fun selectTaskCategory(category: String) {
        val currentCategories = _selectedTaskCategories.value.toMutableSet()
        if (currentCategories.contains(category)) {
            currentCategories.remove(category)
        } else {
            currentCategories.add(category)
        }
        _selectedTaskCategories.value = currentCategories
    }

    suspend fun saveSetup() {
        userRepository.updateUser(mapOf(
            "preferences.shirt" to _user.value?.preferences?.shirt,
            "preferences.hair.color" to _user.value?.preferences?.hair?.color,
            "preferences.hair.bangs" to _user.value?.preferences?.hair?.bangs,
            "preferences.hair.base" to _user.value?.preferences?.hair?.base,
            "preferences.hair.flower" to _user.value?.preferences?.hair?.flower,
            "preferences.skin" to _user.value?.preferences?.skin,
            "preferences.chair" to _user.value?.preferences?.chair,
            "items.gear.equipped.eyeWear" to _user.value?.items?.gear?.equipped?.eyeWear,
        ))

    }
}
