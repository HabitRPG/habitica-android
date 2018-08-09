package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class GuildsOverviewFragment : BaseMainFragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var challengeRepository: ChallengeRepository

    private val guildsListView: LinearLayout? by bindView(R.id.my_guilds_listview)
    private val publicGuildsButton: Button? by bindView(R.id.publicGuildsButton)
    private val swipeRefreshLayout: SwipeRefreshLayout? by bindView(R.id.chat_refresh_layout)

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

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout?.isRefreshing = true
        }
        fetchGuilds()
    }

    private fun fetchGuilds() {
        this.socialRepository.retrieveGroups("guilds")
                .subscribe(Consumer {
                    swipeRefreshLayout?.isRefreshing = false
                }, RxErrorHandler.handleEmptyError())
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
        val fragment: BaseMainFragment
        if (v === this.publicGuildsButton) {
            val publicGuildsFragment = PublicGuildsFragment()
            publicGuildsFragment.memberGuildIDs = this.guildIDs
            fragment = publicGuildsFragment
        } else {
            val guildIndex = (v.parent as? ViewGroup)?.indexOfChild(v)
            val guildId = this.guilds?.get(guildIndex ?: 0)?.id ?: return
            val guildFragment = GuildFragment()
            guildFragment.setGuildId(guildId)
            guildFragment.isMember = true
            fragment = guildFragment
        }
        activity?.displayFragment(fragment)
    }


    override fun customTitle(): String {
        return if (isAdded) {
            getString(R.string.sidebar_guilds)
        } else {
            ""
        }
    }
}
