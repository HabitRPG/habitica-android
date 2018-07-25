package com.habitrpg.android.habitica.ui.fragments


import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.activities.AboutActivity
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.PrefsActivity
import com.habitrpg.android.habitica.ui.adapter.NavigationDrawerAdapter
import com.habitrpg.android.habitica.ui.fragments.faq.FAQOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.customization.AvatarOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.equipment.EquipmentOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemsFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.shops.ShopsFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragment
import com.habitrpg.android.habitica.ui.fragments.skills.SkillsFragment
import com.habitrpg.android.habitica.ui.fragments.social.GuildsOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment
import com.habitrpg.android.habitica.ui.fragments.social.TavernDetailFragment
import com.habitrpg.android.habitica.ui.fragments.social.TavernFragment
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyFragment
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.drawer_main.*
import javax.inject.Inject

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [
 * design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction) for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment : DialogFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userRepository: UserRepository

    private var drawerLayout: DrawerLayout? = null
    private var fragmentContainerView: View? = null

    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState: Boolean = false

    private lateinit var adapter: NavigationDrawerAdapter

    private var subscriptions: CompositeDisposable? = null

    val isDrawerOpen: Boolean
        get() = drawerLayout?.isDrawerOpen(fragmentContainerView!!) ?: false

    private val actionBar: ActionBar?
        get() = activity?.actionBar

    private var questContent: QuestContent? = null
    set(value) {
        field = value
        updateQuestDisplay()
    }
    private var quest: Quest? = null
    set(value) {
        field = value
        updateQuestDisplay()
    }

    private fun updateQuestDisplay() {
        val quest = this.quest
        val questContent = this.questContent
        if (quest == null || questContent == null || !quest.active) {
            questMenuView.visibility = View.GONE
            context.notNull {
                adapter.tintColor = ContextCompat.getColor(it, R.color.brand_300)
                adapter.backgroundTintColor = ContextCompat.getColor(it, R.color.brand_200)
            }
            adapter.items.filter { it.identifier == SIDEBAR_TAVERN }.forEach {
                it.additionalInfo = null
            }
            return
        }
        questMenuView.visibility = View.VISIBLE

        menuHeaderView.setBackgroundColor(questContent.colors?.darkColor ?: 0)
        questMenuView.configure(quest)
        questMenuView.configure(questContent)
        adapter.tintColor = questContent.colors?.extraLightColor ?: 0
        adapter.backgroundTintColor = questContent.colors?.darkColor ?: 0


        /* Reenable this once the boss art can be displayed correctly.

        val preferences = context?.getSharedPreferences("collapsible_sections", 0)
        if (preferences?.getBoolean("boss_art_collapsed", false) == true) {
            questMenuView.hideBossArt()
        } else {
            questMenuView.showBossArt()
        }*/
        questMenuView.hideBossArt()

        adapter.items.filter { it.identifier == SIDEBAR_TAVERN }.forEach {
            it.additionalInfo = context?.getString(R.string.active_world_boss)
            it.additionalInfoAsPill = false
        }
        adapter.notifyDataSetChanged()

        questMenuView.setOnClickListener {
            val context = this.context
            if (context != null) {
                TavernDetailFragment.showWorldBossInfoDialog(context, questContent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val context = context
        adapter = if (context != null) {
            NavigationDrawerAdapter(ContextCompat.getColor(context, R.color.brand_300), ContextCompat.getColor(context, R.color.brand_200))
        } else {
            NavigationDrawerAdapter(0, 0)
        }
        subscriptions = CompositeDisposable()
        HabiticaBaseApplication.component?.inject(this)
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true)

        context?.notNull {recyclerView.setPadding(0, 0, 0,  NavbarUtils.getNavbarHeight(it)) }
        recyclerView.clipToPadding = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.drawer_main, container, false) as ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        initializeMenuItems()

        subscriptions?.add(adapter.getItemSelectionEvents().subscribe(Consumer {
            setSelection(it, true)
        }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(socialRepository.getGroup(Group.TAVERN_ID)
                .doOnNext({  quest = it.quest })
                .filter { it.hasActiveQuest }
                .flatMapMaybe { inventoryRepository.getQuestContent(it.quest?.key ?: "").firstElement() }
                .subscribe(Consumer {
                   questContent = it
                }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(userRepository.getUser().subscribe(Consumer {
            setUsername(it.profile?.name)
            avatarView.setAvatar(it)
            questMenuView.configure(it)
        }, RxErrorHandler.handleEmptyError()))

        messagesButton.setOnClickListener { setSelection(SIDEBAR_INBOX) }
        settingsButton.setOnClickListener { setSelection(SIDEBAR_SETTINGS) }
    }

    override fun onDestroy() {
        subscriptions?.dispose()
        socialRepository.close()
        inventoryRepository.close()
        userRepository.close()
        super.onDestroy()
    }

    private fun initializeMenuItems() {
        val items = ArrayList<HabiticaDrawerItem>()
        context.notNull {context ->
            items.add(HabiticaDrawerItem(SIDEBAR_TASKS, context.getString(R.string.sidebar_tasks)))
            items.add(HabiticaDrawerItem(SIDEBAR_SKILLS, context.getString(R.string.sidebar_skills)))
            items.add(HabiticaDrawerItem(SIDEBAR_STATS, context.getString(R.string.sidebar_stats)))
            items.add(HabiticaDrawerItem(SIDEBAR_SOCIAL, context.getString(R.string.sidebar_section_social), true))
            items.add(HabiticaDrawerItem(SIDEBAR_TAVERN, context.getString(R.string.sidebar_tavern)))
            items.add(HabiticaDrawerItem(SIDEBAR_PARTY, context.getString(R.string.sidebar_party)))
            items.add(HabiticaDrawerItem(SIDEBAR_GUILDS, context.getString(R.string.sidebar_guilds)))
            items.add(HabiticaDrawerItem(SIDEBAR_CHALLENGES, context.getString(R.string.sidebar_challenges)))
            items.add(HabiticaDrawerItem(SIDEBAR_INVENTORY, context.getString(R.string.sidebar_section_inventory), true))
            items.add(HabiticaDrawerItem(SIDEBAR_SHOPS, context.getString(R.string.sidebar_shops)))
            items.add(HabiticaDrawerItem(SIDEBAR_AVATAR, context.getString(R.string.sidebar_avatar)))
            items.add(HabiticaDrawerItem(SIDEBAR_EQUIPMENT, context.getString(R.string.sidebar_equipment)))
            items.add(HabiticaDrawerItem(SIDEBAR_ITEMS, context.getString(R.string.sidebar_items)))
            items.add(HabiticaDrawerItem(SIDEBAR_STABLE, context.getString(R.string.sidebar_stable)))
            items.add(HabiticaDrawerItem(SIDEBAR_PURCHASE, context.getString(R.string.sidebar_purchaseGems)))
            items.add(HabiticaDrawerItem(SIDEBAR_ABOUT_HEADER, context.getString(R.string.sidebar_about), true))
            items.add(HabiticaDrawerItem(SIDEBAR_NEWS, context.getString(R.string.sidebar_news)))
            items.add(HabiticaDrawerItem(SIDEBAR_HELP, context.getString(R.string.sidebar_help)))
            items.add(HabiticaDrawerItem(SIDEBAR_ABOUT, context.getString(R.string.sidebar_about)))
        }
        adapter.updateItems(items)
    }

    fun setSelection(identifier: String?, openSelection: Boolean = true) {
        adapter.selectedItem = identifier
        closeDrawer()

        var fragment: BaseMainFragment? = null
        var newActivityClass: Class<*>? = null

        when (identifier) {
            SIDEBAR_TASKS -> {
                fragment = TasksFragment()
            }
            SIDEBAR_SKILLS -> {
                fragment = SkillsFragment()
            }
            SIDEBAR_STATS -> {
                fragment = StatsFragment()
            }
            SIDEBAR_INBOX -> {
                fragment = InboxFragment()
            }
            SIDEBAR_PARTY -> {
                fragment = PartyFragment()
            }
            SIDEBAR_GUILDS -> {
                fragment = GuildsOverviewFragment()
            }
            SIDEBAR_TAVERN -> {
                fragment = TavernFragment()
            }
            SIDEBAR_CHALLENGES -> {
                fragment = ChallengesOverviewFragment()
            }
            SIDEBAR_SHOPS -> {
                fragment = ShopsFragment()
            }
            SIDEBAR_AVATAR -> {
                fragment = AvatarOverviewFragment()
            }
            SIDEBAR_EQUIPMENT -> {
                fragment = EquipmentOverviewFragment()
            }
            SIDEBAR_ITEMS -> {
                fragment = ItemsFragment()
            }
            SIDEBAR_STABLE -> {
                fragment = StableFragment()
            }
            SIDEBAR_PURCHASE -> {
                newActivityClass = GemPurchaseActivity::class.java
            }
            SIDEBAR_NEWS -> {
                fragment = NewsFragment()
            }
            SIDEBAR_SETTINGS -> {
                newActivityClass = PrefsActivity::class.java
            }
            SIDEBAR_HELP -> {
                fragment = FAQOverviewFragment()
            }
            SIDEBAR_ABOUT -> {
                newActivityClass = AboutActivity::class.java
            }
        }

        //sharedPreferences.edit().putInt(STATE_SELECTED_POSITION, position).apply()

        val activity = activity as MainActivity?
        if (activity != null) {
            if (fragment != null) {
                fragment.fragmentSidebarIdentifier = identifier
                activity.displayFragment(fragment)
            }
            if (newActivityClass != null) {
                val passUserId = Intent(activity, newActivityClass)
                passUserId.putExtra("userId", activity.userID)
                if (identifier == SIDEBAR_PURCHASE) {
                    activity.startActivityForResult(passUserId, MainActivity.GEM_PURCHASE_REQUEST)
                } else {
                    activity.startActivity(passUserId)
                }
            }
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout) {
        fragmentContainerView = activity?.findViewById(fragmentId)
        this.drawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout?.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        // set up the drawer's list view with items and click listener
    }

    fun openDrawer() {
        val containerView = fragmentContainerView
        if (containerView != null) {
            drawerLayout?.openDrawer(containerView)
        }
    }

    fun closeDrawer() {
        val containerView = fragmentContainerView
        if (containerView != null) {
            drawerLayout?.closeDrawer(containerView)
        }
    }

    fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
            adapter.getItemWithIdentifier(identifier)

    fun updateItem(item: HabiticaDrawerItem) {
        adapter.updateItem(item)
    }

    fun setUsername(name: String?) {
        if (toolbarTitle != null) {
            if (name != null && name.isNotEmpty()) {
                toolbarTitle.text = name
            } else {
                toolbarTitle.text = "Habitica"
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    companion object {

        const val SIDEBAR_TASKS = "tasks"
        const val SIDEBAR_SKILLS = "skills"
        const val SIDEBAR_STATS = "stats"
        const val SIDEBAR_SOCIAL = "social"
        const val SIDEBAR_INBOX = "inbox"
        const val SIDEBAR_TAVERN = "tavern"
        const val SIDEBAR_PARTY = "party"
        const val SIDEBAR_GUILDS = "guilds"
        const val SIDEBAR_CHALLENGES = "challenges"
        const val SIDEBAR_INVENTORY = "inventory"
        const val SIDEBAR_SHOPS = "shops"
        const val SIDEBAR_AVATAR = "avatar"
        const val SIDEBAR_EQUIPMENT = "equipment"
        const val SIDEBAR_ITEMS = "items"
        const val SIDEBAR_STABLE = "stable"
        const val SIDEBAR_PURCHASE = "purchase"
        const val SIDEBAR_ABOUT_HEADER = "about_header"
        const val SIDEBAR_NEWS = "news"
        const val SIDEBAR_SETTINGS = "settings"
        const val SIDEBAR_HELP = "help"
        const val SIDEBAR_ABOUT = "about"

        /**
         * Remember the position of the selected item.
         */
        private val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}
