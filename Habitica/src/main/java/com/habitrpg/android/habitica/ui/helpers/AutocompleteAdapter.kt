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
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel

class AutocompleteAdapter(val context: Context, val socialRepository: SocialRepository) : BaseAdapter(), Filterable {
    private var results: List<FindUsernameResult> = arrayListOf()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    results = socialRepository.findUsernames(constraint.toString()).blockingFirst(arrayListOf())
                    filterResults.values = results
                    filterResults.count = results.size
                }
                return filterResults
            }

            override fun publishResults(contraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = parent?.inflate(R.layout.autocomplete_username)
        val result = getItem(position)
        val displaynameView = view?.findViewById<UsernameLabel>(R.id.display_name_view)
        displaynameView?.username = result.username
        displaynameView?.tier = result.contributor?.level ?: 0
        view?.findViewById<TextView>(R.id.username_view)?.text = result.formattedUsername
        return view ?: View(context)
    }

    override fun getItem(position: Int): FindUsernameResult {
        return results[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun getCount(): Int {
        return results.size
    }


}