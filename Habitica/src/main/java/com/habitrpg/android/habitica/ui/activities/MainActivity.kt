package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.*
import com.habitrpg.android.habitica.events.*
import com.habitrpg.android.habitica.events.commands.*
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.interactors.*
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.TutorialView
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.ValueBar
import com.habitrpg.android.habitica.ui.views.yesterdailies.YesterdailyDialog
import com.habitrpg.android.habitica.userpicture.BitmapUtils
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider
import com.roughike.bottombar.BottomBar
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

open class MainActivity : BaseActivity(), TutorialView.OnTutorialReaction {
    @Inject
    internal lateinit var apiClient: ApiClient
    @Inject
    internal lateinit var soundManager: SoundManager
    @Inject
    internal lateinit var maintenanceService: MaintenanceApiService
    @Inject
    internal lateinit var hostConfig: HostConfig
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager
    @Inject
    internal lateinit var habitScoreUseCase: HabitScoreUseCase
    @Inject
    internal lateinit var dailyCheckUseCase: DailyCheckUseCase
    @Inject
    internal lateinit var todoCheckUseCase: TodoCheckUseCase
    @Inject
    internal lateinit var buyRewardUseCase: BuyRewardUseCase
    @Inject
    internal lateinit var checklistCheckUseCase: ChecklistCheckUseCase
    @Inject
    internal lateinit var checkClassSelectionUseCase: CheckClassSelectionUseCase
    @Inject
    internal lateinit var displayItemDropUseCase: DisplayItemDropUseCase
    @Inject
    internal lateinit var notifyUserUseCase: NotifyUserUseCase
    @Inject
    internal lateinit var taskRepository: TaskRepository
    @Inject
    internal lateinit var userRepository: UserRepository
    @Inject
    internal lateinit var tagRepository: TagRepository
    @Inject
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    internal lateinit var taskAlarmManager: TaskAlarmManager
    @Inject
    internal lateinit var remoteConfigManager: RemoteConfigManager

    val floatingMenuWrapper: ViewGroup by bindView(R.id.floating_menu_wrapper)
    private val bottomNavigation: BottomBar by bindView(R.id.bottom_navigation)

    private val appBar: AppBarLayout by bindView(R.id.appbar)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val toolbarAccessoryContainer: FrameLayout by bindView(R.id.toolbar_accessory_container)
    private val toolbarTitleTextView: TextView by bindView(R.id.toolbar_title)
    private val collapsingToolbar: CollapsingToolbarLayout by bindView(R.id.collapsing_toolbar)
    private val detailTabs: TabLayout by bindView(R.id.detail_tabs)
    val avatarWithBars: View by bindView(R.id.avatar_with_bars)
    private val overlayLayout: ViewGroup by bindView(R.id.overlayFrameLayout)

    var user: User? = null

    private var activeFragment: WeakReference<BaseMainFragment>? = null
    private var avatarInHeader: AvatarWithBarsViewModel? = null
    private var faintDialog: AlertDialog? = null
    private var sideAvatarView: AvatarView? = null
    private var activeTutorialView: TutorialView? = null
    private var drawerFragment: NavigationDrawerFragment? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private var keyboardUtil: KeyboardUtil? = null
    private var resumeFromActivity = false

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    val userID: String
        get() = user?.id ?: ""

    val isAppBarExpanded: Boolean
        get() = appBar.height - appBar.bottom == 0


    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    @SuppressLint("ObsoleteSdkInt")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageHelper = LanguageHelper(sharedPreferences.getString("language", "en"))
        Locale.setDefault(languageHelper.locale)
        val configuration = Configuration()
        if (SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            @Suppress("Deprecation")
            configuration.locale = languageHelper.locale
        } else {
            configuration.setLocale(languageHelper.locale)
        }
        @Suppress("Deprecation")
        resources.updateConfiguration(configuration,
                resources.displayMetrics)


        if (!HabiticaBaseApplication.checkUserAuthentication(this, hostConfig)) {
            return
        }

        setupToolbar(toolbar)

