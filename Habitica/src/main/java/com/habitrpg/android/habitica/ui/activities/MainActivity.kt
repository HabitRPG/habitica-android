package com.habitrpg.android.habitica.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.*
import com.habitrpg.android.habitica.data.local.UserQuestStatus
import com.habitrpg.android.habitica.databinding.ActivityMainBinding
import com.habitrpg.android.habitica.events.*
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.*
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.interactors.CheckClassSelectionUseCase
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.TutorialView
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.android.habitica.ui.views.AdventureGuideDrawerArrowDrawable
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.ValueBar
import com.habitrpg.android.habitica.ui.views.dialogs.*
import com.habitrpg.android.habitica.ui.views.yesterdailies.YesterdailyDialog
import com.habitrpg.android.habitica.userpicture.BitmapUtils
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.kotlin.isValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class MainActivity : BaseActivity(), TutorialView.OnTutorialReaction {
    private var launchScreen: String? = null
    private lateinit var drawerIcon: AdventureGuideDrawerArrowDrawable

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
    internal lateinit var analyticsManager: AnalyticsManager
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager
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
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    internal lateinit var contentRepository: ContentRepository
    @Inject
    internal lateinit var taskAlarmManager: TaskAlarmManager
    @Inject
    internal lateinit var appConfigManager: AppConfigManager

    lateinit var binding: ActivityMainBinding

    val snackbarContainer: ViewGroup
        get() = binding.snackbarContainer
    var user: User? = null

    private var avatarInHeader: AvatarWithBarsViewModel? = null
    var notificationsViewModel: NotificationsViewModel? = null
    private var faintDialog: HabiticaAlertDialog? = null
    private var sideAvatarView: AvatarView? = null
    private var activeTutorialView: TutorialView? = null
    private var drawerFragment: NavigationDrawerFragment? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    private var resumeFromActivity = false
    private var userQuestStatus = UserQuestStatus.NO_QUEST
    private var lastNotificationOpen: Long? = null

    val userID: String
        get() = user?.id ?: ""

    val isAppBarExpanded: Boolean
        get() = binding.appbar.height - binding.appbar.bottom == 0

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun getContentView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    private var launchTrace: com.google.firebase.perf.metrics.Trace? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        try {
            launchTrace = FirebasePerformance.getInstance().newTrace("MainActivityLaunch")
        } catch (e: IllegalStateException) {
            RxErrorHandler.reportError(e)
        }
        launchTrace?.start()
        super.onCreate(savedInstanceState)

        if (!HabiticaBaseApplication.checkUserAuthentication(this, hostConfig)) {
            return
        }

        setupToolbar(binding.toolbar)
        drawerIcon = AdventureGuideDrawerArrowDrawable(supportActionBar?.themedContext)

        avatarInHeader = AvatarWithBarsViewModel(this, binding.avatarWithBars, userRepository)
        sideAvatarView = AvatarView(this, showBackground = true, showMount = false, showPet = false)

        compositeSubscription.add(
            userRepository.getUser()
                .subscribe(
                    { newUser ->
                        this@MainActivity.user = newUser
                        this@MainActivity.setUserData()
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
        compositeSubscription.add(
            userRepository.getUserQuestStatus().subscribeWithErrorHandler {
                userQuestStatus = it
            }
        )

        val viewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        notificationsViewModel = viewModel

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        drawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as? NavigationDrawerFragment

        drawerFragment?.setUp(R.id.navigation_drawer, drawerLayout, viewModel)

        drawerToggle = object : ActionBarDrawerToggle(
            this, /* host Activity */
            drawerLayout, /* DrawerLayout object */
            R.string.navigation_drawer_open, /* "open drawer" description */
            R.string.navigation_drawer_close /* "close drawer" description */
        ) {}
        drawerToggle?.drawerArrowDrawable = drawerIcon
        // Set the drawer toggle as the DrawerListener
        drawerToggle?.let { drawerLayout.addDrawerListener(it) }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            private var isOpeningDrawer: Boolean? = null

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val modernHeaderStyle = sharedPreferences.getBoolean("modern_header_style", true)
                if (!isUsingNightModeResources() && modernHeaderStyle) {
                    if (slideOffset < 0.5f && isOpeningDrawer == null) {
                        window.updateStatusBarColor(getThemeColor(R.attr.colorPrimaryDark), false)
                        isOpeningDrawer = true
                    } else if (slideOffset > 0.5f && isOpeningDrawer == null) {
                        window.updateStatusBarColor(getThemeColor(R.attr.headerBackgroundColor), true)
                        isOpeningDrawer = false
                    }
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard()
                val modernHeaderStyle = sharedPreferences.getBoolean("modern_header_style", true)
                if (!isUsingNightModeResources() && modernHeaderStyle) {
                    window.updateStatusBarColor(getThemeColor(R.attr.colorPrimaryDark), false)
                }
                isOpeningDrawer = null

                drawerFragment?.updatePromo()
            }

            override fun onDrawerClosed(drawerView: View) {
                val modernHeaderStyle = sharedPreferences.getBoolean("modern_header_style", true)
                if (!isUsingNightModeResources() && modernHeaderStyle) {
                    window.updateStatusBarColor(getThemeColor(R.attr.headerBackgroundColor), true)
                }
                isOpeningDrawer = null
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        setupNotifications()
        setupBottomnavigationLayoutListener()

        try {
            taskAlarmManager.scheduleAllSavedAlarms(sharedPreferences.getBoolean("preventDailyReminder", false))
        } catch (e: Exception) {
            analyticsManager.logException(e)
        }
    }

    override fun setTitle(title: CharSequence?) {
        binding.toolbarTitle.text = title
    }

    override fun setTitle(titleId: Int) {
        binding.toolbarTitle.text = getString(titleId)
    }

    private fun updateToolbarTitle(destination: NavDestination, arguments: Bundle?) {
        title = if (destination.id == R.id.promoInfoFragment) {
            ""
        } else if (destination.id == R.id.petDetailRecyclerFragment || destination.id == R.id.mountDetailRecyclerFragment) {
            arguments?.getString("type")
        } else if (destination.label.isNullOrEmpty() && user?.isValid == true) {
            user?.profile?.name
        } else if (destination.label != null) {
            destination.label
        } else {
            ""
        }
        if (destination.id == R.id.petDetailRecyclerFragment || destination.id == R.id.mountDetailRecyclerFragment) {
            compositeSubscription.add(
                inventoryRepository.getItem("egg", arguments?.getString("type") ?: "").firstElement().subscribe(
                    {
                        if (!it.isValid()) return@subscribe
                        binding.toolbarTitle.text = if (destination.id == R.id.petDetailRecyclerFragment) {
                            (it as? Egg)?.text
                        } else {
                            (it as? Egg)?.mountText
                        }
                    },
                    RxErrorHandler.handleEmptyError()
                )
            )
        }
        drawerFragment?.setSelection(destination.id, null, false)
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        onBackPressed()
        return true
    }

    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channel = NotificationChannel(
                channelId,
                "Habitica Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun setupBottomnavigationLayoutListener() {
        binding.bottomNavigation.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.bottomNavigation.visibility == View.VISIBLE) {
                snackbarContainer.setPadding(0, 0, 0, binding.bottomNavigation.barHeight + 12.dpToPx(this))
            } else {
                snackbarContainer.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle?.syncState()

        launchScreen = sharedPreferences.getString("launch_screen", "")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
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

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onResume() {
        super.onResume()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navigationController = navHostFragment.navController
        MainNavigationController.setup(navigationController)
        navigationController.addOnDestinationChangedListener { _, destination, arguments -> updateToolbarTitle(destination, arguments) }

        if (launchScreen == "/party") {
            if (user == null || user?.party?.id != null) {
                MainNavigationController.navigate(R.id.partyFragment)
            }
        }
        launchScreen = null

        if (!resumeFromActivity) {
            retrieveUser()
            this.checkMaintenance()
        }
        resumeFromActivity = false

        // Track when the app was last opened, so that we can use this to send out special reminders after a week of inactivity
        sharedPreferences.edit {
            putLong("lastAppLaunch", Date().time)
            putBoolean("preventDailyReminder", false)
        }

        if ((intent.hasExtra("notificationIdentifier") || intent.hasExtra("openURL")) && lastNotificationOpen != intent.getLongExtra("notificationTimeStamp", 0)) {
            lastNotificationOpen = intent.getLongExtra("notificationTimeStamp", 0)
            val identifier = intent.getStringExtra("notificationIdentifier") ?: ""
            if (intent.hasExtra("sendAnalytics")) {
                val additionalData = HashMap<String, Any>()
                additionalData["identifier"] = identifier
                AmplitudeManager.sendEvent(
                    "open notification",
                    AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR,
                    AmplitudeManager.EVENT_HITTYPE_EVENT,
                    additionalData
                )
            }
            retrieveUser(true)
            NotificationOpenHandler.handleOpenedByNotification(identifier, intent)
        }

        launchTrace?.stop()
        launchTrace = null

        if (binding.toolbarTitle.text?.isNotBlank() != true) {
            navigationController.currentDestination?.let { updateToolbarTitle(it, null) }
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

    fun navigate(transitionId: Int) {
        findNavController(R.id.nav_host_fragment).navigate(transitionId)
    }

    private fun setUserData() {
        if (user != null) {
            val preferences = user?.preferences

            preferences?.language?.let { apiClient.setLanguageCode(it) }
            if (preferences?.language != sharedPreferences.getString("language", "en")) {
                sharedPreferences.edit {
                    putString("language", preferences?.language)
                }
            }
            preferences?.sound?.let { soundManager.soundTheme = it }

            displayDeathDialogIfNeeded()
            YesterdailyDialog.showDialogIfNeeded(this, user?.id, userRepository, taskRepository)

            if (user?.flags?.verifiedUsername == false && isActivityVisible) {
                val intent = Intent(this, VerifyUsernameActivity::class.java)
                startActivity(intent)
            }

            val quest = user?.party?.quest
            if (quest?.completed?.isNotBlank() == true) {
                compositeSubscription.add(
                    inventoryRepository.getQuestContent(user?.party?.quest?.completed ?: "").firstElement().subscribe(
                        {
                            QuestCompletedDialog.showWithQuest(this, it)

                            userRepository.updateUser("party.quest.completed", "").subscribe({}, RxErrorHandler.handleEmptyError())
                        },
                        RxErrorHandler.handleEmptyError()
                    )
                )
            }

            if (user?.flags?.welcomed == false) {
                compositeSubscription.add(userRepository.updateUser("flags.welcomed", true).subscribe({}, RxErrorHandler.handleEmptyError()))
            }

            if (appConfigManager.enableAdventureGuide()) {
                drawerIcon.setEnabled(user?.hasCompletedOnboarding == false)
            } else {
                drawerIcon.setEnabled(false)
            }

            try {
                val navigationController = findNavController(R.id.nav_host_fragment)
                if (binding.toolbarTitle.text?.isNotBlank() != true) {
                    navigationController.currentDestination?.let { updateToolbarTitle(it, null) }
                }
            } catch (e: java.lang.IllegalStateException) {
                // Has no Navcontroller right now.
            }
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
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PurchaseHandler.findForActivity(this)?.onResult(requestCode, resultCode, data)
    }

    // region Events

    public override fun onDestroy() {
        userRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }

    @Subscribe
    fun onEvent(event: FeedCommand) {
        if (event.usingFood == null || event.usingPet == null) {
            return
        }
        val pet = event.usingPet
        compositeSubscription.add(
            this.inventoryRepository.feedPet(event.usingPet, event.usingFood)
                .subscribe(
                    { feedResponse ->
                        HabiticaSnackbar.showSnackbar(snackbarContainer, feedResponse.message, SnackbarDisplayType.NORMAL)
                        if (feedResponse.value == -1) {
                            val mountWrapper = View.inflate(this, R.layout.pet_imageview, null) as? FrameLayout
                            val mountImageView = mountWrapper?.findViewById(R.id.pet_imageview) as? ImageView

                            DataBindingUtils.loadImage(mountImageView, "Mount_Icon_" + event.usingPet.key)
                            val dialog = HabiticaAlertDialog(this@MainActivity)
                            dialog.setTitle(getString(R.string.evolved_pet_title, pet.text))
                            dialog.setAdditionalContentView(mountWrapper)
                            dialog.addButton(R.string.onwards, true)
                            dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                                val event1 = ShareEvent()
                                event1.identifier = "raisedPet"
                                event1.sharedMessage = getString(R.string.share_raised, pet.text)
                                val mountImageSideLength = 99
                                val sharedImage = Bitmap.createBitmap(mountImageSideLength, mountImageSideLength, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(sharedImage)
                                mountImageView?.drawable?.setBounds(0, 0, mountImageSideLength, mountImageSideLength)
                                mountImageView?.drawable?.draw(canvas)
                                event1.shareImage = sharedImage
                                EventBus.getDefault().post(event1)
                                hatchingDialog.dismiss()
                            }
                            dialog.enqueue()
                        }
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    // endregion

    internal fun displayTaskScoringResponse(data: TaskScoringResult?) {
        if (user != null && data != null) {
            val damageValue = when (userQuestStatus) {
                UserQuestStatus.QUEST_BOSS -> data.questDamage
                else -> 0.0
            }
            compositeSubscription.add(
                notifyUserUseCase.observable(
                    NotifyUserUseCase.RequestValues(
                        this, snackbarContainer,
                        user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, damageValue, data.hasLeveledUp, data.level
                    )
                )
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
            )
        }

        val showItemsFound = userQuestStatus == UserQuestStatus.QUEST_COLLECT
        compositeSubscription.add(
            displayItemDropUseCase.observable(DisplayItemDropUseCase.RequestValues(data, this, snackbarContainer, showItemsFound))
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun displayDeathDialogIfNeeded() {
        if (user?.stats?.hp ?: 1.0 > 0) {
            return
        }

        if (this.faintDialog == null && !this.isFinishing) {

            val customView = View.inflate(this, R.layout.dialog_faint, null)
            if (customView != null) {
                val hpBarView = customView.findViewById<View>(R.id.hpBar) as? ValueBar

                hpBarView?.setLightBackground(true)
                hpBarView?.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())

                val dialogAvatarView = customView.findViewById<View>(R.id.avatarView) as? AvatarView
                user?.let { dialogAvatarView?.setAvatar(it) }
            }

            this.faintDialog = HabiticaAlertDialog(this)
            faintDialog?.setTitle(R.string.faint_header)
            faintDialog?.setAdditionalContentView(customView)
            faintDialog?.addButton(R.string.faint_button, true) { _, _ ->
                faintDialog = null
                user?.let {
                    userRepository.revive(it).subscribe({ }, RxErrorHandler.handleEmptyError())
                }
            }
            soundManager.loadAndPlayAudio(SoundManager.SoundDeath)
            this.faintDialog?.enqueue()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            drawerFragment?.openDrawer()
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    protected fun retrieveUser(forced: Boolean = false) {
        if (hostConfig.hasAuthentication()) {
            compositeSubscription.add(
                contentRepository.retrieveWorldState(this)
                    .flatMap { userRepository.retrieveUser(true, forced) }
                    .doOnNext { user1 ->
                        FirebaseAnalytics.getInstance(this).setUserProperty("has_party", if (user1.party?.id?.isNotEmpty() == true) "true" else "false")
                        FirebaseAnalytics.getInstance(this).setUserProperty("is_subscribed", if (user1.isSubscribed) "true" else "false")
                        FirebaseAnalytics.getInstance(this).setUserProperty("checkin_count", user1.loginIncentives.toString())
                        FirebaseAnalytics.getInstance(this).setUserProperty("level", user1.stats?.lvl?.toString() ?: "")
                        pushNotificationManager.setUser(user1)
                        pushNotificationManager.addPushDeviceUsingStoredToken()
                    }
                    .flatMap { userRepository.retrieveTeamPlans() }
                    .flatMap { contentRepository.retrieveContent(this) }
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
            )
        }
    }

    fun displayTutorialStep(step: TutorialStep, text: String, canBeDeferred: Boolean) {
        removeActiveTutorialView()
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialText(text)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        binding.overlayFrameLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    fun displayTutorialStep(step: TutorialStep, texts: List<String>, canBeDeferred: Boolean) {
        removeActiveTutorialView()
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialTexts(texts)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        binding.overlayFrameLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialCompleted(step: TutorialStep) {
        compositeSubscription.add(
            userRepository.updateUser("flags.tutorial." + step.tutorialGroup + "." + step.identifier, true)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
        binding.overlayFrameLayout.removeView(this.activeTutorialView)
        this.removeActiveTutorialView()

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = true
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialDeferred(step: TutorialStep) {
        taskRepository.modify(step) { it.displayedOn = Date() }
        this.removeActiveTutorialView()
    }

    private fun removeActiveTutorialView() {
        if (this.activeTutorialView != null) {
            binding.overlayFrameLayout.removeView(this.activeTutorialView)
            this.activeTutorialView = null
        }
    }

    @Subscribe
    fun shareEvent(event: ShareEvent) {
        analyticsManager.logEvent("shared", bundleOf(Pair("identifier", event.identifier)))
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "*/*"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, event.sharedMessage)
        BitmapUtils.clearDirectoryContent("$filesDir/shared_images")
        val f = event.shareImage?.let {
            BitmapUtils.saveToShareableFile(
                "$filesDir/shared_images", "${Date()}.png",
                it
            )
        }
        val fileUri = f?.let { FileProvider.getUriForFile(this, getString(R.string.content_provider), it) }
        if (fileUri != null) {
            sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            val resInfoList = this.packageManager.queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                this.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
    }

    private fun checkMaintenance() {
        compositeSubscription.add(
            this.maintenanceService.maintenanceStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Consumer { maintenanceResponse ->
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
                                    RxErrorHandler.reportError(e)
                                }
                            }
                        }
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
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
        HabiticaSnackbar.showSnackbar(snackbarContainer, event.leftImage, event.title, event.text, event.specialView, event.rightIcon, event.rightTextColor, event.rightText, event.type)
    }

    @Subscribe
    fun showCheckinDialog(event: ShowCheckinDialog) {
        val notificationData = event.notification.data as? LoginIncentiveData
        val title = notificationData?.message

        val factory = LayoutInflater.from(this)
        val view = factory.inflate(R.layout.dialog_login_incentive, null)

        val imageView = view.findViewById(R.id.imageView) as? ImageView
        var imageKey = notificationData?.rewardKey?.get(0)
        if (imageKey?.contains("armor") == true) {
            imageKey = "slim_$imageKey"
        }
        DataBindingUtils.loadImage(imageView, imageKey)

        val youEarnedMessage = this.getString(R.string.checkInRewardEarned, notificationData?.rewardText)
        val youEarnedTexView = view.findViewById(R.id.you_earned_message) as? TextView
        youEarnedTexView?.text = youEarnedMessage

        val nextUnlockTextView = view.findViewById(R.id.next_unlock_message) as? TextView
        if (event.nextUnlockCount > 0) {
            nextUnlockTextView?.text = event.nextUnlockText
        } else {
            nextUnlockTextView?.visibility = View.GONE
        }

        lifecycleScope.launch(context = Dispatchers.Main) {
            val alert = HabiticaAlertDialog(this@MainActivity)
            alert.setAdditionalContentView(view)
            alert.setTitle(title)
            alert.addButton(R.string.see_you_tomorrow, true) { _, _ ->
                apiClient.readNotification(event.notification.id)
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
            }
            alert.show()
        }
    }

    @Subscribe
    fun showAchievementDialog(event: ShowAchievementDialog) {
        retrieveUser(true)
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = AchievementDialog(this@MainActivity)
            dialog.isLastOnboardingAchievement = event.isLastOnboardingAchievement
            dialog.setType(event.type, event.message, event.text)
            dialog.enqueue()
            apiClient.readNotification(event.id)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        }
    }

    @Subscribe
    fun showFirstDropDialog(event: ShowFirstDropDialog) {
        retrieveUser(true)
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = FirstDropDialog(this@MainActivity)
            dialog.configure(event.egg, event.hatchingPotion)
            dialog.enqueue()
            apiClient.readNotification(event.id)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        }
    }

    @Subscribe
    fun showWonChallengeDialog(event: ShowWonChallengeDialog) {
        retrieveUser(true)
        lifecycleScope.launch(context = Dispatchers.Main) {
            val dialog = WonChallengeDialog(this@MainActivity)
            dialog.configure(event.data)
            dialog.enqueue()
            apiClient.readNotification(event.id)
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun onEvent(event: ShowConnectionProblemEvent) {
        if (event.title != null) {
            super.onEvent(event)
        } else {
            binding.connectionIssueTextview.visibility = View.VISIBLE
            binding.connectionIssueTextview.text = event.message
            compositeSubscription.add(
                Observable.just("")
                    .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            binding.connectionIssueTextview.visibility = View.GONE
                        },
                        {}
                    )
            )
        }
    }

    fun hatchPet(potion: HatchingPotion, egg: Egg) {
        compositeSubscription.add(
            this.inventoryRepository.hatchPet(egg, potion) {
                val petWrapper = View.inflate(this, R.layout.pet_imageview, null) as? FrameLayout
                val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? ImageView

                DataBindingUtils.loadImage(petImageView, "stable_Pet-" + egg.key + "-" + potion.key)
                val potionName = potion.text
                val eggName = egg.text
                val dialog = HabiticaAlertDialog(this)
                dialog.setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
                dialog.setAdditionalContentView(petWrapper)
                dialog.addButton(R.string.equip, true) { _, _ ->
                    inventoryRepository.equip(user, "pet", egg.key + "-" + potion.key)
                        .subscribe({}, RxErrorHandler.handleEmptyError())
                }
                dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                    val event1 = ShareEvent()
                    event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName)
                    event1.identifier = "hatchedPet"
                    val petImageSideLength = 140
                    val sharedImage = Bitmap.createBitmap(petImageSideLength, petImageSideLength, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(sharedImage)
                    petImageView?.drawable?.setBounds(0, 0, petImageSideLength, petImageSideLength)
                    petImageView?.drawable?.draw(canvas)
                    event1.shareImage = sharedImage
                    EventBus.getDefault().post(event1)
                    hatchingDialog.dismiss()
                }
                dialog.setExtraCloseButtonVisibility(View.VISIBLE)
                dialog.enqueue()
            }.subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        compositeSubscription.add(userRepository.retrieveUser(withTasks = false, forced = true).subscribe({}, RxErrorHandler.handleEmptyError()))
    }
}
