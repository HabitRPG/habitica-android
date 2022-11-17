package com.habitrpg.android.habitica.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuViewHolder
import com.habitrpg.android.habitica.ui.views.promo.SubscriptionBuyGemsPromoView
import com.habitrpg.android.habitica.ui.views.promo.SubscriptionBuyGemsPromoViewHolder
import com.habitrpg.common.habitica.extensions.dpToPx

class NavigationDrawerAdapter(tintColor: Int, backgroundTintColor: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var tintColor: Int = tintColor
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var backgroundTintColor: Int = backgroundTintColor
        set(value) {
            field = value
            for (item in items) {
                if (item.isHeader) {
                    val visiblePosition = getVisibleItemPosition(item.identifier)
                    if (visiblePosition >= 0) {
                        notifyItemChanged(visiblePosition)
                    }
                }
            }
        }

    internal val items: MutableList<HabiticaDrawerItem> = ArrayList()
    var selectedItem: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var itemSelectedEvents: ((HabiticaDrawerItem) -> Unit)? = null
    var promoClosedSubject: ((String) -> Unit)? = null

    var activePromo: HabiticaPromotion? = null

    fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
        items.find { it.identifier == identifier }

    private fun getItemPosition(identifier: String): Int =
        items.indexOfFirst { it.identifier == identifier }
    private fun getVisibleItemPosition(identifier: String): Int =
        items.filter { it.isVisible }.indexOfFirst { it.identifier == identifier }

    fun updateItem(item: HabiticaDrawerItem) {
        val position = getItemPosition(item.identifier)
        items[position] = item
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<HabiticaDrawerItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val drawerItem = getItem(position)
        when {
            getItemViewType(position) == 0 -> {
                val itemHolder = holder as? DrawerItemViewHolder
                itemHolder?.tintColor = tintColor
                itemHolder?.bind(drawerItem, drawerItem.transitionId == selectedItem)
                itemHolder?.itemView?.setOnClickListener { itemSelectedEvents?.invoke(drawerItem) }
            }
            getItemViewType(position) == 1 -> {
                (holder as? SectionHeaderViewHolder)?.backgroundTintColor = backgroundTintColor
                (holder as? SectionHeaderViewHolder)?.bind(drawerItem)
            }
            getItemViewType(position) == 5 -> {
                activePromo?.let { promo ->
                    (holder as? PromoMenuViewHolder)?.bind(promo)
                    (holder as? PromoMenuViewHolder)?.promoView?.binding?.closeButton?.setOnClickListener {
                        promoClosedSubject?.invoke(promo.identifier)
                    }
                }
            }
        }
    }

    private fun getItem(position: Int) = items.filter { it.isVisible }[position]

    override fun getItemCount(): Int = items.count { it.isVisible }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader) {
            1
        } else {
            getItem(position).itemViewType ?: 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            2 -> {
                val itemView = SubscriptionBuyGemsPromoView(parent.context)
                itemView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    148.dpToPx(parent.context)
                )
                SubscriptionBuyGemsPromoViewHolder(itemView)
            }
            5 -> {
                val promoView = PromoMenuView(parent.context)
                promoView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    148.dpToPx(parent.context)
                )
                PromoMenuViewHolder(promoView)
            }
            1 -> SectionHeaderViewHolder(parent.inflate(R.layout.drawer_main_section_header))
            else -> DrawerItemViewHolder(parent.inflate(R.layout.drawer_main_item))
        }
    }

    class DrawerItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tintColor: Int = 0

        private val titleTextView: TextView? = itemView.findViewById(R.id.titleTextView)
        private val pillView: TextView? = itemView.findViewById(R.id.pillView)
        private val bubbleView: View? = itemView.findViewById(R.id.bubble_view)
        private val additionalInfoView: TextView? = itemView.findViewById(R.id.additionalInfoView)

        fun bind(drawerItem: HabiticaDrawerItem, isSelected: Boolean) {
            titleTextView?.text = drawerItem.text

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.content_background_offset))
                itemView.background.alpha = 69
                titleTextView?.setTextColor(tintColor)
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.content_background))
                titleTextView?.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }

            if (drawerItem.pillText != null) {
                pillView?.let { pillView ->
                    pillView.visibility = View.VISIBLE
                    pillView.text = drawerItem.pillText

                    val pL = pillView.paddingLeft
                    val pT = pillView.paddingTop
                    val pR = pillView.paddingRight
                    val pB = pillView.paddingBottom

                    pillView.background = drawerItem.pillBackground ?: ContextCompat.getDrawable(itemView.context, R.drawable.pill_bg_purple_200)
                    pillView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    pillView.setPadding(pL, pT, pR, pB)
                }
            } else {
                pillView?.visibility = View.GONE
            }
            if (drawerItem.subtitle != null) {
                additionalInfoView?.let { additionalInfoView ->
                    additionalInfoView.text = drawerItem.subtitle
                    additionalInfoView.visibility = View.VISIBLE
                    additionalInfoView.setTextColor(drawerItem.subtitleTextColor ?: tintColor)
                }
            } else {
                additionalInfoView?.visibility = View.GONE
            }
            bubbleView?.visibility = if (drawerItem.showBubble) View.VISIBLE else View.GONE
        }
    }

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var backgroundTintColor: Int = 0

        fun bind(drawerItem: HabiticaDrawerItem) {
            (itemView as? TextView)?.text = drawerItem.text
            itemView.setBackgroundColor(backgroundTintColor)
        }
    }
}
