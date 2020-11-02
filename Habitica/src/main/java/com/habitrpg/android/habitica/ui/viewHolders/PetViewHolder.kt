package com.habitrpg.android.habitica.ui.viewHolders

import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PetDetailItemBinding
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.views.dialogs.PetSuggestHatchDialog
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus

class PetViewHolder(parent: ViewGroup, private val equipEvents: PublishSubject<String>, private val animalIngredientsRetriever: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.pet_detail_item)), View.OnClickListener {
    private var hasMount: Boolean = false
    private var hasUnlockedPotion: Boolean = false
    private var hasUnlockedEgg: Boolean = false
    private var eggCount: Int = 0
    private var potionCount: Int = 0
    private var ownsSaddles = false
    private var animal: Pet? = null

    private var binding: PetDetailItemBinding = PetDetailItemBinding.bind(itemView)

    private var isOwned: Boolean = false

    private var canRaiseToMount: Boolean = false

    private val canHatch: Boolean
        get() = eggCount > 0 && potionCount > 0

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(item: Pet,
             trained: Int,
             eggCount: Int,
             potionCount: Int,
             canRaiseToMount: Boolean,
             ownsSaddles: Boolean,
             hasUnlockedEgg: Boolean,
             hasUnlockedPotion: Boolean,
             hasMount: Boolean) {
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
        binding.imageView.visibility = View.VISIBLE
        binding.itemWrapper.visibility = View.GONE
        binding.checkmarkView.visibility = View.GONE

        binding.titleTextView.visibility = View.GONE

        val imageName = "stable_Pet-${item.animal}-${item.color}"
        itemView.setBackgroundResource(R.drawable.layout_rounded_bg_window)
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
            if (canHatch) {
                binding.imageView.visibility = View.GONE
                binding.itemWrapper.visibility = View.VISIBLE
                binding.checkmarkView.visibility = View.VISIBLE
                itemView.setBackgroundResource(R.drawable.layout_rounded_bg_window_tint_border)
                DataBindingUtils.loadImage(binding.eggView, "Pet_Egg_${item.animal}")
                DataBindingUtils.loadImage(binding.hatchingPotionView, "Pet_HatchingPotion_${item.color}")
            }
        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            binding.trainedProgressBar.progressBackgroundTintMode = PorterDuff.Mode.SRC_OVER
        }
        binding.imageView.background = null
        DataBindingUtils.loadImage(imageName) {
            val resources = itemView.context.resources ?: return@loadImage
            val drawable = BitmapDrawable(resources, if (trained  == 0) it.extractAlpha() else it)
            Observable.just(drawable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        binding.imageView.background = drawable
                    }, RxErrorHandler.handleEmptyError())
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
        menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.equip)))
        if (canRaiseToMount) {
            menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.feed)))
            if (ownsSaddles) {
                menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.use_saddle)))
            }
        }
        menu.setSelectionRunnable { index ->
            when (index) {
                0 -> {
                    animal?.let {
                        equipEvents.onNext(it.key ?: "")
                    }
                }
                1 -> {
                    val event = FeedCommand()
                    event.usingPet = animal
                    EventBus.getDefault().post(event)
                }
                2 -> {
                    val event = FeedCommand()
                    event.usingPet = animal
                    val saddle = Food()
                    saddle.key = "Saddle"
                    event.usingFood = saddle
                    EventBus.getDefault().post(event)
                }
            }
        }
        menu.show()
    }

    private fun showRequirementsDialog() {
        val context = itemView.context
        val dialog = PetSuggestHatchDialog(context)
        animal?.let {
            animalIngredientsRetriever?.invoke(it) { ingredients ->
                dialog.configure(it,
                        ingredients.first,
                        ingredients.second,
                        eggCount,
                        potionCount,
                        hasUnlockedEgg,
                        hasUnlockedPotion,
                        hasMount)
                dialog.show()
            }
        }
    }
}
