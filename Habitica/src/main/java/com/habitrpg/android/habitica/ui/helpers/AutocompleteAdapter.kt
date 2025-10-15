package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.ui.views.UsernameLabel
import com.habitrpg.common.habitica.extensions.inflate
import com.habitrpg.common.habitica.helpers.EmojiMap
import com.habitrpg.common.habitica.helpers.EmojiParser
import java.util.Date

class AutocompleteAdapter(
    val context: Context,
    val socialRepository: SocialRepository? = null,
    var autocompleteContext: String? = null,
    var groupID: String? = null,
    val remoteAutocomplete: Boolean = false
) : BaseAdapter(), Filterable {
    var chatMessages: List<ChatMessage> = arrayListOf()
    var groupMembers: List<Member> = arrayListOf()
    private var userResults: List<FindUsernameResult> = arrayListOf()
    private var emojiResults: List<String> = arrayListOf()
    private var isAutocompletingUsers = true
    private var lastAutocomplete: Long = 0

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (!constraint.isNullOrEmpty()) {
                    if (constraint[0] == '@' && constraint.length >= 3 && socialRepository != null && remoteAutocomplete) {
                        if (Date().time - lastAutocomplete > 2000) {
                            lastAutocomplete = Date().time
                            userResults = arrayListOf()
                            isAutocompletingUsers = true
                            /*socialRepository.findUsernames(constraint.toString().drop(1), autocompleteContext, groupID).blockingSubscribe {
                                userResults = it
                                filterResults.values = userResults
                                filterResults.count = userResults.size
                            }*/
                        } else {
                            filterResults.values = userResults
                            filterResults.count = userResults.size
                        }
                    } else if (constraint[0] == '@') {
                        lastAutocomplete = Date().time
                        isAutocompletingUsers = true

                        userResults = if (groupMembers.isNotEmpty()) {
                            groupMembers
                                .filter { member ->
                                    member.username?.startsWith(constraint.toString().drop(1), ignoreCase = true) ?: false
                                }
                                .map { member ->
                                    val result = FindUsernameResult()
                                    result.id = member.id
                                    result.authentication = Authentication()
                                    result.authentication?.localAuthentication = LocalAuthentication()
                                    result.authentication?.localAuthentication?.username = member.username
                                    result.contributor = member.contributor
                                    result.profile = Profile()
                                    result.profile?.name = member.displayName
                                    result
                                }
                        } else {
                            chatMessages
                                .filter { it.isValid }
                                .distinctBy {
                                    it.username
                                }.filter {
                                    it.username?.startsWith(constraint.toString().drop(1)) ?: false
                                }.map { message ->
                                    val result = FindUsernameResult()
                                    result.authentication = Authentication()
                                    result.authentication?.localAuthentication = LocalAuthentication()
                                    result.authentication?.localAuthentication?.username =
                                        message.username
                                    result.contributor = message.contributor
                                    result.profile = Profile()
                                    result.profile?.name = message.user
                                    result
                                }
                        }
                        filterResults.values = userResults
                        filterResults.count = userResults.size
                    } else if (constraint[0] == ':') {
                        isAutocompletingUsers = false
                        emojiResults =
                            EmojiMap.invertedEmojiMap.keys.filter { it.startsWith(constraint) }
                        filterResults.values = emojiResults
                        filterResults.count = emojiResults.size
                    }
                }
                return filterResults
            }

            override fun publishResults(
                contraint: CharSequence?,
                results: FilterResults?
            ) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        return if (isAutocompletingUsers) {
            val view = parent?.inflate(R.layout.autocomplete_username)
            val result = getItem(position) as? FindUsernameResult
            val displaynameView = view?.findViewById<UsernameLabel>(R.id.display_name_view)
            displaynameView?.username = result?.profile?.name
            displaynameView?.tier = result?.contributor?.level ?: 0
            view?.findViewById<TextView>(R.id.username_view)?.text = result?.formattedUsername
            view
        } else {
            val view = parent?.inflate(R.layout.autocomplete_emoji)
            val result = getItem(position) as? String
            val emojiTextView = view?.findViewById<TextView>(R.id.emoji_textview)
            emojiTextView?.text = EmojiParser.parseEmojis(result)
            view?.findViewById<TextView>(R.id.label)?.text = result
            view
        } ?: View(context)
    }

    override fun getItem(position: Int): Any? {
        return if (isAutocompletingUsers) {
            userResults.getOrNull(position)
        } else {
            emojiResults.getOrNull(
                position
            )
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getCount(): Int {
        return if (isAutocompletingUsers) userResults.size else emojiResults.size
    }
}