        avatarInHeader = AvatarWithBarsViewModel(this, avatarWithBars, userRepository)
        sideAvatarView = AvatarView(this, true, false, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.black_10_alpha)
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            toolbar.setPadding(0, statusBarHeight, 0, 0)
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
            avatarWithBars.setPadding(px.toInt(), statusBarHeight, px.toInt(), 0)
        }

        compositeSubscription.add(userRepository.getUser(hostConfig.user)
                .subscribe(Consumer { newUser ->
                    this@MainActivity.user = newUser
                    this@MainActivity.setUserData()
                }, RxErrorHandler.handleEmptyError()))

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        drawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as? NavigationDrawerFragment

        drawerFragment?.setUp(R.id.navigation_drawer, drawerLayout)
        selectMenuItem(NavigationDrawerFragment.SIDEBAR_TASKS)

        drawerToggle = object : ActionBarDrawerToggle(
                this, /* host Activity */
                findViewById(R.id.drawer_layout), /* DrawerLayout object */
                R.string.navigation_drawer_open, /* "open drawer" description */
                R.string.navigation_drawer_close  /* "close drawer" description */
        ) {

        }

        // Set the drawer toggle as the DrawerListener
        drawerToggle.notNull { drawerLayout.addDrawerListener(it) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        keyboardUtil = KeyboardUtil(this, this.findViewById(android.R.id.content))
        this.keyboardUtil?.enable()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle?.syncState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e("RESTORED:", savedInstanceState.toString())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    override fun onResume() {
        super.onResume()

        if(!resumeFromActivity){
            retrieveUser()
            this.checkMaintenance()
        }
        resumeFromActivity = false


        if (this.sharedPreferences.getLong("lastReminderSchedule", 0) < Date().time - 86400000) {
            try {
                taskAlarmManager.scheduleAllSavedAlarms()
            } catch (e: Exception) {
                crashlyticsProxy.logException(e)
            }

        }

        //after the activity has been stopped and is thereafter resumed,
        //a state can arise in which the active fragment no longer has a
        //reference to the tabLayout (and all its adapters are null).
        //Recreate the fragment as a result.
        if (activeFragment?.get()?.tabLayout == null) {
            activeFragment = null
            var selection: String? = NavigationDrawerFragment.SIDEBAR_TASKS
            try {
                selection = this.sharedPreferences.getString("lastActivePosition", NavigationDrawerFragment.SIDEBAR_TASKS)
            } catch (ignored: java.lang.RuntimeException) {
            }
            if (selection != null) {
                selectMenuItem(selection)
            }
        }

        if (intent.hasExtra("notificationIdentifier")) {
            val identifier = intent.getStringExtra("notificationIdentifier")
            val additionalData = HashMap<String, Any>()
            additionalData["identifier"] = identifier
            AmplitudeManager.sendEvent("open notification", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
            NotificationOpenHandler.handleOpenedByNotification(identifier, intent, this, user)
        }
    }

    override fun onPause() {
        updateWidgets()
        super.onPause()
    }

    override fun startActivity(intent: Intent?) {
        resumeFromActivity = true
        super.startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        resumeFromActivity = true
        super.startActivityForResult(intent, requestCode)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        resumeFromActivity = true
        super.startActivity(intent, options)
    }

    private fun updateWidgets() {
        updateWidget(AvatarStatsWidgetProvider::class.java)
        updateWidget(TodoListWidgetProvider::class.java)
        updateWidget(DailiesWidgetProvider::class.java)
        updateWidget(HabitButtonWidgetProvider::class.java)
    }

    private fun updateWidget(widgetClass: Class<*>) {
        val intent = Intent(this, widgetClass)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, widgetClass))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun displayFragment(fragment: BaseMainFragment) {
        if (fragment.javaClass == this.activeFragment?.get()?.javaClass) {
            return
        }
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && this.isDestroyed) {
            return
        }
        this.activeFragment = WeakReference(fragment)
        fragment.arguments = intent.extras
        fragment.user = user
        fragment.activity = this
        fragment.tabLayout = detailTabs
        fragment.toolbarAccessoryContainer = toolbarAccessoryContainer
        fragment.collapsingToolbar = collapsingToolbar
        fragment.bottomNavigation = bottomNavigation
        fragment.floatingMenuWrapper = floatingMenuWrapper


        if (supportFragmentManager.fragments == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss()
        } else {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragment_container, fragment)
            if (fragment.addToBackStack()) {
                transaction.addToBackStack(null)
            }
            transaction.commitAllowingStateLoss()
        }
    }

    private fun setUserData() {
        if (user != null) {
            val preferences = user?.preferences

            preferences?.language.notNull { apiClient.setLanguageCode(it) }
            preferences?.language.notNull { soundManager.soundTheme = it }
            runOnUiThread {
                updateSidebar()
                if (activeFragment != null && activeFragment?.get() != null) {
                    activeFragment?.get()?.updateUserData(user)
                } else {
                    selectMenuItem(NavigationDrawerFragment.SIDEBAR_TASKS)
                }
            }

            displayDeathDialogIfNeeded()
            YesterdailyDialog.showDialogIfNeeded(this, user?.id, userRepository, taskRepository)

            displayNewInboxMessagesBadge()
        }
    }

    private fun displayNewInboxMessagesBadge() {
        /*int numberOfUnreadPms = this.user.getInbox().getNewMessages();
        IDrawerItem newInboxItem;

        if (numberOfUnreadPms <= 0) {
            newInboxItem = new PrimaryDrawerItem()
                    .withName(this.getString(R.string.sidebar_inbox))
                    .withIdentifier(MainDrawerBuilder.INSTANCE.getSIDEBAR_INBOX());
        } else {
            String numberOfUnreadPmsLabel = String.valueOf(numberOfUnreadPms);
            BadgeStyle badgeStyle = new BadgeStyle()
                    .withTextColor(Color.WHITE)
                    .withColorRes(R.color.md_red_700);

            newInboxItem = new PrimaryDrawerItem()
                    .withName(this.getString(R.string.sidebar_inbox))
                    .withIdentifier(MainDrawerBuilder.INSTANCE.getSIDEBAR_INBOX())
                    .withBadge(numberOfUnreadPmsLabel)
                    .withBadgeStyle(badgeStyle);
        }
        if (this.drawerFragment != null) {
            this.drawer.updateItemAtPosition(newInboxItem, this.drawer.getPosition(MainDrawerBuilder.INSTANCE.getSIDEBAR_INBOX()));
        }*/
    }

    private fun updateSidebar() {
        drawerFragment?.setUsername(user?.profile?.name)

        if (user?.preferences == null || user?.flags == null) {
            return
        }

        val specialItems = user?.items?.special
        var hasSpecialItems = false
        if (specialItems != null) {
            hasSpecialItems = specialItems.hasSpecialItems()
        }
        val item = drawerFragment?.getItemWithIdentifier(NavigationDrawerFragment.SIDEBAR_SKILLS)
        if (item != null) {
            if (user?.hasClass() == false && (!hasSpecialItems)) {
                item.isVisible = false
            } else {
                if (user?.stats?.lvl ?: 0 < HabiticaSnackbar.MIN_LEVEL_FOR_SKILLS && (!hasSpecialItems)) {
                    item.additionalInfo = getString(R.string.unlock_lvl_11)
                } else {
                    item.additionalInfo = null
                }
                item.isVisible = true
            }
            drawerFragment?.updateItem(item)
        }
        val statsItem = drawerFragment?.getItemWithIdentifier(NavigationDrawerFragment.SIDEBAR_STATS)
        if (statsItem != null) {
            if (user?.stats?.lvl ?: 0 >= 0 && user?.stats?.points ?: 0 > 0) {
                statsItem.additionalInfo = user?.stats?.points.toString()
            } else {
                statsItem.additionalInfo = null
            }
            drawerFragment?.updateItem(statsItem)
        }
    }

    fun setActiveFragment(fragment: BaseMainFragment?) {
        this.activeFragment = WeakReference<BaseMainFragment>(fragment)
        setTranslatedFragmentTitle(fragment)
        val identifier = activeFragment?.get()?.fragmentSidebarIdentifier
        if (identifier != null) {
            selectMenuItem(identifier, false)
        }
    }

    private fun setTranslatedFragmentTitle(fragment: BaseMainFragment?) {
        if (supportActionBar == null) {
            return
        }
        if (fragment?.customTitle() != null) {
            toolbarTitleTextView.text = fragment.customTitle()
        } else if (user?.profile != null) {
            toolbarTitleTextView.text = user?.profile?.name
        }
    }

    override fun onBackPressed() {
        if (this.activeTutorialView != null) {
            this.removeActiveTutorialView()
        }
        if (drawerFragment?.isDrawerOpen == true) {
            drawerFragment?.closeDrawer()
        } else {
            try {
                super.onBackPressed()
            } catch (ignored: Exception) {
            }
             this.activeFragment?.get()?.updateUserData(user)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == SELECT_CLASS_RESULT) {
            retrieveUser()
        } else if (requestCode == GEM_PURCHASE_REQUEST) {
            retrieveUser()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // region Events

    public override fun onDestroy() {
        userRepository.close()
        tagRepository.close()
        inventoryRepository.close()
        keyboardUtil?.disable()
        super.onDestroy()
    }

    @Subscribe
    fun onEvent(event: OpenMenuItemCommand) {
        drawerFragment?.setSelection(event.identifier, true)
    }

    @Subscribe
    @Suppress("ReturnCount")
    fun onEvent(event: BuyRewardCommand) {
        val rewardKey = event.Reward.id

        if (user?.stats?.gp ?: 0.toDouble() < event.Reward.value) {
            HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.no_gold), SnackbarDisplayType.FAILURE)
            return
        }

        if ("potion" == rewardKey) {
            val currentHp = user?.stats?.gp?.toInt()
            val maxHp = user?.stats?.maxHealth

            if (currentHp == maxHp) {
                HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.no_potion), SnackbarDisplayType.FAILURE_BLUE)
                return
            }
        }

        if (event.Reward.specialTag != null && event.Reward.specialTag == "item") {
            val id = event.Reward.id ?: return
            inventoryRepository.buyItem(user, id, event.Reward.value)
                    .subscribe(Consumer { buyResponse ->
                        var snackbarMessage = getString(R.string.successful_purchase, event.Reward.text)
                        if (event.Reward.id == "armoire") {
                            var dropArticle = buyResponse.armoire["dropArticle"]
                            if (buyResponse.armoire["dropArticle"] == null || buyResponse.armoire["dropArticle"].equals("null",true)) {
                                dropArticle = ""
                            }
                            snackbarMessage = when {
                                buyResponse.armoire["type"] == "gear" -> applicationContext.getString(R.string.armoireEquipment, buyResponse.armoire["dropText"])
                                buyResponse.armoire["type"] == "food" -> applicationContext.getString(R.string.armoireFood, dropArticle, buyResponse.armoire["dropText"])
                                else -> applicationContext.getString(R.string.armoireExp)
                            }
                            soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop)
                        }
                        HabiticaSnackbar.showSnackbar(floatingMenuWrapper, null, snackbarMessage, BitmapDrawable(resources, HabiticaIconsHelper.imageOfGold()), ContextCompat.getColor(this, R.color.yellow_10), "-" + event.Reward.value, SnackbarDisplayType.NORMAL)
                    }, RxErrorHandler.handleEmptyError())
        } else {
            buyRewardUseCase.observable(BuyRewardUseCase.RequestValues(user, event.Reward))
                    .subscribe(Consumer {
                        HabiticaSnackbar.showSnackbar(floatingMenuWrapper, null, getString(R.string.notification_purchase_reward),
                                BitmapDrawable(resources, HabiticaIconsHelper.imageOfGold()),
                                ContextCompat.getColor(this, R.color.yellow_10),
                                "-" + event.Reward.value.toInt(),
                                SnackbarDisplayType.DROP)
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    @Subscribe
    fun openMysteryItem(event: OpenMysteryItemEvent) {
        inventoryRepository.openMysteryItem(user)
                .flatMap { userRepository.retrieveUser(false) }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun openGemPurchaseFragment(event: OpenGemPurchaseFragmentCommand?) {
        drawerFragment?.setSelection(NavigationDrawerFragment.SIDEBAR_PURCHASE, true)
    }

    @Subscribe
    fun onEvent(tutorialEvent: DisplayTutorialEvent) {
        if (tutorialEvent.tutorialText != null) {
            this.displayTutorialStep(tutorialEvent.step, tutorialEvent.tutorialText, tutorialEvent.canBeDeferred)
        } else {
            this.displayTutorialStep(tutorialEvent.step, tutorialEvent.tutorialTexts, tutorialEvent.canBeDeferred)
        }
    }

    @Subscribe
    fun onEvent(event: DisplayFragmentEvent) {
        this.displayFragment(event.fragment)
    }

    @Subscribe
    fun onEvent(event: HatchingCommand) {
        if (event.usingEgg == null || event.usingHatchingPotion == null) {
            return
        }
        this.inventoryRepository.hatchPet(event.usingEgg, event.usingHatchingPotion)
                .subscribe(Consumer {
                    val petWrapper = View.inflate(this, R.layout.pet_imageview, null) as? FrameLayout
                    val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? SimpleDraweeView

                    DataBindingUtils.loadImage(petImageView, "Pet-" + event.usingEgg.key + "-" + event.usingHatchingPotion.key)
                    val potionName = event.usingHatchingPotion.text
                    val eggName = event.usingEgg.text
                    val dialog = AlertDialog.Builder(this@MainActivity)
                            .setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
                            .setView(petWrapper)
                            .setPositiveButton(R.string.close) { hatchingDialog, _ -> hatchingDialog.dismiss() }
                            .setNeutralButton(R.string.share) { hatchingDialog, _ ->
                                val event1 = ShareEvent()
                                event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet"
                                val sharedImage = Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(sharedImage)
                                canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300))
                                petImageView?.drawable?.draw(canvas)
                                event1.shareImage = sharedImage
                                EventBus.getDefault().post(event1)
                                hatchingDialog.dismiss()
                            }
                            .create()
                    dialog.show()
                }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(event: FeedCommand) {
        if (event.usingFood == null || event.usingPet == null) {
            return
        }
        val pet = event.usingPet
        this.inventoryRepository.feedPet(event.usingPet, event.usingFood)
                .subscribe(Consumer { feedResponse ->
                    HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.notification_pet_fed, pet.colorText, pet.animalText), SnackbarDisplayType.NORMAL)
                    if (feedResponse.value == -1) {
                        val mountWrapper = View.inflate(this, R.layout.pet_imageview, null) as FrameLayout
                        val mountImageView = mountWrapper.findViewById<View>(R.id.pet_imageview) as SimpleDraweeView

                        DataBindingUtils.loadImage(mountImageView, "Mount_Icon_" + event.usingPet.key)
                        val colorName = event.usingPet.colorText
                        val animalName = event.usingPet.animalText
                        val dialog = AlertDialog.Builder(this@MainActivity)
                                .setTitle(getString(R.string.evolved_pet_title, colorName, animalName))
                                .setView(mountWrapper)
                                .setPositiveButton(R.string.close) { hatchingDialog, _ -> hatchingDialog.dismiss() }
                                .setNeutralButton(R.string.share) { hatchingDialog, _ ->
                                    val event1 = ShareEvent()
                                    event1.sharedMessage = getString(R.string.share_raised, colorName, animalName) + " https://habitica.com/social/raise-pet"
                                    val sharedImage = Bitmap.createBitmap(99, 99, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(sharedImage)
                                    canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300))
                                    mountImageView.drawable.draw(canvas)
                                    event1.shareImage = sharedImage
                                    EventBus.getDefault().post(event1)
                                    hatchingDialog.dismiss()
                                }
                                .create()
                        dialog.show()
                    }
                }, RxErrorHandler.handleEmptyError())
    }

    // endregion

    private fun displayTaskScoringResponse(data: TaskScoringResult?) {
        if (user != null && data != null) {
            notifyUserUseCase.observable(NotifyUserUseCase.RequestValues(this, floatingMenuWrapper,
                    user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, data.questDamage, data.hasLeveledUp))
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }

        displayItemDropUseCase.observable(DisplayItemDropUseCase.RequestValues(data, this, floatingMenuWrapper))
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }


    private fun displayDeathDialogIfNeeded() {

        if (user?.stats?.hp ?: 0.0 > 0) {
            return
        }

        if (this.faintDialog == null && !this.isFinishing) {

            val customView = View.inflate(this, R.layout.dialog_faint, null)
            if (customView != null) {
                val hpBarView = customView.findViewById<View>(R.id.hpBar) as ValueBar

                hpBarView.setLightBackground(true)
                hpBarView.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())

                val dialogAvatarView = customView.findViewById<View>(R.id.avatarView) as AvatarView
                user.notNull { dialogAvatarView.setAvatar(it) }
            }

            this.faintDialog = AlertDialog.Builder(this)
                    .setTitle(R.string.faint_header)
                    .setView(customView)
                    .setPositiveButton(R.string.faint_button) { _, _ ->
                        faintDialog = null
                        user.notNull {
                            userRepository.revive(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                        }
                    }
                    .create()

            soundManager.loadAndPlayAudio(SoundManager.SoundDeath)
            this.faintDialog?.show()
        }
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            drawerFragment?.openDrawer()
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    protected fun retrieveUser() {
        if (hostConfig.hasAuthentication()) {
            this.userRepository.retrieveUser(true)
                    .doOnNext { user1 ->
                        pushNotificationManager.setUser(user1)
                        pushNotificationManager.addPushDeviceUsingStoredToken()
                    }
                    .flatMap { userRepository.retrieveInboxMessages() }
                    .flatMap { inventoryRepository.retrieveContent(false) }
                    .flatMap { inventoryRepository.retrieveWorldState() }
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    @Subscribe
    fun displayClassSelectionActivity(event: SelectClassEvent) {
        checkClassSelectionUseCase.observable(CheckClassSelectionUseCase.RequestValues(user, event, this))
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    private fun displayTutorialStep(step: TutorialStep, text: String, canBeDeferred: Boolean) {
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialText(text)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        this.overlayLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    private fun displayTutorialStep(step: TutorialStep, texts: List<String>, canBeDeferred: Boolean) {
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialTexts(texts)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        this.overlayLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialCompleted(step: TutorialStep) {
        val path = "flags.tutorial." + step.tutorialGroup + "." + step.identifier
        val updateData = HashMap<String, Any>()
        updateData[path] = true
        userRepository.updateUser(user, updateData)
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        this.overlayLayout.removeView(this.activeTutorialView)
        this.removeActiveTutorialView()

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = true
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialDeferred(step: TutorialStep) {
        taskRepository.executeTransaction(Realm.Transaction { step.displayedOn = Date() })

        this.removeActiveTutorialView()
    }

    private fun removeActiveTutorialView() {
        if (this.activeTutorialView != null) {
            this.overlayLayout.removeView(this.activeTutorialView)
            this.activeTutorialView = null
        }
    }

    @Subscribe
    fun shareEvent(event: ShareEvent) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "*/*"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, event.sharedMessage)
        val f = BitmapUtils.saveToShareableFile(filesDir.toString() + "/shared_images", "share.png", event.shareImage)
        val fileUri = FileProvider.getUriForFile(this, getString(R.string.content_provider), f)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        val resInfoList = this.packageManager.queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            this.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
    }

    @Subscribe
    fun onEvent(event: TaskCheckedCommand) {
        when (event.Task.type) {
            Task.TYPE_DAILY -> {
                dailyCheckUseCase.observable(DailyCheckUseCase.RequestValues(user, event.Task, !event.Task.completed))
                        .subscribe(Consumer<TaskScoringResult> { this.displayTaskScoringResponse(it) }, RxErrorHandler.handleEmptyError())
            }
            Task.TYPE_TODO -> {
                todoCheckUseCase.observable(TodoCheckUseCase.RequestValues(user, event.Task, !event.Task.completed))
                        .subscribe(Consumer<TaskScoringResult> { this.displayTaskScoringResponse(it) }, RxErrorHandler.handleEmptyError())
            }
        }
    }

    @Subscribe
    fun onEvent(event: ChecklistCheckedCommand) {
        checklistCheckUseCase.observable(ChecklistCheckUseCase.RequestValues(event.task.id, event.item.id)).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(event: HabitScoreEvent) {
        habitScoreUseCase.observable(HabitScoreUseCase.RequestValues(user, event.habit, event.Up))
                .subscribe(Consumer<TaskScoringResult> { this.displayTaskScoringResponse(it) }, RxErrorHandler.handleEmptyError())
    }

    private fun checkMaintenance() {
        this.maintenanceService.maintenanceStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { maintenanceResponse ->
                    if (maintenanceResponse == null) {
                        return@Consumer
                    }
                    if (maintenanceResponse.activeMaintenance) {
                        val intent = createMaintenanceIntent(maintenanceResponse, false)
                        startActivity(intent)
                    } else {
                        if (maintenanceResponse.minBuild != null) {
                            try {
                                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                                @Suppress("DEPRECATION")
                                if (packageInfo.versionCode < maintenanceResponse.minBuild) {
                                    val intent = createMaintenanceIntent(maintenanceResponse, true)
                                    startActivity(intent)
                                }
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }, RxErrorHandler.handleEmptyError())
    }

    private fun createMaintenanceIntent(maintenanceResponse: MaintenanceResponse, isDeprecationNotice: Boolean): Intent {
        val intent = Intent(this, MaintenanceActivity::class.java)
        val data = Bundle()
        data.putString("title", maintenanceResponse.title)
        data.putString("imageUrl", maintenanceResponse.imageUrl)
        data.putString("description", maintenanceResponse.description)
        data.putBoolean("deprecationNotice", isDeprecationNotice)
        intent.putExtras(data)
        return intent
    }

    @Subscribe
    fun showSnackBarEvent(event: ShowSnackbarEvent) {
        HabiticaSnackbar.showSnackbar(floatingMenuWrapper, event.leftImage, event.title, event.text, event.specialView, event.rightIcon, event.rightTextColor, event.rightText, event.type)
    }

    @Subscribe
    fun showCheckinDialog(event: ShowCheckinDialog) {
        val title = event.notification.data.message

        val factory = LayoutInflater.from(this)
        val view = factory.inflate(R.layout.dialog_login_incentive, null)

        val imageView = view.findViewById<View>(R.id.imageView) as? SimpleDraweeView
        val imageKey = event.notification.data.rewardKey[0]
        DataBindingUtils.loadImage(imageView, imageKey)

        val youEarnedMessage = this.getString(R.string.checkInRewardEarned, event.notification.data.rewardText)

        val titleTextView = TextView(this)
        titleTextView.setBackgroundResource(R.color.blue_100)
        titleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        val density = this.resources.displayMetrics.density
        val paddingDp = (16 * density).toInt()
        titleTextView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
        titleTextView.textSize = 18f
        titleTextView.gravity = Gravity.CENTER_HORIZONTAL
        titleTextView.text = title

        val youEarnedTexView = view.findViewById<View>(R.id.you_earned_message) as? TextView
        youEarnedTexView?.text = youEarnedMessage

        val nextUnlockTextView = view.findViewById<View>(R.id.next_unlock_message) as? TextView
        nextUnlockTextView?.text = event.nextUnlockText

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setView(view)
                .setCustomTitle(titleTextView)
                .setPositiveButton(R.string.start_day) { _, _ ->
                    apiClient.readNotification(event.notification.id)
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
                .setMessage("")

        Completable.complete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action {
                    val dialog = builder.create()
                    dialog.show()
                }, RxErrorHandler.handleEmptyError())
    }

    public fun selectMenuItem(identifier: String, openSelection: Boolean = true) {
        drawerFragment?.setSelection(identifier, openSelection)
    }

    companion object {

        const val SELECT_CLASS_RESULT = 11
        const val GEM_PURCHASE_REQUEST = 111
    }
}
