package com.habitrpg.android.habitica.ui.fragments.support

import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.ui.views.SupportCollapsibleSection

sealed class SearchableFAQItem {
    abstract val title: String
    abstract val subtitle: String?
    abstract val matchSnippet: String?

    data class CollapsibleItem(
        override val title: String,
        override val subtitle: String?,
        val description: String,
        val collapsibleSection: SupportCollapsibleSection,
        override val matchSnippet: String? = null
    ) : SearchableFAQItem()

    data class NavigableItem(
        val article: FAQArticle,
        override val matchSnippet: String? = null
    ) : SearchableFAQItem() {
        override val title: String
            get() = article.question ?: ""
        override val subtitle: String? = null
    }
}
