package com.habitrpg.wearos.habitica.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.databinding.RowHeaderBinding
import com.habitrpg.android.habitica.databinding.RowSpacerBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.wearos.habitica.ui.viewHolders.HeaderViewHolder
import com.habitrpg.wearos.habitica.ui.viewHolders.SpacerViewHolder

abstract class BaseAdapter<D : Any> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var title: String = ""
        set(value) {
            val previous = field
            field = value
            notifyItemChanged(0)
        }
    var onRefresh: (() -> Unit)? = null
    var isDisconnected = false
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    var data: List<D> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    fun hasData(): Boolean = data.isNotEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(RowHeaderBinding.inflate(parent.context.layoutInflater, parent, false).root)
            else -> SpacerViewHolder(RowSpacerBinding.inflate(parent.context.layoutInflater, parent, false).root)
        }
    }

    override fun getItemCount() = data.size + 1
    protected fun getItemAt(position: Int) = data[position - 1]

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(title)
            holder.setIsDisconnected(isDisconnected)
            holder.itemView.setOnClickListener { onRefresh?.invoke() }
        }
    }

    companion object {
        const val TYPE_HEADER = -10
    }
}
