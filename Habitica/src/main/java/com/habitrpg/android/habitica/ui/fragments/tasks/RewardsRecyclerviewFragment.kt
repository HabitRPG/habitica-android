package com.habitrpg.android.habitica.ui.fragments.tasks

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import kotlinx.android.synthetic.main.fragment_refresh_recyclerview.*
import rx.functions.Action1
import java.util.*

class RewardsRecyclerviewFragment : TaskRecyclerViewFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inventoryRepository.retrieveInAppRewards().subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (recyclerAdapter?.getItemViewType(position) ?: 0 < 2) {
                    (layoutManager as GridLayoutManager).spanCount
                } else {
                    1
                }
            }
        }

        view.post { setGridSpanCount(view.width) }
        context.notNull {
            recyclerView.setBackgroundColor(ContextCompat.getColor(it, R.color.white))
        }
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        inventoryRepository.inAppRewards.subscribe(Action1 {
            (recyclerAdapter as RewardsRecyclerViewAdapter?)?.updateItemRewards(it)
        }, RxErrorHandler.handleEmptyError())
    }

    override fun getLayoutManager(context: Context?): LinearLayoutManager =
            GridLayoutManager(context, 4)

    override fun onRefresh() {
        refreshLayout.isRefreshing = true
        userRepository.retrieveUser(true, true)
                .flatMap<List<ShopItem>> { inventoryRepository.retrieveInAppRewards() }
                .doOnTerminate {
                    refreshLayout?.isRefreshing = false
                }.subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
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
