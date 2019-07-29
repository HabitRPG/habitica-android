package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.app.Activity
import android.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengesFilterRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.utils.Action1
import java.util.*

internal class ChallengeFilterDialogHolder private constructor(view: View, private val context: Activity) {

    private val groupRecyclerView: RecyclerView? by bindView(view, R.id.challenge_filter_recycler_view)
    private val allButton: Button? by bindView(view, R.id.challenge_filter_button_all)
    private val noneButton: Button? by bindView(view, R.id.challenge_filter_button_none)
    private val checkboxOwned: CheckBox? by bindView(view, R.id.challenge_filter_owned)
    private val checkboxNotOwned: CheckBox? by bindView(view, R.id.challenge_filter_not_owned)

    private var dialog: AlertDialog? = null
    private var challengesViewed: List<Challenge>? = null
    private var currentFilter: ChallengeFilterOptions? = null
    private var selectedGroupsCallback: Action1<ChallengeFilterOptions>? = null
    private var adapter: ChallengesFilterRecyclerViewAdapter? = null

    init {
        allButton?.setOnClickListener { allClicked() }
        noneButton?.setOnClickListener { noneClicked() }
    }

    fun bind(builder: AlertDialog.Builder, challengesViewed: List<Challenge>,
             currentFilter: ChallengeFilterOptions?,
             selectedGroupsCallback: Action1<ChallengeFilterOptions>) {
        builder.setPositiveButton(context.getString(R.string.done)) { _, _ -> doneClicked() }
                .show()
        this.challengesViewed = challengesViewed
        this.currentFilter = currentFilter
        this.selectedGroupsCallback = selectedGroupsCallback
        fillChallengeGroups()

        if (currentFilter != null) {
            checkboxOwned?.isChecked = currentFilter.showOwned
            checkboxNotOwned?.isChecked = currentFilter.notOwned
        }
    }

    private fun fillChallengeGroups() {
        this.groupRecyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = ChallengesFilterRecyclerViewAdapter(getGroups(challengesViewed))
        if (currentFilter != null && currentFilter?.showByGroups != null) {
            adapter?.selectAll(currentFilter?.showByGroups ?: emptyList())
        }

        this.groupRecyclerView?.adapter = adapter
    }

    private fun getGroups(challenges: List<Challenge>?): Collection<Group> {
        val groupMap = HashMap<String, Group>()

        if (challenges != null) {
            for (challenge in challenges) {
                if (groupMap.containsKey(challenge.groupName)) {
                    continue
                }
                val group = Group()
                group.id = challenge.groupId ?: ""
                group.name = challenge.groupName

                groupMap[challenge.groupName ?: ""] = group
            }
        }

        return groupMap.values
    }

    private fun doneClicked() {
        val options = ChallengeFilterOptions()
        options.showByGroups = this.adapter?.checkedEntries
        options.showOwned = checkboxOwned?.isChecked ?: false
        options.notOwned = checkboxNotOwned?.isChecked ?: false

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

        fun showDialog(activity: Activity, challengesViewed: List<Challenge>,
                       currentFilter: ChallengeFilterOptions?,
                       selectedGroupsCallback: Action1<ChallengeFilterOptions>) {
            val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_challenge_filter, null)

            val challengeFilterDialogHolder = ChallengeFilterDialogHolder(dialogLayout, activity)

            val builder = AlertDialog.Builder(activity)
                    .setTitle(R.string.filter)
                    .setView(dialogLayout)

            challengeFilterDialogHolder.bind(builder, challengesViewed, currentFilter, selectedGroupsCallback)
        }
    }

}

