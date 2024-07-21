package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogHatchPetButtonBinding
import com.habitrpg.android.habitica.databinding.DialogPetSuggestHatchBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.interactors.HatchPetUseCase
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.common.habitica.extensionsCommon.DataBindingUtils
import com.habitrpg.common.habitica.extensionsCommon.loadImage
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale

class PetSuggestHatchDialog(context: Context) : HabiticaAlertDialog(context) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PetSuggestHatchDialogEntryPoint {
        fun useCase(): HatchPetUseCase

        fun mainUserViewModel(): MainUserViewModel
    }

    private val userViewModel: MainUserViewModel
    private val hatchPetUseCase: HatchPetUseCase

    private lateinit var binding: DialogPetSuggestHatchBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        inflater?.let { binding = DialogPetSuggestHatchBinding.inflate(it) }
        setAdditionalContentView(binding.root)
        binding.shimmerView.startShimmer()

        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                context,
                PetSuggestHatchDialogEntryPoint::class.java,
            )
        hatchPetUseCase = hiltEntryPoint.useCase()
        userViewModel = hiltEntryPoint.mainUserViewModel()
    }

    private var hasAllItems = false

    override fun show() {
        super.show()
        if (!hasAllItems) {
            Analytics.sendNavigationEvent("pet suggestion modal")
        }
    }

    fun configure(
        pet: Animal,
        egg: Egg?,
        potion: HatchingPotion?,
        eggCount: Int,
        potionCount: Int,
        hasUnlockedEgg: Boolean,
        hasUnlockedPotion: Boolean,
        hasMount: Boolean,
    ) {
        binding.eggView.loadImage("Pet_Egg_${pet.animal}")
        binding.hatchingPotionView.loadImage("Pet_HatchingPotion_${pet.color}")
        binding.petTitleView.text = pet.text

        val hasEgg = eggCount > 0
        val hasPotion = potionCount > 0

        binding.eggView.alpha = if (hasEgg) 1.0f else 0.5f
        binding.hatchingPotionView.alpha = if (hasPotion) 1.0f else 0.5f

        val eggName =
            egg?.text ?: pet.animal.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(
                        Locale.getDefault(),
                    )
                } else {
                    it.toString()
                }
            }
        val potionName =
            potion?.text ?: pet.color.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(
                        Locale.getDefault(),
                    )
                } else {
                    it.toString()
                }
            }

        if (hasEgg) {
            binding.eggCountView.visibility = View.VISIBLE
            binding.eggCountView.text = eggCount.toString()
        } else {
            binding.eggCountView.visibility = View.GONE
        }
        if (hasPotion) {
            binding.potionCountView.visibility = View.VISIBLE
            binding.potionCountView.text = potionCount.toString()
        } else {
            binding.potionCountView.visibility = View.GONE
        }

        if (hasEgg && hasPotion) {
            binding.descriptionView.text =
                context.getString(
                    R.string.can_hatch_pet,
                    eggName,
                    potionName,
                )
            addButton(R.string.hatch, true, false) { _, _ ->
                val thisPotion = potion ?: return@addButton
                val thisEgg = egg ?: return@addButton
                hatchPet(thisPotion, thisEgg)
            }
            if (hasMount) {
                setTitle(R.string.hatch_your_pet)
            } else {
                setTitle(R.string.hatch_pet_title)
            }
            addButton(R.string.close, false)
            hasAllItems = true
        } else {
            if (hasMount) {
                if (!hasEgg && !hasPotion) {
                    binding.descriptionView.text =
                        context.getString(
                            R.string.suggest_pet_hatch_again_missing_both,
                            eggName,
                            potionName,
                        )
                } else if (!hasEgg) {
                    binding.descriptionView.text =
                        context.getString(R.string.suggest_pet_hatch_again_missing_egg, eggName)
                } else {
                    binding.descriptionView.text =
                        context.getString(
                            R.string.suggest_pet_hatch_again_missing_potion,
                            potionName,
                        )
                }
            } else {
                if (!hasEgg && !hasPotion) {
                    binding.descriptionView.text =
                        context.getString(
                            R.string.suggest_pet_hatch_missing_both,
                            eggName,
                            potionName,
                        )
                } else if (!hasEgg) {
                    binding.descriptionView.text =
                        context.getString(R.string.suggest_pet_hatch_missing_egg, eggName)
                } else {
                    binding.descriptionView.text =
                        context.getString(R.string.suggest_pet_hatch_missing_potion, potionName)
                }
            }

            var hatchPrice = 0
            var canBuy = true
            if (!hasEgg) {
                hatchPrice += getItemPrice(pet, egg, hasUnlockedEgg)
                if (pet.type == "quest" && !hasUnlockedEgg) {
                    canBuy = false
                }
            }

            if (!hasPotion) {
                hatchPrice += getItemPrice(pet, potion, hasUnlockedPotion)
                if (pet.type == "quest" && !hasUnlockedPotion) {
                    canBuy = false
                }
            }

            if (hatchPrice > 0 && canBuy) {
                val binding = DialogHatchPetButtonBinding.inflate(layoutInflater)
                binding.currencyView.value = hatchPrice.toDouble()
                binding.currencyView.currency = "gems"
                binding.currencyView.setTextColor(ContextCompat.getColor(context, R.color.white))
                addButton(binding.root, true) { _, _ ->
                    val activity =
                        (getActivity() as? MainActivity) ?: (
                            HabiticaBaseApplication.getInstance(
                                context,
                            )?.currentActivity?.get() as? MainActivity
                        ) ?: return@addButton
                    if ((userViewModel.user.value?.gemCount ?: hatchPrice) < hatchPrice) {
                        InsufficientGemsDialog(activity, hatchPrice).show()
                        Analytics.sendEvent(
                            "show insufficient gems modal",
                            EventCategory.BEHAVIOUR,
                            HitType.EVENT,
                            mapOf("reason" to "pet suggest modal"),
                        )
                        return@addButton
                    }
                    val thisPotion = potion ?: return@addButton
                    val thisEgg = egg ?: return@addButton
                    longLivingScope.launchCatching {
                        if (!hasEgg) {
                            activity.inventoryRepository.purchaseItem("eggs", thisEgg.key, 1)
                        }
                        if (!hasPotion) {
                            activity.inventoryRepository.purchaseItem(
                                "hatchingPotions",
                                thisPotion.key,
                                1,
                            )
                        }
                        activity.userRepository.retrieveUser(true, forced = true)
                        hatchPet(thisPotion, thisEgg)
                    }
                }
            }

            addButton(R.string.close, false)

            setTitle(R.string.unhatched_pet)
        }

        val imageName = "stable_Pet-${pet.animal}-${pet.color}"
        DataBindingUtils.loadImage(context, imageName) {
            val resources = context.resources ?: return@loadImage
            val drawable =
                if (hasMount) it else BitmapDrawable(resources, it.toBitmap().extractAlpha())
            lifecycleScope.launchCatching {
                binding.petView.bitmap = drawable.toBitmap()
            }
        }
    }

    private fun hatchPet(
        potion: HatchingPotion,
        egg: Egg,
    ) {
        longLivingScope.launchCatching {
            hatchPetUseCase.callInterActor(
                HatchPetUseCase.RequestValues(
                    potion,
                    egg,
                    context,
                ),
            )
        }
    }

    private fun getItemPrice(
        pet: Animal,
        item: Item?,
        hasUnlocked: Boolean,
    ): Int {
        if (pet.type == "drop" || (pet.type == "quest" && hasUnlocked)) {
            return item?.value ?: 0
        }
        return 0
    }
}
