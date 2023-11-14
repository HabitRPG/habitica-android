package com.habitrpg.wearos.habitica.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.wear.widget.WearableRecyclerView
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.RecyclerViewState
import com.habitrpg.common.habitica.helpers.RecyclerViewStateAdapter
import com.habitrpg.wearos.habitica.ui.adapters.BaseAdapter

class HabiticaRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WearableRecyclerView(context, attrs) {

    init {
        isVerticalScrollBarEnabled = true
        focusable = View.FOCUSABLE
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            setPadding(0, (height * 0.1).toInt(), 0, (height * 0.25).toInt())
            scrollToPosition(0)
            requestFocus()
        }
    }

    var onRefresh: (() -> Unit)?
        get() = emptyAdapter.onRefresh
        set(value) { emptyAdapter.onRefresh = value }

    var state: RecyclerViewState = RecyclerViewState.LOADING
        set(value) {
            field = value
            when (field) {
                RecyclerViewState.DISPLAYING_DATA -> updateAdapter(actualAdapter)
                else -> {
                    updateAdapter(emptyAdapter)
                    emptyAdapter.state = value
                }
            }
        }

    private fun updateAdapter(newAdapter: Adapter<*>?) {
        if (adapter != newAdapter) {
            super.setAdapter(newAdapter)
        }
    }

    var emptyItem: EmptyItem?
        get() = emptyAdapter.emptyItem
        set(value) {
            emptyAdapter.emptyItem = value
        }

    var emptyViewBuilder: (() -> View)?
        get() = emptyAdapter.emptyViewBuilder
        set(value) {
            emptyAdapter.emptyViewBuilder = value
        }

    private var actualAdapter: Adapter<*>? = null
    private val emptyAdapter = RecyclerViewStateAdapter(true)

    private val observer = object : AdapterDataObserver() {
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

    internal fun updateState(isInitial: Boolean = false) {
        state = if (actualAdapter != null && !isInitial) {
            val emptyViewVisible = if (actualAdapter is BaseAdapter<*>) {
                (actualAdapter as? BaseAdapter<*>)?.hasData() != true
            } else {
                actualAdapter?.itemCount == 0
            }
            if (emptyViewVisible) {
                RecyclerViewState.EMPTY
            } else {
                RecyclerViewState.DISPLAYING_DATA
            }
        } else {
            RecyclerViewState.LOADING
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
