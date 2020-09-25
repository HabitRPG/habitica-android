package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogChallengeFilterBinding
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter
import com.habitrpg.android.habitica.utils.Action1

internal class ChallengeFilterDialogHolder private constructor(view: View, private val context: Activity) {
    private val binding = DialogChallengeFilterBinding.bind(view)

    private var dialog: AlertDialog? = null
    private var filterGroups: List<Group>? = null
    private var currentFilter: ChallengeFilterOptions? = null
    private var selectedGroupsCallback: Action1<ChallengeFilterOptions>? = null
    private var adapter: ChallengesFilterRecyclerViewAdapter? = null

    init {
        binding.challengeFilterButtonAll.setOnClickListener { allClicked() }
        binding.challengeFilterButtonNone.setOnClickListener { noneClicked() }
    }

    fun bind(builder: AlertDialog.Builder, filterGroups: List<Group>,
             currentFilter: ChallengeFilterOptions?,
             selectedGroupsCallback: Action1<ChallengeFilterOptions>) {
        builder.setPositiveButton(context.getString(R.string.done)) { _, _ -> doneClicked() }
                .show()
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

    private fun doneClicked() {
        val options = ChallengeFilterOptions()
        options.showByGroups = this.adapter?.checkedEntries
        options.showOwned = binding.challengeFilterOwned.isChecked
        options.notOwned = binding.challengeFilterNotOwned.isChecked

        selectedGroupsCallback?.call(options)
        this.dialog?.hide()
    }

    private fun allClicked() {
        this.adapter?.selectAll()
    }

    private fun noneClicked() {
        this.adapter?.deSelectAll()
    }

    companion object {

        fun showDialog(activity: Activity, filterGroups: List<Group>,
                       currentFilter: ChallengeFilterOptions?,
                       selectedGroupsCallback: Action1<ChallengeFilterOptions>) {
            val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_challenge_filter, null)

            val challengeFilterDialogHolder = ChallengeFilterDialogHolder(dialogLayout, activity)

            val builder = AlertDialog.Builder(activity)
                    .setTitle(R.string.filter)
                    .setView(dialogLayout)

            challengeFilterDialogHolder.bind(builder, filterGroups, currentFilter, selectedGroupsCallback)
        }
    }

}

