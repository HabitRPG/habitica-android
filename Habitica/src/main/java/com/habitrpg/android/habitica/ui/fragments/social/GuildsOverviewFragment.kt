package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.net.toUri
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentGuildsOverviewBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class GuildsOverviewFragment : BaseMainFragment<FragmentGuildsOverviewBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    override var binding: FragmentGuildsOverviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGuildsOverviewBinding {
        return FragmentGuildsOverviewBinding.inflate(inflater, container, false)
    }

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
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.publicGuildsButton?.setOnClickListener {
            MainNavigationController.navigate(GuildsOverviewFragmentDirections.openPublicGuilds())
        }
        compositeSubscription.add(socialRepository.getUserGroups("guild").subscribe({ this.setGuilds(it) }, RxErrorHandler.handleEmptyError()))
    }
    override fun onDestroy() {
        socialRepository.close()
        challengeRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_create_refresh, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                onRefresh()
                return true
            }
            R.id.menu_create_item -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val context = context ?: return
        val dialog = HabiticaAlertDialog(context)
        dialog.setTitle(R.string.create_guild)
        dialog.setMessage(R.string.create_guild_description)
        dialog.addButton(R.string.open_website, true, false) { _, _ ->
            val uriUrl = "https://habitica.com/groups/myGuilds".toUri()
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }
        dialog.addCloseButton()
        dialog.show()
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
                .subscribe({
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
