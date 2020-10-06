package com.habitrpg.android.habitica.ui.viewHolders

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.MountOverviewItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class MountViewHolder(parent: ViewGroup, private val equipEvents: PublishSubject<String>) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.mount_overview_item)), View.OnClickListener {
    private var binding: MountOverviewItemBinding = MountOverviewItemBinding.bind(itemView)
    private var owned: Boolean = false
    var animal: Mount? = null

    var resources: Resources = itemView.resources

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(item: Mount, owned: Boolean) {
        animal = item
        this.owned = owned
        binding.titleTextView.visibility = View.GONE
        binding.ownedTextView.visibility = View.GONE
        val imageName = "stable_Mount_Icon_" + item.animal + "-" + item.color
        binding.imageView.alpha = 1.0f
        if (!owned) {
            binding.imageView.alpha = 0.2f
        }
        binding.imageView.background = null
        DataBindingUtils.loadImage(imageName) {
            val drawable = BitmapDrawable(itemView.context.resources, if (owned) it else it.extractAlpha())
            Observable.just(drawable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        binding.imageView.background = drawable
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun onClick(v: View) {
        if (!owned) {
            return
        }
        val menu = BottomSheetMenu(itemView.context)
        menu.setTitle(animal?.text)
        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.equip)))
        menu.setSelectionRunnable {
            animal?.let { equipEvents.onNext(it.key ?: "") }
        }
        menu.show()
    }
}