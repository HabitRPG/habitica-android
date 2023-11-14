package com.habitrpg.common.habitica.helpers

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.databinding.EmptyItemBinding
import com.habitrpg.common.habitica.databinding.FailedItemBinding

data class EmptyItem(
    var title: String,
    var text: String? = null,
    var iconResource: Int? = null,
    var tintedIcon: Boolean = true,
    var onButtonTap: (() -> Unit)? = null
)

enum class RecyclerViewState {
    LOADING,
    EMPTY,
    DISPLAYING_DATA,
    FAILED
}

class FailedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = FailedItemBinding.bind(itemView)

    fun bind(onRefresh: (() -> Unit)?) {
        if (onRefresh != null) {
            binding.refreshButton.visibility = View.VISIBLE
            binding.refreshButton.setOnClickListener { onRefresh() }
        } else {
            binding.refreshButton.visibility = View.GONE
        }
    }
}

class HolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = EmptyItemBinding.bind(itemView)

    fun bind(emptyItem: EmptyItem?) {
        if (emptyItem?.tintedIcon == true) {
            binding.emptyIconView.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.text_dimmed
                ),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
        }
        emptyItem?.iconResource?.let { binding.emptyIconView.setImageResource(it) }
        binding.emptyViewTitle.text = emptyItem?.title
        binding.emptyViewDescription.setMarkdown(emptyItem?.text)
        if (emptyItem?.onButtonTap != null) {
            binding.emptyView.setOnClickListener { emptyItem.onButtonTap?.invoke() }
        }
    }
}

class RecyclerViewStateAdapter(val showLoadingAsEmpty: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onRefresh: (() -> Unit)? = null
    var emptyViewBuilder: (() -> View)? = null
    var emptyItem: EmptyItem? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    var state: RecyclerViewState = RecyclerViewState.LOADING
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = parent.inflate(R.layout.loading_item)
                val animation1 = AlphaAnimation(0.0f, 1.0f)
                animation1.duration = 300
                animation1.startOffset = 500
                animation1.fillAfter = true
                view.findViewById<ProgressBar>(R.id.loading_indicator).startAnimation(animation1)
                object : RecyclerView.ViewHolder(view) {}
            }
            1 -> FailedViewHolder(parent.inflate(R.layout.failed_item))
            else -> if (emptyViewBuilder != null) {
                HolderViewHolder(emptyViewBuilder?.invoke() ?: View(parent.context))
            } else {
                EmptyViewHolder(parent.inflate(R.layout.empty_item))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmptyViewHolder) {
            holder.bind(emptyItem)
        } else if (holder is FailedViewHolder) {
            holder.bind(onRefresh)
        }
        (holder as? EmptyViewHolder)?.bind(emptyItem)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            state == RecyclerViewState.LOADING && !showLoadingAsEmpty -> 0
            state == RecyclerViewState.FAILED -> 1
            else -> 2
        }
    }
}
