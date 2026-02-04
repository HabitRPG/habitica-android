package com.habitrpg.android.habitica.ui.viewHolders

import android.app.Activity
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PetDetailItemBinding
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.ui.views.dialogs.PetSuggestHatchDialog
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.android.habitica.ui.views.stable.PetBottomSheet
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.extensions.inflate
import com.habitrpg.common.habitica.helpers.SpriteSubstitutionManager
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import dagger.hilt.android.internal.managers.ViewComponentManager

class PetViewHolder(
    parent: ViewGroup,
    private val onEquip: ((String) -> Unit)?,
    private val onFeed: (suspend (Pet, Food?) -> FeedResponse?)?,
    private val ingredientsReceiver: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)?
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.pet_detail_item)),
    View.OnClickListener {
    private var hasMount: Boolean = false
    private var hasUnlockedPotion: Boolean = false
    private var hasUnlockedEgg: Boolean = false
    private var eggCount: Int = 0
    private var potionCount: Int = 0
    private var ownsSaddles = false
    private var animal: Pet? = null
    private var trained: Int = 0
    private var currentPet: String? = null

    private var binding: PetDetailItemBinding = PetDetailItemBinding.bind(itemView)

    private var isOwned: Boolean = false

    private var canRaiseToMount: Boolean = false

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(
        item: Pet,
        trained: Int,
        eggCount: Int,
        potionCount: Int,
        canRaiseToMount: Boolean,
        ownsSaddles: Boolean,
        hasUnlockedEgg: Boolean,
        hasUnlockedPotion: Boolean,
        hasMount: Boolean,
        currentPet: String?
    ) {
        this.animal = item
        this.trained = trained
        isOwned = trained > 0
        binding.imageView.alpha = 1.0f
        this.canRaiseToMount = canRaiseToMount
        this.eggCount = eggCount
        this.potionCount = potionCount
        this.ownsSaddles = ownsSaddles
        this.hasUnlockedEgg = hasUnlockedEgg
        this.hasUnlockedPotion = hasUnlockedPotion
        this.hasMount = hasMount
        this.currentPet = currentPet
        binding.imageView.visibility = View.VISIBLE
        binding.itemWrapper.visibility = View.GONE
        binding.checkmarkView.visibility = View.GONE

        binding.titleTextView.visibility = View.GONE
        binding.root.contentDescription = item.text

        val name = "Pet-${item.animal}-${item.color}"
        val imageName = "stable_${SpriteSubstitutionManager.substitute(name, "pets")}"
        if (trained > 0) {
            if (this.canRaiseToMount) {
                binding.trainedProgressBar.visibility = View.VISIBLE
                binding.trainedProgressBar.progress = trained
            } else {
                binding.trainedProgressBar.visibility = View.GONE
            }
        } else {
            binding.trainedProgressBar.visibility = View.GONE
            binding.imageView.alpha = 0.2f
        }

        binding.trainedProgressBar.progressBackgroundTintMode = PorterDuff.Mode.SRC_OVER
        binding.imageView.background = null
        binding.activeIndicator.visibility =
            if (currentPet.equals(animal?.key)) View.VISIBLE else View.GONE
        binding.imageView.tag = imageName
        DataBindingUtils.loadImage(itemView.context, imageName) {
            val resources = itemView.context.resources ?: return@loadImage
            val drawable =
                if (trained == 0 && canRaiseToMount) {
                    it.toBitmap().extractAlpha().toDrawable(resources)
                } else {
                    it
                }
            if (binding.imageView.tag == imageName) {
                binding.imageView.bitmap = drawable.toBitmap()
            }
        }
    }

    override fun onClick(v: View) {
        if (!isOwned) {
            showRequirementsDialog()
            return
        }
        val context = itemView.context
        animal?.let { pet ->
            (
                if (context is ViewComponentManager.FragmentContextWrapper) {
                    context.baseContext
                } else {
                    context
                } as Activity
                ).showAsBottomSheet {
                PetBottomSheet(
                    pet,
                    trained,
                    currentPet.equals(animal?.key),
                    canRaiseToMount,
                    ownsSaddles,
                    onEquip,
                    onFeed,
                    it
                )
            }
        }
    }

    private fun showRequirementsDialog() {
        val context = itemView.context
        val dialog = PetSuggestHatchDialog(context)
        animal?.let {
            ingredientsReceiver?.invoke(it) { ingredients ->
                dialog.configure(
                    it,
                    ingredients.first,
                    ingredients.second,
                    eggCount,
                    potionCount,
                    hasUnlockedEgg,
                    hasUnlockedPotion,
                    hasMount
                )
                dialog.show()
            }
        }
    }
}
