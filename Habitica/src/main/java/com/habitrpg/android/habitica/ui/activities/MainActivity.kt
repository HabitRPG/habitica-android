package com.habitrpg.android.habitica.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.view.children
import androidx.core.view.setPadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.wearable.Wearable
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.ActivityMainBinding
import com.habitrpg.android.habitica.extensions.hideKeyboard
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NotificationOpenHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.collectAsStateLifecycleAware
import com.habitrpg.android.habitica.interactors.CheckClassSelectionUseCase
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserQuestStatus
import com.habitrpg.android.habitica.ui.TutorialView
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.MainActivityViewModel
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.android.habitica.ui.views.AppHeaderView
import com.habitrpg.android.habitica.ui.views.GroupPlanMemberList
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.QuestCompletedDialog
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.android.habitica.ui.views.yesterdailies.YesterdailyDialog
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.shared.habitica.models.responses.MaintenanceResponse
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

var mainActivityCreatedAt: Date? = null

@AndroidEntryPoint
open class MainActivity : BaseActivity(), SnackbarActivity {
    private var launchScreen: String? = null

    @Inject
    internal lateinit var apiClient: ApiClient

    @Inject
    internal lateinit var soundManager: SoundManager

    @Inject
    internal lateinit var checkClassSelectionUseCase: CheckClassSelectionUseCase

    @Inject
    internal lateinit var displayItemDropUseCase: DisplayItemDropUseCase

    @Inject
    internal lateinit var notifyUserUseCase: NotifyUserUseCase

    @Inject
    internal lateinit var taskRepository: TaskRepository

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    @Inject
    internal lateinit var appConfigManager: AppConfigManager

    lateinit var binding: ActivityMainBinding

    val snackbarContainer: ViewGroup
        get() = binding.content.snackbarContainer

