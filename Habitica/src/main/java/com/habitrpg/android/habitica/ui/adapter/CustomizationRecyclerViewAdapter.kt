package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CustomizationGridItemBinding
import com.habitrpg.android.habitica.databinding.CustomizationSectionHeaderBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class CustomizationRecyclerViewAdapter() : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var userSize: String? = null
    var hairColor: String? = null
    var customizationType: String? = null
    var gemBalance: Int = 0
    var unsortedCustomizations: List<Customization> = ArrayList()
    var customizationList: MutableList<Any> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var additionalSetItems: List<Customization> = ArrayList()
    var activeCustomization: String? = null
    set(value) {
        field = value
        this.notifyDataSetChanged()
    }

    var ownedCustomizations: List<String> = listOf()

    private val selectCustomizationEvents = PublishSubject.create<Customization>()
    private val unlockCustomizationEvents = PublishSubject.create<Customization>()
    private val unlockSetEvents = PublishSubject.create<CustomizationSet>()

    fun updateOwnership(ownedCustomizations: List<String>) {
        this.ownedCustomizations = ownedCustomizations
        setCustomizations(unsortedCustomizations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.customization_section_header, parent, false)
            SectionViewHolder(view)
        } else {
            val viewID: Int = if (customizationType == "background") {
                R.layout.customization_grid_background_item
            } else {
                R.layout.customization_grid_item
            }

            val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
            CustomizationViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val obj = customizationList[position]
        if (obj.javaClass == CustomizationSet::class.java) {
            (holder as SectionViewHolder).bind(obj as CustomizationSet)
        } else {
            (holder as CustomizationViewHolder).bind(customizationList[position] as Customization)

        }
    }

    override fun getItemCount(): Int {
        return customizationList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.customizationList[position].javaClass == CustomizationSet::class.java) {
            0
        } else {
            1
        }
    }

    fun setCustomizations(newCustomizationList: List<Customization>) {
        unsortedCustomizations = newCustomizationList
        this.customizationList = ArrayList()
        var lastSet = CustomizationSet()
        val today = Date()
        for (customization in newCustomizationList.reversed()) {
            if (customization.availableFrom != null || customization.availableUntil != null) {
                if ((customization.availableFrom?.compareTo(today) ?: 0 > 0 || customization.availableUntil?.compareTo(today) ?: 0 < 0) && !customization.isUsable(ownedCustomizations.contains(customization.id))) {
                    continue
                }
            }
            if (customization.customizationSet != null && customization.customizationSet != lastSet.identifier) {
                val set = CustomizationSet()
                set.identifier = customization.customizationSet
                set.text = customization.customizationSetName
                set.price = customization.setPrice ?: 0
                set.hasPurchasable = !customization.isUsable(ownedCustomizations.contains(customization.id))
                lastSet = set
                customizationList.add(set)
            }
            customizationList.add(customization)
            if (!customization.isUsable(ownedCustomizations.contains(customization.id)) && !lastSet.hasPurchasable) {
                lastSet.hasPurchasable = true
            }
        }
        this.notifyDataSetChanged()
    }

    fun getSelectCustomizationEvents(): Flowable<Customization> {
        return selectCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockCustomizationEvents(): Flowable<Customization> {
        return unlockCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockSetEvents(): Flowable<CustomizationSet> {
        return unlockSetEvents.toFlowable(BackpressureStrategy.DROP)
    }

    internal inner class CustomizationViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationGridItemBinding.bind(itemView)
        var customization: Customization? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(customization: Customization) {
            this.customization = customization

            if (customization.type == "background" && customization.identifier == "") {
                binding.imageView.setActualImageResource(R.drawable.no_background)
            } else {
                DataBindingUtils.loadImage(binding.imageView, customization.getIconName(userSize, hairColor))
            }

            if (customization.type == "background") {
                val params = (binding.imageView.layoutParams as? LinearLayout.LayoutParams)?.apply {
                    gravity = Gravity.CENTER
                }
                binding.imageView.layoutParams = params
            }

            if (customization.isUsable(ownedCustomizations.contains(customization.id))) {
                binding.buyButton.visibility = View.GONE
            } else {
                binding.buyButton.visibility = View.VISIBLE
                if (customization.customizationSet?.contains("timeTravel") == true) {
                    binding.priceLabel.currency = "hourglasses"
                } else {
                    binding.priceLabel.currency = "gems"
                }
                binding.priceLabel.value = customization.price?.toDouble() ?: 0.0
            }

            if (activeCustomization == customization.identifier) {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window_tint_border)
            } else {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window)
            }
        }

        override fun onClick(v: View) {
            if (customization?.isUsable(ownedCustomizations.contains(customization?.id)) == false) {
                if (customization?.customizationSet?.contains("timeTravel") == true) {
                    val dialog = HabiticaAlertDialog(itemView.context)
                    dialog.setMessage(R.string.purchase_from_timetravel_shop)
                    dialog.addButton(R.string.go_shopping, true) { _, _ ->
                        MainNavigationController.navigate(R.id.timeTravelersShopFragment)
                    }
                    dialog.addButton(R.string.reward_dialog_dismiss, false)
                    dialog.show()
                } else {
                    val dialogContent = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_purchase_customization, null) as LinearLayout

                    val imageView = dialogContent.findViewById<SimpleDraweeView>(R.id.imageView)
                    DataBindingUtils.loadImage(imageView, customization?.getImageName(userSize, hairColor))

                    val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
                    priceLabel.text = customization?.price.toString()

                    (dialogContent.findViewById<View>(R.id.gem_icon) as? ImageView)?.setImageBitmap(HabiticaIconsHelper.imageOfGem())

                    val dialog = HabiticaAlertDialog(itemView.context)
                    dialog.addButton(R.string.purchase_button, true) { _, _ ->
                        if (customization?.price ?: 0 > gemBalance) {
                            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
                            return@addButton
                        }

                        customization?.let {
                            unlockCustomizationEvents.onNext(it)
                        }
                    }
                    dialog.setTitle(R.string.purchase_customization)
                    dialog.setAdditionalContentView(dialogContent)
                    dialog.addButton(R.string.reward_dialog_dismiss, false)
                    dialog.show()
                }
                return
            }

            if (customization?.type != "background" && customization?.identifier == activeCustomization) {
                return
            }

            customization?.let {
                selectCustomizationEvents.onNext(it)
            }
        }
    }

    internal inner class SectionViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationSectionHeaderBinding.bind(itemView)
        var context: Context = itemView.context
        private var set: CustomizationSet? = null

        init {
            binding.purchaseSetButton.setOnClickListener(this)
        }

        fun bind(set: CustomizationSet) {
            this.set = set
            binding.label.text = set.text
            if (set.hasPurchasable && set.identifier?.contains("timeTravel") != true) {
                binding.purchaseSetButton.visibility = View.VISIBLE
                binding.setPriceLabel.value = set.price.toDouble()
                binding.setPriceLabel.currency = "gems"
            } else {
                binding.purchaseSetButton.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            val dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_purchase_customization, null) as LinearLayout

            val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
            priceLabel.text = set?.price.toString()

            val dialog = HabiticaAlertDialog(context)
            dialog.addButton(R.string.purchase_button, true) { _, _ ->
                        if (set?.price ?: 0 > gemBalance) {
                            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
                            return@addButton
                        }
                        set?.customizations = ArrayList()
                        customizationList
                                .filter { Customization::class.java.isAssignableFrom(it.javaClass) }
                                .map { it as Customization }
                                .filter { it.customizationSet != null && it.customizationSet == set?.identifier }
                                .forEach { set?.customizations?.add(it) }
                        if (additionalSetItems.isNotEmpty()) {
                            additionalSetItems
                                    .filter { !it.isUsable(ownedCustomizations.contains(it.id)) && it.customizationSet == set?.identifier }
                                    .forEach { set?.customizations?.add(it) }
                        }
                        set?.let {
                            unlockSetEvents.onNext(it)
                        }
                    }
            dialog.setTitle(context.getString(R.string.purchase_set_title, set?.text))
            dialog.setAdditionalContentView(dialogContent)
            dialog.addButton(R.string.reward_dialog_dismiss, false)
            dialog.show()
        }
    }
}
