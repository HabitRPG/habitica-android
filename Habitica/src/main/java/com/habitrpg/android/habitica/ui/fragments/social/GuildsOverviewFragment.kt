package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class GuildsOverviewFragment : BaseMainFragment(), View.OnClickListener, androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var challengeRepository: ChallengeRepository

    private val guildsListView: LinearLayout? by bindView(R.id.my_guilds_listview)
    private val publicGuildsButton: Button? by bindView(R.id.publicGuildsButton)
    private val swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? by bindView(R.id.chat_refresh_layout)

    private var guilds: List<Group>? = null
    private var guildIDs: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.fetchGuilds()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_guilds_overview)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        swipeRefreshLayout?.setOnRefreshListener(this)
        this.publicGuildsButton?.setOnClickListener(this)
        compositeSubscription.add(socialRepository.getUserGroups().subscribe(Consumer { this.setGuilds(it) }, RxErrorHandler.handleEmptyError()))
    }
    override fun onDestroy() {
        socialRepository.close()
        challengeRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout?.isRefreshing = true
        }
        fetchGuilds()
    }

    private fun fetchGuilds() {
        compositeSubscription.add(this.socialRepository.retrieveGroups("guilds")
                .subscribe(Consumer {
                    swipeRefreshLayout?.isRefreshing = false
                }, RxErrorHandler.handleEmptyError()))
    }

    private fun setGuilds(guilds: RealmResults<Group>) {
        this.guilds = guilds
        if (this.guildsListView == null) {
            return
        }
        this.guildIDs = ArrayList()
        this.guildsListView?.removeAllViewsInLayout()
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        for (guild in guilds) {
            val entry = inflater?.inflate(R.layout.plain_list_item, this.guildsListView, false) as? TextView
            entry?.text = guild.name
            entry?.setOnClickListener(this)
            this.guildsListView?.addView(entry)
            this.guildIDs?.add(guild.id)
        }
    }

    override fun onClick(v: View) {
        if (v === this.publicGuildsButton) {
            MainNavigationController.navigate(GuildsOverviewFragmentDirections.openPublicGuilds())
        } else {
            val guildIndex = (v.parent as? ViewGroup)?.indexOfChild(v)
            val guildId = this.guilds?.get(guildIndex ?: 0)?.id ?: return
            MainNavigationController.navigate(GuildsOverviewFragmentDirections.openGuildDetail(guildId))
        }
    }


}
