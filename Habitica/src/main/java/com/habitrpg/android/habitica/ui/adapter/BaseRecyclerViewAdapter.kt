package com.habitrpg.android.habitica.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.models.BaseMainObject

open class DiffCallback<T : BaseMainObject>(
    protected val oldList: List<BaseMainObject>,
    protected val newList: List<BaseMainObject>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldList[oldItemPosition].primaryIdentifier == newList[newItemPosition].primaryIdentifier

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }
}

abstract class BaseRecyclerViewAdapter<T : BaseMainObject, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    open fun getDiffCallback(
        oldList: List<T>,
        newList: List<T>,
    ): DiffCallback<T>? = null

    var data: List<T> = emptyList()
        set(value) {
            val diffCallback = getDiffCallback(data, value)
            field = value
            if (diffCallback != null) {
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                diffResult.dispatchUpdatesTo(this)
            } else {
                notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int = data.size

    open fun getItem(position: Int): T? =
        if (position >= 0 && data.size > position) {
            data[position]
        } else {
            null
        }
}
