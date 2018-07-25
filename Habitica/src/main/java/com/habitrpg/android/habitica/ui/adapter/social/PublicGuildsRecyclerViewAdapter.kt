package com.habitrpg.android.habitica.ui.adapter.social

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.events.DisplayFragmentEvent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.greenrobot.eventbus.EventBus

class PublicGuildsRecyclerViewAdapter(data: OrderedRealmCollection<Group>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Group, PublicGuildsRecyclerViewAdapter.GuildViewHolder>(data, autoUpdate), Filterable {

    var apiClient: ApiClient? = null
    private var memberGuildIDs: MutableList<String> = mutableListOf()

    fun setMemberGuildIDs(memberGuildIDs: MutableList<String>) {
        this.memberGuildIDs = memberGuildIDs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildViewHolder {
        val guildViewHolder = GuildViewHolder(parent.inflate(R.layout.item_public_guild))
        guildViewHolder.itemView.setOnClickListener { v ->
            val guild = v.tag as? Group ?: return@setOnClickListener
            val guildFragment = GuildFragment()
            guildFragment.setGuildId(guild.id)
            guildFragment.isMember = isInGroup(guild)
            val event = DisplayFragmentEvent()
            event.fragment = guildFragment
            EventBus.getDefault().post(event)
        }
        guildViewHolder.joinLeaveButton.setOnClickListener { v ->
            val guild = v.tag as? Group ?: return@setOnClickListener
            val isMember = this.memberGuildIDs.contains(guild.id)
            if (isMember) {
                this@PublicGuildsRecyclerViewAdapter.apiClient?.leaveGroup(guild.id)
                        ?.subscribe(Consumer {
                            memberGuildIDs.remove(guild.id)
                            if (data != null) {
                                val indexOfGroup = data!!.indexOf(guild)
                                notifyItemChanged(indexOfGroup)
                            }
                        }, RxErrorHandler.handleEmptyError())
            } else {
                this@PublicGuildsRecyclerViewAdapter.apiClient?.joinGroup(guild.id)
                        ?.subscribe(Consumer { group ->
                            memberGuildIDs.add(group.id)
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
        data.notNull {
            val guild = it[position]
            val isInGroup = isInGroup(guild)
            holder.bind(guild, isInGroup)
            holder.itemView.tag = guild
            holder.joinLeaveButton.tag = guild
        }
    }

    private fun isInGroup(guild: Group): Boolean {
        return this.memberGuildIDs.contains(guild.id)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
                val results = Filter.FilterResults()
                results.values = constraint
                return Filter.FilterResults()
            }

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                data.notNull {
                    if (constraint.isNotEmpty()) {
                        updateData(it.where()
                                .contains("name", constraint.toString(), Case.INSENSITIVE)
                                .findAll())
                    }
                }
            }
        }
    }

    class GuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView by bindView(R.id.nameTextView)
        private val memberCountTextView: TextView by bindView(R.id.memberCountTextView)
        private val descriptionTextView: TextView by bindView(R.id.descriptionTextView)
        internal val joinLeaveButton: Button by bindView(R.id.joinleaveButton)


        fun bind(guild: Group, isInGroup: Boolean) {
            this.nameTextView.text = guild.name
            this.memberCountTextView.text = guild.memberCount.toString()
            this.descriptionTextView.text = guild.description
            if (isInGroup) {
                this.joinLeaveButton.setText(R.string.leave)
            } else {
                this.joinLeaveButton.setText(R.string.join)
            }
        }
    }
}
