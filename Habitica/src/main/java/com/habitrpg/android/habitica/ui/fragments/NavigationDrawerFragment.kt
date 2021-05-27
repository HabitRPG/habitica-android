package com.habitrpg.android.habitica.ui.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.DrawerMainBinding
import com.habitrpg.android.habitica.extensions.*
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.PromoType
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
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.time.ExperimentalTime
import kotlin.time.minutes
import kotlin.time.seconds


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

    private var activePromo: HabiticaPromotion? = null

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
            binding?.questMenuView?.visibility = View.GONE
            context?.let {
                adapter.tintColor = it.getThemeColor(R.attr.colorPrimary)
                if (context?.isUsingNightModeResources() == true) {
                    adapter.backgroundTintColor = ContextCompat.getColor(it, R.color.gray_50)
                } else {
                    adapter.backgroundTintColor = it.getThemeColor(R.attr.colorPrimary)
                }
            }
            adapter.items.filter { it.identifier == SIDEBAR_TAVERN }.forEach {
                it.subtitle = null
            }
            return
        }
        binding?.questMenuView?.visibility = View.VISIBLE

        binding?.menuHeaderView?.setBackgroundColor(questContent.colors?.darkColor ?: 0)
        binding?.questMenuView?.configure(quest)
        binding?.questMenuView?.configure(questContent)
        adapter.tintColor = questContent.colors?.extraLightColor ?: 0
        adapter.backgroundTintColor = questContent.colors?.darkColor ?: 0


        binding?.messagesBadge?.visibility = View.GONE
        binding?.settingsBadge?.visibility = View.GONE
        binding?.notificationsBadge?.visibility = View.GONE

        /* Reenable this once the boss art can be displayed correctly.

        val preferences = context?.getSharedPreferences("collapsible_sections", 0)
        if (preferences?.getBoolean("boss_art_collapsed", false) == true) {
            questMenuView.hideBossArt()
        } else {
            questMenuView.showBossArt()
        }*/
        binding?.questMenuView?.hideBossArt()

        adapter.items.filter { it.identifier == SIDEBAR_TAVERN }.forEach {
            it.subtitle = context?.getString(R.string.active_world_boss)
        }
        adapter.notifyDataSetChanged()

        binding?.questMenuView?.setOnClickListener {
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

    private var seasonalShopJob: Job? = null

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DrawerMainBinding.bind(view)

        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        initializeMenuItems()

        subscriptions?.add(adapter.getItemSelectionEvents().subscribe({
            setSelection(it.transitionId, it.bundle, true)
        }, RxErrorHandler.handleEmptyError()))
        subscriptions?.add(adapter.getPromoCloseEvents().subscribe({
            sharedPreferences.edit {
                putBoolean("hide${it}", true)
            }
            updatePromo()
            adapter.notifyDataSetChanged()
        }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(socialRepository.getGroup(Group.TAVERN_ID)
                .doOnNext {  quest = it.quest }
                .filter { it.hasActiveQuest }
                .flatMapMaybe { inventoryRepository.getQuestContent(it.quest?.key ?: "").firstElement() }
                .subscribe({
                   questContent = it
                }, RxErrorHandler.handleEmptyError()))

        subscriptions?.add(
                Flowable.combineLatest(contentRepository.getWorldState(), inventoryRepository.getAvailableLimitedItems(), { state, items ->
                    return@combineLatest Pair(state, items)
                }).subscribe( { pair ->
                    updateSeasonalMenuEntries(pair.first, pair.second)
                    seasonalShopJob?.cancel()
                    seasonalShopJob = GlobalScope.launch(Dispatchers.Main) {
                        val gearEvent = pair.first.events.firstOrNull { it.gear }
                        while (gearEvent?.end?.after(Date()) == true || pair.second.isNotEmpty()) {
                            updateSeasonalMenuEntries(pair.first, pair.second)
                            val diff = (gearEvent?.end?.time ?: 0) - Date().time
                            delay(if (diff < (60 * 60 * 1000)) 1.seconds else 1.minutes)
                        }
                    }
            getItemWithIdentifier(SIDEBAR_SHOPS_SEASONAL)?.isVisible = false
        }, RxErrorHandler.handleEmptyError()))

        if (configManager.enableTeamBoards()) {
            subscriptions?.add(userRepository.getTeamPlans()
                    .distinctUntilChanged { firstTeams, secondTeams -> firstTeams == secondTeams }
                    .subscribe({
                        getItemWithIdentifier(SIDEBAR_TEAMS)?.isVisible = it.size != 0
                        adapter.setTeams(it)
                    }, RxErrorHandler.handleEmptyError()))
        } else {
            getItemWithIdentifier(SIDEBAR_TEAMS)?.isVisible = false
        }

        subscriptions?.add(userRepository.getUser().subscribe({
            updateUser(it)
        }, RxErrorHandler.handleEmptyError()))

        binding?.messagesButtonWrapper?.setOnClickListener { setSelection(R.id.inboxFragment, null, true, preventReselection = false) }
        binding?.settingsButtonWrapper?.setOnClickListener { setSelection(R.id.prefsActivity, null, true, preventReselection = false) }
        binding?.notificationsButtonWrapper?.setOnClickListener { startNotificationsActivity() }
    }

    private fun updateSeasonalMenuEntries(worldState: WorldState, items: List<Item>) {
        val market = getItemWithIdentifier(SIDEBAR_SHOPS_MARKET) ?: return
        if (items.isNotEmpty() && items.firstOrNull()?.event?.end?.after(Date()) == true) {
            market.pillText = context?.getString(R.string.something_new)
            market.subtitle = context?.getString(R.string.limited_potions_available)
        } else {
            market.pillText = null
            market.subtitle = null
        }
        adapter.notifyDataSetChanged()

        val shop = getItemWithIdentifier(SIDEBAR_SHOPS_SEASONAL) ?: return
        shop.pillText = context?.getString(R.string.open)
        val gearEvent = worldState.events.firstOrNull { it.gear }
        if (gearEvent?.end?.after(Date()) == true) {
            shop.isVisible = true
            shop.subtitle = context?.getString(R.string.open_for, gearEvent.end?.getShortRemainingString())
        } else {
            shop.isVisible = false
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateUser(user: User) {
        setMessagesCount(user.inbox?.newMessages ?: 0)
        setSettingsCount(if (user.flags?.verifiedUsername != true) 1 else 0 )
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
                if (user.stats?.lvl ?: 0 >= 10 && user.stats?.points ?: 0 > 0) {
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
                    subscriptionItem?.subtitle = user.purchased?.plan?.dateTerminated?.getRemainingString(it.resources)
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
        if (user.hasParty() && partyMenuItem?.bundle == null) {
            partyMenuItem?.transitionId = R.id.partyFragment
            partyMenuItem?.bundle = bundleOf(Pair("partyID", user.party?.id))
        } else if (!user.hasParty()) {
            partyMenuItem?.transitionId = R.id.noPartyFragment
            partyMenuItem?.bundle = null
        }

        val adventureGuideItem = getItemWithIdentifier(SIDEBAR_ADVENTURE_GUIDE)
        if (configManager.enableAdventureGuide()) {
            adventureGuideItem?.isVisible = !user.hasCompletedOnboarding
            adventureGuideItem?.user = user
        } else {
            adventureGuideItem?.isVisible = false
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
            val adventureItem = HabiticaDrawerItem(R.id.adventureGuideActivity, SIDEBAR_ADVENTURE_GUIDE)
            adventureItem.itemViewType = 4
            items.add(adventureItem)
            items.add(HabiticaDrawerItem(R.id.tasksFragment, SIDEBAR_TASKS, context.getString(R.string.sidebar_tasks)))
            items.add(HabiticaDrawerItem(R.id.skillsFragment, SIDEBAR_SKILLS, context.getString(R.string.sidebar_skills)))
            items.add(HabiticaDrawerItem(R.id.statsFragment, SIDEBAR_STATS, context.getString(R.string.sidebar_stats)))
            items.add(HabiticaDrawerItem(R.id.achievementsFragment, SIDEBAR_ACHIEVEMENTS, context.getString(R.string.sidebar_achievements)))

            items.add(HabiticaDrawerItem(0, SIDEBAR_TEAMS, context.getString(R.string.sidebar_teams), true))

            items.add(HabiticaDrawerItem(0, SIDEBAR_INVENTORY, context.getString(R.string.sidebar_shops), true))
            items.add(HabiticaDrawerItem(R.id.marketFragment, SIDEBAR_SHOPS_MARKET, context.getString(R.string.market)))
            items.add(HabiticaDrawerItem(R.id.questShopFragment, SIDEBAR_SHOPS_QUEST, context.getString(R.string.questShop)))
            val seasonalShopEntry = HabiticaDrawerItem(R.id.seasonalShopFragment, SIDEBAR_SHOPS_SEASONAL, context.getString(R.string.seasonalShop))
            seasonalShopEntry.isVisible = false
            items.add(seasonalShopEntry)
            items.add(HabiticaDrawerItem(R.id.timeTravelersShopFragment, SIDEBAR_SHOPS_TIMETRAVEL, context.getString(R.string.timeTravelers)))

            items.add(HabiticaDrawerItem(0, SIDEBAR_INVENTORY, context.getString(R.string.sidebar_section_inventory), true))
            items.add(HabiticaDrawerItem(R.id.itemsFragment, SIDEBAR_ITEMS, context.getString(R.string.sidebar_items)))
            items.add(HabiticaDrawerItem(R.id.equipmentOverviewFragment, SIDEBAR_EQUIPMENT, context.getString(R.string.sidebar_equipment)))
            items.add(HabiticaDrawerItem(R.id.stableFragment, SIDEBAR_STABLE, context.getString(R.string.sidebar_stable)))
            items.add(HabiticaDrawerItem(R.id.avatarOverviewFragment, SIDEBAR_AVATAR, context.getString(R.string.sidebar_avatar)))
            items.add(HabiticaDrawerItem(R.id.gemPurchaseActivity, SIDEBAR_GEMS, context.getString(R.string.sidebar_gems)))
            items.add(HabiticaDrawerItem(R.id.subscriptionPurchaseActivity, SIDEBAR_SUBSCRIPTION, context.getString(R.string.sidebar_subscription)))

            items.add(HabiticaDrawerItem(0, SIDEBAR_SOCIAL, context.getString(R.string.sidebar_section_social), true))
            items.add(HabiticaDrawerItem(R.id.partyFragment, SIDEBAR_PARTY, context.getString(R.string.sidebar_party)))
            items.add(HabiticaDrawerItem(R.id.tavernFragment, SIDEBAR_TAVERN, context.getString(R.string.sidebar_tavern)))
            items.add(HabiticaDrawerItem(R.id.guildOverviewFragment, SIDEBAR_GUILDS, context.getString(R.string.sidebar_guilds)))
            items.add(HabiticaDrawerItem(R.id.challengesOverviewFragment, SIDEBAR_CHALLENGES, context.getString(R.string.sidebar_challenges)))

            items.add(HabiticaDrawerItem(0, SIDEBAR_ABOUT_HEADER, context.getString(R.string.sidebar_about), true))
            items.add(HabiticaDrawerItem(R.id.newsFragment, SIDEBAR_NEWS, context.getString(R.string.sidebar_news)))
            items.add(HabiticaDrawerItem(R.id.supportMainFragment, SIDEBAR_HELP, context.getString(R.string.sidebar_help)))
            items.add(HabiticaDrawerItem(R.id.aboutFragment, SIDEBAR_ABOUT, context.getString(R.string.sidebar_about)))
        }


        val promoItem = HabiticaDrawerItem(R.id.subscriptionPurchaseActivity, SIDEBAR_PROMO)
        promoItem.itemViewType = 5
        promoItem.isVisible = false
        items.add(promoItem)

        if (configManager.showSubscriptionBanner()) {
            val item = HabiticaDrawerItem(R.id.subscriptionPurchaseActivity, SIDEBAR_SUBSCRIPTION_PROMO)
            item.itemViewType = 2
            items.add(item)
        }
        adapter.updateItems(items)
    }

    fun setSelection(transitionId: Int?, bundle: Bundle? = null, openSelection: Boolean = true, preventReselection: Boolean = true) {
        closeDrawer()
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

        subscriptions?.add(viewModel.getNotificationCount().subscribeWithErrorHandler {
            setNotificationsCount(it)
        })
        subscriptions?.add(viewModel.allNotificationsSeen().subscribeWithErrorHandler {
            setNotificationsSeen(it)
        })
        subscriptions?.add(viewModel.getHasPartyNotification().subscribeWithErrorHandler {
            val partyMenuItem = getItemWithIdentifier(SIDEBAR_PARTY)
            partyMenuItem?.showBubble = it
        })
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

    private fun setMessagesCount(unreadMessages: Int) {
        if (unreadMessages == 0) {
            binding?.messagesBadge?.visibility = View.GONE
        } else {
            binding?.messagesBadge?.visibility = View.VISIBLE
            binding?.messagesBadge?.text = unreadMessages.toString()
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

    fun updatePromo() {
        activePromo = configManager.activePromo()
        val promoItem = getItemWithIdentifier(SIDEBAR_PROMO) ?: return
        if (activePromo != null && !sharedPreferences.getBoolean("hide${activePromo?.identifier}", false)) {
            promoItem.isVisible = true
            adapter.activePromo = activePromo

            var promotedItem: HabiticaDrawerItem? = null
            if (activePromo?.promoType == PromoType.GEMS_AMOUNT || activePromo?.promoType == PromoType.GEMS_PRICE) {
                promotedItem = getItemWithIdentifier(SIDEBAR_GEMS)
            }
            if (activePromo?.promoType == PromoType.SUBSCRIPTION) {
                promotedItem = getItemWithIdentifier(SIDEBAR_SUBSCRIPTION)
            }
            promotedItem?.pillText = context?.getString(R.string.sale)
            promotedItem?.pillBackground = context?.let { activePromo?.pillBackgroundDrawable(it) }
            promotedItem?.let { updateItem(it) }
        } else {
            promoItem.isVisible = false
        }
        updateItem(promoItem)
    }

    companion object {

        const val SIDEBAR_TASKS = "tasks"
        const val SIDEBAR_SKILLS = "skills"
        const val SIDEBAR_STATS = "stats"
        const val SIDEBAR_ACHIEVEMENTS = "achievements"
        const val SIDEBAR_TEAMS = "teams"
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
        const val SIDEBAR_G1G1_PROMO = "g1g1promo"
        const val SIDEBAR_PROMO = "promo"
        const val SIDEBAR_ADVENTURE_GUIDE = "adventureguide"
        const val SIDEBAR_ABOUT_HEADER = "about_header"
        const val SIDEBAR_NEWS = "news"
        const val SIDEBAR_HELP = "help"
        const val SIDEBAR_ABOUT = "about"

        private const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"
    }
}
