package com.habitrpg.android.habitica.ui.adapter.social.challenges

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.util.*

class ChallengesFilterRecyclerViewAdapter(entries: Collection<Group>) : RecyclerView.Adapter<ChallengesFilterRecyclerViewAdapter.ChallengeViewHolder>() {


    private val entries: List<Group>
    private val holderList: MutableList<ChallengeViewHolder>
    val checkedEntries: List<Group>
        get() {
            val result = ArrayList<Group>()

            for (h in holderList) {
                if (h.checkbox.isChecked) {
                    h.group?.let {
                        result.add(it)
                    }
                }
            }
            return result
        }

    init {
        this.entries = ArrayList(entries)
        this.holderList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.dialog_challenge_filter_group_item, parent, false)

        val challengeViewHolder = ChallengeViewHolder(view)
        holderList.add(challengeViewHolder)

        return challengeViewHolder
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    fun deSelectAll() {
        for (h in holderList) {
            h.checkbox.isChecked = false
        }
    }

    fun selectAll() {
        for (h in holderList) {
            h.checkbox.isChecked = true
        }
    }

    fun selectAll(groupsToCheck: List<Group>) {
        for (h in holderList) {
            h.checkbox.isChecked =groupsToCheck.find { g -> h.group?.id == g.id } != null
        }
    }

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val checkbox: CheckBox by bindView(itemView, R.id.challenge_filter_group_checkbox)

        var group: Group? = null

        fun bind(group: Group) {
            this.group = group

            checkbox.text = group.name
        }
    }
}