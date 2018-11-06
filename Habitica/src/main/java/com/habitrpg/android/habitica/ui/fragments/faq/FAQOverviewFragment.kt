package com.habitrpg.android.habitica.ui.fragments.faq

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.FAQOverviewRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import javax.inject.Inject

class FAQOverviewFragment : BaseMainFragment() {
    @Inject
    lateinit var faqRepository: FAQRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView? by bindView(R.id.recyclerView)

    internal var adapter: FAQOverviewRecyclerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_recyclerview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        adapter = FAQOverviewRecyclerAdapter()
        adapter?.getResetWalkthroughEvents()?.subscribe(Consumer { this.userRepository.resetTutorial(user) }, RxErrorHandler.handleEmptyError()).notNull { compositeSubscription.add(it) }
        adapter?.activity = activity
        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        activity.notNull { recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(it, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)) }
        recyclerView?.adapter = adapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadArticles()
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun loadArticles() {
        if (user == null || adapter == null) {
            return
        }
        compositeSubscription.add(faqRepository.getArticles().subscribe(Consumer { adapter?.setArticles(it) }, RxErrorHandler.handleEmptyError()))
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.FAQ)
    }
}
