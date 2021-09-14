package com.habitrpg.android.habitica.ui.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var data: List<T> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return data.size
    }

    open fun getItem(position: Int): T? {
        return if (position >= 0 && data.size > position) {
            data[position]
        } else {
            null
        }
    }
}
