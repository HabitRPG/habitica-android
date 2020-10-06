package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.ViewGroup
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.ui.viewHolders.MountViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject

class MountDetailRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    private var ownedMounts: Map<String, OwnedMount>? = null

    private val equipEvents = PublishSubject.create<String>()

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            when (viewType) {
                1 -> SectionViewHolder(parent)
                else -> MountViewHolder(parent, equipEvents)
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (val obj = this.itemList[position]) {
            is StableSection -> (holder as? SectionViewHolder)?.bind(obj)
            is Mount  -> (holder as? MountViewHolder)?.bind(obj, ownedMounts?.get(obj.key ?: "")?.owned == true)
        }
    }

    override fun getItemViewType(position: Int): Int = if (itemList[position] is StableSection) 1 else 2

    override fun getItemCount(): Int = itemList.size

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }
}
