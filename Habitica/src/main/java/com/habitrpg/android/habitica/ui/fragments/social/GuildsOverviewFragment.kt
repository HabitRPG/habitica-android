package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentGuildsOverviewBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class GuildsOverviewFragment : BaseMainFragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    private var binding: FragmentGuildsOverviewBinding? = null
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var challengeRepository: ChallengeRepository

    private var guilds: List<Group>? = null
    private var guildIDs: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.fetchGuilds()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentGuildsOverviewBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.publicGuildsButton?.setOnClickListener {
            MainNavigationController.navigate(GuildsOverviewFragmentDirections.openPublicGuilds())
        }
        compositeSubscription.add(socialRepository.getUserGroups("guild").subscribe(Consumer { this.setGuilds(it) }, RxErrorHandler.handleEmptyError()))
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
        binding?.refreshLayout?.isRefreshing = true
        fetchGuilds()
    }

    private fun fetchGuilds() {
        compositeSubscription.add(this.socialRepository.retrieveGroups("guilds")
                .subscribe(Consumer {
                    binding?.refreshLayout?.isRefreshing = false
                }, RxErrorHandler.handleEmptyError()))
    }

    private fun setGuilds(guilds: RealmResults<Group>) {
        this.guilds = guilds
        if (binding?.myGuildsListview == null) {
            return
        }
        this.guildIDs = ArrayList()
        binding?.myGuildsListview?.removeAllViewsInLayout()
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        for (guild in guilds) {
            val entry = inflater?.inflate(R.layout.plain_list_item, binding?.myGuildsListview, false) as? TextView
            entry?.text = guild.name
            entry?.setOnClickListener {
                MainNavigationController.navigate(GuildsOverviewFragmentDirections.openGuildDetail(guild.id))
            }
            binding?.myGuildsListview?.addView(entry)
            this.guildIDs?.add(guild.id)
        }
    }
}
