package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogChallengeFilterBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog

internal class ChallengeFilterDialogHolder private constructor(
    view: View,
    private val context: Activity
) {
    private val binding = DialogChallengeFilterBinding.bind(view)

    private var filterGroups: List<Group>? = null
    private var currentFilter: ChallengeFilterOptions? = null
    private var selectedGroupsCallback: ((ChallengeFilterOptions) -> Unit)? = null
    private var adapter: ChallengesFilterRecyclerViewAdapter? = null

    init {
        binding.challengeFilterButtonAll.setOnClickListener { allClicked() }
        binding.challengeFilterButtonNone.setOnClickListener { noneClicked() }
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
        }
    }

    private fun fillChallengeGroups() {
        binding.challengeFilterRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = filterGroups?.let { ChallengesFilterRecyclerViewAdapter(it) }
        if (currentFilter != null && currentFilter?.showByGroups != null) {
            adapter?.selectAll(currentFilter?.showByGroups ?: emptyList())
        }

        binding.challengeFilterRecyclerView.adapter = adapter
    }

    private fun allClicked() {
        this.adapter?.selectAll()
    }

    private fun noneClicked() {
        this.adapter?.deSelectAll()
    }

    companion object {

        fun showDialog(
            activity: Activity,
            filterGroups: List<Group>,
            currentFilter: ChallengeFilterOptions?,
            selectedGroupsCallback: ((ChallengeFilterOptions) -> Unit)?
        ) {
            val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_challenge_filter, null)

            val challengeFilterDialogHolder = ChallengeFilterDialogHolder(dialogLayout, activity)

            val sheet = HabiticaBottomSheetDialog(activity)
            sheet.setContentView(dialogLayout)

            challengeFilterDialogHolder.bind(filterGroups, currentFilter, selectedGroupsCallback)
            sheet.show()
        }
    }
}
