package com.habitrpg.android.habitica.ui.viewHolders

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.MountOverviewItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import io.reactivex.rxjava3.subjects.PublishSubject

class MountViewHolder(parent: ViewGroup, private val equipEvents: PublishSubject<String>) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.mount_overview_item)), View.OnClickListener {
    private var binding: MountOverviewItemBinding = MountOverviewItemBinding.bind(itemView)
    private var owned: Boolean = false
    var animal: Mount? = null
    private var user: User? = null

    var resources: Resources = itemView.resources

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(item: Mount, owned: Boolean, user: User?) {
        animal = item
        this.owned = owned
        this.user = user
        binding.titleTextView.visibility = View.GONE
        binding.ownedTextView.visibility = View.GONE
        val imageName = "stable_Mount_Icon_" + item.animal + "-" + item.color
        binding.imageView.alpha = 1.0f
        if (!owned) {
            binding.imageView.alpha = 0.2f
        }
        binding.imageView.background = null
        DataBindingUtils.loadImage(itemView.context, imageName) {
            val drawable = if (owned) it else BitmapDrawable(itemView.context.resources, it.toBitmap().extractAlpha())
            binding.imageView.background = drawable
        }
    }

    override fun onClick(v: View) {
        if (!owned) {
            return
        }
        val menu = BottomSheetMenu(itemView.context)
        menu.setTitle(animal?.text)

        val hasCurrentMount = user?.currentMount.equals(animal?.key)
        val labelId = if (hasCurrentMount) R.string.unequip else R.string.equip
        menu.addMenuItem(BottomSheetMenuItem(resources.getString(labelId)))
        menu.setSelectionRunnable {
            animal?.let { equipEvents.onNext(it.key ?: "") }
        }
        menu.show()
    }
}
