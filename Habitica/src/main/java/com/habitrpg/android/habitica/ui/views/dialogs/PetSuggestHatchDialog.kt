package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogPetSuggestHatchBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

class PetSuggestHatchDialog(context: Context) : HabiticaAlertDialog(context) {


    private lateinit var binding: DialogPetSuggestHatchBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        inflater?.let { binding = DialogPetSuggestHatchBinding.inflate(it) }
        setAdditionalContentView(binding.root)
    }

    fun configure(pet: Animal, egg: Egg?, potion: HatchingPotion?, canHatch: Boolean) {
        DataBindingUtils.loadImage(binding.eggView, "Pet_Egg_${pet.animal}")
        DataBindingUtils.loadImage(binding.hatchingPotionView, "Pet_HatchingPotion_${pet.color}")
        binding.petTitleView.text = pet.text


        if (canHatch) {
            binding.descriptionView.text = context.getString(R.string.can_hatch_pet,
                    egg?.text ?: pet.animal.capitalize(),
                    potion?.text ?: pet.color.capitalize())
            addButton(R.string.hatch_pet, true, false) { _, _ ->
                val thisPotion = potion ?: return@addButton
                val thisEgg = egg ?: return@addButton
                (getActivity() as? MainActivity)?.hatchPet(thisPotion, thisEgg)
            }
            setTitle(R.string.hatch_pet_title)
        } else {
            binding.descriptionView.text = context.getString(R.string.suggest_pet_hatch,
                    egg?.text ?: pet.animal.capitalize(),
                    potion?.text ?: pet.color.capitalize())
            setTitle(R.string.unhatched_pet)
        }

        addButton(R.string.close, !canHatch)

        val imageName = "social_Pet-${pet.animal}-${pet.color}"
        DataBindingUtils.loadImage(imageName) {
            val resources = context.resources ?: return@loadImage
            val drawable = BitmapDrawable(resources, it.extractAlpha())
            Observable.just(drawable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer {
                        binding.petView.background = drawable
                    }, RxErrorHandler.handleEmptyError())
        }
    }
}
