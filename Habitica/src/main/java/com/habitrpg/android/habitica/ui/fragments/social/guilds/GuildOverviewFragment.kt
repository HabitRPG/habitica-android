package com.habitrpg.android.habitica.ui.fragments.social.guilds

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import javax.inject.Inject

class GuildOverviewFragment : BaseMainFragment<FragmentViewpagerBinding>(), SearchView.OnQueryTextListener {

    @Inject
    internal lateinit var socialRepository: SocialRepository

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    private var statePagerAdapter: FragmentStatePagerAdapter? = null
    private var userGuildsFragment: GuildListFragment? = GuildListFragment()
    private var publicGuildsFragment: GuildListFragment? = GuildListFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewPagerAdapter()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_public_guild, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val guildSearchView = searchItem?.actionView as? SearchView
        val theTextArea = guildSearchView?.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text)
        context?.let { theTextArea?.setHintTextColor(ContextCompat.getColor(it, R.color.white)) }
        guildSearchView?.queryHint = getString(R.string.guild_search_hint)
        guildSearchView?.setOnQueryTextListener(this)
    }

    @Suppress("ReturnCount")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create_item -> {
                showCreationDialog()
                return true
            }
            R.id.action_reload -> {
                getActiveFragment()?.fetchGuilds()
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
            val l = context.packageManager.queryIntentActivities(launchBrowser, PackageManager.MATCH_DEFAULT_ONLY)
            val notHabitica = l.first { !it.activityInfo.processName.contains("habitica") }
            launchBrowser.setPackage(notHabitica.activityInfo.processName)
            startActivity(launchBrowser)
        }
        dialog.addCloseButton()
        dialog.show()
    }

    private fun getActiveFragment(): GuildListFragment? {
        return if (binding?.viewPager?.currentItem == 0) {
            userGuildsFragment
        } else {
            publicGuildsFragment
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        statePagerAdapter = object : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment {
                return if (position == 0) {
                    userGuildsFragment?.onlyShowUsersGuilds = true
                    userGuildsFragment
                } else {
                    publicGuildsFragment?.onlyShowUsersGuilds = false
                    publicGuildsFragment
                } ?: Fragment()
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence {
                return when (position) {
                    0 -> getString(R.string.my_guilds)
                    1 -> getString(R.string.discover)
                    else -> ""
                }
            }
        }
        binding?.viewPager?.adapter = statePagerAdapter
        tabLayout?.setupWithViewPager(binding?.viewPager)
        statePagerAdapter?.notifyDataSetChanged()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return getActiveFragment()?.onQueryTextSubmit(query) ?: false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return getActiveFragment()?.onQueryTextChange(newText) ?: false
    }
}