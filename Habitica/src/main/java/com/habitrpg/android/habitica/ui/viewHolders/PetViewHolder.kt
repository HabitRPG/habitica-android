package com.habitrpg.android.habitica.ui.viewHolders

import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PetDetailItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.views.dialogs.PetSuggestHatchDialog
import io.reactivex.rxjava3.subjects.PublishSubject

class PetViewHolder(
    parent: ViewGroup,
    private val equipEvents: PublishSubject<String>,
    private val feedEvents: PublishSubject<Pair<Pet, Food?>>,
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

        val imageName = "stable_Pet-${item.animal}-${item.color}"
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

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            binding.trainedProgressBar.progressBackgroundTintMode = PorterDuff.Mode.SRC_OVER
        }
        binding.imageView.background = null
        binding.activeIndicator.visibility =
            if (currentPet.equals(animal?.key)) View.VISIBLE else View.GONE
        binding.imageView.tag = imageName
        DataBindingUtils.loadImage(itemView.context, imageName) {
            val resources = itemView.context.resources ?: return@loadImage
            val drawable =
                if (trained == 0) BitmapDrawable(resources, it.toBitmap().extractAlpha()) else it
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
        val menu = BottomSheetMenu(context)
        menu.setTitle(animal?.text)
        menu.setImage("stable_Pet-${animal?.animal}-${animal?.color}")

        val hasCurrentPet = currentPet.equals(animal?.key)
        val labelId = if (hasCurrentPet) R.string.unequip else R.string.equip
        menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(labelId)))

        if (canRaiseToMount) {
            menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.feed)))
            if (ownsSaddles) {
                menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.use_saddle)))
            }
        }
        menu.setSelectionRunnable { index ->
            val pet = animal ?: return@setSelectionRunnable
            when (index) {
                0 -> {
                    animal?.let {
                        equipEvents.onNext(it.key ?: "")
                    }
                }
                1 -> {
                    feedEvents.onNext(Pair(pet, null))
                }
                2 -> {
                    val saddle = Food()
                    saddle.key = "Saddle"
                    feedEvents.onNext(Pair(pet, saddle))
                }
            }
        }
        menu.show()
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
