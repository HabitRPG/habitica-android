package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogChallengeFilterBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.helpers.RecyclerViewState

internal class ChallengeFilterDialogHolder private constructor(
    view: View,
    private val context: Activity
) {
    private val binding = DialogChallengeFilterBinding.bind(view)

    private var filterGroups: List<Group> = emptyList()
    private var currentFilter: ChallengeFilterOptions? = null
    private var selectedGroupsCallback: ((ChallengeFilterOptions) -> Unit)? = null
    private var adapter: ChallengesFilterRecyclerViewAdapter? = null

    init {
        binding.challengeFilterOwned.setOnCheckedChangeListener { _, isChecked ->
            currentFilter?.showOwned = isChecked
        }
        binding.challengeFilterNotOwned.setOnCheckedChangeListener { _, isChecked ->
            currentFilter?.notOwned = isChecked
        }
        binding.challengeFilterParticipating.setOnCheckedChangeListener { _, isChecked ->
            currentFilter?.showParticipating = isChecked
        }
        binding.challengeFilterNotParticipating.setOnCheckedChangeListener { _, isChecked ->
            currentFilter?.notParticipating = isChecked
        }

        binding.clearButton.visibility = View.VISIBLE
        binding.clearButton.setOnClickListener {
            clearFilters()
        }
    }

    private fun clearFilters() {
        binding.challengeFilterOwned.isChecked = false
        binding.challengeFilterNotOwned.isChecked = false
        binding.challengeFilterParticipating.isChecked = false
        binding.challengeFilterNotParticipating.isChecked = false
        adapter?.checkedEntries?.clear()
        adapter?.notifyDataSetChanged()
        currentFilter?.showOwned = false
        currentFilter?.notOwned = false
        currentFilter?.showParticipating = false
        currentFilter?.notParticipating = false
        currentFilter?.showByGroups = emptyList()
    }

    fun bind(
        filterGroups: List<Group>,
        currentFilter: ChallengeFilterOptions?,
        selectedGroupsCallback: ((ChallengeFilterOptions) -> Unit)?
    ) {
        this.filterGroups = filterGroups
        this.currentFilter = currentFilter
        this.selectedGroupsCallback = selectedGroupsCallback
        fillChallengeGroups()

        if (currentFilter != null) {
            binding.challengeFilterOwned.isChecked = currentFilter.showOwned
            binding.challengeFilterNotOwned.isChecked = currentFilter.notOwned
            binding.challengeFilterParticipating.isChecked = currentFilter.showParticipating
            binding.challengeFilterNotParticipating.isChecked = currentFilter.notParticipating
        }
    }

    private fun fillChallengeGroups() {
        binding.challengeFilterRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ChallengesFilterRecyclerViewAdapter(filterGroups)
        currentFilter?.let { currentFilter ->
            adapter?.checkedEntries?.addAll(currentFilter.showByGroups)
        }
        binding.challengeFilterRecyclerView.adapter = adapter

        binding.challengeFilterRecyclerView.state = RecyclerViewState.DISPLAYING_DATA
    }

    companion object {
        fun showDialog(
            activity: Activity,
            filterGroups: List<Group>,
            currentFilter: ChallengeFilterOptions?,
            selectedGroupsCallback: ((ChallengeFilterOptions) -> Unit)
        ) {
            val dialogLayout =
                activity.layoutInflater.inflate(R.layout.dialog_challenge_filter, null)

            val holder = ChallengeFilterDialogHolder(dialogLayout, activity)

            val sheet = HabiticaBottomSheetDialog(activity)
            sheet.setContentView(dialogLayout)
            sheet.setOnDismissListener {
                selectedGroupsCallback(
                    ChallengeFilterOptions(
                        holder.adapter?.checkedEntries ?: emptyList(),
                        holder.binding.challengeFilterOwned.isChecked,
                        holder.binding.challengeFilterNotOwned.isChecked,
                        holder.binding.challengeFilterParticipating.isChecked,
                        holder.binding.challengeFilterNotParticipating.isChecked
                    )
                )
            }

            holder.bind(filterGroups, currentFilter, selectedGroupsCallback)
            sheet.show()
        }
    }
}
