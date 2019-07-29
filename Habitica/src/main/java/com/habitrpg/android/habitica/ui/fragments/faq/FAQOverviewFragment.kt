package com.habitrpg.android.habitica.ui.fragments.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.extensions.inflate
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
        adapter?.getResetWalkthroughEvents()?.subscribe(Consumer { this.userRepository.resetTutorial(user) }, RxErrorHandler.handleEmptyError())?.let { compositeSubscription.add(it) }
        adapter?.activity = activity
        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        activity?.let { recyclerView?.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(it, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)) }
        recyclerView?.adapter = adapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadArticles()
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadArticles() {
        if (adapter == null) {
            return
        }
        compositeSubscription.add(faqRepository.getArticles().subscribe(Consumer { adapter?.setArticles(it) }, RxErrorHandler.handleEmptyError()))
    }

}
