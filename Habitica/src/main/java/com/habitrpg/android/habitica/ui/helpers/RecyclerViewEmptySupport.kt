package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.EmptyItemBinding
import com.habitrpg.android.habitica.databinding.FailedItemBinding
import com.habitrpg.android.habitica.extensions.inflate

data class EmptyItem(
    var title: String,
    var text: String? = null,
    var iconResource: Int? = null,
    var buttonLabel: String? = null,
    var onButtonTap: (() -> Unit)? = null
)

enum class RecyclerViewState {
    LOADING,
    EMPTY,
    DISPLAYING_DATA,
    FAILED
}

class RecyclerViewEmptySupport : RecyclerView {
    var onRefresh: (() -> Unit)? = null
    var state: RecyclerViewState = RecyclerViewState.LOADING
        set(value) {
            field = value
            when (field) {
                RecyclerViewState.DISPLAYING_DATA -> updateAdapter(actualAdapter)
                else -> {
                    updateAdapter(emptyAdapter)
                    emptyAdapter.notifyDataSetChanged()
                }
            }
        }

    private fun updateAdapter(newAdapter: Adapter<*>?) {
        if (adapter != newAdapter) {
            super.setAdapter(newAdapter)
        }
    }

    var emptyItem: EmptyItem? = null
        set(value) {
            field = value
            emptyAdapter.notifyDataSetChanged()
        }

    private var actualAdapter: Adapter<*>? = null
    private val emptyAdapter: Adapter<ViewHolder> = object : Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when (viewType) {
                0 -> {
                    val view = parent.inflate(R.layout.loading_item)
                    val animation1 = AlphaAnimation(0.0f, 1.0f)
                    animation1.duration = 300
                    animation1.startOffset = 500
                    animation1.fillAfter = true
                    view.findViewById<ProgressBar>(R.id.loading_indicator).startAnimation(animation1)
                    object : ViewHolder(view) {}
                }
                1 -> FailedViewHolder(parent.inflate(R.layout.failed_item))
                else -> EmptyViewHolder(parent.inflate(R.layout.empty_item))
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
            return when (state) {
                RecyclerViewState.LOADING -> 0
                RecyclerViewState.FAILED -> 1
                else -> 2
            }
        }
    }

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            updateState()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            updateState()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            updateState()
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    internal fun updateState(isInitial: Boolean = false) {
        if (actualAdapter != null && !isInitial) {
            val emptyViewVisible = actualAdapter?.itemCount == 0
            if (emptyViewVisible) {
                state = RecyclerViewState.EMPTY
            } else {
                state = RecyclerViewState.DISPLAYING_DATA
            }
        } else {
            state = RecyclerViewState.LOADING
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = actualAdapter
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        actualAdapter = adapter
        updateState(true)
    }
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

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = EmptyItemBinding.bind(itemView)

    fun bind(emptyItem: EmptyItem?) {
        binding.emptyIconView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_dimmed), android.graphics.PorterDuff.Mode.MULTIPLY)
        emptyItem?.iconResource?.let { binding.emptyIconView.setImageResource(it) }
        binding.emptyViewTitle.text = emptyItem?.title
        binding.emptyViewDescription.text = emptyItem?.text

        val buttonLabel = emptyItem?.buttonLabel
        if (buttonLabel != null) {
            binding.button.visibility = View.VISIBLE
            binding.button.text = buttonLabel
            binding.button.setOnClickListener { emptyItem.onButtonTap?.invoke() }
        } else {
            binding.button.visibility = View.GONE
        }
    }
}
