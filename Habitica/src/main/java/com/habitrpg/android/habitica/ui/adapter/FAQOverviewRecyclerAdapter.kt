package com.habitrpg.android.habitica.ui.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.faq.FAQOverviewFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class FAQOverviewRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var activity: MainActivity? = null
    private var articles: List<FAQArticle> = emptyList()

    private val resetWalkthroughEvents = PublishSubject.create<String>()

    fun setArticles(articles: List<FAQArticle>) {
        this.articles = articles
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_JUSTIN -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.button_list_item, parent, false)
                ResetWalkthroughViewHolder(view)
            }
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.help_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.plain_list_item, parent, false)
                FAQArticleViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_FAQ) {
            (holder as? FAQArticleViewHolder)?.bind(articles[position - 1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            articles.size+1 -> VIEW_TYPE_JUSTIN
            else -> VIEW_TYPE_FAQ
        }
    }

    override fun getItemCount(): Int {
        return this.articles.size + 2
    }

    fun getResetWalkthroughEvents(): Flowable<String> {
        return resetWalkthroughEvents.toFlowable(BackpressureStrategy.DROP)
    }

    internal inner class FAQArticleViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val textView: TextView by bindView(itemView, R.id.textView)

        private var article: FAQArticle? = null

        init {
            textView.setOnClickListener(this)
        }

        fun bind(article: FAQArticle) {
            this.article = article
            this.textView.text = this.article?.question
        }

        override fun onClick(v: View) {
            article?.let {
                MainNavigationController.navigate(FAQOverviewFragmentDirections.openFAQDetail(it.position))
            }
        }
    }

    private inner class ResetWalkthroughViewHolder internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        init {
            val button = itemView as? Button
            button?.text = itemView.context.getString(R.string.reset_walkthrough)
            button?.setOnClickListener { resetWalkthroughEvents.onNext("") }
        }
    }

    private inner class HeaderViewHolder internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        init {
            val textView = itemView.findViewById<TextView>(R.id.text_view)
            textView.text =  MarkdownParser.parseMarkdown(itemView.context.getString(R.string.need_help_header_description, "[Habitica Help Guild](https://habitica.com/groups/guild/5481ccf3-5d2d-48a9-a871-70a7380cee5a)"))
            textView.setOnClickListener { MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "5481ccf3-5d2d-48a9-a871-70a7380cee5a")) }
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    companion object {

        private const val VIEW_TYPE_JUSTIN = 0
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_FAQ = 2
    }
}
