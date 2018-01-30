package com.habitrpg.android.habitica.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindOptionalView
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import rx.Observable
import rx.subjects.PublishSubject

class NavigationDrawerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal val items: MutableList<HabiticaDrawerItem> = ArrayList()
    var selectedItem: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val itemSelectedEvents = PublishSubject.create<String>()


    fun getItemSelectionEvents(): Observable<String> = itemSelectedEvents.asObservable()

    fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
            items.find { it.identifier == identifier }

    fun getItemPosition(identifier: String): Int =
            items.indexOfFirst { it.identifier == identifier }

    fun updateItem(item: HabiticaDrawerItem) {
        val position = getItemPosition(item.identifier)
        items[position] = item
        notifyItemChanged(position)
    }

    fun updateItems(newItems: List<HabiticaDrawerItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val drawerItem = getItem(position)
        if (getItemViewType(position) == 0) {
            (holder as DrawerItemViewHolder?)?.bind(drawerItem, drawerItem.identifier == selectedItem)
            holder?.itemView?.setOnClickListener { itemSelectedEvents.onNext(drawerItem.identifier) }
        } else {
            (holder as SectionHeaderViewHolder?)?.bind(drawerItem)
        }
    }

    private fun getItem(position: Int) = items.filter { it.isVisible }[position]

    override fun getItemCount(): Int = items.count { it.isVisible }

    override fun getItemViewType(position: Int): Int = if (getItem(position).isHeader) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            DrawerItemViewHolder(parent?.inflate(R.layout.drawer_main_item))
        } else {
            SectionHeaderViewHolder(parent?.inflate(R.layout.drawer_main_section_header))
        }
    }

    class DrawerItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView: TextView? by bindOptionalView(itemView, R.id.titleTextView)
        private val additionalInfoView: TextView? by bindOptionalView(itemView, R.id.additionalInfoView)

        fun bind(drawerItem: HabiticaDrawerItem, isSelected: Boolean) {
            titleTextView?.text = drawerItem.text

            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.gray_600))
                titleTextView?.setTextColor(ContextCompat.getColor(itemView.context, R.color.brand_300))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                titleTextView?.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_50))
            }

            if (drawerItem.additionalInfo != null) {
                additionalInfoView?.visibility = View.VISIBLE
                additionalInfoView?.text = drawerItem.additionalInfo
            } else {
                additionalInfoView?.visibility = View.GONE
            }
        }
    }

    class SectionHeaderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(drawerItem: HabiticaDrawerItem) {
            (itemView as TextView).text = drawerItem.text
        }
    }
}