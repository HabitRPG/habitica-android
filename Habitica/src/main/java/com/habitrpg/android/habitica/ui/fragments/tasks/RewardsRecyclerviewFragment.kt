package com.habitrpg.android.habitica.ui.fragments.tasks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.launch

class RewardsRecyclerviewFragment : TaskRecyclerViewFragment() {

    private var showCustomRewards: Boolean = true
    private var selectedCard: ShopItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.launchCatching {
            inventoryRepository.retrieveInAppRewards()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (layoutManager as? GridLayoutManager)?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((recyclerAdapter?.getItemViewType(position) ?: 0) < 3) {
                    (layoutManager as? GridLayoutManager)?.spanCount ?: 1
                } else {
                    1
                }
            }
        }

        view.post { setGridSpanCount(view.width) }
        context?.let {
            binding?.recyclerView?.setBackgroundColor(ContextCompat.getColor(it, R.color.content_background))
        }
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        if (showCustomRewards) {
            lifecycleScope.launchCatching {
                inventoryRepository.getInAppRewards().collect {
                    (recyclerAdapter as? RewardsRecyclerViewAdapter)?.updateItemRewards(it)
                }
            }
        }

        (recyclerAdapter as? RewardsRecyclerViewAdapter)?.purchaseCardEvents = {
            selectedCard = it
            val intent = Intent(activity, SkillMemberActivity::class.java)
            cardSelectedResult.launch(intent)
        }
        recyclerAdapter?.brokenTaskEvents = { showBrokenChallengeDialog(it) }

        viewModel.user.observe(viewLifecycleOwner) {
            (recyclerAdapter as? RewardsRecyclerViewAdapter)?.user = it
        }
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun getLayoutManager(context: Context?): LinearLayoutManager {
        return GridLayoutManager(context, 4)
    }

    override fun onRefresh() {
        binding?.refreshLayout?.isRefreshing = true
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(withTasks = true, forced = true)
            inventoryRepository.retrieveInAppRewards()
            binding?.refreshLayout?.isRefreshing = false
        }
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
        (layoutManager as? GridLayoutManager)?.spanCount = spanCount
    }

    private val cardSelectedResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launchCatching {
                userRepository.useSkill(
                    selectedCard?.key ?: "",
                    "member",
                    it.data?.getStringExtra("member_id") ?: ""
                )
                val activity = (activity as? MainActivity) ?: return@launchCatching
                HabiticaSnackbar.showSnackbar(
                    activity.snackbarContainer,
                    context?.getString(R.string.sent_card, selectedCard?.text),
                    HabiticaSnackbar.SnackbarDisplayType.BLUE
                )
            }
        }
    }

    companion object {
        fun newInstance(context: Context?, classType: TaskType, showCustomRewards: Boolean): RewardsRecyclerviewFragment {
            val fragment = RewardsRecyclerviewFragment()
            fragment.taskType = classType
            fragment.showCustomRewards = showCustomRewards

            if (context != null) {
                fragment.tutorialStepIdentifier = "rewards"
                fragment.tutorialTexts = ArrayList(listOf(context.getString(R.string.tutorial_rewards_1), context.getString(R.string.tutorial_rewards_2)))
            }
            fragment.tutorialCanBeDeferred = false

            return fragment
        }
    }
}