    val notificationsViewModel: NotificationsViewModel by viewModels()
    val viewModel: MainActivityViewModel by viewModels()
    private var sideAvatarView: AvatarView? = null
    private var drawerFragment: NavigationDrawerFragment? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    var showBirthdayIcon = false
    var showBackButton: Boolean? = null
        set(value) {
            if (field == value) return
            if (value == true && showBirthdayIcon) {
                drawerToggle?.isDrawerIndicatorEnabled = false
                drawerToggle?.setHomeAsUpIndicator(R.drawable.arrow_back)
            } else if (value == false && showBirthdayIcon) {
                drawerToggle?.isDrawerIndicatorEnabled = false
                drawerToggle?.setHomeAsUpIndicator(R.drawable.icon_birthday)
            } else {
                drawerToggle?.isDrawerIndicatorEnabled = value != true
            }
            field = value
        }
    private var resumeFromActivity = false
    private var userQuestStatus = UserQuestStatus.NO_QUEST
    private var lastNotificationOpen: Long? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.pushNotificationManager.addPushDeviceUsingStoredToken()
        } else {
            viewModel.updateAllowPushNotifications(false)
        }
    }

    private val classSelectionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.retrieveUser(true, true)
            }
        }

    val isAppBarExpanded: Boolean
        get() = binding.content.appbar.height - binding.content.appbar.bottom == 0

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    private var launchTrace: com.google.firebase.perf.metrics.Trace? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            mainActivityCreatedAt = Date()
        }
        try {
            launchTrace = FirebasePerformance.getInstance().newTrace("MainActivityLaunch")
        } catch (e: IllegalStateException) {
            ExceptionHandler.reportError(e)
        }
        launchTrace?.start()
        super.onCreate(savedInstanceState)
        DataBindingUtils.configManager = appConfigManager

        if (!viewModel.isAuthenticated) {
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        } else {
            Wearable.getCapabilityClient(this).addLocalCapability("provide_auth")
        }

        setupToolbar(binding.content.toolbar)

        sideAvatarView = AvatarView(this, showBackground = true, showMount = false, showPet = false)

        viewModel.user.observe(this) {
            setUserData(it)
        }
        lifecycleScope.launchCatching {
            userRepository.getUserQuestStatus().collect {
                userQuestStatus = it
            }
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerFragment =
            supportFragmentManager.findFragmentById(R.id.navigation_drawer) as? NavigationDrawerFragment
        drawerFragment?.setUp(R.id.navigation_drawer, drawerLayout, notificationsViewModel)

        drawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {}
        // Set the drawer toggle as the DrawerListener
        drawerToggle?.let { drawerLayout.addDrawerListener(it) }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            private var isOpeningDrawer: Boolean? = null

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (!isUsingNightModeResources()) {
                    if (slideOffset < 0.5f && isOpeningDrawer == null) {
                        window.updateStatusBarColor(getThemeColor(R.attr.colorPrimaryDark), false)
                        isOpeningDrawer = true
                    } else if (slideOffset > 0.5f && isOpeningDrawer == null) {
                        window.updateStatusBarColor(
                            getThemeColor(R.attr.headerBackgroundColor),
                            true
                        )
                        isOpeningDrawer = false
                    }
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                hideKeyboard()
                if (!isUsingNightModeResources()) {
                    window.updateStatusBarColor(getThemeColor(R.attr.colorPrimaryDark), false)
                }
                isOpeningDrawer = null
                drawerFragment?.updatePromo()
            }

            override fun onDrawerClosed(drawerView: View) {
                if (!isUsingNightModeResources()) {
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

        binding.content.headerView.setContent {
            HabiticaTheme {
                val user by viewModel.user.observeAsState(null)
                val teamPlan by viewModel.userViewModel.currentTeamPlan.collectAsStateLifecycleAware(
                    null
                )
                val teamPlanMembers by viewModel.userViewModel.currentTeamPlanMembers.observeAsState()
                val canShowTeamHeader: Boolean by viewModel.canShowTeamPlanHeader
                AppHeaderView(
                    user,
                    teamPlan = if (canShowTeamHeader) teamPlan else null,
                    teamPlanMembers = teamPlanMembers,
                    onMemberRowClicked = {
                        showAsBottomSheet { onClose ->
                            val group by viewModel.userViewModel.currentTeamPlanGroup.collectAsState(
                                null
                            )
                            val members by viewModel.userViewModel.currentTeamPlanMembers.observeAsState()
                            GroupPlanMemberList(members, group) {
                                onClose()
                                FullProfileActivity.open(it)
                            }

                        }
                    },
                    onClassSelectionClicked = {
                        val bundle = Bundle()
                        val isClassSelected = user?.flags?.classSelected ?: false
                        bundle.putBoolean("isInitialSelection", isClassSelected)
                        val intent = Intent(this@MainActivity, ClassSelectionActivity::class.java)
                        intent.putExtras(bundle)
                        classSelectionResult.launch(intent)
                    }
                )
            }
        }

        viewModel.onCreate()
    }

    override fun setTitle(title: CharSequence?) {
        binding.content.toolbarTitle.text = title
    }

    override fun setTitle(titleId: Int) {
        binding.content.toolbarTitle.text = getString(titleId)
    }

    private fun updateToolbarTitle(destination: NavDestination, arguments: Bundle?) {
        viewModel.getToolbarTitle(destination.id, destination.label, arguments?.getString("type")) {
            title = it
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
        binding.content.bottomNavigation.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.content.bottomNavigation.visibility == View.VISIBLE) {
                snackbarContainer.setPadding(
                    0,
                    0,
                    0,
                    binding.content.bottomNavigation.barHeight + 12.dpToPx(this)
                )
            } else {
                snackbarContainer.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle?.syncState()

        launchScreen = viewModel.launchScreen
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (binding.root.parent is DrawerLayout && drawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else if (item.itemId == android.R.id.home) {
            if (showBackButton != true) {
                drawerFragment?.toggleDrawer()
            } else {
                MainNavigationController.navigateBack()
            }
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navigationController = navHostFragment.navController
        MainNavigationController.setup(navigationController)
        navigationController.addOnDestinationChangedListener { _, destination, arguments ->
            updateToolbarTitle(
                destination,
                arguments
            )
        }

        if (Build.VERSION.SDK_INT >= 33) {
            observeNotificationPermission()
        }

        if (launchScreen == "/party") {
            viewModel.user.observeOnce(this) {
                if (viewModel.userViewModel.isUserInParty) {
                    MainNavigationController.navigate(R.id.partyFragment)
                }
            }
        }
        launchScreen = null

        if (!resumeFromActivity) {
            retrieveUser()
            this.checkMaintenance()
        }
        resumeFromActivity = false

        if ((intent.hasExtra("notificationIdentifier") || intent.hasExtra("openURL")) && lastNotificationOpen != intent.getLongExtra(
                "notificationTimeStamp",
                0
            )
        ) {
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

        if (binding.content.toolbarTitle.text?.isNotBlank() != true) {
            navigationController.currentDestination?.let { updateToolbarTitle(it, null) }
        }
    }

    @RequiresApi(33)
    fun observeNotificationPermission() {
        viewModel.requestNotificationPermission.observe(this) { requestNotificationPermission ->
            if (requestNotificationPermission) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                viewModel.requestNotificationPermission.value = false
            }
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
        val ids = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(application, widgetClass))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    fun navigate(transitionId: Int) {
        findNavController(R.id.nav_host_fragment).navigate(transitionId)
    }

    private fun setUserData(user: User?) {
        if (user != null) {
            val preferences = user.preferences

            preferences?.language?.let { apiClient.languageCode = it }
            if (preferences?.language != viewModel.preferenceLanguage) {
                viewModel.preferenceLanguage = preferences?.language
            }
            preferences?.sound?.let { soundManager.soundTheme = it }

            val crashlytics = Firebase.crashlytics
            crashlytics.setCustomKey("day_start", user.preferences?.dayStart ?: 0)
            crashlytics.setCustomKey("timezone_offset", user.preferences?.timezoneOffset ?: 0)

            displayDeathDialogIfNeeded()
            YesterdailyDialog.showDialogIfNeeded(this, user.id, userRepository, taskRepository)

            val quest = user.party?.quest
            if (quest?.completed?.isNotBlank() == true) {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val questContent =
                        inventoryRepository.getQuestContent(user.party?.quest?.completed ?: "")
                            .firstOrNull()
                    if (questContent != null) {
                        QuestCompletedDialog.showWithQuest(this@MainActivity, questContent)
                    }
                    viewModel.updateUser("party.quest.completed", "")
                }
            }

            if (user.flags?.welcomed == false) {
                viewModel.updateUser("flags.welcomed", true)
            }

            val title = binding.content.toolbarTitle.text
            if (title.isBlank()) {
                viewModel.getToolbarTitle(0, null, null) { newTitle ->
                    this.title = newTitle
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawerFragment?.isDrawerOpen == true) {
            drawerFragment?.closeDrawer()
        } else {
            try {
                super.onBackPressed()
            } catch (ignored: Exception) {
            }
        }
    }

    // region Events

    public override fun onDestroy() {
        userRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }
    // endregion

    internal fun displayTaskScoringResponse(data: TaskScoringResult?) {
        if (viewModel.user.value != null && data != null) {
            val damageValue = when (userQuestStatus) {
                UserQuestStatus.QUEST_BOSS -> data.questDamage
                else -> 0.0
            }
            lifecycleScope.launchCatching {
                notifyUserUseCase.callInteractor(
                    NotifyUserUseCase.RequestValues(
                        this@MainActivity,
                        snackbarContainer,
                        viewModel.user.value,
                        data.experienceDelta,
                        data.healthDelta,
                        data.goldDelta,
                        data.manaDelta,
                        damageValue,
                        data.hasLeveledUp,
                        data.level
                    )
                )
            }
        }

        val showItemsFound = userQuestStatus == UserQuestStatus.QUEST_COLLECT
        lifecycleScope.launchCatching {
            displayItemDropUseCase.callInteractor(
                DisplayItemDropUseCase.RequestValues(
                    data,
                    this@MainActivity,
                    snackbarContainer,
                    showItemsFound
                )
            )
        }
    }

    private var lastDeathDialogDisplay = 0L

    private fun displayDeathDialogIfNeeded() {
        if (!viewModel.userViewModel.isUserFainted) {
            return
        }

        val now = Date().time
        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(1000L)
            if (!this@MainActivity.isFinishing && MainNavigationController.isReady && now - lastDeathDialogDisplay > 60000) {
                lastDeathDialogDisplay = now
                MainNavigationController.navigate(R.id.deathActivity)
            }
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
        viewModel.retrieveUser(forced)
    }

    fun displayTutorialStep(step: TutorialStep, texts: List<String>, canBeDeferred: Boolean) {
        val view = TutorialView(this, step, viewModel)
        view.setTutorialTexts(texts)
        view.setCanBeDeferred(canBeDeferred)
        binding.content.overlayFrameLayout.children.forEach {
            if (it is TutorialView) {
                binding.content.overlayFrameLayout.removeView(it)
            }
        }
        binding.content.overlayFrameLayout.addView(view)
        viewModel.logTutorialStatus(step, false)
    }

    private fun checkMaintenance() {
        viewModel.ifNeedsMaintenance { maintenanceResponse ->
            if (maintenanceResponse.activeMaintenance == true) {
                val intent = createMaintenanceIntent(maintenanceResponse, false)
                startActivity(intent)
            } else {
                if (maintenanceResponse.minBuild != null) {
                    try {
                        val packageInfo = packageManager.getPackageInfo(packageName, 0)
                        @Suppress("DEPRECATION")
                        if (packageInfo.versionCode < (maintenanceResponse.minBuild ?: 0)) {
                            val intent = createMaintenanceIntent(maintenanceResponse, true)
                            startActivity(intent)
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        ExceptionHandler.reportError(e)
                    }
                }
            }
        }
    }

    private fun createMaintenanceIntent(
        maintenanceResponse: MaintenanceResponse,
        isDeprecationNotice: Boolean
    ): Intent {
        val intent = Intent(this, MaintenanceActivity::class.java)
        val data = Bundle()
        data.putString("title", maintenanceResponse.title)
        data.putString("imageUrl", maintenanceResponse.imageUrl)
        data.putString("description", maintenanceResponse.description)
        data.putBoolean("deprecationNotice", isDeprecationNotice)
        intent.putExtras(data)
        return intent
    }

    override fun snackbarContainer(): ViewGroup {
        return snackbarContainer
    }

    private var errorJob: Job? = null

    override fun showConnectionProblem(title: String?, message: String) {
        if (title != null) {
            super.showConnectionProblem(title, message)
        } else {
            if (errorJob?.isCancelled == false) {
                // a new error resets the timer to hide the error message
                errorJob?.cancel()
            }
            binding.content.connectionIssueView.visibility = View.VISIBLE
            binding.content.connectionIssueTextview.text = message
            errorJob = lifecycleScope.launch(Dispatchers.Main) {
                delay(1.toDuration(DurationUnit.MINUTES))
                binding.content.connectionIssueView.visibility = View.GONE
            }
        }
    }

    override fun hideConnectionProblem() {
        if (errorJob?.isCancelled == false) {
            errorJob?.cancel()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (binding.content.connectionIssueView.visibility == View.VISIBLE) {
                binding.content.connectionIssueView.visibility = View.GONE
            }
        }
    }

    fun updateToolbarInteractivity(titleInteractive : Boolean) {
        viewModel.canShowTeamPlanHeader.value = titleInteractive
        binding.content.toolbarTitle.background?.alpha = if (titleInteractive) 255 else 0
        if (titleInteractive) {
            binding.content.toolbarTitle.setScaledPadding(this, 16, 4, 16, 4)
        } else {
            binding.content.toolbarTitle.setPadding(0)
        }
    }
}
