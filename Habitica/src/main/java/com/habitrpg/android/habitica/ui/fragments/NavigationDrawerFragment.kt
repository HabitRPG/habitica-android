package com.habitrpg.android.habitica.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.SimpleItemAnimator
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.DrawerMainBinding
import com.habitrpg.android.habitica.extensions.getMinuteOrSeconds
import com.habitrpg.android.habitica.extensions.getRemainingString
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.WorldStateEvent
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.PromoType
import com.habitrpg.android.habitica.models.user.Inbox
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.NotificationsActivity
import com.habitrpg.android.habitica.ui.adapter.NavigationDrawerAdapter
import com.habitrpg.android.habitica.ui.menu.HabiticaDrawerItem
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.common.habitica.extensions.getThemeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class NavigationDrawerFragment : DialogFragment() {

    private var binding: DrawerMainBinding? = null

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var contentRepository: ContentRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var userViewModel: MainUserViewModel

    private var activePromo: HabiticaPromotion? = null

    private var drawerLayout: DrawerLayout? = null
    private var fragmentContainerView: View? = null

    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState: Boolean = false

    private lateinit var adapter: NavigationDrawerAdapter

    val isDrawerOpen: Boolean
        get() = drawerLayout?.isDrawerOpen(GravityCompat.START) ?: false

    private var isTabletUI: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val context = context
        adapter = if (context != null) {
            NavigationDrawerAdapter(
                context.getThemeColor(R.attr.colorPrimaryText),
                context.getThemeColor(R.attr.colorPrimaryOffset)
            )
        } else {
            NavigationDrawerAdapter(0, 0)
        }
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }
        isTabletUI = resources.getBoolean(R.bool.isTabletUI)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.drawer_main, container, false) as? ViewGroup

    private var updatingJobs = mutableMapOf<String, Job>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DrawerMainBinding.bind(view)

        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
        (binding?.recyclerView?.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations =
            false
        initializeMenuItems()

        adapter.itemSelectedEvents = {
                setSelection(it.transitionId, it.bundle, true)
            }
        adapter.promoClosedSubject = {
                sharedPreferences.edit {
                    putBoolean("hide$it", true)
                }
                updatePromo()
            }

        lifecycleScope.launchCatching {
            contentRepository.getWorldState()
                .combine(
                    inventoryRepository.getAvailableLimitedItems()
                ) { state, items -> Pair(state, items) }
                .collect { pair ->
                    val gearEvent = pair.first.events.firstOrNull { it.gear }
                    createUpdatingJob("seasonal", {
                        gearEvent?.isCurrentlyActive == true || pair.second.isNotEmpty()
                    }, {
                        val diff = (gearEvent?.end?.time ?: 0) - Date().time
                        if (diff < (1.toDuration(DurationUnit.HOURS).inWholeMilliseconds)) 1.toDuration(
                            DurationUnit.SECONDS
                        ) else 1.toDuration(DurationUnit.MINUTES)
                    }) {
                        updateSeasonalMenuEntries(gearEvent, pair.second)
                    }

                    val event = configManager.getBirthdayEvent()
                    val item = getItemWithIdentifier(SIDEBAR_BIRTHDAY)
                    if (event != null && item == null) {
                        adapter.currentEvent = event
                        val birthdayItem = HabiticaDrawerItem(R.id.birthdayActivity, SIDEBAR_BIRTHDAY)
                        birthdayItem.itemViewType = 6
                        val newItems = mutableListOf<HabiticaDrawerItem>()
                        newItems.addAll(adapter.items)
                        newItems.add(0, birthdayItem)
                        adapter.updateItems(newItems)
                        (activity as? MainActivity)?.showBirthdayIcon = true
                    } else if (event == null && item != null) {
                        item.isVisible = false
                        adapter.updateItem(item)
                        (activity as? MainActivity)?.showBirthdayIcon = false
                    }
                }
        }

        userViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                updateUser(it)
            }
        }

        binding?.messagesButtonWrapper?.setOnClickListener {
            setSelection(
                R.id.inboxFragment,
                null,
                true,
                preventReselection = false
            )
        }
        binding?.settingsButtonWrapper?.setOnClickListener {
            setSelection(
                R.id.prefsActivity,
                null,
                true,
                preventReselection = false
            )
        }
        binding?.notificationsButtonWrapper?.setOnClickListener { startNotificationsActivity() }
    }

    private fun createUpdatingJob(
        key: String,
        endingCondition: () -> Boolean,
        delayFunc: () -> Duration,
        function: () -> Unit
    ) {
        function()
        if (updatingJobs[key]?.isActive == true) {
            updatingJobs[key]?.cancel()
        }
        updatingJobs[key] = lifecycleScope.launch(Dispatchers.Main) {
            while (endingCondition()) {
                function()
                delay(delayFunc())
            }
        }
    }

    private fun updateSeasonalMenuEntries(gearEvent: WorldStateEvent?, items: List<Item>) {
        val market = getItemWithIdentifier(SIDEBAR_SHOPS_MARKET) ?: return
        if (items.isNotEmpty() && items.firstOrNull()?.event?.end?.after(Date()) == true) {
            market.pillText = context?.getString(R.string.something_new)
            market.subtitle = context?.getString(R.string.limited_potions_available)
        } else {
            market.pillText = null
            market.subtitle = null
        }
        adapter.updateItem(market)

        val shop = getItemWithIdentifier(SIDEBAR_SHOPS_SEASONAL) ?: return
        shop.pillText = context?.getString(R.string.open)
        if (gearEvent?.isCurrentlyActive == true) {
            shop.isVisible = true
            shop.subtitle =
                context?.getString(R.string.open_for, gearEvent.end?.getShortRemainingString())
        } else {
            shop.isVisible = false
        }
        adapter.updateItem(shop)
    }

    private fun updateUser(user: User) {
        binding?.avatarView?.setOnClickListener {
            MainNavigationController.navigate(
                R.id.openProfileActivity,
                bundleOf(Pair("userID", user.id))
            )
        }

        setMessagesCount(user.inbox)
        setSettingsCount(if (user.flags?.verifiedUsername != true) 1 else 0)
        setDisplayName(user.profile?.name)
        setUsername(user.formattedUsername)
        binding?.avatarView?.setAvatar(user)
        binding?.questMenuView?.configure(user)

        val tavernItem = getItemWithIdentifier(SIDEBAR_TAVERN)
        if (user.preferences?.sleep == true) {
            tavernItem?.subtitle = context?.getString(R.string.damage_paused)
        } else {
            tavernItem?.subtitle = null
        }

        val userItems = user.items
        var hasSpecialItems = false
        if (userItems != null) {
            hasSpecialItems = userItems.hasTransformationItems
        }
        val item = getItemWithIdentifier(SIDEBAR_SKILLS)
        if (item != null) {
            if (!user.hasClass && !hasSpecialItems) {
                item.isVisible = false
            } else {
                if ((user.stats?.lvl
                        ?: 0) < HabiticaSnackbar.MIN_LEVEL_FOR_SKILLS && (!hasSpecialItems)
                ) {
                    item.pillText = getString(R.string.unlock_lvl_11)
                } else {
                    item.pillText = null
                }
                item.isVisible = true
            }
            updateItem(item)
        }
        val statsItem = getItemWithIdentifier(SIDEBAR_STATS)
        if (statsItem != null) {
            if (user.preferences?.disableClasses != true) {
                if ((user.stats?.lvl ?: 0) >= 10 && (user.stats?.points ?: 0) > 0) {
                    statsItem.pillText = user.stats?.points.toString()
                } else {
                    statsItem.pillText = null
                }
                statsItem.isVisible = true
            } else {
                statsItem.isVisible = false
            }
            updateItem(statsItem)
        }

        val subscriptionItem = getItemWithIdentifier(SIDEBAR_SUBSCRIPTION)
        if (user.isSubscribed && user.purchased?.plan?.dateTerminated != null) {
            val terminatedCalendar = Calendar.getInstance()
            terminatedCalendar.time = user.purchased?.plan?.dateTerminated ?: Date()
            val msDiff = terminatedCalendar.timeInMillis - Calendar.getInstance().timeInMillis
            val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            if (daysDiff <= 30) {
                context?.let {
                    subscriptionItem?.subtitle =
                        user.purchased?.plan?.dateTerminated?.getRemainingString(it.resources)
                    subscriptionItem?.subtitleTextColor = when {
                        daysDiff <= 2 -> ContextCompat.getColor(it, R.color.red_100)
                        daysDiff <= 7 -> ContextCompat.getColor(it, R.color.brand_400)
                        else -> it.getThemeColor(R.attr.textColorSecondary)
                    }
                }
            }
        } else if (user.isSubscribed) {
            subscriptionItem?.subtitle = null
        } else {
            subscriptionItem?.subtitle = context?.getString(R.string.more_out_of_habitica)
        }

        subscriptionItem?.let { updateItem(it) }

        val promoItem = getItemWithIdentifier(SIDEBAR_SUBSCRIPTION_PROMO)
        if (promoItem != null) {
            promoItem.isVisible = !user.isSubscribed
            updateItem(promoItem)
        }
        getItemWithIdentifier(SIDEBAR_NEWS)?.let {
            it.showBubble = user.flags?.newStuff ?: false
        }

        val partyMenuItem = getItemWithIdentifier(SIDEBAR_PARTY)
        if (user.hasParty && partyMenuItem?.bundle == null) {
            partyMenuItem?.transitionId = R.id.partyFragment
            partyMenuItem?.bundle = bundleOf(Pair("partyID", user.party?.id))
        } else if (!user.hasParty) {
            partyMenuItem?.transitionId = R.id.noPartyFragment
            partyMenuItem?.bundle = null
        }
    }

    override fun onDestroy() {
        socialRepository.close()
        inventoryRepository.close()
        userRepository.close()
        updatingJobs.forEach { it.value.cancel() }
        updatingJobs.clear()
        super.onDestroy()
    }

    private fun initializeMenuItems() {
        val items = ArrayList<HabiticaDrawerItem>()
        context?.let { context ->
            items.add(
                HabiticaDrawerItem(
                    R.id.tasksFragment,
                    SIDEBAR_TASKS,
                    context.getString(R.string.sidebar_tasks)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.skillsFragment,
                    SIDEBAR_SKILLS,
                    context.getString(R.string.sidebar_skills)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.statsFragment,
                    SIDEBAR_STATS,
                    context.getString(R.string.sidebar_stats)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.achievementsFragment,
                    SIDEBAR_ACHIEVEMENTS,
                    context.getString(R.string.sidebar_achievements)
                )
            )

            items.add(
                HabiticaDrawerItem(
                    0,
                    SIDEBAR_INVENTORY,
                    context.getString(R.string.sidebar_shops),
                    isHeader = true
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.marketFragment,
                    SIDEBAR_SHOPS_MARKET,
                    context.getString(R.string.market)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.questShopFragment,
                    SIDEBAR_SHOPS_QUEST,
                    context.getString(R.string.questShop)
                )
            )
            val seasonalShopEntry = HabiticaDrawerItem(
                R.id.seasonalShopFragment,
                SIDEBAR_SHOPS_SEASONAL,
                context.getString(R.string.seasonalShop)
            )
            seasonalShopEntry.isVisible = false
            items.add(seasonalShopEntry)
            items.add(
                HabiticaDrawerItem(
                    R.id.timeTravelersShopFragment,
                    SIDEBAR_SHOPS_TIMETRAVEL,
                    context.getString(R.string.timeTravelers)
                )
            )

            items.add(
                HabiticaDrawerItem(
                    0,
                    SIDEBAR_INVENTORY,
                    context.getString(R.string.sidebar_section_inventory),
                    isHeader = true
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.avatarOverviewFragment,
                    SIDEBAR_AVATAR,
                    context.getString(R.string.sidebar_avatar)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.equipmentOverviewFragment,
                    SIDEBAR_EQUIPMENT,
                    context.getString(R.string.sidebar_equipment)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.itemsFragment,
                    SIDEBAR_ITEMS,
                    context.getString(R.string.sidebar_items)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.stableFragment,
                    SIDEBAR_STABLE,
                    context.getString(R.string.sidebar_stable)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.gemPurchaseActivity,
                    SIDEBAR_GEMS,
                    context.getString(R.string.sidebar_gems)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.subscriptionPurchaseActivity,
                    SIDEBAR_SUBSCRIPTION,
                    context.getString(R.string.sidebar_subscription)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    0,
                    SIDEBAR_SOCIAL,
                    context.getString(R.string.sidebar_section_social),
                    isHeader = true
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.partyFragment,
                    SIDEBAR_PARTY,
                    context.getString(R.string.sidebar_party)
                )
            )
            if (!configManager.hideTavern()) {
                items.add(
                    HabiticaDrawerItem(
                        R.id.tavernFragment,
                        SIDEBAR_TAVERN,
                        context.getString(R.string.sidebar_tavern)
                    )
                )
            }
            if (!configManager.hideGuilds()) {
                items.add(
                    HabiticaDrawerItem(
                        R.id.guildOverviewFragment,
                        SIDEBAR_GUILDS,
                        context.getString(R.string.sidebar_guilds)
                    )
                )
            }
            if (!configManager.hideChallenges()) {
                items.add(
                    HabiticaDrawerItem(
                        R.id.challengesOverviewFragment,
                        SIDEBAR_CHALLENGES,
                        context.getString(R.string.sidebar_challenges)
                    )
                )
            }

            items.add(
                HabiticaDrawerItem(
                    0,
                    SIDEBAR_ABOUT_HEADER,
                    context.getString(R.string.sidebar_about),
                    isHeader = true
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.newsFragment,
                    SIDEBAR_NEWS,
                    context.getString(R.string.sidebar_news)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.supportMainFragment,
                    SIDEBAR_HELP,
                    context.getString(R.string.sidebar_help)
                )
            )
            items.add(
                HabiticaDrawerItem(
                    R.id.aboutFragment,
                    SIDEBAR_ABOUT,
                    context.getString(R.string.sidebar_about)
                )
            )
        }

        val promoItem = HabiticaDrawerItem(R.id.subscriptionPurchaseActivity, SIDEBAR_PROMO)
        promoItem.itemViewType = 5
        promoItem.isVisible = false
        items.add(promoItem)

        if (configManager.showSubscriptionBanner()) {
            val item =
                HabiticaDrawerItem(R.id.subscriptionPurchaseActivity, SIDEBAR_SUBSCRIPTION_PROMO)
            item.itemViewType = 2
            items.add(item)
        }
        adapter.updateItems(items)
    }

    fun setSelection(
        transitionId: Int?,
        bundle: Bundle? = null,
        openSelection: Boolean = true,
        preventReselection: Boolean = true
    ) {
        if (!isTabletUI) {
            closeDrawer()
        }
        if (adapter.selectedItem != null && adapter.selectedItem == transitionId && bundle == null && preventReselection) return
        adapter.selectedItem = transitionId

        if (!openSelection) {
            return
        }

        if (transitionId != null) {
            if (bundle != null) {
                MainNavigationController.navigate(transitionId, bundle)
            } else {
                MainNavigationController.navigate(transitionId)
            }
        }
    }

    private fun startNotificationsActivity() {
        if (!isTabletUI) {
            closeDrawer()
        }

        val activity = activity as? MainActivity
        if (activity != null) {
            // NotificationsActivity will return a result intent with a notificationId if a
            // notification item was clicked
            val intent = Intent(activity, NotificationsActivity::class.java)
            notificationClickResult.launch(intent)
        }
    }

    private val notificationClickResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                (activity as? MainActivity)?.notificationsViewModel?.click(
                    it.data?.getStringExtra("notificationId") ?: "",
                    MainNavigationController
                )
            }
        }

    /**
     * Users of this fragment must call this method to set UP the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(
        fragmentId: Int,
        drawerLayout: DrawerLayout,
        viewModel: NotificationsViewModel
    ) {
        fragmentContainerView = activity?.findViewById(fragmentId)
        this.drawerLayout = drawerLayout
        // set UP the drawer's list view with items and click listener

        lifecycleScope.launchCatching {
            viewModel.getNotificationCount().collect {
                setNotificationsCount(it)
            }
        }
        lifecycleScope.launchCatching {
            viewModel.allNotificationsSeen().collect {
                setNotificationsSeen(it)
            }
        }
        lifecycleScope.launchCatching {
            viewModel.getHasPartyNotification().collect {
                val partyMenuItem = getItemWithIdentifier(SIDEBAR_PARTY)
                partyMenuItem?.showBubble = it
            }
        }
    }

    fun openDrawer() {
        val containerView = fragmentContainerView
        if (containerView != null && containerView.parent is DrawerLayout) {
            drawerLayout?.openDrawer(containerView)
        } else {
            containerView?.isVisible = true
        }
    }

    fun closeDrawer() {
        val containerView = fragmentContainerView
        if (containerView != null && containerView.parent is DrawerLayout) {
            drawerLayout?.closeDrawer(containerView)
        } else {
            containerView?.isVisible = false
        }
    }

    fun toggleDrawer() {
        val containerView = fragmentContainerView
        if (containerView != null && containerView.parent is DrawerLayout) {
            if (drawerLayout?.isDrawerOpen(containerView) == true) {
                drawerLayout?.closeDrawer(containerView)
            } else {
                drawerLayout?.openDrawer(containerView)
            }
        } else {
            containerView?.isVisible = containerView?.isVisible != true
        }
    }

    private fun getItemWithIdentifier(identifier: String): HabiticaDrawerItem? =
        adapter.getItemWithIdentifier(identifier)

    private fun updateItem(item: HabiticaDrawerItem) {
        adapter.updateItem(item)
    }

    private fun setDisplayName(name: String?) {
        if (name != null && name.isNotEmpty()) {
            binding?.toolbarTitle?.text = name
        } else {
            binding?.toolbarTitle?.text = context?.getString(R.string.app_name)
        }
    }

    private fun setUsername(name: String?) {
        binding?.usernameTextView?.text = name
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    private fun setNotificationsCount(unreadNotifications: Int) {
        if (unreadNotifications == 0) {
            binding?.notificationsBadge?.visibility = View.GONE
        } else {
            binding?.notificationsBadge?.visibility = View.VISIBLE
            binding?.notificationsBadge?.text = unreadNotifications.toString()
        }
    }

    private fun setNotificationsSeen(allSeen: Boolean) {
        context?.let {
            val color = if (allSeen) {
                ContextCompat.getColor(it, R.color.gray_200)
            } else {
                it.getThemeColor(R.attr.colorAccent)
            }

            val bg = binding?.notificationsBadge?.background as? GradientDrawable
            bg?.color = ColorStateList.valueOf(color)
            binding?.notificationsBadge?.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
    }

    private fun setMessagesCount(inbox: Inbox?) {
        val numOfUnreadMessages = inbox?.newMessages ?: 0
        if (numOfUnreadMessages != 0) {
            binding?.messagesBadge?.visibility = View.VISIBLE
            binding?.messagesBadge?.text = numOfUnreadMessages.toString()
            context?.let {
                val color = if (inbox?.hasUserSeenInbox != true) {
                    it.getThemeColor(R.attr.colorAccent)
                } else {
                    ContextCompat.getColor(it, R.color.gray_200)
                }
                val background = binding?.messagesBadge?.background as? GradientDrawable
                background?.color = ColorStateList.valueOf(color)
                binding?.messagesBadge?.setTextColor(ContextCompat.getColor(it, R.color.white))
            }
        } else {
            binding?.messagesBadge?.visibility = View.GONE
        }
    }

    private fun setSettingsCount(count: Int) {
        if (count == 0) {
            binding?.settingsBadge?.visibility = View.GONE
        } else {
            binding?.settingsBadge?.visibility = View.VISIBLE
            binding?.settingsBadge?.text = count.toString()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun updatePromo() {
        activePromo = configManager.activePromo()
        val promoItem = getItemWithIdentifier(SIDEBAR_PROMO) ?: return
        activePromo?.let { activePromo ->
            promoItem.isVisible =
                !sharedPreferences.getBoolean("hide${activePromo.identifier}", false)
            adapter.activePromo = activePromo
            var promotedItem: HabiticaDrawerItem? = null
            if (activePromo.promoType == PromoType.GEMS_AMOUNT || activePromo.promoType == PromoType.GEMS_PRICE) {
                promotedItem = getItemWithIdentifier(SIDEBAR_GEMS)
            }
            if (activePromo.promoType == PromoType.SUBSCRIPTION) {
                promotedItem = getItemWithIdentifier(SIDEBAR_SUBSCRIPTION)
            }
            if (promotedItem == null) return@let
            promotedItem.pillText = context?.getString(R.string.sale)
            promotedItem.pillBackground =
                context?.let { activePromo.pillBackgroundDrawable(it) }
            createUpdatingJob(activePromo.promoType.name, {
                activePromo.isActive
            }, {
                val diff =
                    (activePromo.endDate.time - Date().time).toDuration(DurationUnit.SECONDS)
                1.toDuration(diff.getMinuteOrSeconds())
            }) {
                if (activePromo.isActive) {
                    promotedItem.subtitle = context?.getString(
                        R.string.sale_ends_in,
                        activePromo.endDate.getShortRemainingString()
                    )
                    updateItem(promotedItem)
                } else {
                    promotedItem.subtitle = null
                    promotedItem.pillText = null
                    updateItem(promotedItem)
                }
            }
        } ?: run {
            promoItem.isVisible = false
        }
        updateItem(promoItem)
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
        const val SIDEBAR_SHOPS_MARKET = "market"
        const val SIDEBAR_SHOPS_QUEST = "questShop"
        const val SIDEBAR_SHOPS_SEASONAL = "seasonalShop"
        const val SIDEBAR_SHOPS_TIMETRAVEL = "timeTravelersShop"
        const val SIDEBAR_AVATAR = "avatar"
        const val SIDEBAR_EQUIPMENT = "equipment"
        const val SIDEBAR_ITEMS = "items"
        const val SIDEBAR_STABLE = "stable"
        const val SIDEBAR_GEMS = "gems"
        const val SIDEBAR_SUBSCRIPTION = "subscription"
        const val SIDEBAR_SUBSCRIPTION_PROMO = "subscriptionpromo"
        const val SIDEBAR_BIRTHDAY = "birthday"
        const val SIDEBAR_PROMO = "promo"
        const val SIDEBAR_ABOUT_HEADER = "about_header"
        const val SIDEBAR_NEWS = "news"
        const val SIDEBAR_HELP = "help"
        const val SIDEBAR_ABOUT = "about"

        private const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"
    }
}
