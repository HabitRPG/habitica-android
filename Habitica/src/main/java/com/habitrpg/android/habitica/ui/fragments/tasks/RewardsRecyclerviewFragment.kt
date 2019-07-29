package com.habitrpg.android.habitica.ui.fragments.tasks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_refresh_recyclerview.*
import java.util.*

class RewardsRecyclerviewFragment : TaskRecyclerViewFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        compositeSubscription.add(inventoryRepository.retrieveInAppRewards().subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (layoutManager as? GridLayoutManager)?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (recyclerAdapter?.getItemViewType(position) ?: 0 < 2) {
                    (layoutManager as? GridLayoutManager)?.spanCount ?: 1
                } else {
                    1
                }
            }
        }

        view.post { setGridSpanCount(view.width) }
        context?.let {
            recyclerView.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
        }
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        compositeSubscription.add(inventoryRepository.getInAppRewards().subscribe(Consumer {
            (recyclerAdapter as? RewardsRecyclerViewAdapter)?.updateItemRewards(it)
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun getLayoutManager(context: Context?): LinearLayoutManager =
            GridLayoutManager(context, 4)

    override fun onRefresh() {
        refreshLayout.isRefreshing = true
        compositeSubscription.add(userRepository.retrieveUser(true, true)
                .flatMap<List<ShopItem>> { inventoryRepository.retrieveInAppRewards() }
                .doOnTerminate {
                    refreshLayout?.isRefreshing = false
                }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        if (context != null && context?.resources != null) {
            val itemWidth: Float = context?.resources?.getDimension(R.dimen.reward_width) ?: 0f

            spanCount = (width / itemWidth).toInt()
        }

        if (spanCount == 0) {
            spanCount = 1
        }
        (layoutManager as GridLayoutManager).spanCount = spanCount
    }

    companion object {

        fun newInstance(context: Context?, user: User?, classType: String): RewardsRecyclerviewFragment {
            val fragment = RewardsRecyclerviewFragment()
            fragment.retainInstance = true
            fragment.user = user
            fragment.classType = classType

            if (context != null) {
                fragment.tutorialStepIdentifier = "rewards"
                fragment.tutorialTexts = ArrayList(Arrays.asList(context.getString(R.string.tutorial_rewards_1),
                        context.getString(R.string.tutorial_rewards_2)))
            }
            fragment.tutorialCanBeDeferred = false

            return fragment
        }
    }
}
