package com.habitrpg.wearos.habitica.ui.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BindableViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(data: T)
}