package com.habitrpg.android.habitica.ui.fragments.faq

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser

import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews

class FAQDetailFragment : BaseMainFragment() {
    private val questionTextView: TextView? by bindView(R.id.questionTextView)
    private val answerTextView: TextView? by bindView(R.id.answerTextView)

    private var article: FAQArticle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_faq_detail)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        if (this.article != null) {
            this.questionTextView!!.text = this.article!!.question
            //TODO: FIX
            this.answerTextView?.text = MarkdownParser.parseMarkdown(article?.answer)
        }
        this.answerTextView!!.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun injectFragment(component: AppComponent) {}

    fun setArticle(article: FAQArticle) {
        this.article = article
        if (this.questionTextView != null) {
            this.questionTextView!!.text = this.article!!.question
        }
        if (this.answerTextView != null) {
            //TODO: FIX
            this.answerTextView?.text = MarkdownParser.parseMarkdown(article.answer)
        }
    }
}
