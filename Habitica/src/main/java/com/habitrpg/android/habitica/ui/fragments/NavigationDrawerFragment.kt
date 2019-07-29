package com.habitrpg.android.habitica.ui.fragments


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity.Companion.NOTIFICATION_CLICK
import com.habitrpg.android.habitica.ui.activities.NotificationsActivity
import com.habitrpg.android.habitica.ui.adapter.NavigationDrawerAdapter
import com.habitrpg.android.habitica.ui.fragments.social.TavernDetailFragment
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
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

    private var drawerLayout: androidx.drawerlayout.widget.DrawerLayout? = null
    private var fragmentContainerView: View? = null

    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState: Boolean = false

    private lateinit var adapter: NavigationDrawerAdapter

    private var subscriptions: CompositeDisposable? = null

    val isDrawerOpen: Boolean
        get() = drawerLayout?.isDrawerOpen(GravityCompat.START) ?: false

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
            context?.let {
                adapter.tintColor = it.getThemeColor(R.attr.colorPrimary)
                adapter.backgroundTintColor = it.getThemeColor(R.attr.colorPrimaryOffset)
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


        messagesBadge.visibility = View.GONE
        settingsBadge.visibility = View.GONE
        notificationsBadge.visibility = View.GONE

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
            NavigationDrawerAdapter(context.getThemeColor(R.attr.colorPrimary), context.getThemeColor(R.attr.colorPrimaryOffset))
        } else {
            NavigationDrawerAdapter(0, 0)
        }
        subscriptions = CompositeDisposable()
        HabiticaBaseApplication.userComponent?.inject(this)
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.drawer_main, container, false) as? ViewGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        initializeMenuItems()

        subscriptions?.add(adapter.getItemSelectionEvents().subscribe(Consumer {
            setSelection(it, true)
        }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(socialRepository.getGroup(Group.TAVERN_ID)
                .doOnNext {  quest = it.quest }
                .filter { it.hasActiveQuest }
                .flatMapMaybe { inventoryRepository.getQuestContent(it.quest?.key ?: "").firstElement() }
                .subscribe(Consumer {
                   questContent = it
                }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(userRepository.getUser().subscribe(Consumer {
            updateUser(it)
        }, RxErrorHandler.handleEmptyError()))

        messagesButtonWrapper.setOnClickListener { setSelection(R.id.inboxFragment) }
        settingsButtonWrapper.setOnClickListener { setSelection(R.id.prefsActivity) }
        notificationsButtonWrapper.setOnClickListener { startNotificationsActivity() }
    }

    private fun updateUser(user: User) {
        setMessagesCount(user.inbox?.newMessages ?: 0)
        setSettingsCount(if (user.flags?.isVerifiedUsername != true) 1 else 0 )
        setDisplayName(user.profile?.name)
        setUsername(user.username)
        avatarView.setAvatar(user)
        questMenuView.configure(user)

        val tavernItem = getItemWithIdentifier(SIDEBAR_TAVERN)
        if (user.preferences?.sleep == true) {
            tavernItem?.additionalInfo = context?.getString(R.string.damage_paused)
        } else {
            tavernItem?.additionalInfo = null
        }

        val specialItems = user.items?.special
        var hasSpecialItems = false
        if (specialItems != null) {
            hasSpecialItems = specialItems.hasSpecialItems()
        }
        val item = getItemWithIdentifier(SIDEBAR_SKILLS)
        if (item != null) {
            if (!user.hasClass() && !hasSpecialItems) {
                item.isVisible = false
            } else {
                if (user.stats?.lvl ?: 0 < HabiticaSnackbar.MIN_LEVEL_FOR_SKILLS && (!hasSpecialItems)) {
                    item.additionalInfo = getString(R.string.unlock_lvl_11)
                } else {
                    item.additionalInfo = null
                }
                item.isVisible = true
            }
            updateItem(item)
        }
        val statsItem = getItemWithIdentifier(SIDEBAR_STATS)
        if (statsItem != null) {
            if (user.preferences?.disableClasses != true) {
                if (user.stats?.lvl ?: 0 >= 10 && user.stats?.points ?: 0 > 0) {
                    statsItem.additionalInfo = user.stats?.points.toString()
                } else {
                    statsItem.additionalInfo = null
                }
                statsItem.isVisible = true
            } else {
                statsItem.isVisible = false
            }
            updateItem(statsItem)
        }
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
        context?.let {context ->
            items.add(HabiticaDrawerItem(R.id.tasksFragment, SIDEBAR_TASKS, context.getString(R.string.sidebar_tasks)))
            items.add(HabiticaDrawerItem(R.id.skillsFragment, SIDEBAR_SKILLS, context.getString(R.string.sidebar_skills)))
            items.add(HabiticaDrawerItem(R.id.statsFragment, SIDEBAR_STATS, context.getString(R.string.sidebar_stats)))
            items.add(HabiticaDrawerItem(R.id.achievementsFragment, SIDEBAR_ACHIEVEMENTS, context.getString(R.string.sidebar_achievements)))
            items.add(HabiticaDrawerItem(0, SIDEBAR_SOCIAL, context.getString(R.string.sidebar_section_social), true))
            items.add(HabiticaDrawerItem(R.id.tavernFragment, SIDEBAR_TAVERN, context.getString(R.string.sidebar_tavern), isHeader = false, additionalInfoAsPill = false))
            items.add(HabiticaDrawerItem(R.id.partyFragment, SIDEBAR_PARTY, context.getString(R.string.sidebar_party)))
            items.add(HabiticaDrawerItem(R.id.guildsOverviewFragment, SIDEBAR_GUILDS, context.getString(R.string.sidebar_guilds)))
            items.add(HabiticaDrawerItem(R.id.challengesOverviewFragment, SIDEBAR_CHALLENGES, context.getString(R.string.sidebar_challenges)))
            items.add(HabiticaDrawerItem(0, SIDEBAR_INVENTORY, context.getString(R.string.sidebar_section_inventory), true))
            items.add(HabiticaDrawerItem(R.id.shopsFragment, SIDEBAR_SHOPS, context.getString(R.string.sidebar_shops)))
            items.add(HabiticaDrawerItem(R.id.avatarOverviewFragment, SIDEBAR_AVATAR, context.getString(R.string.sidebar_avatar)))
            items.add(HabiticaDrawerItem(R.id.equipmentOverviewFragment, SIDEBAR_EQUIPMENT, context.getString(R.string.sidebar_equipment)))
            items.add(HabiticaDrawerItem(R.id.itemsFragment, SIDEBAR_ITEMS, context.getString(R.string.sidebar_items)))
            items.add(HabiticaDrawerItem(R.id.stableFragment, SIDEBAR_STABLE, context.getString(R.string.sidebar_stable)))
            items.add(HabiticaDrawerItem(R.id.gemPurchaseActivity, SIDEBAR_PURCHASE, context.getString(R.string.sidebar_purchaseGems)))
            items.add(HabiticaDrawerItem(0, SIDEBAR_ABOUT_HEADER, context.getString(R.string.sidebar_about), true))
            items.add(HabiticaDrawerItem(R.id.newsFragment, SIDEBAR_NEWS, context.getString(R.string.sidebar_news)))
            items.add(HabiticaDrawerItem(R.id.FAQOverviewFragment, SIDEBAR_HELP, context.getString(R.string.sidebar_help)))
            items.add(HabiticaDrawerItem(R.id.aboutFragment, SIDEBAR_ABOUT, context.getString(R.string.sidebar_about)))
        }
        adapter.updateItems(items)
    }

    fun setSelection(transitionId: Int?, openSelection: Boolean = true) {
        adapter.selectedItem = transitionId
        closeDrawer()

        if (!openSelection) {
            return
        }

        val activity = activity as? MainActivity
        if (activity != null) {
            if (transitionId != null) {
                activity.navigate(transitionId)
            }
        }
    }

    private fun startNotificationsActivity() {
        closeDrawer()

        val activity = activity as? MainActivity
        if (activity != null) {
            // NotificationsActivity will return a result intent with a notificationId if a
            // notification item was clicked
            val intent = Intent(activity, NotificationsActivity::class.java)
            activity.startActivityForResult(intent, NOTIFICATION_CLICK)
        }
    }

    /**
     * Users of this fragment must call this method to set UP the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: androidx.drawerlayout.widget.DrawerLayout, viewModel: NotificationsViewModel) {
        fragmentContainerView = activity?.findViewById(fragmentId)
        this.drawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout?.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        // set UP the drawer's list view with items and click listener

        subscriptions?.add(viewModel.getNotificationCount().subscribeWithErrorHandler(Consumer {
            setNotificationsCount(it)
        }))
        subscriptions?.add(viewModel.allNotificationsSeen().subscribeWithErrorHandler(Consumer {
            setNotificationsSeen(it)
        }))
        subscriptions?.add(viewModel.getHasPartyNotification().subscribeWithErrorHandler(Consumer {
            val partyMenuItem = getItemWithIdentifier(SIDEBAR_PARTY)
            if (it) {
                partyMenuItem?.additionalInfo = ""
            } else {
                partyMenuItem?.additionalInfo = null
            }
        }))
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

    private fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
            adapter.getItemWithIdentifier(identifier)

    private fun updateItem(item: HabiticaDrawerItem) {
        adapter.updateItem(item)
    }

    private fun setDisplayName(name: String?) {
        if (toolbarTitle != null) {
            if (name != null && name.isNotEmpty()) {
                toolbarTitle.text = name
            } else {
                toolbarTitle.text = context?.getString(R.string.app_name)
            }
        }
    }

    private fun setUsername(name: String?) {
        if (usernameTextView != null) {
            usernameTextView.text = name
            usernameTextView.visibility = View.VISIBLE
        } else {
            usernameTextView.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    private fun setNotificationsCount(unreadNotifications: Int) {
        if (unreadNotifications == 0) {
            notificationsBadge.visibility = View.GONE
        } else {
            notificationsBadge.visibility = View.VISIBLE
            notificationsBadge.text = unreadNotifications.toString()
        }
    }

    private fun setNotificationsSeen(allSeen: Boolean) {
        context?.let {
            val colorId = if (allSeen) R.color.gray_200 else R.color.brand_400

            val bg = notificationsBadge.background as? GradientDrawable
            bg?.color = ColorStateList.valueOf(ContextCompat.getColor(it, colorId))
        }
    }

    private fun setMessagesCount(unreadMessages: Int) {
        if (unreadMessages == 0) {
            messagesBadge.visibility = View.GONE
        } else {
            messagesBadge.visibility = View.VISIBLE
            messagesBadge.text = unreadMessages.toString()
        }
    }

    private fun setSettingsCount(count: Int) {
        if (count == 0) {
            settingsBadge.visibility = View.GONE
        } else {
            settingsBadge.visibility = View.VISIBLE
            settingsBadge.text = count.toString()
        }
    }

    companion object {

        const val SIDEBAR_TASKS = "tasks"
        const val SIDEBAR_SKILLS = "skills"
        const val SIDEBAR_STATS = "stats"
        const val SIDEBAR_ACHIEVEMENTS = "achievements"
        const val SIDEBAR_SOCIAL = "social"
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
        const val SIDEBAR_HELP = "help"
        const val SIDEBAR_ABOUT = "about"

        private const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"
    }
}
