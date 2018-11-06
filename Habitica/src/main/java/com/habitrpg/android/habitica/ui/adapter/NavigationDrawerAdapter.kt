package com.habitrpg.android.habitica.ui.adapter

import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.backgroundCompat
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject


class NavigationDrawerAdapter(tintColor: Int, backgroundTintColor: Int): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var tintColor: Int = tintColor
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var backgroundTintColor: Int = backgroundTintColor
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    internal val items: MutableList<HabiticaDrawerItem> = ArrayList()
    var selectedItem: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val itemSelectedEvents = PublishSubject.create<String>()


    fun getItemSelectionEvents(): Flowable<String> = itemSelectedEvents.toFlowable(BackpressureStrategy.DROP)

    fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
            items.find { it.identifier == identifier }

    private fun getItemPosition(identifier: String): Int =
            items.indexOfFirst { it.identifier == identifier }

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

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val drawerItem = getItem(position)
        if (getItemViewType(position) == 0) {
            (holder as DrawerItemViewHolder?)?.tintColor = tintColor
            holder.backgroundTintColor = backgroundTintColor
            holder.bind(drawerItem, drawerItem.identifier == selectedItem)
            holder.itemView.setOnClickListener { itemSelectedEvents.onNext(drawerItem.identifier) }
        } else {
            (holder as SectionHeaderViewHolder?)?.backgroundTintColor = backgroundTintColor
            holder.bind(drawerItem)
        }
    }

    private fun getItem(position: Int) = items.filter { it.isVisible }[position]

    override fun getItemCount(): Int = items.count { it.isVisible }

    override fun getItemViewType(position: Int): Int = if (getItem(position).isHeader) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 0) {
            DrawerItemViewHolder(parent.inflate(R.layout.drawer_main_item))
        } else {
            SectionHeaderViewHolder(parent.inflate(R.layout.drawer_main_section_header))
        }
    }

    class DrawerItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        var tintColor: Int = 0
        var backgroundTintColor: Int = 0

        private val titleTextView: TextView? by bindOptionalView(itemView, R.id.titleTextView)
        private val pillView: TextView? by bindOptionalView(itemView, R.id.pillView)
        private val additionalInfoView: TextView? by bindOptionalView(itemView, R.id.additionalInfoView)

        fun bind(drawerItem: HabiticaDrawerItem, isSelected: Boolean) {
            titleTextView?.text = drawerItem.text

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.gray_600))
                titleTextView?.setTextColor(tintColor)
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                titleTextView?.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_10))
            }

            if (drawerItem.additionalInfo == null) {
                pillView?.visibility = View.GONE
                additionalInfoView?.visibility = View.GONE
            } else {
                if (drawerItem.additionalInfoAsPill) {
                    additionalInfoView?.visibility = View.GONE
                    val pillView = this.pillView
                    if (pillView != null) {
                        pillView.visibility = View.VISIBLE
                        pillView.text = drawerItem.additionalInfo
                        val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.pill_bg)
                        if (drawable != null) {
                            DrawableCompat.setTint(drawable, backgroundTintColor)

                            val pL = pillView.paddingLeft
                            val pT = pillView.paddingTop
                            val pR = pillView.paddingRight
                            val pB = pillView.paddingBottom

                            pillView.backgroundCompat = drawable
                            pillView.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                            pillView.setPadding(pL, pT, pR, pB)
                        }
                    }
                } else {
                    pillView?.visibility = View.GONE
                    val additionalInfoView = this.additionalInfoView
                    if (additionalInfoView != null) {
                        additionalInfoView.text = drawerItem.additionalInfo
                        additionalInfoView.visibility = View.VISIBLE
                        additionalInfoView.setTextColor(tintColor)
                    }
                }
            }
        }
    }

    class SectionHeaderViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        var backgroundTintColor: Int = 0

        fun bind(drawerItem: HabiticaDrawerItem) {
            (itemView as? TextView)?.text = drawerItem.text
            itemView.setBackgroundColor(backgroundTintColor)
        }
    }
}