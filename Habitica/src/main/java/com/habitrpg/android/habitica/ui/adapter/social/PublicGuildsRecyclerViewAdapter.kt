package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ItemPublicGuildBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.social.PublicGuildsFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PublicGuildsRecyclerViewAdapter(data: OrderedRealmCollection<Group>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Group, PublicGuildsRecyclerViewAdapter.GuildViewHolder>(data, autoUpdate), Filterable {

    var socialRepository: SocialRepository? = null
    private var memberGuildIDs: List<String> = listOf()

    fun setMemberGuildIDs(memberGuildIDs: List<String>) {
        this.memberGuildIDs = memberGuildIDs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildViewHolder {
        val guildViewHolder = GuildViewHolder(parent.inflate(R.layout.item_public_guild))
        guildViewHolder.itemView.setOnClickListener { v ->
            val guild = v.tag as? Group ?: return@setOnClickListener
            val directions = PublicGuildsFragmentDirections.openGuildDetail(guild.id)
            directions.isMember = isInGroup(guild)
            MainNavigationController.navigate(directions)
        }
        guildViewHolder.binding.joinleaveButton.setOnClickListener { v ->
            val guild = v.tag as? Group ?: return@setOnClickListener
            val isMember = this.memberGuildIDs.contains(guild.id)
            if (isMember) {
                this@PublicGuildsRecyclerViewAdapter.socialRepository?.leaveGroup(guild.id, true)
                        ?.subscribe({
                            if (data != null) {
                                val indexOfGroup = data?.indexOf(guild)
                                notifyItemChanged(indexOfGroup ?: 0)
                            }
                        }, RxErrorHandler.handleEmptyError())
            } else {
                this@PublicGuildsRecyclerViewAdapter.socialRepository?.joinGroup(guild.id)
                        ?.subscribe({ group ->
                            if (data != null) {
                                val indexOfGroup = data?.indexOf(group)
                                notifyItemChanged(indexOfGroup ?: 0)
                            }
                        }, RxErrorHandler.handleEmptyError())
            }

        }
        return guildViewHolder
    }

    override fun onBindViewHolder(holder: GuildViewHolder, position: Int) {
        data?.let {
            val guild = it[position]
            val isInGroup = isInGroup(guild)
            holder.bind(guild, isInGroup)
            holder.itemView.tag = guild
            holder.binding.joinleaveButton.tag = guild
        }
    }

    private fun isInGroup(guild: Group): Boolean {
        return this.memberGuildIDs.contains(guild.id)
    }

    private var unfilteredData: OrderedRealmCollection<Group>? = null

    fun setUnfilteredData(data: OrderedRealmCollection<Group>?) {
        updateData(data)
        unfilteredData = data
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                results.values = constraint
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                unfilteredData?.let {
                    if (constraint.isNotEmpty()) {
                        updateData(it.where()
                                .beginGroup()
                                .contains("name", constraint.toString(), Case.INSENSITIVE)
                                .or()
                                .contains("summary", constraint.toString(), Case.INSENSITIVE)
                                .endGroup()
                                .findAll())
                    }
                }
            }
        }
    }

    class GuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemPublicGuildBinding.bind(itemView)

        fun bind(guild: Group, isInGroup: Boolean) {
            binding.nameTextView.text = guild.name
            binding.memberCountTextView.text = guild.memberCount.toString()
            binding.descriptionTextView.setMarkdown(guild.summary)
            if (isInGroup) {
                binding.joinleaveButton.setText(R.string.leave)
            } else {
                binding.joinleaveButton.setText(R.string.join)
            }
        }
    }
}
