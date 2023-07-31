package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ItemPublicGuildBinding
import com.habitrpg.android.habitica.databinding.ItemUserGuildBinding
import com.habitrpg.android.habitica.databinding.PillTextviewBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.EmojiParser
import com.habitrpg.common.habitica.helpers.NumberAbbreviator
import io.realm.Case
import io.realm.OrderedRealmCollection
import java.util.Locale

class GuildListAdapter : BaseRecyclerViewAdapter<Group, RecyclerView.ViewHolder>(), Filterable {

    var socialRepository: SocialRepository? = null

    var onlyShowUsersGuilds = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (onlyShowUsersGuilds) {
            UserGuildViewHolder(parent.inflate(R.layout.item_user_guild))
        } else {
            GuildViewHolder(parent.inflate(R.layout.item_public_guild))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val guild = data[position]
        if (onlyShowUsersGuilds && holder is UserGuildViewHolder) {
            holder.bind(guild)
        } else if (holder is GuildViewHolder) {
            holder.bind(guild)
            holder.itemView.tag = guild
        }
        holder.itemView.setOnClickListener {
            MainNavigationController.navigate(R.id.guildFragment, bundleOf(Pair("groupID", guild.id)))
        }
    }

    private var unfilteredData: List<Group>? = null

    fun setUnfilteredData(data: List<Group>?) {
        this.data = data ?: emptyList()
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
                    if (constraint.isNotEmpty() && it is OrderedRealmCollection) {
                        data = it.where()
                            .beginGroup()
                            .contains("name", constraint.toString(), Case.INSENSITIVE)
                            .or()
                            .contains("summary", constraint.toString(), Case.INSENSITIVE)
                            .endGroup()
                            .findAll()
                    } else {
                        data = it
                    }
                }
            }
        }
    }

    class UserGuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemUserGuildBinding.bind(itemView)

        fun bind(guild: Group) {
            binding.titleTextView.text = guild.name

            val number = when {
                guild.memberCount < 1000 -> 2
                guild.memberCount < 10000 -> 1
                else -> 0
            }
            val formattedNumber = NumberAbbreviator.abbreviate(itemView.context, guild.memberCount.toDouble(), number)
            binding.guildBadgeView.setImageBitmap(
                HabiticaIconsHelper.imageOfGuildCrest(
                    false,
                    false,
                    guild.memberCount.toFloat(),
                    formattedNumber
                )
            )
        }
    }

    class GuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemPublicGuildBinding.bind(itemView)

        fun bind(guild: Group) {
            binding.nameTextView.text = guild.name
            val number = when {
                guild.memberCount < 1000 -> 2
                guild.memberCount < 10000 -> 1
                else -> 0
            }
            binding.memberCountTextView.text = NumberAbbreviator.abbreviate(itemView.context, guild.memberCount.toDouble(), number)
            binding.descriptionTextView.text = EmojiParser.parseEmojis(guild.summary)
            binding.descriptionTextView.setOnClickListener {
                itemView.callOnClick()
            }
            binding.guildBadgeView.setImageBitmap(HabiticaIconsHelper.imageOfGuildCrestSmall(guild.memberCount.toFloat()))

            binding.tagWrapper.removeAllViews()
            guild.categories?.forEach { category ->
                val textView = PillTextviewBinding.inflate(itemView.context.layoutInflater, binding.tagWrapper, true)
                textView.root.text = category.name?.split("_")?.joinToString(" ") {
                    it.replaceFirstChar {
                        if (it.isLowerCase()) {
                            it.titlecase(
                                Locale.getDefault()
                            )
                        } else {
                            it.toString()
                        }
                    }
                }
                textView.root.background = if (category.slug == "habitica_official") {
                    textView.root.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    ContextCompat.getDrawable(itemView.context, R.drawable.pill_bg_purple_400)
                } else {
                    textView.root.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                    ContextCompat.getDrawable(itemView.context, R.drawable.pill_bg_gray)
                }
                val hPadding = 10.dpToPx(itemView.context)
                val vPadding = 3.dpToPx(itemView.context)
                textView.root.setPadding(hPadding, vPadding, hPadding, vPadding)
            }
        }
    }
}
